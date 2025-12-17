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

package de.iip_ecosphere.platform.services.environment;

import java.io.File;

import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorPluginDescriptor;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.plugins.PluginInstanceDescriptor;
import de.iip_ecosphere.platform.support.plugins.PluginManager;
import de.iip_ecosphere.platform.support.plugins.PluginManager.PluginFilter;
import de.iip_ecosphere.platform.support.plugins.PluginManager.PluginInfo;
import de.iip_ecosphere.platform.support.setup.AbstractSetup;
import de.iip_ecosphere.platform.support.plugins.PluginManager.ConjunctivePluginFilter;

/**
 * Declares the type of a service plugin descriptor.
 * 
 * @param <S> the actual type of service being created
 * @author Holger Eichelberger, SSE
 */
public interface ServicePluginDescriptor<S extends Service> extends PluginInstanceDescriptor<ServiceDescriptor<S>> {

    public static final String PLUGIN_ID_PREFIX = "service-";
    public static final String PLUGIN_TEST_ID_PREFIX = PLUGIN_ID_PREFIX + "test-";
    
    /**
     * Returns a plugin filter for platform components that filters out all service plugins 
     * ({@link PLUGIN_ID_PREFIX}).
     * 
     * @return the plugin filter
     */
    public static PluginFilter getServicePluginFilter() {
        return new PluginFilter() {
            
            @Override
            public boolean accept(PluginInfo info) {
                return !info.getName().startsWith(PLUGIN_ID_PREFIX); 
            }
        };
    }

    /**
     * Returns a plugin filter for platform components that filters out all connector 
     * ({@link ConnectorDescriptor#PLUGIN_ID_PREFIX}) and service plugins ({@link PLUGIN_ID_PREFIX}).
     * 
     * @return the plugin filter
     */
    public static PluginFilter getConnectorAndServicePluginFilter() {
        return new ConjunctivePluginFilter(getServicePluginFilter(), 
            ConnectorPluginDescriptor.getConnectorPluginFilter());
    }

    /**
     * Helper function to load all plugins from the plugins directory specified in env/sys property 
     * {@link AbstractSetup#PARAM_PLUGINS} applying {@link #getConnectorAndServicePluginFilter()}.
     */
    public static void loadPlatformPlugins() {
        loadPlatformPlugins(System.getProperty(AbstractSetup.PARAM_PLUGINS));
    }

    /**
     * Helper function to load all plugins from {@code pluginsFolder} applying 
     * {@link #getConnectorAndServicePluginFilter()}.
     * 
     * @param pluginsFolder the plugins folder, may be <b>null</b> or empty for none
     */
    public static void loadPlatformPlugins(String pluginsFolder) {
        if (null != pluginsFolder && pluginsFolder.length() > 0) {
            File f = new File(pluginsFolder);
            if (f.isDirectory()) {
                LoggerFactory.getLogger(ServicePluginDescriptor.class).info("Loading plugins from {}", f);
                PluginManager.loadAllFrom(f, getConnectorAndServicePluginFilter());
            }
        }
    }
    
}
