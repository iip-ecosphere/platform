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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.monitoring.MonitoringReceiver;
import de.iip_ecosphere.platform.monitoring.prometheus.ConfigModifier.ScrapeEndpoint;
import de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProvider;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Schema;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.prometheus.client.hotspot.DefaultExports;
import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.DEvent;
import si.matjazcerkvenik.alertmonitor.util.AmMetrics;

/**
 * Observes IIP-Ecosphere standard transport channels and feeds the information into Prometheus. [public for testing]
 * 
 * @author Holger Eichelberger, SSE
 */
public class IipEcospherePrometheusExporter extends MonitoringReceiver {

    public static final String DEFAULT_METRICS_SERVLET_NAME = "metrics";
    public static final String DEFAULT_METRICS_ENDPOINT = "/" + DEFAULT_METRICS_SERVLET_NAME;
    private static final MeterFilter[] METER_FILTERS = MetricsProvider.append(
        MetricsProvider.DEFAULT_METER_FILTERS, MeterFilter.denyNameStartsWith("alertmonitor."));
    
    private Tomcat server;
    private Context context;
    private Supplier<ConfigModifier> modifier;
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
            port = setup.getPrometheus().getExporter().getPort();
            if (port < 0) {
                port = NetUtils.getEphemeralPort();
                setup.getPrometheus().getExporter().setPort(port); // "reconfigure"
            }
            LoggerFactory.getLogger(getClass()).info("Starting prometheus export endpoint on port {}", port);
            Thread serverThread = new Thread(() -> {
                try {
    
                    server = new Tomcat();
                    File home = server.getEngine().getCatalinaHome();
                    server.setBaseDir(home.getName());
                    server.setPort(port);
                    server.setHostname("localhost");
                    server.getHost().setAppBase(".");
                    webapps = new File(home, "webapps");
                    webapps.mkdirs();
                    String contextPath = "";
                    String docBase = new File(".").getAbsolutePath();
                    context = server.addContext(contextPath, docBase);
        
                    DefaultExports.initialize();
                    addServlet(DEFAULT_METRICS_SERVLET_NAME, new PrometheusMetricsServlet());

                    server.getConnector();
                    server.start();
                    server.getServer().await();
                } catch (LifecycleException e) {
                    e.printStackTrace();
                }
            });
            serverThread.start();
        } catch (Exception  e) {
            LoggerFactory.getLogger(getClass()).error("Starting prometheus export endpoint: {}", e.getMessage());
        }
    }

    /**
     * The default metrics servlet. Adapted from 
     * {@link import si.matjazcerkvenik.alertmonitor.web.PrometheusMetricsServlet} as that class is not sufficiently 
     * configurable/reusable. 
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class PrometheusMetricsServlet extends HttpServlet {

        private static final long serialVersionUID = 7153742267407172657L;

        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
                throws IOException {

            AmMetrics.alertmonitor_active_alerts_count.clear();
            for (DEvent n : DAO.getInstance().getActiveAlerts().values()) {
                AmMetrics.alertmonitor_active_alerts_count.labels(n.getAlertname(), n.getSeverity()).inc();
            }
            AmMetrics.alertmonitor_alerts_balance_factor.set(DAO.getInstance().calculateAlertsBalanceFactor());
            AmMetrics.alertmonitor_last_event_timestamp.set(AmMetrics.lastEventTimestamp);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(TextFormat.CONTENT_TYPE_004);

            Writer writer = resp.getWriter();
            try {
                Set<String> included = parse(req);
                TextFormat.write004(writer, AmMetrics.registry.filteredMetricFamilySamples(
                    id -> included.contains(id) // below: names in scrape format, filter potentially with "."
                        || MetricsProvider.include(id.replaceAll("_", "."), METER_FILTERS)));
                writer.flush();
            } finally {
                writer.close();
            }
        }

        /**
         * Parses the names to be included from the servlet request.
         * 
         * @param req the request
         * @return the set of names, may be empty
         */
        private Set<String> parse(HttpServletRequest req) {
            String[] includedParam = req.getParameterValues("name[]");
            if (includedParam == null) {
                return Collections.emptySet();
            } else {
                return new HashSet<String>(Arrays.asList(includedParam));
            }
        }

        @Override
        protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
                throws IOException {
            doGet(req, resp);
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
            // usual stuff added automatically in a spring environment
            MetricsProvider.apply(this, MetricsProvider.DEFAULT_METER_FILTERS);
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
     * A simple metrics servlet for a given local, bridged metrics registry. A bit like 
     * {@link PrometheusMetricsServlet} (unfortunately not much reusable), but with definable registry suitable
     * for our purposes.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class RegistryServlet extends HttpServlet {

        private static final long serialVersionUID = -3178584303722836948L;
        private PrometheusMeterRegistry registry;

        /**
         * Creates the servlet instance.
         * 
         * @param registry the registry.
         */
        private RegistryServlet(PrometheusMeterRegistry registry) {
            this.registry = registry;
        }

        /**
         * Parses the requested names from the request.
         * 
         * @param req the request
         * @return the names
         */
        public static Set<String> parseNames(HttpServletRequest req) {
            String[] includedParam = req.getParameterValues("name[]");
            if (includedParam == null) {
                return Collections.emptySet();
            } else {
                return new HashSet<String>(Arrays.asList(includedParam));
            }
        }

        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
            resp.setContentType(TextFormat.CONTENT_TYPE_004);

            Writer writer = new BufferedWriter(resp.getWriter());
            try {
                writer.append(registry.scrape(TextFormat.CONTENT_TYPE_004, parseNames(req)));
                writer.flush();
            } finally {
                writer.close();
            }
            resp.setStatus(200);
        }

        @Override
        protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
            doGet(req, resp);
        }

    }

    /**
     * Adds a servlet to the tomcat instance. Tomcat must at least be initialized:
     * 
     * @param id the id of the servlet, also used for the path
     * @param servlet the servlet instance
     * @return the path to the servlet
     */
    protected String addServlet(String id, Servlet servlet) {
        String path = "/" + id;
        Wrapper newWrapper = context.createWrapper();
        newWrapper.setName(id);
        newWrapper.setLoadOnStartup(1);
        newWrapper.setServlet(servlet);
        context.addChild(newWrapper);
        context.addServletMappingDecoded(path + "/*", id);
        return path;
    }

    /**
     * Implements a specialized exporter for prometheus.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected class PrometheusExporter extends Exporter {

        private MyPrometheusMeterRegistry registry = new MyPrometheusMeterRegistry();
        private ScrapeEndpoint entry;
        private RegistryServlet servlet = new RegistryServlet(registry);        
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
            String path = addServlet(id, servlet);
            entry = new ScrapeEndpoint(id, new Endpoint(Schema.HTTP, port, path));
            LoggerFactory.getLogger(getClass()).info("Added device context {}", path);
        }
        
        /**
         * Returns the scrape entry.
         * 
         * @return the entry
         */
        protected ScrapeEndpoint getScrapeEntry() {
            return entry;
        }
        
        // checkstyle: stop exception type check
        
        @Override
        protected void addMeter(Meter meter)  {
            if (null != meter) {
                try {
                    registry.remove(meter.getId());
                    registry.createMeter(meter.getId(), meter.getId().getType(), meter.measure());
                } catch (Throwable t) {
                    LoggerFactory.getLogger(getClass()).error("Cannot add meter ({}, {}, {}): {}", meter.getId(), 
                        meter.getId().getType(), meter.measure(), t.getMessage());
                }
            }
        }

        // checkstyle: resume exception type check

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
        if (null != modifier) {
            ConfigModifier m = modifier.get();
            for (Exporter e : exporters()) {
                m.addScrapeEndpoint(((PrometheusExporter) e).getScrapeEntry());
            }
            m.end();
        }
    }

}
