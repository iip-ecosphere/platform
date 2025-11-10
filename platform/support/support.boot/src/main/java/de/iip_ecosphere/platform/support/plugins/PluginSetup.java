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
 * Basic setup for plugins.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PluginSetup {

    private static ClassLoader loader = PluginSetup.class.getClassLoader();

    /**
     * Sets the class loader to use, also as central class loader for resource loading.
     * 
     * @param ldr the class loader, ignored if <b>null</b>
     */
    public static void setClassLoader(ClassLoader ldr) {
        loader = ldr;
    }
    
    /**
     * Returns the class loader, also for resource loading.
     * 
     * @return the class loader
     */
    public static ClassLoader getClassLoader() {
        return loader;
    }
    
}
