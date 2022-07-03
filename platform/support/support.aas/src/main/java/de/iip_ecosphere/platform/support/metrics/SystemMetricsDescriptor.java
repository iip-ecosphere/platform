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

/**
 * The JSL system metrics descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface SystemMetricsDescriptor {

    /**
     * Creates the actual system metrics instance.
     * 
     * @return the actual system metrics instance
     */
    public SystemMetrics createInstance();

    /**
     * Is this system metrics descriptor enabled if multiple are specified. Usually, there shall be only a single 
     * plugin, but for testing or generating just a few container for all devices, a dynamic selection may be helpful.
     * The default plugin is always enabled as fallback. This method may represent a system-specific condition. 
     * 
     * @return {@code true} for enabled, {@code false} else
     */
    public default boolean isEnabled() {
        return false;
    }

    /**
     * In case that there are multiple non-enabled descriptors, shall this descriptor act as fallback. Only generic
     * plugins shall act as fallback, never system-specific ones.
     * 
     * @return {@code true} for fallback, {@code false} else
     */
    public default boolean isFallback() {
        return false;
    }

}
