/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.monitoring.prometheus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.monitoring.MonitoringReceiver;
import de.iip_ecosphere.platform.monitoring.prometheus.ConfigModifier.ScrapeEndpoint;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Schema;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.common.TextFormat;

/**
 * Observes IIP-Ecosphere standard transport channels and feeds the information into Prometheus. [public for testing]
 * 
 * @author Holger Eichelberger, SSE
 */
public class IipEcospherePrometheusExporter extends MonitoringReceiver {

    private Tomcat server;
    private Context context;
    private Supplier<ConfigModifier> modifier;
    private Map<String, Context> contexts = Collections.synchronizedMap(new HashMap<>());
    private int port;
    private File webapps;
    
    /**
     * Creates a prometheus exporter.
     * 
     * @see #setModifierSupplier(Supplier)
     */
    public IipEcospherePrometheusExporter() {
    }
    
    /**
     * Defines the modifier supplier.
     * 
     * @param modifier the modifier supplier
     */
    public void setModifierSupplier(Supplier<ConfigModifier> modifier) {
        this.modifier = modifier;
    }

    // checkstyle: stop exception type check
    
    @Override
    public void start() {
        super.start();
        try {
            PrometheusMonitoringSetup setup = PrometheusMonitoringSetup.getInstance();
            port = setup.getExporterPort();
            if (port < 0) {
                port = NetUtils.getEphemeralPort();
            }
            LoggerFactory.getLogger(getClass()).info("Starting prometheus export endpoint on port {}", port);
            server = new Tomcat();
            server.setPort(port);
            File home = server.getEngine().getCatalinaHome();
            webapps = new File(home, "webapps");
            webapps.mkdirs();
            context = server.addContext(server.getHost(), "", "");
            server.start();
        } catch (Exception  e) {
            LoggerFactory.getLogger(getClass()).error("Starting prometheus export endpoint: {}", e.getMessage());
        }
    }
    
    @Override
    public void stop() {
        if (null != server) {
            try {
                server.stop();
                server = null;
            } catch (Exception  e) {
                LoggerFactory.getLogger(getClass()).error("Shutting down prometheus export endpoint: {}", 
                    e.getMessage());
            }
        }
        super.stop();
    }

    // checkstyle: resume exception type check

   /**
     * Exhibits creating a custom meter.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class MyPrometheusMeterRegistry extends PrometheusMeterRegistry {
        
        /**
         * Creates a default registry.
         */
        MyPrometheusMeterRegistry() {
            super(PrometheusConfig.DEFAULT);
        }
        
        /**
         * Build a new custom meter to be added to the registry.
         *
         * @param id           The id that uniquely identifies the custom meter.
         * @param type         What kind of meter this is.
         * @param measurements A set of measurements describing how to sample this meter.
         * @return A new custom meter.
         */
        protected Meter createMeter(Meter.Id id, Meter.Type type, Iterable<Measurement> measurements) {
            return newMeter(id, type, measurements);
        }

    }

    /**
     * Implements a specialized exporter for prometheus.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected class PrometheusExporter extends Exporter {

        private MyPrometheusMeterRegistry registry = new MyPrometheusMeterRegistry();
        private ScrapeEndpoint entry;
                
        private HttpServlet servlet = new HttpServlet() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
                resp.setStatus(200);
                String contentType = TextFormat.chooseContentType(req.getHeader("Accept"));
                resp.setContentType(contentType);

                Writer writer = new BufferedWriter(resp.getWriter());
                try {
                    writer.append(registry.scrape());
                    writer.flush();
                } finally {
                    writer.close();
                }
            }

            @Override
            protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
                doGet(req, resp);
            }

        };
        
        /**
         * Creates an exporter.
         * 
         * @param id the source id
         */
        protected PrometheusExporter(String id) {
            super(id);
        }
        
        @Override
        protected void initialize() {
            String id = getId();
            String path = "/" + id;
            entry = new ScrapeEndpoint(id, new Endpoint(Schema.HTTP, port, path));
            Context ctx = contexts.get(id);
            if (null != ctx) {
                ctx = server.addContext(path, "");
                contexts.put(id, context);
            }
            Tomcat.addServlet(ctx, id, servlet);
        }
        
        /**
         * Returns the scrape entry.
         * 
         * @return the entry
         */
        protected ScrapeEndpoint getScrapeEntry() {
            return entry;
        }
        
        @Override
        protected void addMeter(Meter meter)  {
            if (null != meter) {
                registry.remove(meter.getId());
                registry.createMeter(meter.getId(), meter.getId().getType(), meter.measure());
            }
        }

        @Override
        protected void dispose() {
            servlet.destroy();
            super.dispose();
        }

    }
    
    @Override
    protected Exporter createExporter(String id) {
        return new PrometheusExporter(id);
    }

    @Override
    protected void notifyExporterAdded(Exporter exporter) {
        writeScrapeConfig();
    }

    @Override
    protected void notifyExporterRemoved(Exporter exporter) {
        writeScrapeConfig();
    }

    /**
     * Changes the prometheus configuration for modified scrape setup.
     */
    private void writeScrapeConfig() {
        ConfigModifier m = modifier.get();
        for (Exporter e : exporters()) {
            m.addScrapeEndpoint(((PrometheusExporter) e).getScrapeEntry());
        }
    }

}
