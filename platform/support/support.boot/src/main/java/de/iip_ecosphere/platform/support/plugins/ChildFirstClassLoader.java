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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.function.Function;

import de.iip_ecosphere.platform.support.OsUtils;

/**
 * A delegating child-first classloader.
 * 
 * @author Stackoverflow
 * @author Holger Eichelberger, SSE
 */
public class ChildFirstClassLoader extends ClassLoader implements IdentifyingClassloader {
    
    //https://stackoverflow.com/questions/5445511/how-do-i-create-a-parent-last-child-first-classloader
    // -in-java-or-how-to-overr
    
    private ChildClassLoader childClassLoader;
    
    /**
     * Creates a child-first classloader using the context class loader of the current thread as parent.
     * 
     * @param creator a function creating the child classloader
     */
    public ChildFirstClassLoader(Function<FindClassClassLoader, ChildClassLoader> creator) {
        this(creator, null);
    }        

    /**
     * Creates a child-first classloader.
     * 
     * @param creator a function creating the child classloader
     * @param parent the parent class loader
     */
    public ChildFirstClassLoader(Function<FindClassClassLoader, ChildClassLoader> creator, ClassLoader parent) {
        super(null == parent ? Thread.currentThread().getContextClassLoader() : parent);
        childClassLoader = creator.apply(new FindClassClassLoader(this.getParent()));
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return childClassLoader.findClass(name);
        } catch (ClassNotFoundException e) {
            return super.loadClass(name, resolve);
        }
    }        
    
    @Override
    public URL getResource(String name) {
        URL result = childClassLoader.getResource(name);
        if (result == null) {
            result = super.getResource(name);
        }
        return result;
    }
    
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> result = childClassLoader.getResources(name);
        if (result == null) {
            result = super.getResources(name);
        }
        return result;
    }

    // the other getResource methods in ClassLoader rely on these two

    @Override
    public void setClassAssertionStatus(String className, boolean enabled) {
        childClassLoader.setClassAssertionStatus(className, enabled);
        super.setClassAssertionStatus(className, enabled);
    }

    @Override
    public void clearAssertionStatus() {
        childClassLoader.clearAssertionStatus();
        super.clearAssertionStatus();
    }

    @Override
    public void setDefaultAssertionStatus(boolean enabled) {
        childClassLoader.setDefaultAssertionStatus(enabled);
        super.setDefaultAssertionStatus(enabled);
    }

    @Override
    public void setPackageAssertionStatus(String packageName, boolean enabled) {
        childClassLoader.setPackageAssertionStatus(packageName, enabled);
        super.setPackageAssertionStatus(packageName, enabled);
    }
    
    @Override
    public boolean amI(ClassLoader loader) {
        return this == loader || childClassLoader == loader;
    }
    
    @Override
    public String toString() {
        return super.toString() + " with child " + childClassLoader;
    }
    
    /**
     * Returns whether a child-first classloader shall be used. This method is for experiments and shall be removed
     * later.
     * 
     * @return {@code true} or the value of the environment variable {@code OKTO_PLUGIN_CHILD_FIRST}
     */
    public static boolean useChildFirst() {
        boolean result = true;
        String tmp = OsUtils.getEnv("OKTO_PLUGIN_CHILD_FIRST");
        if (tmp != null) {
            result = Boolean.valueOf(tmp);
        }
        return result;
    }
    
}