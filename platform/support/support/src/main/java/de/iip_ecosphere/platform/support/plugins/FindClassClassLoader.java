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
 * A to make internal methods accessible.
 * 
 * @author Stackoverflow
 */
public class FindClassClassLoader extends ClassLoader {

    //https://stackoverflow.com/questions/5445511/how-do-i-create-a-parent-last-child-first-classloader
    // -in-java-or-how-to-overr

    /**
     * Creates the class loader with given parent.
     * 
     * @param parent the parent class laoder
     */
    public FindClassClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }
    
}