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

/**
 * Represents a loaded plugin.
 * 
 * @param <T> the type of the plugin
 * @author Holger Eichelberger, SSE
 */
public interface PluginDescriptor<T> {

    /**
     * Returns the unique/primary plugin id.
     * 
     * @return the id
     */
    public String getId();
    
    /**
     * Returns further ids this plugin represents.
     * 
     * @return the further ids, may be <b>null</b>, empty, unmodifiable
     */
    public default List<String> getFurtherIds() {
        return null;
    }
    
    /**
     * Creates a plugin instance.
     * 
     * @param installDir the installation directory, may be <b>null</b>
     * @return the plugin instance
     */
    public Plugin<T> createPlugin(File installDir);
    
    /**
     * Returns the type of plugin being created.
     * 
     * @return
     */
    public Class<T> getType();
    
}
