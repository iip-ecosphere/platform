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

package de.iip_ecosphere.platform.configuration.aas;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.plugins.FolderClasspathPluginSetupDescriptor;
import de.iip_ecosphere.platform.support.plugins.Plugin;
import de.iip_ecosphere.platform.support.plugins.PluginManager;

/**
 * Tries reading the AASX files of the IDTA specs through the available BaSyx plugins (explicitly loaded).
 * 
 * @author Holger Eichelberger, SSE
 */
public class TryBasyx {
    
    /**
     * Returns the AAS factory to use, based on {@code aasFactoryPluginId}.
     *
     * @param aasFactoryPluginId the plugin id of the AAS factory to use
     * @return the factory to use
     */
    private static AasFactory getAasFactory(String aasFactoryPluginId) {
        AasFactory factory;
        Plugin<AasFactory> plugin = PluginManager.getPlugin(aasFactoryPluginId, AasFactory.class);
        if (null != plugin) {
            factory = plugin.getInstance();
        } else { // fallback
            factory = AasFactory.getInstance();
        }
        return factory;
    }
    
    /**
     * Tries reading the files with the AAS factory of the given plugin id.
     * 
     * @param pluginId the AAS factory plugin id
     */
    private static void tryReading(String pluginId) {
        List<File> aasxFiles = new ArrayList<>();
        FileUtils.listFiles(new File("src/test/resources/idta"), 
            f -> f.isDirectory() || f.getName().endsWith(".aasx"), 
            f -> aasxFiles.add(f));
        
        
        //    private static String aasFactoryPluginId = AasFactory.DEFAULT_PLUGIN_ID;
        AasFactory factory = getAasFactory(pluginId);
        int successful = 0;
        int failed = 0;
        int all = 0;
        for (File f: aasxFiles) {
            System.out.println("Reading " + f.getName());
            try {
                factory.createPersistenceRecipe().readFrom(f);
                System.out.println(" Successful " + f.getName());
                successful++;
            } catch (IOException e) {
                System.out.println(" Failed " + f.getName());
                failed++;
            }
            all++;
        }
        System.out.println(successful + " successful, " + failed + " failed, " + all + " total");
    }

    /**
     * Reads the AASX files through both currently available BaSyx plugins.
     * 
     * @param args the command line arguments, ignored
     */
    public static void main(String[] args) {
        // explicitly load plugins
        PluginManager.registerPlugin(new FolderClasspathPluginSetupDescriptor(
            new File("../../support/support.aas.basyx1_5")));
        PluginManager.registerPlugin(new FolderClasspathPluginSetupDescriptor(
            new File("../../support/support.aas.basyx1_0")));
        PluginManager.registerPlugin(new FolderClasspathPluginSetupDescriptor(
            new File("../../support/support.aas.basyx")));

        System.out.println("If this program fails due to a PersistencyRecipt NullPointerException, please run ");
        System.out.println("the mvn build process on all involved plugins. This program is not part of the tests of ");
        System.out.println("this component - we cannot set production dependencies to optional components here.");
        
        System.out.println("BaSyx 1.3");
        tryReading(AasFactory.DEFAULT_PLUGIN_ID);
        
        System.out.println();
        System.out.println("BaSyx 1.0");
        tryReading("aas.basyx-1.0");

        System.out.println();
        System.out.println("BaSyx 1.5");
        tryReading("aas.basyx-1.5");
    }

}
