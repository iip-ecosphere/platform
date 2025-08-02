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

package test.de.iip_ecosphere.platform.support;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;

import de.iip_ecosphere.platform.support.plugins.FolderClasspathPluginSetupDescriptor;
import de.iip_ecosphere.platform.support.plugins.PluginManager;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Plugin-based test, aiming at inheriting plugin loading.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestWithPlugin {

    public static final String PROP_AAS_PLUGIN = "okto.test.aas.pluginId";
    
    private static boolean loaded = false;
    private static List<PluginLocation> locations = new ArrayList<>();
    private static String installDir = "target/oktoPlugins";
    private static List<Runnable> runAfterLoading = new ArrayList<>();
    private static boolean enableLocalPlugins = true;

    /**
     * Represents a plugin location.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class PluginLocation {
        
        private String parent;
        private String folder;
        private String installFolder;
        private boolean descriptorOnly;
        private String[] appends; 

        /**
         * Creates a new plugin location.
         * 
         * @param parent the parent folder (in git workspace)
         * @param folder the plugin folder within the parent folder (in git workspace)
         * @param installFolder (in unpacked plugins)
         * @param descriptorOnly shall only be descriptor JARs loaded or the full classpath
         * @param appends optional plugins to be appended
         */
        private PluginLocation(String parent, String folder, String installFolder, boolean descriptorOnly, 
            String... appends) {
            this.parent = parent;
            this.folder = folder;
            this.installFolder = installFolder;
            this.descriptorOnly = descriptorOnly;
            this.appends = appends;
        }
    }

    /**
     * Adds a new plugin location.
     * 
     * @param parent the parent folder (in git workspace)
     * @param folder the plugin folder within the parent folder (in git workspace)
     * @param installFolder (in unpacked plugins)
     * @param descriptorOnly shall only be descriptor JARs loaded or the full classpath
     * @param appends optional plugins to be appended
     */
    public static void addPluginLocation(String parent, String folder, String installFolder, boolean descriptorOnly, 
        String... appends) {
        locations.add(new PluginLocation(parent, folder, installFolder, descriptorOnly, appends));
        LoggerFactory.getLogger(TestWithPlugin.class).info("Added plugin location for {} (descriptor only: {})", 
            folder, descriptorOnly);
    }

    /**
     * Collects the appended plugins.
     * 
     * @param base the base folder for relocation
     * @param appends the appended plugins
     * @return the appended plugins, may be <b>null</b>
     */
    private static File[] collectAppends(File base, String[] appends) {
        File[] result;
        if (null == appends || appends.length == 0) {
            result = null;
        } else {
            result = new File[appends.length];
            for (int a = 0; a < appends.length; a++) {
                result[a] = new File(base, appends[a]);
            }
        }
        return result;
    }
    
    /**
     * Whether local plugins shall be enabled.
     * 
     * @param enable enable or disable (default is enabled)
     */
    public static void enableLocalPlugins(boolean enable) {
        enableLocalPlugins = enable;
    }
    
    /**
     * Sets the folder where the plugins are installed.
     * 
     * @param dir the folder
     */
    public static void setInstallDir(String dir) {
        installDir = dir;
    }
    
    /**
     * Loads plugins statically.
     */
    public static void loadPlugins() {
        if (!loaded) {
            loaded = true;
            boolean found = false;
            for (PluginLocation loc : locations) {
                File folder = new File("..", loc.folder); // for platform parts in "support"
                if (!folder.isDirectory()) { // just in case
                    folder = new File("../" + loc.parent, loc.folder); 
                }
                if (!folder.isDirectory()) { // usual nesting of platform part in different folder
                    folder = new File("../../" + loc.parent, loc.folder); 
                }
                if (enableLocalPlugins && folder.isDirectory()) { // in local git repo
                    LoggerFactory.getLogger(TestWithPlugin.class).info("Loading plugin from {} (development)", folder);
                    File[] appends = collectAppends(folder.getParentFile(), loc.appends);
                    PluginManager.registerPlugin(new FolderClasspathPluginSetupDescriptor(folder, loc.descriptorOnly, 
                        appends));
                    found = true;
                } else { // local, unpacked
                    folder = new File(installDir);
                    if (folder.isDirectory()) {
                        File[] appends = collectAppends(folder.getParentFile(), loc.appends);
                        LoggerFactory.getLogger(TestWithPlugin.class).info("Loading plugin from {} "
                            + "(test deployment)", installDir);
                        File cpFile = new File(installDir + "/" + loc.folder);
                        if (!cpFile.exists()) { // initial style, transition
                            cpFile = new File(installDir + "/" + loc.installFolder);
                        }
                        PluginManager.registerPlugin(new FolderClasspathPluginSetupDescriptor(
                            cpFile, loc.descriptorOnly, appends));
                        found = true;
                    }
                }
                if (!found) {
                    LoggerFactory.getLogger(TestWithPlugin.class).info("No plugins found for {}. "
                        + "Test may fail.", loc.folder);
                }
            }
            for (Runnable r : runAfterLoading) {
                r.run();
            }
        }
    }
    
    /**
     * Adds functions to be executed after loading.
     * 
     * @param runnable the runnable to add
     */
    public static void addRunAfterLoading(Runnable runnable) {
        runAfterLoading.add(runnable);
    }
    
    /**
     * Removes all plugin locations.
     */
    public static void clear() {
        locations.clear();
    }
    
    /**
     * Sets up plugins. Non-static so that loading is inherited.
     */
    @Before
    public void setup() {
        loadPlugins();
    }

}
