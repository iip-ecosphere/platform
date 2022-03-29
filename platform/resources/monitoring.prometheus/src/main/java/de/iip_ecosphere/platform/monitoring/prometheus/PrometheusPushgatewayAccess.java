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

import de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProvider;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;
import io.prometheus.client.exporter.PushGateway;

/** Pushgateway access.
 * 
 * @author const
 *
 */
public class PrometheusPushgatewayAccess {
    @SuppressWarnings("unused")
    private String host;
    @SuppressWarnings("unused")
    private int port;
    private PushGateway pushgateway;

    /** Constructor with Hostname and Port.
     * 
     * @param host
     * @param port
     */
    public PrometheusPushgatewayAccess(String host, int port) {
        this.host = host;
        this.port = port;
        pushgateway = new PushGateway(host + ":" + port);
    }
    
    // checkstyle: stop parameter number check
    
    /** Counter job.
     * 
     * @param metricsProvider
     * @param prometheusRegistry
     * @param job_name
     * @param metric_name
     * @param help_message
     * @param gateway
     * @throws IOException
     */
    public void runBatchJobCounter(MetricsProvider metricsProvider, PrometheusMeterRegistry prometheusRegistry,
            String job_name, String metric_name, String help_message, PushGateway gateway) throws IOException {
        @SuppressWarnings("unused")
        Counter counter = Counter.build().name(metric_name).help(help_message)
                .register(prometheusRegistry.getPrometheusRegistry());
        pushgateway.pushAdd(prometheusRegistry.getPrometheusRegistry(), job_name);
    }
    /** Gauge Job.
     * 
     * @param metricsProvider
     * @param prometheusRegistry
     * @param job_name
     * @param metric_name
     * @param help_message
     * @param gateway
     * @throws IOException
     */
    public void runBatchJobGauge(MetricsProvider metricsProvider, PrometheusMeterRegistry prometheusRegistry,
            String job_name, String metric_name, String help_message, PushGateway gateway) throws IOException {
        @SuppressWarnings("unused")
        Gauge gauge = Gauge.build().name(metric_name).help(help_message)
                .register(prometheusRegistry.getPrometheusRegistry());
        pushgateway.pushAdd(prometheusRegistry.getPrometheusRegistry(), job_name);
    }
    
    /** Summary Job.
     * 
     * @param metricsProvider
     * @param prometheusRegistry
     * @param job_name
     * @param metric_name
     * @param help_message
     * @param gateway
     * @throws IOException
     */
    public void runBatchJobSummary(MetricsProvider metricsProvider, PrometheusMeterRegistry prometheusRegistry,
            String job_name, String metric_name, String help_message, PushGateway gateway) throws IOException {
        @SuppressWarnings("unused")
        Summary summary = Summary.build().name(metric_name).help(help_message)
                .register(prometheusRegistry.getPrometheusRegistry());
        pushgateway.pushAdd(prometheusRegistry.getPrometheusRegistry(), job_name);
    }

    /** Histogram Job.
     * 
     * @param metricsProvider
     * @param prometheusRegistry
     * @param job_name
     * @param metric_name
     * @param help_message
     * @param gateway
     * @throws IOException
     */
    public void runBatchJobHistogram(MetricsProvider metricsProvider, PrometheusMeterRegistry prometheusRegistry,
            String job_name, String metric_name, String help_message, PushGateway gateway) throws IOException {
        @SuppressWarnings("unused")
        Histogram histogram = Histogram.build().name(metric_name).help(help_message)
                .register(prometheusRegistry.getPrometheusRegistry());
        pushgateway.pushAdd(prometheusRegistry.getPrometheusRegistry(), job_name);
    }
    
    // checkstyle: resume parameter number check

    /** Getter for the gateway.
     * 
     * @return pushgateway
     */
    public PushGateway getPushgateway() {
        return pushgateway;
    }

}
