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

package de.iip_ecosphere.platform.support.metrics;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Provides the system metrics instance to use. This class installs a shutdown hook on {@link SystemMetrics#close()}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SystemMetricsFactory {
    
    private static SystemMetrics instance;
    
    /**
     * Returns the system metrics instance.
     * 
     * @return the system metrics instance
     */
    public static SystemMetrics getSystemMetrics() {
        if (null == instance) {
            SystemMetricsDescriptor desc;
            Iterator<SystemMetricsDescriptor> iterator = ServiceLoader.load(SystemMetricsDescriptor.class).iterator();
            SystemMetricsDescriptor first = null;
            SystemMetricsDescriptor firstEnabled = null;
            SystemMetricsDescriptor firstFallback = null;
            int count = 0;
            while (iterator.hasNext()) {
                SystemMetricsDescriptor d = iterator.next();
                if (null == first) {
                    first = d;
                }
                if (null == firstEnabled && d.isEnabled()) {
                    firstEnabled = d;
                }
                if (null == firstFallback && !d.isEnabled()) {
                    firstFallback = d;
                }
                count++;
            }
            if (count > 1 && null != firstEnabled) {
                desc = firstEnabled;
            } else if (count > 1 && null != firstFallback) {
                desc = firstFallback;
            } else if (null != first) {
                desc = first;
            } else {
                desc = new DefaultSystemMetricsDescriptor();
            }
            instance = desc.createInstance();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> instance.close()));
        } 
        return instance;
    }

}
