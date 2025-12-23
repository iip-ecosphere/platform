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
    private PluginSupplier<T> pluginSupplier;

    /**
     * Declares the plugin supplier type.
     * 
     * @param <T> plugin type
     * @author Holger Eichelberger, SSE
     */
    public interface PluginSupplier<T> extends Function<Plugin<T>, T> {
        
    }
    
    /**
     * Creates a descriptor instance.
     * 
     * @param id the plugin id
     * @param ids optional secondary ids, may be <b>null</b> or empty
     * @param pluginClass the instance class
     * @param pluginSupplier the creator supplier
     * @see #initId(String)
     * @see #initIds(List)
     * @see #initPluginSupplier(PluginSupplier)
     */
    public DefaultPluginDescriptor(String id, List<String> ids, Class<T> pluginClass, 
        PluginSupplier<T> pluginSupplier) {
        this.id = initId(id);
        this.ids = initIds(ids);
        this.pluginClass = initPluginClass(pluginClass);
        this.pluginSupplier = initPluginSupplier(pluginSupplier);
    }
    
    /**
     * Returns the plugin supplier upon creation. May override the provided supplier, in particular if the 
     * result refers to <b>this</b>, which is not available in the super call of a constructor.
     * 
     * @param pluginSupplier the supplied supplier
     * @return {@code pluginSupplier}
     */
    protected PluginSupplier<T> initPluginSupplier(PluginSupplier<T> pluginSupplier) {
        return pluginSupplier;
    }

    /**
     * Returns the plugin class upon creation. May override the provided class, in particular if the 
     * result refers to <b>this</b> or {@link #getClass()}, which is not available in the super call of a constructor.
     * 
     * @param pluginClass the plugin class
     * @return {@code pluginSupplier}
     */
    protected Class<T> initPluginClass(Class<T> pluginClass) {
        return pluginClass;
    }

    /**
     * Returns the plugin id upon creation. May override the provided id.
     * 
     * @param id the supplied plugin id
     * @return {@code id}
     */
    protected String initId(String id) {
        return id;
    }

    /**
     * Returns the additional plugin ids upon creation.
     * 
     * @param ids the supplied plugin ids
     * @return {@code ids}
     */
    protected List<String> initIds(List<String> ids) {
        return ids;
    }

    @Override
    public List<String> getFurtherIds() {
        return ids;
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
        PluginSupplier<T> pluginSupplier, File installDir) {
        return new Plugin<T>(id, ids, pluginClass, pluginSupplier, installDir);
    }

    @Override
    public Class<T> getType() {
        return pluginClass;
    }

}
