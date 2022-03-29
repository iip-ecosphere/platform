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
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Does OS/JVM level monitoring.
 * 
 * @author Holger Eichelberger, SSE
 */
class Monitor extends de.iip_ecosphere.platform.transport.status.Monitor {
    
    public static final String TRANSPORT_METRICS_CHANNEL = "EcsMetrics";
    private static MetricsProvider provider = new MetricsProvider(new SimpleMeterRegistry());
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
     * Starts metrics scheduling.
     */
    static void startScheduling() {
        final String id = Id.getDeviceId();
        createConnector();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                provider.calculateNonNativeSystemMetrics();
                if (null != getConnector()) {
                    try {
                        getConnector().asyncSend(TRANSPORT_METRICS_CHANNEL, provider.toJson(id, update));
                    } catch (IOException e) {
                        LoggerFactory.getLogger(Monitor.class).error(
                            "Cannot sent monitoring message: " + e.getMessage());
                    }
                    update = true;
                }
            }
            
        }, 0, EcsFactory.getSetup().getMonitoringUpdatePeriod());
    }

    /**
     * Stops metrics scheduling.
     */
    static void stopScheduling() {
        MetricsAasConstructor.clear();
        timer.cancel();
        releaseConnector();
    }

}
