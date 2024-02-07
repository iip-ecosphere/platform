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
 * Represents a plugin.
 * 
 * @param <T> the type of the implementing class
 * @author Holger Eichelberger, SSE
 */
public class Plugin<T> {

    private String id;
    private Class<T> instanceCls;
    private Supplier<T> creator;
    
    /**
     * Creates a plugin instance.
     * 
     * @param id the plugin id
     * @param instanceCls the instance class
     * @param creator the creator supplier
     */
    public Plugin(String id, Class<T> instanceCls, Supplier<T> creator) {
        this.id = id;
        this.instanceCls = instanceCls;
        this.creator = creator;
    }
    
    /**
     * Returns the unique plugin id. Shall comply with the corresponding {@link PluginDescriptor}.
     * 
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the type of the implementing class.
     * 
     * @return the implementing class type
     */
    public Class<T> getInstanceClass() {
        return instanceCls;
    }

    /**
     * Returns the implementing class.
     * 
     * @return the implementing class
     */
    public T getInstance() {
        return creator.get();
    }
    
    /**
     * Cleans up this plugin.
     */
    public void cleanup() {
    }

}
