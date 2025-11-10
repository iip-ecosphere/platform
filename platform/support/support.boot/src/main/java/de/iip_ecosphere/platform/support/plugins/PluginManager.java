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
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.support.IOUtils;
import de.iip_ecosphere.platform.support.OsUtils;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Manages plugins to separate overlapping classpaths and dependencies of alternatives and optionals.
 * Identifies plugins via {@link PluginSetupDescriptor} and loads plugin on demand and via the plugin 
 * id. Additionally, considers comma/semicolon separated paths in env/system property 
 * {@value #FILE_PLUGINS_PROPERTY} to be loaded as unpacked plugins.
 * 
 * The {@link PluginManager} does not load plugins automatically as the first point in time to address
 * the {@link PluginManager} may be too early, see {@link PluginSetup}. Call {@link #loadPlugins()}, 
 * {@link #loadAllFrom(File, PluginSetupDescriptor...)}
 * {@link #loadAllFrom(File, PluginFilter, PluginSetupDescriptor...)} explicitly.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PluginManager {

    /**
     * Postfix of plugin id to indicate a default plugin that is also returned if no plugin id matches.
     */
    public static final String POSTFIX_ID_DEFAULT = "-default";

    /**
     * Prefix identification key for classpath meta-information on the setup descriptor.
     */
    public static final String KEY_SETUP_DESCRIPTOR = "# setupDescriptor: ";

    /**
     * Prefix identification key for sequence number meta-information on the setup descriptor.
     */
    public static final String KEY_SEQUENCE_NR = "# sequenceNr: ";

    /**
     * Prefix identification key for classpath meta-information on the optional plugin ids.
     */
    public static final String KEY_PLUGIN_IDS = "# pluginIds: ";
    
    public static final String FILE_PLUGINS_PROPERTY = "okto.plugins";
    private static Map<String, Plugin<?>> plugins = new HashMap<>();
    private static Map<Class<?>, List<Plugin<?>>> pluginsByType = new HashMap<>();
    private static Map<String, PluginDescriptor<?>> descriptors = new HashMap<>();
    private static Predicate<File> pluginCpFilter = f -> true;

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
            Optional<I> svc = ServiceLoaderUtils.filterExcluded(iCls);
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
            .stream(ServiceLoaderUtils.load(PluginSetupDescriptor.class))
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
     * @see PluginSetup#getClassLoader()
     */
    private static void registerPlugin(PluginSetupDescriptor desc, boolean onlyNew) {
        LoggerFactory.getLogger(PluginManager.class).info("Found plugin setup descriptor {}. Registering plugin...", 
            desc.getClass());
        boolean allowAll = !desc.preventDuplicates();
        ClassLoader loader = PluginSetup.getClassLoader();
        ClassLoader descLoader = desc.createClassLoader(loader);
        desc.getPluginDescriptors(descLoader)
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
     * Registers the given plugin descriptor as a new plugin without installation directory.
     * 
     * @param desc the descriptor to register
     */
    public static void registerPlugin(PluginDescriptor<?> desc) {
        registerPlugin(desc, true, null);
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
    public static void registerPlugin(PluginDescriptor<?> desc, boolean onlyNew, File installDir) {
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

    /**
     * Loads all plugins in {@code folder}, either in individual folders per plugin or merged into on jar folder with 
     * individual classpath files.
     * 
     * @param folder the parent folder of the plugins folder or of the classpaths (jars in parent folder)
     * @param local additional local plugin setup descriptors to be considered before the file-based ones
     * @see #setPluginClasspathFilter(Predicate)
     */
    public static void loadAllFrom(File folder, PluginSetupDescriptor... local) {
        loadAllFrom(folder, null, local);
    }

    /**
     * Loads all plugins in {@code folder}, either in individual folders per plugin or merged into on jar folder with 
     * individual classpath files.
     * 
     * @param folder the parent folder of the plugins folder or of the classpaths (jars in parent folder)
     * @param filter optional filter removing those plugins that shall not be loaded, e.g., as covered by 
     *     {@code local}; may be <b>null</b> for none
     * @param local additional local plugin setup descriptors to be considered before the file-based ones
     * @see #setPluginClasspathFilter(Predicate)
     */
    public static void loadAllFrom(File folder, PluginFilter filter, PluginSetupDescriptor... local) {
        long startTime = System.currentTimeMillis();
        File[] files = folder.listFiles();
        for (File f : files) {
            File cpFile = null;
            if (f.isDirectory()) { // test unpacking with contained jars
                cpFile = new File(f, "classpath"); // by convention
            } else { // resolved, relocated
                if (!f.getName().endsWith("-win") && !f.getName().endsWith("-linux")) {
                    cpFile = f;
                }
            }
            if (null != cpFile && pluginCpFilter.test(cpFile)) {
                if (!cpFile.exists()) {
                    LoggerFactory.getLogger(PluginManager.class).warn("No plugin classpath file {}. Ignoring.", cpFile);
                } else {
                    loadPluginFrom(cpFile, files.length, filter, local);
                }
            }
        }
        LoggerFactory.getLogger(PluginManager.class).info("Plugin loading completed in {} ms. ", 
            System.currentTimeMillis() - startTime);
    }
    
    /**
     * Defines the plugin classpath filter for {@link #loadAllFrom(File, PluginSetupDescriptor...)} and 
     * {@link #loadAllFrom(File, PluginFilter, PluginSetupDescriptor...)}.
     * 
     * @param filter the filter, ignored if <b>null</b>; default filter accepts all files
     * @return the filter before modification
     */
    public static Predicate<File> setPluginClasspathFilter(Predicate<File> filter) {
        Predicate<File> result = pluginCpFilter;
        if (filter != null) {
            pluginCpFilter = filter;
        }
        return result;
    }
    
    /**
     * Extracts the suffix after removing the prefix.
     * 
     * @param prefix the prefix to look for, may be <b>null</b>
     * @param line the line to extract the suffix from
     * @param dflt the default value if there is no prefix, usually {@code line}
     * @return {@code line} or the line without the prefix
     */
    private static String extractSuffix(String prefix, String line, String dflt) {
        String result = dflt;
        if (null != prefix && line.startsWith(prefix)) {
            result = line.substring(prefix.length()).trim();
        }
        return result;
    }
    
    /**
     * Collects information about a plugin.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class PluginInfo {

        private int sequenceNr;
        private File file;
        private Supplier<PluginSetupDescriptor> supplier = null;
        private List<String> pluginIds;
        
        /**
         * Creates an instance for local setup descriptors.
         * 
         * @param supplier the supplier to create the descriptor
         */
        private PluginInfo(Supplier<PluginSetupDescriptor> supplier) {
            this(null, supplier, Integer.MIN_VALUE, null);
        }
        
        /**
         * Creates an instance.
         * 
         * @param file the file path to the plugin
         * @param supplier the setup descriptor supplier
         * @param sequenceNr the indicative plugin loading sequence number
         * @param pluginIds the dependent plugin ids to be considered, may be <b>null</b>
         */
        private PluginInfo(File file, Supplier<PluginSetupDescriptor> supplier, int sequenceNr, 
            List<String> pluginIds) {
            this.file = file;
            this.supplier = supplier;
            this.sequenceNr = sequenceNr;
            this.pluginIds = pluginIds;
        }

        /**
         * Returns the name of the plugin.
         * 
         * @return the name
         */
        public String getName() {
            return null == file ? "" : file.getName();
        }
        
        /**
         * Returns the initial loading sequence number.
         * 
         * @return the initial sequence number
         */
        public int getSequenceNr() {
            return sequenceNr;
        }
        
        /**
         * Returns whether this plugin has dependent plugin ids.
         * 
         * @return {@code true} for dependent plugin ids, {@code false} else
         */
        public boolean hasPluginIds() {
            return pluginIds != null && pluginIds.size() > 0;
        }
        
    }

    /**
     * Allows to filter out plugins that shall not be loaded. Local plugins will not be subject to filtering.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface PluginFilter {
        
        /**
         * Returns whether {@code info} shall be loaded/registered.
         * 
         * @param info the info to be considered
         * @return {@code true} for load/register, {@code false} else
         */
        public boolean accept(PluginInfo info);
        
    }

    /**
     * Creates a {@link PluginInfo} and adds it to the set of plugins to load.
     * 
     * @param toLoad the list of plugins to load
     * @param cpFile the classpath file defining the plugin
     * @param sequenceNr the indicative sequence number to load the plugin
     * @param supplier the supplier to create the setup descriptor
     */
    private static void addToLoad(List<PluginInfo> toLoad, File cpFile, int sequenceNr, 
        Supplier<PluginSetupDescriptor> supplier) {
        addToLoad(toLoad, cpFile, sequenceNr, supplier, null);
    }

    /**
     * Creates a {@link PluginInfo} and adds it to the set of plugins to load.
     * 
     * @param toLoad the list of plugins to load
     * @param cpFile the classpath file defining the plugin
     * @param sequenceNr the indicative sequence number to load the plugin
     * @param supplier the supplier to create the setup descriptor
     * @param pluginIds the dependent plugin ids to be considered, may be <b>null</b>
     */
    private static void addToLoad(List<PluginInfo> toLoad, File cpFile, int sequenceNr, 
        Supplier<PluginSetupDescriptor> supplier, List<String> pluginIds) {
        if (null != supplier) {
            toLoad.add(new PluginInfo(cpFile, supplier, sequenceNr, pluginIds));
        }
    }
    
    /**
     * Parses the plugin sequence number.
     * 
     * @param cpFile the plugin being processed (for logging)
     * @param text the text representing the sequence number, may be <b>null</b>
     * @param dflt the default sequence number to use in case of parsing problems
     * @return the sequence number
     */
    private static int parseSequenceNr(File cpFile, String text, int dflt) {
        int result = dflt;
        if (null != text) {
            try {
                result = Integer.parseInt(text);
            } catch (NumberFormatException e) {
                LoggerFactory.getLogger(PluginManager.class).warn("Sequence number of plugin {} is not a "
                    + "number ({}). Using {}.", cpFile, e.getMessage(), dflt);
            }
        }
        return result;
    }

    /**
     * Loads the plugin specified by its classpath file and its jar file folder based on metadata in the classpath file.
     * 
     * @param cpFile the classpath file
     * @param filter optional filter removing those plugins that shall not be loaded, e.g., as covered by 
     *     {@code local}; may be <b>null</b> for none
     * @param local additional local plugin setup descriptors to be considered before the file-based ones
     */
    private static void loadPluginFrom(File cpFile, int maxFiles, PluginFilter filter, PluginSetupDescriptor... local) {
        List<PluginInfo> toLoad = new ArrayList<>();
        int seqNr = maxFiles + 1; // give explicitly specified ones precedence
        try (FileInputStream fis = new FileInputStream(cpFile)) {
            List<String> lines = IOUtils.readLines(fis, Charset.defaultCharset());
            String setupDescriptor = null;
            String pluginIds = null;
            String sequenceNr = null;
            for (String line: lines) {
                setupDescriptor = extractSuffix(KEY_SETUP_DESCRIPTOR, line, setupDescriptor);
                pluginIds = extractSuffix(KEY_PLUGIN_IDS, line, pluginIds);
                sequenceNr = extractSuffix(KEY_SEQUENCE_NR, line, sequenceNr);
            }
            setupDescriptor = setupDescriptor == null ? "FolderClasspath" : setupDescriptor;
            setupDescriptor = setupDescriptor.toLowerCase();
            pluginIds = pluginIds == null ? "" : pluginIds;
            int pSeqNr = parseSequenceNr(cpFile, sequenceNr, seqNr);

            switch (setupDescriptor) {
            case "folderclasspath":
                addToLoad(toLoad, cpFile, pSeqNr, () -> new FolderClasspathPluginSetupDescriptor(cpFile));
                break;
            case "currentcontext":
                addToLoad(toLoad, cpFile, pSeqNr, () -> CurrentContextPluginSetupDescriptor.INSTANCE);
                break;
            case "currentclassloader":
                addToLoad(toLoad, cpFile, pSeqNr, () -> CurrentClassloaderPluginSetupDescriptor.INSTANCE);
                break;
            case "pluginbased":
                StringTokenizer plIds = new StringTokenizer(pluginIds, ",");
                List<String> ids = new ArrayList<>();
                while (plIds.hasMoreTokens()) {
                    ids.add(plIds.nextToken().trim());
                }
                String plId = ids.size() > 0 ? ids.get(0) : "";
                addToLoad(toLoad, cpFile, pSeqNr, () -> new PluginBasedSetupDescriptor(plId), ids);
                break;
            case "process":
                addToLoad(toLoad, cpFile, pSeqNr, () -> new FolderClasspathPluginSetupDescriptor(cpFile, true));
                break;
            case "":
            case "none":
                // do not load!
                break;
            default:
                try {
                    Class<?> cls = Class.forName(setupDescriptor);
                    Object inst = cls.getDeclaredConstructor().newInstance();
                    if (inst instanceof PluginSetupDescriptor) {
                        addToLoad(toLoad, cpFile, pSeqNr, () -> (PluginSetupDescriptor) inst);
                    } else {
                        LoggerFactory.getLogger(PluginManager.class).warn("Plugin setup descriptor for "
                            + "plugin {} is not instanceof PluginSetupDescriptor. Ignoring. Reason: {}", cpFile);
                    }
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException 
                    | NoSuchMethodException | InvocationTargetException e) {
                    LoggerFactory.getLogger(PluginManager.class).warn("Cannot determine plugin setup descriptor for "
                        + "plugin classpath file {}. Ignoring. Reason: {}", cpFile, e.getMessage());
                }
                break;
            }
            seqNr++;
        } catch (IOException e) {
            LoggerFactory.getLogger(PluginManager.class).warn("Cannot load plugin classpath file {}. "
                + "Ignoring. Reason: {}", cpFile, e.getMessage());
        }
        // filter out those that shall not be loaded
        if (null != filter) {
            toLoad.removeIf(i -> !filter.accept(i));
        }
        sortPlugins(toLoad);
        // add those that shall be loaded anyway
        for (PluginSetupDescriptor l: local) {
            toLoad.add(0, new PluginInfo(() -> l));
        }
        // and register/load all remaining
        for (PluginInfo info : toLoad) {
            registerPlugin(info.supplier.get());
        }
    }
    
    /**
     * Sorts the plugins, basically according to their initial sequence number and then, if given, after the last 
     * specified dependent plugin (or if not matching at the end).
     * 
     * @param infos the plugin informations to sort
     */
    private static List<PluginInfo> sortPlugins(List<PluginInfo> infos) {
        Collections.sort(infos, (i1, i2) -> Integer.compare(i1.getSequenceNr(), i2.getSequenceNr()));
        List<PluginInfo> result = new ArrayList<>();
        List<PluginInfo> secondary = new ArrayList<>();
        for (PluginInfo info : infos) {
            if (info.hasPluginIds()) {
                secondary.add(info);
            } else {
                result.add(info);
            }
        }
        int beforeSize = -1; // permit one round
        while (!secondary.isEmpty() && beforeSize != secondary.size()) {
            int tmpBeforeSize = secondary.size();
            for (int s = secondary.size() - 1; s >= 0; s--) {
                PluginInfo info = secondary.get(s);
                List<String> infoPluginIds = info.pluginIds;
                int foundCount = 1;
                int insertPos = -1;
                for (int r = 0; r < result.size() && foundCount < infoPluginIds.size(); r++) {
                    if (infoPluginIds.contains(result.get(r).getName())) {
                        foundCount++;
                        insertPos = r;
                    }
                }
                if (foundCount == infoPluginIds.size()) {
                    result.add(insertPos, secondary.remove(s));
                }
            }
            beforeSize = tmpBeforeSize;
        }
        result.addAll(secondary);
        return result;
    }

}
