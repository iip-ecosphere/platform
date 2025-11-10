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

package de.iip_ecosphere.platform.support.plugins;

import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Loads a plugin based on the classloader of an already loaded plugin. Requires some form of ordered plugin loading!
 * 
 * @author Holger Eichelberger, SSE
 */
public class PluginBasedSetupDescriptor implements PluginSetupDescriptor {
    
    private Class<?> pluginCls;
    private String pluginId;

    /**
     * Creates a setup descriptor for a given plugin id.
     * 
     * @param pluginId the plugin id
     */
    public PluginBasedSetupDescriptor(String pluginId) {
        this.pluginId = pluginId;
    }

    /**
     * Creates a setup descriptor for a given plugin class (factory, descriptor, etc).
     * 
     * @param pluginCls the class determining the plugin
     */
    public PluginBasedSetupDescriptor(Class<?> pluginCls) {
        this.pluginCls = pluginCls;
    }

    @Override
    public ClassLoader createClassLoader(ClassLoader parent) {
        ClassLoader result = parent;
        Plugin<?> plugin = null;
        if (null != pluginCls) {
            plugin = PluginManager.getPlugin(pluginCls);
        }
        if (null == plugin && null != pluginId) {
            plugin = PluginManager.getPlugin(pluginId);
        }
        if (null != plugin) {
            result = plugin.getClass().getClassLoader(); // may need a step up in case of childclassloader
        } else {
            LoggerFactory.getLogger(this).warn("No plugin classloader for {} identified, using parent classloader");
        }
        return result;
    }
    
}
