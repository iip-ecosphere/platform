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
/**
 * Class to with constant values for config purposes on test setup.
 */
public class PrometheusProjectConstants {
    public static final String PROMETHEUSSERVERIP = "192.168.2.118";
    public static final String PROMETHEUSPUSHGATEWAYIP = "192.168.2.118";
    public static final int PROMETHEUSSERVERPORT = 9090;
    public static final int PROMETHEUSPUSHGATEWAYPORT = 9400;
    
    //Hivemq test server
    public static final String HIVEMQSERVERIP = "192.168.2.101";
    public static final String HIVEMQMQTTBROKERIP = "192.168.2.101";
    public static final int HIVEMQSERVERPORT = 9321;
    public static final int HIVEMQMQTTBROKERPORT = 1883;
    
    public static final String PROMETHEUS = "prometheus";
    public static final String PROMETHEUS_VERSION = "2.34.0";
    public static final String PROMETHEUS_CONFIG = "prometheus.yml";
    public static final String PROMETHEUS_ZIP_WINDOWS = "src/main/resources/prometheus-2.34.0-win64.zip";
    public static final String PROMETHEUS_BINARY_WINDOWS = "src/main/resources";
    public static final String PROMETHEUS_ZIP_LINUX = "src/main/resources/prometheus-2.34.0.zip";
    public static final String PROMETHEUS_BINARY_LINUX = "src/main/resources";
}
