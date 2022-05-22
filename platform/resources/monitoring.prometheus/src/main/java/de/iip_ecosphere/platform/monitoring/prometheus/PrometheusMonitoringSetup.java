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

import java.io.IOException;

import de.iip_ecosphere.platform.monitoring.MonitoringSetup;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.iip_aas.config.ServerAddressHolder;

/**
 * Extended prometheus monitoring setup.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PrometheusMonitoringSetup extends MonitoringSetup {

    public static final String DEFAULT_PROMETHEUS_SERVER = "localhost";
    public static final int DEFAULT_PROMETHEUSSERVER_PORT = 9090;
    public static final int DEFAULT_ALERTMGR_PORT = 9090;

    private static PrometheusMonitoringSetup instance;
    
    public static class PrometheusSetup {

        private ServerAddressHolder server = new ServerAddressHolder(
            Schema.HTTP, DEFAULT_PROMETHEUS_SERVER, DEFAULT_PROMETHEUSSERVER_PORT);
        private ServerAddressHolder exporter = new ServerAddressHolder(
            Schema.HTTP, DEFAULT_PROMETHEUS_SERVER, -1); // later ephemeral
        private ServerAddressHolder alertMgr = new ServerAddressHolder(
            Schema.HTTP, DEFAULT_PROMETHEUS_SERVER, DEFAULT_ALERTMGR_PORT);

        private int scrapeInterval = 1000;
        private int scrapeTimeout = 1000; 
        private int evaluationInterval = 3000; 

        /**
         * Returns the Prometheus server information.
         * 
         * @return the Prometheus server information
         */
        public ServerAddressHolder getServer() {
            return server;
        }

        /**
         * Returns the Prometheus server information. [snakeyaml]
         * 
         * @param server the Prometheus server information
         */
        public void setServer(ServerAddressHolder server) {
            this.server = server;
        }

        /**
         * Returns the scrape interval.
         * 
         * @return the scrape interval in ms
         */
        public int getScrapeInterval() {
            return scrapeInterval;
        }

        /**
         * Defines the scrape interval. [snakeyaml]
         * 
         * @param scrapeInterval in ms
         */
        public void setScrapeInterval(int scrapeInterval) {
            this.scrapeInterval = scrapeInterval;
        }

        /**
         * Returns the evaluation interval.
         * 
         * @return the evaluation interval in ms
         */
        public int getEvaluationInterval() {
            return evaluationInterval;
        }

        /**
         * Defines the evaluation interval. [snakeyaml]
         * 
         * @param evaluationInterval in ms
         */
        public void setEvaluationInterval(int evaluationInterval) {
            this.evaluationInterval = evaluationInterval;
        }

        /**
         * Returns the scrape timeout.
         * 
         * @return the scrape timeout in ms
         */
        public int getScrapeTimeout() {
            return scrapeTimeout;
        }

        /**
         * Returns the safe scrape timeout, i.e., bounded by {@link #getScrapeInterval()}.
         * 
         * @return the scrape timeout in ms
         */
        public int getScrapeTimeoutSafe() {
            return scrapeTimeout > scrapeInterval 
                ? scrapeInterval : scrapeTimeout;
        }

        /**
         * Defines the scrape timeout. [snakeyaml]
         * 
         * @param scrapeTimeout in ms
         */
        public void setScrapeTimeout(int scrapeTimeout) {
            this.scrapeTimeout = scrapeTimeout;
        }

        /**
         * Returns the address for the prometheus exporter.
         * 
         * @return the address, port may be negative for ephemeral
         */
        public ServerAddressHolder getExporter() {
            return exporter;
        }

        /**
         * Defines the address for the prometheus exporter. [snakeyaml]
         * 
         * @param exporter the exporter address, may have a negative port for ephemeral
         */
        public void setExporter(ServerAddressHolder exporter) {
            this.exporter = exporter;
        }

        /**
         * Returns the port for the prometheus alert manager.
         * 
         * @return the port, may be negative for disabled
         */
        public ServerAddressHolder getAlertMgr() {
            return alertMgr;
        }

        /**
         * Defines the alert manager address. [snakeyaml]
         * 
         * @param alertMgr the alert manger address
         */
        public void setAlertMgr(ServerAddressHolder alertMgr) {
            this.alertMgr = alertMgr;
        }

    }

    private PrometheusSetup prometheus = new PrometheusSetup();

    /**
     * Returns the prometheus setup.
     * 
     * @return the prometheus setup
     */
    public PrometheusSetup getPrometheus() {
        return prometheus;
    }

    /**
     * Defines the prometheus setup. [snakeyaml]
     * 
     * @param prometheus the prometheus setup
     */
    public void setPrometheus(PrometheusSetup prometheus) {
        this.prometheus = prometheus;
    }

    /**
     * Reads a {@link PrometheusMonitoringSetup} instance from {@link AbstractSetup#DEFAULT_FNAME) in the root folder 
     * of the jar/classpath. 
     *
     * @return the configuration instance
     * @see #readFromYaml(Class)
     */
    public static PrometheusMonitoringSetup readConfiguration() throws IOException {
        return readFromYaml(PrometheusMonitoringSetup.class);
    }
    
    /**
     * Returns the setup instance.
     * 
     * @return the instance
     */
    public static PrometheusMonitoringSetup getInstance() {
        if (null == instance) {
            try {
                instance = readConfiguration();
            } catch (IOException e) {
                instance = new PrometheusMonitoringSetup();
            }
        }
        return instance;
    }
    
}
