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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.StringTokenizer;

import de.iip_ecosphere.platform.support.OsUtils;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Manages plugins to separate overlapping classpaths and dependencies of alternatives and optionals.
 * Identifies plugins via {@link PluginSetupDescriptor} and loads plugin on demand and via the plugin 
 * id. Additionally, considers comma/semicolon separated paths in env/system property 
 * {@value #FILE_PLUGINS_PROPERTY} to be loaded as unpacked plugins.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PluginManager {

    /**
     * Postfix of plugin id to indicate a default plugin that is also returned if no plugin id matches.
     */
    public static final String POSTFIX_ID_DEFAULT = "-default";
    public static final String FILE_PLUGINS_PROPERTY = "okto.plugins";
    private static Map<String, Plugin<?>> plugins = new HashMap<>();
    private static Map<Class<?>, List<Plugin<?>>> pluginsByType = new HashMap<>();
    private static Map<String, PluginDescriptor<?>> descriptors = new HashMap<>();
    
    /**
     * Returns a specific plugin.
     * 
     * @param id the unique id of the plugin
     * 
     * @return the plugin, may be <b>null</b> for none
     */
    public static Plugin<?> getPlugin(String id) {
        return id == null ? null : plugins.get(id);
    }

    /**
     * Returns a specific plugin.
     * 
     * @param <T> the type of plugin to return
     * @param id the id of the plugin
     * @param cls the class the plugin shall be of
     * 
     * @return the plugin (primary or for secondary ids the first registered one), may be <b>null</b> for none
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
     * Returns a plugin for a specific type, possibly the default plugin.
     * 
     * @param <T> the type of plugin to return
     * @param cls the class the plugin shall be of
     * @return the plugin, preferrably the default plugin, may be <b>null</b> for none
     */
    public static <T> Plugin<T> getPlugin(Class<T> cls) {
        return getPlugin(cls, null);
    }
    
    /**
     * Returns an instance of a plugin, either via this plugin manager or via an optional JSL descriptor.
     * 
     * @param <T> the type of the plugin
     * @param <I> the type of the JSL instance descriptor
     * @param pluginCls the plugin class
     * @param iCls the instance descriptor class, may be <b>null</b>
     * @return the plugin instance, <b>null</b> if none was found/created
     */
    public static <T, I extends PluginInstanceDescriptor<T>> T getPluginInstance(Class<T> pluginCls, Class<I> iCls) {
        T result = null;
        Plugin<T> plugin = PluginManager.getPlugin(pluginCls);
        if (null != plugin) {
            result = plugin.getInstance();
        } else if (iCls != null) {
            Optional<I> svc = ServiceLoaderUtils.findFirst(iCls);
            if (svc.isPresent()) {
                result = svc.get().create();
            }
        }
        return result;
    }

    /**
     * Returns a plugin for a specific type.
     * 
     * @param <T> the type of plugin to return
     * @param cls the class the plugin shall be of
     * @param id the optional id of the plugin to return, may be <b>null</b> or empty leading to the 
     *     default/first registered plugin 
     * @return the plugin, preferrably the default plugin if {@code id} does not match, may be <b>null</b> for none
     */
    @SuppressWarnings("unchecked")
    public static <T> Plugin<T> getPlugin(Class<T> cls, String id) {
        Plugin<T> result = null;
        if (null != cls) {
            List<Plugin<?>> pls = pluginsByType.get(cls);
            if (null != pls && pls.size() > 0) {
                Plugin<?> tmp = null;
                for (Plugin<?> p : pls) {
                    if (p.getAllIds().contains(id)) {
                        tmp = p;
                        break;
                    }
                }
                if (tmp == null) {
                    tmp = pls.get(0);
                }
                if (null != tmp && cls.isAssignableFrom(tmp.getInstanceClass())) {
                    result = (Plugin<T>) tmp;
                } else if (tmp != null) {
                    LoggerFactory.getLogger(PluginManager.class).warn(
                        "Plugin for type '{}' found, but not compatible with {}", cls.getName(), cls);
                }
            }
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
     * @see #registerPlugin(PluginDescriptor, boolean, File)
     */
    public static void registerPlugin(PluginSetupDescriptor desc) {
        registerPlugin(desc, false);
    }

    /**
     * Registers the given plugin (setup) descriptor. Obtains the class loader
     * of the descriptor and loads the known {@link PluginDescriptor plugin descriptors}. Mandates that known
     * {@link PluginDescriptor plugin descriptors} are loaded by the class loader(s) of {@code desc}.
     * 
     * @param desc the plugin setup descriptor
     * @param onlyNew if {@code true} considers only unknown/new plugins, if {@code false} consides all plugins and 
     *   issues warnings
     * @see #registerPlugin(PluginDescriptor, boolean, File)
     */
    private static void registerPlugin(PluginSetupDescriptor desc, boolean onlyNew) {
        LoggerFactory.getLogger(PluginManager.class).info("Found plugin setup descriptor {}. Trying registration", 
            desc.getClass());
        boolean allowAll = !desc.preventDuplicates();
        ClassLoader loader = PluginManager.class.getClassLoader();
        ClassLoader descLoader = desc.createClassLoader(loader);
        ServiceLoaderUtils
            .stream(ServiceLoader.load(PluginDescriptor.class, descLoader))
            .filter(d -> allowAll || loadedBy(d, descLoader))
            .forEach(d -> registerPlugin(d, onlyNew, desc.getInstallDir()));
    }
    
    /**
     * Returns whether {@code obj} was loaded by {@code loader}.
     * 
     * @param obj the object to inspect
     * @param loader the class loader to compare
     * @return {@code true} if {@code obj} was loaded by {@code loader}, {@code false} else
     */
    private static boolean loadedBy(Object obj, ClassLoader loader) {
        boolean found = false;
        ClassLoader iter = obj.getClass().getClassLoader();
        IdentifyingClassloader cfLoader = loader instanceof IdentifyingClassloader 
            ? (IdentifyingClassloader) loader : null;
        while (iter != null && !found) {
            if (null != cfLoader) {
                found = cfLoader.amI(iter);
            } else {
                found = iter == loader;
            }
            iter = iter.getParent();
        }
        return found;
    }

    /**
     * Registers the given plugin descriptor. May warn if a {@link PluginDescriptor#getId() plugin id}
     * is already registered and ignores then {@code desc}.
     * 
     * @param desc the descriptor to register
     * @param onlyNew if {@code true} considers only unknown/new plugins, if {@code false} considers all plugins and 
     *   issues warnings
     * @param installDir the installation directory, may be <b>null</b>
     */
    private static void registerPlugin(PluginDescriptor<?> desc, boolean onlyNew, File installDir) {
        Plugin<?> plugin = desc.createPlugin(installDir);
        Class<?> pluginClass = plugin.getInstanceClass();
        List<String> ids = plugin.getAllIds();
        boolean dflt = ids.stream().anyMatch(i -> i.endsWith(POSTFIX_ID_DEFAULT));
        boolean isKnown = false;
        for (String id : ids) {
            Plugin<?> known = plugins.get(id);
            if (null != known) {
                if (descriptors.get(id) != desc) {
                    LoggerFactory.getLogger(PluginManager.class).warn(
                        "Plugin id '{}' is already registered for {}. Ignoring descriptor {}.", 
                        id, known.getClass(), desc.getClass());
                }
                isKnown = true;
            }
        }
        if ((onlyNew && !isKnown) || !onlyNew) {
            for (String id : ids) {
                Plugin<?> known = plugins.get(id);
                if (null == known) {
                    plugins.put(id, plugin);
                    descriptors.put(id, desc);
                    LoggerFactory.getLogger(PluginManager.class).info("Plugin {} registered", id);
                }
            }
            List<Plugin<?>> pls = pluginsByType.get(pluginClass);
            if (null == pls) {
                pls = new ArrayList<>();
                pluginsByType.put(pluginClass, pls);
            }
            if (dflt) {
                pls.add(0, plugin);
            } else {
                pls.add(plugin);
            }
        }
    }

    /**
     * Returns the class loader of the specified plugin.
     * 
     * @param id the plugin id
     * @return the class loader or the current cpntext class loader if unknown
     */
    public static ClassLoader getPluginLoader(String id) {
        return getPluginLoader(id, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Returns the class loader of the specified plugin.
     * 
     * @param id the plugin id
     * @return the class loader or {@code dflt} if unknown
     */
    public static ClassLoader getPluginLoader(String id, ClassLoader dflt) {
        ClassLoader result = dflt;
        if (null != id) {
            PluginDescriptor<?> desc = descriptors.get(id);
            if (null != desc) {
                result = desc.getClass().getClassLoader();
            }
        }
        return result;
    }

}
