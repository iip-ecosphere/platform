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

/**
 * Extended prometheus monitoring setup.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PrometheusMonitoringSetup extends MonitoringSetup {

    public static final String DEFAULT_PROMETHEUS_SERVER = "localhost";
    public static final int DEFAULT_PROMETHEUSSERVERPORT = 9090;
    public static final int DEFAULT_PROMETHEUSPUSHGATEWAYPORT = 9400;

    private static PrometheusMonitoringSetup instance;
    
    private PrometheusServerAddressHolder prometheusServer = new PrometheusServerAddressHolder(
        Schema.HTTP, DEFAULT_PROMETHEUS_SERVER, DEFAULT_PROMETHEUSSERVERPORT);

    /*private PrometheusServerAddressHolder prometheusPushGateway = new PrometheusServerAddressHolder(
        Schema.HTTP, DEFAULT_PROMETHEUS_SERVER, DEFAULT_PROMETHEUSPUSHGATEWAYPORT);*/
    
    private int prometheusExporterPort = -1; // ephemeral

    /**
     * Returns the Prometheus server information.
     * 
     * @return the Prometheus server information
     */
    public PrometheusServerAddressHolder getPrometheusServer() {
        return prometheusServer;
    }

    /**
     * Returns the Prometheus push gateway information.
     * 
     * @return the Prometheus push gateway information
     */
    /*public PrometheusServerAddressHolder getPrometheusPushGateway() {
        return prometheusPushGateway;
    }*/
    

    /**
     * Returns the Prometheus server information. [snakeyaml]
     * 
     * @param prometheusServer the Prometheus server information
     */
    public void setPrometheusServer(PrometheusServerAddressHolder prometheusServer) {
        this.prometheusServer = prometheusServer;
    }

    /**
     * Changes the Prometheus push gateway information. [snakeyaml]
     * 
     * @param prometheusPushGateway the Prometheus push gateway information
     */
    /*public void getPrometheusPushGateway(PrometheusServerAddressHolder prometheusPushGateway) {
        this.prometheusPushGateway = prometheusPushGateway;
    }*/
    
    /**
     * Returns the port for the prometheus exporter.
     * 
     * @return the port, may be negative for ephemeral
     */
    public int getPrometheusExporterPort() {
        return prometheusExporterPort;
    }

    /**
     * Defines the port for the prometheus exporter. [snakeyaml]
     * 
     * @param exporterPort the port, may be negative for ephemeral
     */
    public void setPrometheusExporterPort(int exporterPort) {
        this.prometheusExporterPort = exporterPort;
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