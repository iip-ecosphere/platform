/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.plugins;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.StringTokenizer;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.OsUtils;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;

/**
 * Manages plugins to separate overlapping classpaths and dependencies of alternatives and optionals.
 * Identifies plugins via {@link PluginSetupDescriptor} and loads plugin on demand and via the plugin 
 * id. Additionally, considers comma/semicolon separated paths in env/system property 
 * {@value #FILE_PLUGINS_PROPERTY} to be loaded as unpacked plugins.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PluginManager {

    public static final String FILE_PLUGINS_PROPERTY = "okto.plugins";
    private static Map<String, Plugin<?>> plugins = new HashMap<>();
    
    /**
     * Returns a specific plugin.
     * 
     * @param id the unique id of the plugin
     * 
     * @return the plugin, may be <b>null</b> for none
     */
    public static Plugin<?> getPlugin(String id) {
        return plugins.get(id);
    }

    /**
     * Returns a specific plugin.
     * 
     * @param <T> the type of plugin to return
     * @param id the unique id of the plugin
     * @param cls the class the plugin shall be of
     * 
     * @return the plugin, may be <b>null</b> for none
     */
    @SuppressWarnings("unchecked")
    public static <T> Plugin<T> getPlugin(String id, Class<T> cls) {
        Plugin<T> result = null;
        Plugin<?> tmp = getPlugin(id);
        if (null != tmp && cls.isAssignableFrom(tmp.getInstanceClass())) {
            result = (Plugin<T>) tmp;
        } else if (tmp != null) {
            LoggerFactory.getLogger(PluginManager.class).warn(
                "Plugin for id '{}' found, but not compatible with {}", id, cls);
        }
        return result;
    }

    /**
     * Returns all known plugins.
     * 
     * @return all known plugins
     */
    public static Iterable<Plugin<?>> plugins() {
        return plugins.values();
    }
    
    static {
        loadPlugins(false);
    }
    
    /**
     * Loads new plugins from all known plugin setup descriptors.
     */
    public static void loadPlugins() {
        loadPlugins(true);
    }
    
    /**
     * Calls {@link Plugin#cleanup()} on all known plugins.
     */
    public static void cleanup() {
        for (Plugin<?> d : plugins.values()) {
            d.cleanup();
        }
    }

    /**
     * Loads plugins from all known plugin setup descriptors.
     * 
     * @param onlyNew if {@code true} considers only unknown/new plugins, if {@code false} consides all plugins and 
     *   issues warnings
     */
    private static void loadPlugins(boolean onlyNew) {
        ServiceLoaderUtils
            .stream(ServiceLoader.load(PluginSetupDescriptor.class))
            .forEach(d -> registerPlugin(d, onlyNew));

        String plugins = OsUtils.getPropertyOrEnv(FILE_PLUGINS_PROPERTY, "");
        StringTokenizer tokens = new StringTokenizer(plugins, ":;");
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            File file = new File(token);
            if (file.exists() && file.isDirectory()) {
                registerPlugin(new FolderClasspathPluginSetupDescriptor(file), onlyNew);
            } else {
                LoggerFactory.getLogger(PluginManager.class).warn("While reading unpacked plugins from -D{}, "
                    + "{} does not exist/is no directory.", FILE_PLUGINS_PROPERTY, file);
            }
        }
    }
    
    /**
     * Explicitly registers the given plugin (setup) descriptor. Obtains the class loader
     * of the descriptor and loads the known {@link PluginDescriptor plugin descriptors}.
     * 
     * @param desc the plugin setup descriptor
     * @see #registerPlugin(PluginSetupDescriptor, boolean)
     * @see #registerPlugin(PluginDescriptor, boolean)
     */
    public static void registerPlugin(PluginSetupDescriptor desc) {
        registerPlugin(desc, false);
    }

    /**
     * Registers the given plugin (setup) descriptor. Obtains the class loader
     * of the descriptor and loads the known {@link PluginDescriptor plugin descriptors}.
     * 
     * @param desc the plugin setup descriptor
     * @param onlyNew if {@code true} considers only unknown/new plugins, if {@code false} consides all plugins and 
     *   issues warnings
     * @see #registerPlugin(PluginDescriptor, boolean)
     */
    private static void registerPlugin(PluginSetupDescriptor desc, boolean onlyNew) {
        LoggerFactory.getLogger(PluginManager.class).info("Found plugin setup descriptor {}. Trying registration", 
            desc.getClass());
        ClassLoader loader = PluginManager.class.getClassLoader();
        ServiceLoaderUtils
            .stream(ServiceLoader.load(PluginDescriptor.class, desc.createClassLoader(loader)))
            .forEach(d -> registerPlugin(d, onlyNew));
    }

    /**
     * Registers the given plugin descriptor. May warn if a {@link PluginDescriptor#getId() plugin id}
     * is already registered and ignores then {@code desc}.
     * 
     * @param desc the descriptor to register
     * @param onlyNew if {@code true} considers only unknown/new plugins, if {@code false} consides all plugins and 
     *   issues warnings
     */
    private static void registerPlugin(PluginDescriptor desc, boolean onlyNew) {
        String id = desc.getId();
        Plugin<?> known = plugins.get(id);
        if (null != known) {
            if (!onlyNew) {
                LoggerFactory.getLogger(PluginManager.class).warn(
                    "Plugin id '{}' is already registered for {}. Ignoring descriptor {}.", 
                    id, known.getClass(), desc.getClass());
            }
        } else {
            plugins.put(id, desc.createPlugin());
            LoggerFactory.getLogger(PluginManager.class).info("Plugin {} registered", id);
        }
    }

}
