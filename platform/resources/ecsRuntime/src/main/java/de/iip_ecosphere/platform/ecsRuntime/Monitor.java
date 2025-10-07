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

import de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProvider;
import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsAasConstructor;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.streams.StreamNames;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.metrics.MetricsFactory;

/**
 * Does OS/JVM level monitoring. [public for testing]
 * 
 * @author Holger Eichelberger, SSE
 */
public class Monitor {
    
    public static final String TRANSPORT_METRICS_CHANNEL = StreamNames.RESOURCE_METRICS;
    private static MetricsProvider provider = new MetricsProvider(MetricsFactory.getInstance().createRegistry());
    private static Timer timer = new Timer();
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
     * Starts metrics scheduling. [public for testing]
     */
    public static void startScheduling() {
        Transport.createConnector();
        scheduleMonitoringTask();
    }

    /**
     * Schedules the monitoring task an in case of interruption during send tries to re-schedule the task.
     */
    private static void scheduleMonitoringTask() {
        final String id = Id.getDeviceId();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                provider.calculateMetrics();
                if (null != Transport.getConnector()) {
                    try {
                        String json = provider.toJson(id, update);
                        TransportConnector tc = Transport.getConnector();
                        if (null != tc) {
                            tc.asyncSend(TRANSPORT_METRICS_CHANNEL, json);
                        } else {
                            LoggerFactory.getLogger(Monitor.class).error(
                                "Cannot sent monitoring message: No transport connector");
                        }
                        MetricsAasConstructor.pushToAasAlways(json, EcsAas.NAME_SUBMODEL, 
                            MetricsAasConstructor.DFLT_SUBMODEL_SUPPLIER, update, null);
                    } catch (IOException e) {
                        LoggerFactory.getLogger(Monitor.class).error(
                            "Cannot sent monitoring message: " + e.getMessage());
                        if (Thread.currentThread().isInterrupted()) { // keep heartbeat alive
                            scheduleMonitoringTask();
                        }
                    }
                    update = true;
                }
            }
            
        }, 0, EcsFactory.getSetup().getMonitoringUpdatePeriod());
    }

    /**
     * Stops metrics scheduling. [public for testing]
     */
    public static void stopScheduling() {
        MetricsAasConstructor.clear();
        timer.cancel();
        Transport.releaseConnector();
    }

}
