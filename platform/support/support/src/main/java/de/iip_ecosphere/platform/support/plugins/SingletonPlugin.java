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
 * Represents a singleton plugin, i.e., a plugin holds and returns a singleton instance.
 * 
 * @param <T> the type of the implementing class
 * @author Holger Eichelberger, SSE
 */
public class SingletonPlugin<T> extends Plugin<T> {

    private T instance;
    
    /**
     * Creates a plugin instance.
     * 
     * @param id the primary plugin id
     * @param ids further (optional) ids, may be empty or <b>null</b>
     * @param instanceCls the instance class
     * @param creator the creator supplier
     * @param installDir the installation directory, may be <b>null</b>
     */
    public SingletonPlugin(String id, List<String> ids, Class<T> instanceCls, Function<Plugin<T>, T> creator, 
        File installDir) {
        super(id, ids, instanceCls, creator, installDir);
    }

    /**
     * Returns the implementing class.
     * 
     * @return the implementing class
     */
    public T getInstance() {
        if (null == instance) {
            instance = super.getInstance();
        }
        return instance;
    }
    
    // cleanup instance=null?

}
