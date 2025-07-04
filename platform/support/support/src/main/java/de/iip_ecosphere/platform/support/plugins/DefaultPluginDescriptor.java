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
import java.util.List;
import java.util.function.Function;

/**
 * Default plugin descriptor implementation. May create per calling {@link #createPlugin(File)} always a new instance.
 * 
 * @param <T> plugin type
 * @author Holger Eichelberger, SSE
 */
public class DefaultPluginDescriptor<T> implements PluginDescriptor<T> {

    private String id;
    private List<String> ids;
    private Class<T> pluginClass;
    private Function<Plugin<T>, T> pluginSupplier;
    
    /**
     * Creates a descriptor instance.
     * 
     * @param id the plugin id
     * @param ids optional secondary ids, may be <b>null</b> or empty
     * @param pluginClass the instance class
     * @param pluginSupplier the creator supplier
     */
    public DefaultPluginDescriptor(String id, List<String> ids, Class<T> pluginClass, 
        Function<Plugin<T>, T> pluginSupplier) {
        this.id = id;
        this.ids = ids;
        this.pluginClass = pluginClass;
        this.pluginSupplier = pluginSupplier;
    }
    
    @Override
    public String getId() {
        return id;
    }

    @Override
    public Plugin<T> createPlugin(File installDir) {
        return createPlugin(id, ids, pluginClass, pluginSupplier, installDir);
    }

    /**
     * Creates the plugin instance.
     * 
     * @param id the plugin id
     * @param ids optional secondary ids, may be <b>null</b> or empty
     * @param pluginClass the instance class
     * @param pluginSupplier the creator supplier
     * @param installDir the installation directory, may be <b>null</b>
     * @return the plugin instance
     */
    protected Plugin<T> createPlugin(String id, List<String> ids, Class<T> pluginClass, 
        Function<Plugin<T>, T> pluginSupplier, File installDir) {
        return new Plugin<T>(id, ids, pluginClass, pluginSupplier, installDir);
    }

    @Override
    public Class<T> getType() {
        return pluginClass;
    }

}
