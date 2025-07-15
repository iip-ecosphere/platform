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

package test.de.iip_ecosphere.platform.support.aas;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.plugins.FolderClasspathPluginSetupDescriptor;
import de.iip_ecosphere.platform.support.plugins.PluginManager;

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
    private static String aasPluginId = "aas.basyx-2.0"; // shall become the default id then
    
    /**
     * Changes the AAS plugin Id used for testing.
     * 
     * @param id the new plugin Id, ignored if <b>null</b> or empty
     * @return the plugin id before trying to change the value
     */
    public static String setAasPluginId(String id) {
        String old = aasPluginId;
        if (null != id && id.length() > 0) {
            aasPluginId = id;
        }
        return old;
    }

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

        /**
         * Creates a new plugin location.
         * 
         * @param parent the parent folder (in git workspace)
         * @param folder the plugin folder within the parent folder (in git workspace)
         * @param installFolder (in unpacked plugins)
         * @param descriptorOnly shall only be descriptor JARs loaded or the full classpath
         */
        private PluginLocation(String parent, String folder, String installFolder, boolean descriptorOnly) {
            this.parent = parent;
            this.folder = folder;
            this.installFolder = installFolder;
            this.descriptorOnly = descriptorOnly;
        }
    }
    
    static {
        addPluginLocation("support", "support.aas.basyx2", "basyx2", false);
        addPluginLocation("support", "support.aas.basyx", "basyx", false);
    }

    /**
     * Adds a new plugin location.
     * 
     * @param parent the parent folder (in git workspace)
     * @param folder the plugin folder within the parent folder (in git workspace)
     * @param installFolder (in unpacked plugins)
     * @param descriptorOnly shall only be descriptor JARs loaded or the full classpath
     */
    public static void addPluginLocation(String parent, String folder, String installFolder, boolean descriptorOnly) {
        locations.add(new PluginLocation(parent, folder, installFolder, descriptorOnly));
        LoggerFactory.getLogger(TestWithPlugin.class).info("Added plugin location for {} (descriptor only: {})", 
            folder, descriptorOnly);
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
                if (folder.isDirectory()) { // in local git repo
                    LoggerFactory.getLogger(TestWithPlugin.class).info("Loading plugin from {} (development)", folder);
                    PluginManager.registerPlugin(new FolderClasspathPluginSetupDescriptor(folder, loc.descriptorOnly));
                    found = true;
                } else { // local, unpacked
                    folder = new File(installDir);
                    if (folder.isDirectory()) {
                        LoggerFactory.getLogger(TestWithPlugin.class).info("Loading plugin from {} "
                            + "(test deployment)", installDir);
                        PluginManager.registerPlugin(new FolderClasspathPluginSetupDescriptor(
                            new File(installDir + "/" + loc.installFolder), loc.descriptorOnly));
                        found = true;
                    }
                }
                if (!found) {
                    LoggerFactory.getLogger(TestWithPlugin.class).info("No plugins found for {}. "
                        + "Test may fail.", loc.folder);
                }
            }
            // TODO default from AASFactory
            AasFactory.setPluginId(System.getProperty("okto.test.aas.pluginId", aasPluginId)); 
        }
    }
    
    /**
     * Sets up plugins. Non-static so that loading is inherited.
     */
    @Before
    public void setup() {
        loadPlugins();
    }

}
