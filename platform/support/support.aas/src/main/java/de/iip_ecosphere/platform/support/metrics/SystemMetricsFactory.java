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

import java.util.Optional;

import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;

/**
 * Provides the system metrics instance to use.
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
            Optional<SystemMetricsDescriptor> oDesc = ServiceLoaderUtils.findFirst(SystemMetricsDescriptor.class);
            if (oDesc.isPresent()) {
                desc = oDesc.get();
            } else {
                desc = new DefaultSystemMetricsDescriptor();
            }
            instance = desc.createInstance();
        } 
        return instance;
    }

}
