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

package de.iip_ecosphere.platform.configuration.easyProducer;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import de.iip_ecosphere.platform.configuration.cfg.AasChanges;
import de.iip_ecosphere.platform.configuration.cfg.ConfigurationFactoryDescriptor;
import de.iip_ecosphere.platform.configuration.cfg.ConfigurationSetup;
import de.iip_ecosphere.platform.configuration.cfg.PlatformInstantiation;
import de.iip_ecosphere.platform.support.plugins.SingletonPluginDescriptor;

/**
 * The SLF4j plugin descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EasyConfigurationFactoryDescriptor extends SingletonPluginDescriptor<ConfigurationFactoryDescriptor> 
    implements ConfigurationFactoryDescriptor {

    /**
     * Creates the descriptor.
     */
    public EasyConfigurationFactoryDescriptor() {
        super("configuration", List.of("configuration-easyProducer"), ConfigurationFactoryDescriptor.class, null);
    }

    @Override
    protected PluginSupplier<ConfigurationFactoryDescriptor> initPluginSupplier(
        PluginSupplier<ConfigurationFactoryDescriptor> pluginSupplier) {
        return p -> this;
    }    
    
    @Override
    public ConfigurationSetup getSetup() {
        return de.iip_ecosphere.platform.configuration.easyProducer.ConfigurationSetup.getSetup();
    }
    
    @Override
    public AasChanges createAasChanges() {
        return de.iip_ecosphere.platform.configuration.easyProducer.AasChanges.INSTANCE;
    }

    @Override
    public PlatformInstantiation createInstantiator(File localRepo, Consumer<String> warn, Consumer<String> info,
        Consumer<Long> executionTimeConsumer) {
        return new PlatformInstantiatorExecutor(localRepo, warn, info, executionTimeConsumer);
    }
    
    @Override
    public void hintAppsToInstantiate(String apps) {
        System.setProperty(PlatformInstantiator.KEY_PROPERTY_APPS, apps);
    }
    
}
