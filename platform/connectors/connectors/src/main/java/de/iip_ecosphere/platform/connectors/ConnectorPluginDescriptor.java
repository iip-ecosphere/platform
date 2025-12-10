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

package de.iip_ecosphere.platform.connectors;

import de.iip_ecosphere.platform.support.plugins.PluginInstanceDescriptor;
import de.iip_ecosphere.platform.support.plugins.PluginManager.PluginFilter;
import de.iip_ecosphere.platform.support.plugins.PluginManager.PluginInfo;

/**
 * Declares the type of a connector plugin descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ConnectorPluginDescriptor extends PluginInstanceDescriptor<ConnectorDescriptor> {

    public static String PLUGIN_ID_PREFIX = ConnectorDescriptor.PLUGIN_ID_PREFIX;
    public static String PLUGIN_TEST_ID_PREFIX = ConnectorDescriptor.PLUGIN_TEST_ID_PREFIX;
    
    /**
     * Returns a plugin filter for platform components that filters out all connector plugins 
     * ({@link PLUGIN_ID_PREFIX}).
     * 
     * @return the plugin filter
     */
    public static PluginFilter getConnectorPluginFilter() {
        return new PluginFilter() {
            
            @Override
            public boolean accept(PluginInfo info) {
                return !info.getName().startsWith(ConnectorDescriptor.PLUGIN_ID_PREFIX); 
            }
        };
    }
    
}
