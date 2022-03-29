package de.iip_ecosphere.platform.monitoring.prometheus;
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

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProvider;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

/**
 * This class instruments a custom exporter for the metrics in
 * IIP-Ecosphere and creates and a scraping point for Prometheus.
 * @author bettelsc
 *
 */
public class IIPEcosphereExporter {
    @SuppressWarnings("unused")
    private static final String HOST = "localhost";
    @SuppressWarnings("unused")
    private static final String EXPOSE_PORT = "9600";
    private static PrometheusMeterRegistry prometheusRegistry;
    private static MetricsProvider metricsProvider;
    
    /** Empty Constructor.
     * 
     */
    public IIPEcosphereExporter() {}
    
    /** Fetch-method.
     * 
     * @return prometheusRegistry.scrape() scraping method for the metrics
     */
    public static String fetch() {
        prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        //MetricsProvider with PrometheusRegistry
        metricsProvider = new MetricsProvider(prometheusRegistry);
        metricsProvider.calculateNonNativeSystemMetrics();
        metricsProvider.registerNonNativeSystemMetrics();
        metricsProvider.registerMemoryMetrics();
        metricsProvider.registerDiskMetrics();
        
        return prometheusRegistry.scrape();
    }

    /** exposes The metrics to a scraping point.
     * 
     */
    public void runEndpoint() {    
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(9642), 0);
            server.createContext("/prometheus", httpExchange -> {
                String response = fetch();
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });
            
            new Thread(server::start).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }  
}
