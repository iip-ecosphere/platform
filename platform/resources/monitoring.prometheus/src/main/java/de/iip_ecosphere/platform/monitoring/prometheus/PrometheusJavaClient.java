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
import de.iip_ecosphere.platform.transport.serialization.Serializer;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;

import java.io.IOException;

import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter.TransportParameterBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


import io.micrometer.core.instrument.Counter;
//import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tags;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.PushGateway;
import de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProvider;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;

public class PrometheusJavaClient {
    /** Test Main.
      * 
     * @param args
     */
    public static void main(String[] args) {
        // Create PrometheusRegistry
        PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        // MetricsProvider with PrometheusRegistry
        MetricsProvider metricsProvider = new MetricsProvider(prometheusRegistry);
        // Usage for the Pushgateway
        PrometheusPushgatewayAccess gatewayAccess = new PrometheusPushgatewayAccess(
                PrometheusProjectConstants.PROMETHEUSPUSHGATEWAYIP,
                PrometheusProjectConstants.PROMETHEUSPUSHGATEWAYPORT);
        try {
            gatewayAccess.runBatchJobGauge(metricsProvider, prometheusRegistry, "gauge_job", "test_gauge",
                    "Test Gauge", gatewayAccess.getPushgateway());
            gatewayAccess.runBatchJobCounter(metricsProvider, prometheusRegistry, "counter_job", "test_counter",
                    "Test Counter", gatewayAccess.getPushgateway());
            gatewayAccess.runBatchJobSummary(metricsProvider, prometheusRegistry, "summary_job", "test_summary",
                    "Test Counter", gatewayAccess.getPushgateway());
            gatewayAccess.runBatchJobHistogram(metricsProvider, prometheusRegistry, "histogram_job", "test_histogram",
                    "Test Histogram", gatewayAccess.getPushgateway());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            micrometerMetricsTest();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /** Test von Micrometer abgekapselt.
     * 
     * @throws IOException
     */
    public static void micrometerMetricsTest() throws IOException {
        PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        MetricsProvider metricsProvider = new MetricsProvider(prometheusRegistry);
        metricsProvider.calculateNonNativeSystemMetrics();
        metricsProvider.registerNonNativeSystemMetrics();
        metricsProvider.registerMemoryMetrics();
        metricsProvider.registerDiskMetrics();

        Gauge duration = Gauge.build().name("my_batch_job_duration_seconds")
                .help("Duration of my batch job in seconds.").register(prometheusRegistry.getPrometheusRegistry());
        Gauge.Timer durationTimer = duration.startTimer();
        try {
            Gauge lastSuccess = Gauge.build().name("my_batch_job_last_success")
                    .help("Last time my batch job succeeded, in unixtime.")
                    .register(prometheusRegistry.getPrometheusRegistry());
            lastSuccess.setToCurrentTime();
        } finally {
            durationTimer.setDuration();
            PushGateway pg = new PushGateway("192.168.2.118:9400");
            pg.pushAdd(prometheusRegistry.getPrometheusRegistry(), "my_batch_job");
        }

        System.out.println(prometheusRegistry.scrape());
    }
    
    /** Simple metrics test.
     * 
     */
    public static void startMetricTest() {
        PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        Counter.builder("http_requests_total").description("Http Request Total")
                .tags("method", "GET", "handler", "/employee", "status", "200").register(prometheusRegistry)
                .increment();
        Counter.builder("http_requests_total").description("Http Request Total")
                .tags("method", "GET", "handler", "/employee", "status", "200").register(prometheusRegistry)
                .increment();

        DistributionSummary.builder("http_response_time_milliseconds")
                .description("Request completed time in milliseconds")
                .tags("method", "GET", "handler", "/employee", "status", "200").publishPercentiles(.5, .95, .99)
                .register(prometheusRegistry).record(40d);
        DistributionSummary.builder("http_response_time_milliseconds")
                .description("Request completed time in milliseconds")
                .tags("method", "GET", "handler", "/employee", "status", "200").publishPercentiles(.5, .95, .99)
                .register(prometheusRegistry).record(50d);

        prometheusRegistry.counter("http_requests_total2", "method", "GET", "status", "200").increment();
        prometheusRegistry.counter("http_requests_total2", "method", "Post", "status", "200").increment();
        prometheusRegistry.counter("http_requests_total2", "method", "GET", "status", "200").increment();

        prometheusRegistry.newCounter(
                new Meter.Id("query time", Tags.of("select query", "country"), null, "query desc", Meter.Type.COUNTER));
        System.out.println(prometheusRegistry.scrape());

    }
    /** simple connector test.
     * 
     * @param addr
     * @param serializerType
     * @throws IOException
     */
    public static void connectorTest(ServerAddress addr, Class<? extends Serializer<TestObject>> serializerType)
            throws IOException {
        TestObject obj1 = new TestObject("obj1", 10);
        TestObject obj2 = new TestObject("obj2", 20);
        SerializerRegistry.registerSerializer(serializerType);
        TransportParameterBuilder builder = TransportParameterBuilder.newBuilder(addr).setApplicationId("cl1");
        TransportParameter param1 = builder.build();
        TransportConnector cl1 = TransportFactory.createConnector();
        System.out.println("Connecting connector 1 to: " + addr.toUri());
        cl1.connect(param1);
        final String stream1 = cl1.composeStreamName("", "stream1");
        final String stream2 = cl1.composeStreamName("", "stream2");
        final Callback cb1 = new Callback();
        cl1.setReceptionCallback(stream2, cb1);

        TransportParameterBuilder builder2 = TransportParameterBuilder.newBuilder(addr).setApplicationId("cl2");
        TransportParameter param2 = builder2.build();
        TransportConnector cl2 = TransportFactory.createConnector();
        System.out.println("Connecting connector 2 to: " + addr.toUri());
        cl2.connect(param2);
        final Callback cb2 = new Callback();
        cl2.setReceptionCallback(stream1, cb2);

        System.out.println("Sending/Receiving");
        cl1.syncSend(stream1, obj1);
        cl2.syncSend(stream2, obj2);
        TimeUtils.sleep(2000);
        System.out.println("Cleaning up");
        System.out.println("Waiting 20 seconds...");
        TimeUtils.sleep(20000);
        cl1.disconnect();
        cl2.disconnect();
        System.out.println("Connectors disconnected!");
        SerializerRegistry.unregisterSerializer(TestObject.class);

    }

    /** simple batch job for pushgateway.
     * 
     * @param counter
     * @throws Exception
     */
    public static void executeBatchJob(int counter) throws IOException {
        CollectorRegistry registry = new CollectorRegistry();
        Gauge duration = Gauge.build().name("my_batch_job_duration_seconds")
                .help("Duration of my batch job in seconds.").register(registry);
        Gauge.Timer durationTimer = duration.startTimer();
        try {
            Gauge lastSuccess = Gauge.build().name("my_batch_job_last_success")
                    .help("Last time my batch job succeeded, in unixtime.").register(registry);
            lastSuccess.setToCurrentTime();
        } finally {
            durationTimer.setDuration();
            PushGateway pg = new PushGateway("192.168.2.118:9400");
            pg.pushAdd(registry, "my_batch_job" + counter);
        }
    }
    
    /** Simple httpTest.
     * 
     */
    public static void httpTest() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://192.168.2.118:9400/metrics")).build();

        HttpResponse<String> response;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
