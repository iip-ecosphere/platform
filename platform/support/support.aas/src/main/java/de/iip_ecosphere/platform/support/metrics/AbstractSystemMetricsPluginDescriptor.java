/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.metrics;

import de.iip_ecosphere.platform.support.plugins.SingletonPluginDescriptor;

/**
 * Basic system metrics plugin descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractSystemMetricsPluginDescriptor extends SingletonPluginDescriptor<SystemMetrics> 
    implements SystemMetricsDescriptor {

    /**
     * Creates an instance.
     */
    public AbstractSystemMetricsPluginDescriptor() {
        super(PLUGIN_ID, null, SystemMetrics.class, null);
    }

    @Override
    protected PluginSupplier<SystemMetrics> initPluginSupplier(PluginSupplier<SystemMetrics> pluginSupplier) { 
        return p -> createInstance();
    }

}
