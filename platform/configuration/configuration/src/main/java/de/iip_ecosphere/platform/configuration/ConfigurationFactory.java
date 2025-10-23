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

package de.iip_ecosphere.platform.configuration;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Consumer;

import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.plugins.Plugin;
import de.iip_ecosphere.platform.support.plugins.PluginManager;


/**
 * Provides access to the implementation.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConfigurationFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationFactory.class);
    private static ConfigurationFactoryDescriptor desc;
    private static ConfigurationSetup setup;
    private static AasChanges aasChanges;
    
    /**
     * Initializes this factory.
     */
    private static void init() {
        if (null == desc) {
            Plugin<ConfigurationFactoryDescriptor> plugin 
                = PluginManager.getPlugin(ConfigurationFactoryDescriptor.class);
            if (null != plugin) {
                desc = plugin.getInstance();
            } else {
                ServiceLoader<ConfigurationFactoryDescriptor> loader = ServiceLoader.load(
                    ConfigurationFactoryDescriptor.class);
                Optional<ConfigurationFactoryDescriptor> first = ServiceLoaderUtils.findFirst(loader);
                if (first.isPresent()) {
                    desc = first.get();
                } else {
                    LOGGER.error("No configuration factory available.");
                }
            }
        }
    }
    
    /**
     * Returns the configuration setup instance.
     * 
     * @return the setup instance, may be <b>null</b> if no configuration plugin is available
     */
    public static ConfigurationSetup getSetup() {
        init();
        ConfigurationSetup result = setup;
        if (null == result) {
            if (null != desc) {
                result = desc.getSetup();
            }
            if (null == result) {
                try {
                    result = ConfigurationSetup.readFromYaml(ConfigurationSetup.class);
                } catch (IOException e) {
                    result = new ConfigurationSetup();
                }
            }
        }
        return result;
    }

    /**
     * Returns the configuration setup instance.
     * 
     * @return the setup instance, may be <b>null</b> if no configuration plugin is available
     */
    public static AasChanges getAasChanges() {
        init();
        AasChanges result = aasChanges;
        if (null == result) {
            if (null != desc) {
                result = desc.createAasChanges();
            }
        }
        return result;
    }
    
    /**
     * Creates a platform instantiator instance.
     * 
     * @param localRepo the local Maven repository, may be <b>null</b>
     * @param warn a warning message consumer
     * @param info an information message consumer
     * @param executionTimeConsumer optional consumer for the (successful) process execution time, may be <b>null</b> 
     *     for none
     * @return the instantiation instance, may be <b>null</b> if no configuration plugin is available
     */
    public static PlatformInstantiation createInstantiator(File localRepo, Consumer<String> warn, 
        Consumer<String> info, Consumer<Long> executionTimeConsumer) {
        init();
        PlatformInstantiation result = null;
        if (null != desc) {
            result = desc.createInstantiator(localRepo, warn, info, executionTimeConsumer);
        }
        return result;
    }
    
    /**
     * Hints the following executors regarding the apps to instantiate. Maven legacy.
     * 
     * @param apps the app ids as comma separated list or empty
     */
    public static void hintAppsToInstantiate(String apps) {
        if (null != desc) {
            desc.hintAppsToInstantiate(apps);
        }
    }

}
