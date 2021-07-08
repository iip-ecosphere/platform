/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.environment.spring;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import de.iip_ecosphere.platform.services.environment.Service;
import de.iip_ecosphere.platform.services.environment.ServiceMapper;
import de.iip_ecosphere.platform.services.environment.YamlArtifact;
import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsExtractorRestClient;
import de.iip_ecosphere.platform.services.environment.spring.metricsProvider.MetricsProvider;

/**
 * A specialized starter for Spring Cloud Stream in including the metrics provider.
 * 
 * @author Holger Eichelberger, SSE
 */
@ComponentScan(basePackageClasses = MetricsProvider.class)
@EnableScheduling
@Import({MetricsProvider.class})
@Component
public abstract class Starter extends de.iip_ecosphere.platform.services.environment.Starter {

    private static ConfigurableApplicationContext ctx;
    private static Environment environment;
    private static int port = 8080; // assumed default
    
    @Autowired
    private ServerProperties serverProperties;

    /**
     * Creates an instance.
     * 
     * @param env the Spring environment
     */
    @Autowired
    public Starter(Environment env) {
        environment = env;
    }
    
    /**
     * Initializes the services (if available), starts the AAS command server.
     * 
     * @see #createServices(YamlArtifact)
     */
    @PostConstruct
    public void initialize() {
        if (null != serverProperties) {
            port = serverProperties.getPort();
            LoggerFactory.getLogger(Starter.class).info("Using spring application server port " + port);
        } else {
            String tmp = null != environment ? environment.getProperty("server.port") : "";
            if (null != tmp) {
                try {
                    port = Integer.parseInt(tmp);
                    LoggerFactory.getLogger(Starter.class).info("Using spring application server port " + port);
                } catch (NumberFormatException e) {
                    LoggerFactory.getLogger(Starter.class).error("Cannot read spring application server port: " + tmp 
                        + "; " + e.getMessage() + " using assumed default: " + port);    
                }
            } else {
                LoggerFactory.getLogger(Starter.class).info("Using (assumed default) spring application server port " 
                    + port);
            }
        }
        // start the command server
        try {
            // assuming that deployment.yml variants for testing contain the same service descriptions (modulo 
            // technical information)
            YamlArtifact art = YamlArtifact.readFromYaml(
                getClass().getClassLoader().getResourceAsStream("/deployment.yml"));
            List<Service> services = createServices(art);
            if (null != services) { 
                ServiceMapper mapper = new ServiceMapper(Starter.getProtocolBuilder());
                MetricsExtractorRestClient metricsClient = createMetricsClient(environment);
                for (Service service : services) {
                    mapService(mapper, service, metricsClient);
                }
            }
            Starter.start();
        } catch (IOException e) {
            System.out.println("Cannot find service descriptor/start command server.");
        }
    }

    /**
     * Maps a service through the default mapper and the default metrics client. [Convenience method for generation]
     * 
     * @param service the service to be mapped (may be <b>null</b>, no mapping will happen then)
     * 
     * @see #getServiceMapper()
     * @see #createMetricsClient()
     * @see #mapService(ServiceMapper, Service, MetricsExtractorRestClient)
     */
    public static void mapService(Service service) {
        mapService(getServiceMapper(), service, createMetricsClient());
    }

    /**
     * Maps a service through a given mapper and metrics client. No mapping will take place if either {@code service},
     * {@code mapper} or {@link #getProtocolBuilder()} is <b>null</b>. The specific mapping for the metrics will only
     * take place if {@code metricsClient} is not <b>null</b>.
     * 
     * @param mapper the service mapper instance (may be <b>null</b>, no mapping will happen then)
     * @param service the service to be mapped (may be <b>null</b>, no mapping will happen then)
     * @param metricsClient the metrics client (may be <b>null</b> for none)
     */
    public static void mapService(ServiceMapper mapper, Service service, MetricsExtractorRestClient metricsClient) {
        if (null != service && null != mapper && null != Starter.getProtocolBuilder()) {
            mapper.mapService(service);
            if (null != metricsClient) {
                mapper.mapMetrics(service, metricsClient);
            }
        }
    }

    /**
     * Creates a metrics client.
     * 
     * @param environment the Spring environment
     * @return the metrics REST client, may be <b>null</b>
     */
    public static MetricsExtractorRestClient createMetricsClient(Environment environment) {
        return new MetricsExtractorRestClient("localhost", port);
    }
    
    /**
     * Creates a metrics client based on the known Spring environment. Only available after 
     * {@link #main(Class, String[])}.
     * 
     * @return the metrics REST client, may be <b>null</b>
     */
    public static MetricsExtractorRestClient createMetricsClient() {
        return createMetricsClient(environment);
    }
    
    /**
     * Returns the application context. Only available after {@link #main(Class, String[])}.
     * 
     * @return the context, may be <b>null</b>
     */
    protected static ConfigurableApplicationContext getContext() {
        return ctx;
    }
    
    /**
     * Creates the relevant services from the given {@code artifact}.
     * 
     * @param artifact the artifact
     * @return the services (may be empty or <b>null</b> for none)
     */
    protected abstract List<Service> createServices(YamlArtifact artifact);
    
    /**
     * Returns the spring environment.
     * 
     * @return the spring environment
     */
    protected Environment getEnvironment() {
        return environment;
    }
    
    /**
     * Main function.
     * 
     * @param cls the class to start
     * @param args command line arguments
     */
    public static void main(Class<? extends Starter> cls, String[] args) {
        Starter.parse(args);
        // start spring cloud app
        SpringApplication app = new SpringApplication(cls);
        ctx = app.run(args);
    }
    
}
