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

    /**
     * Loads plugins statically.
     */
    public static void loadPlugins() {
        if (!loaded) {
            loaded = true;
            boolean found = false;
            File folder = new File("../support.aas.basyx2");
            if (!folder.isDirectory()) {
                folder = new File("..", folder.toString());
            }
            if (folder.isDirectory()) { // in local git repo
                LoggerFactory.getLogger(TestWithPlugin.class).info("Loading plugins from {} (development)", folder);
                PluginManager.registerPlugin(new FolderClasspathPluginSetupDescriptor(folder));
                folder = new File(folder.getParent(), "support.aas.basxy");
                PluginManager.registerPlugin(new FolderClasspathPluginSetupDescriptor(folder));
                found = true;
            } else { // local, unpacked
                folder = new File("target/oktoPlugins");
                if (folder.isDirectory()) {
                    LoggerFactory.getLogger(TestWithPlugin.class).info("Loading plugins from target/oktoPlugins "
                        + "(test deployment)");
                    PluginManager.registerPlugin(new FolderClasspathPluginSetupDescriptor(
                        new File("target/oktoPlugins/basyx")));
                    PluginManager.registerPlugin(new FolderClasspathPluginSetupDescriptor(
                        new File("target/oktoPlugins/basyx2")));
                    found = true;
                }
            }
            if (!found) {
                LoggerFactory.getLogger(TestWithPlugin.class).info("No (AAS) plugins found in parent folders/target. "
                    + "Test may fail.", folder);
            }
            // TODO default from AASFactory
            AasFactory.setPluginId(System.getProperty("okto.test.aas.pluginId", "aas.basyx-2.0")); 
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
