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

import java.util.function.Supplier;

/**
 * Default plugin descriptor implementation.
 * 
 * @param <T> plugin type
 * @author Holger Eichelberger, SSE
 */
public class DefaultPluginDescriptor<T> implements PluginDescriptor {

    private String id;
    private Class<T> pluginClass;
    private Supplier<T> pluginSupplier;
    
    /**
     * Creates a descriptor instance.
     * 
     * @param id the plugin id
     * @param pluginClass the instance class
     * @param pluginSupplier the creator supplier
     */
    public DefaultPluginDescriptor(String id, Class<T> pluginClass, Supplier<T> pluginSupplier) {
        this.id = id;
        this.pluginClass = pluginClass;
        this.pluginSupplier = pluginSupplier;
    }
    
    @Override
    public String getId() {
        return id;
    }

    @Override
    public Plugin<T> createPlugin() {
        return new Plugin<T>(id, pluginClass, pluginSupplier);
    }

}
