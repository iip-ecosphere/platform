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

package de.iip_ecosphere.platform.support.plugins;

/**
 * A default plugin setup descriptor taking the actual/parent classloader as actual one.
 * This descriptor is intended for plugins that do not need to load further classes or, in turn,
 * use the plugin mechanism for implementation.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CurrentClassloaderPluginSetupDescriptor implements PluginSetupDescriptor {

    /**
     * Instance for the current class loader provided by the plugin manager. This is 
     * {@link PluginSetup#getClassLoader()}.
     */
    public static final CurrentClassloaderPluginSetupDescriptor INSTANCE 
        = new CurrentClassloaderPluginSetupDescriptor();

    private ClassLoader loader;

    /**
     * Creates a default instance using {@link PluginSetup#getClassLoader()}.
     */
    public CurrentClassloaderPluginSetupDescriptor() {
        this(null);
    }
    
    /**
     * Creates an instance for a specific class loader.
     * 
     * @param loader the class loader to use, may be <b>null</b> then {@link PluginSetup#getClassLoader()} is used
     */
    public CurrentClassloaderPluginSetupDescriptor(ClassLoader loader) {
        this.loader = loader;
    }

    @Override
    public ClassLoader createClassLoader(ClassLoader parent) {
        return null == loader ? parent : loader;
    }

}
