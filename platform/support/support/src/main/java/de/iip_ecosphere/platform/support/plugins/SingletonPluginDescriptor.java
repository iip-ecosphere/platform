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
 * Singleton plugin descriptor implementation, creates {@link SingletonPlugin} instances.
 * 
 * @param <T> plugin type
 * @author Holger Eichelberger, SSE
 */
public class SingletonPluginDescriptor<T> extends DefaultPluginDescriptor<T> {

    /**
     * Creates a descriptor instance.
     * 
     * @param id the plugin id
     * @param ids optional secondary ids, may be <b>null</b> or empty
     * @param pluginClass the instance class
     * @param pluginSupplier the creator supplier
     */
    public SingletonPluginDescriptor(String id, List<String> ids, Class<T> pluginClass, 
        Function<Plugin<T>, T> pluginSupplier) {
        super(id, ids, pluginClass, pluginSupplier);
    }
    
    @Override
    protected Plugin<T> createPlugin(String id, List<String> ids, Class<T> pluginClass, 
        Function<Plugin<T>, T> pluginSupplier, File installDir) {
        return new SingletonPlugin<T>(id, ids, pluginClass, pluginSupplier, installDir);
    }

}
