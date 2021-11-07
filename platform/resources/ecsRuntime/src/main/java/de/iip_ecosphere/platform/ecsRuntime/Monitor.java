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

package de.iip_ecosphere.platform.ecsRuntime;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProvider;
import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsAasConstructor;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Does OS/JVM level monitoring.
 * 
 * @author Holger Eichelberger, SSE
 */
class Monitor {
    
    public static final String TRANSPORT_METRICS_CHANNEL = "EcsMetrics";
    private static MetricsProvider provider = new MetricsProvider(new SimpleMeterRegistry());
    private static Timer timer = new Timer();
    private static TransportConnector connector;
    private static boolean update = false;

    /**
     * Returns the metrics provider.
     * 
     * @return the metrics provider
     */
    static MetricsProvider getMetricsProvider() {
        return provider;
    }
    
    /**
     * Starts metrics scheduling.
     */
    static void startScheduling() {
        final String id = Id.getDeviceId();
        EcsSetup config = EcsFactory.getConfiguration();
        TransportSetup transport = config.getTransport();
        if (null != transport) {
            try {
                connector = TransportFactory.createConnector();
                connector.connect(transport.createParameter());
            } catch (IOException e) {
                LoggerFactory.getLogger(Monitor.class).error("Cannot create transport connector: " + e.getMessage());
                connector = null;
            }
        }
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                provider.calculateNonNativeSystemMetrics();
                if (null != connector) {
                    try {
                        connector.asyncSend(TRANSPORT_METRICS_CHANNEL, provider.toJson(id, update));
                    } catch (IOException e) {
                        LoggerFactory.getLogger(Monitor.class).error(
                            "Cannot sent monitoring message: " + e.getMessage());
                    }
                    update = true;
                }
            }
            
        }, 0, config.getMonitoringUpdatePeriod());
    }

    /**
     * Stops metrics scheduling.
     */
    static void stopScheduling() {
        MetricsAasConstructor.clear();
        timer.cancel();
        if (null != connector) {
            try {
                connector.disconnect();
            } catch (IOException e) {
                LoggerFactory.getLogger(Monitor.class).error(
                    "Cannot disconnect transport connector: " + e.getMessage());
            }
        }
    }

}
