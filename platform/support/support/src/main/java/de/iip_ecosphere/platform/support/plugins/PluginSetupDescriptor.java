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

/**
 * A descriptor to describe the presence of a plugin. Used for setting up the {@link PluginManager}.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface PluginSetupDescriptor {

    /**
     * Returns the class loader being responsible for loading the plugin.
     * 
     * @param parent the parent class loader with basic dependencies to use for loading this plugin
     * @return the class loader
     */
    public ClassLoader createClassLoader(ClassLoader parent);
    
}
