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
 * A default plugin setup descriptor taking the actual thread's context class loader or as fallback the specified 
 * parent classloader as actual one. This descriptor is intended for plugins that do not need to load further classes 
 * or, in turn, use the plugin mechanism for implementation.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CurrentContextPluginSetupDescriptor implements PluginSetupDescriptor {

    public static final CurrentContextPluginSetupDescriptor INSTANCE 
        = new CurrentContextPluginSetupDescriptor();
    
    @Override
    public ClassLoader createClassLoader(ClassLoader parent) {
        ClassLoader result = Thread.currentThread().getContextClassLoader();
        return null == result ? parent : result;
    }

}
