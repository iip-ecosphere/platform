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

import java.util.Timer;
import java.util.TimerTask;

import de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProvider;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Does OS/JVM level monitoring.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Monitor {
    
    private static MetricsProvider provider = new MetricsProvider(new SimpleMeterRegistry());
    private static Timer timer = new Timer();

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
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                provider.calculateNonNativeSystemMetrics();
            }
            
        }, 0, 2000); // TODO make configurable
    }

    /**
     * Stops metrics scheduling.
     */
    static void stopScheduling() {
        timer.cancel();
    }

}
