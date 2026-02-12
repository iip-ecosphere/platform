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
import java.net.URLClassLoader;
import java.util.Enumeration;

/**
 * A delegating child classloader to make internal methods accessible.
 * 
 * @author Stackoverflow
 */
class ChildURLClassLoader extends URLClassLoader implements ChildClassLoader {
    
    //https://stackoverflow.com/questions/5445511/how-do-i-create-a-parent-last-child-first-classloader
    // -in-java-or-how-to-overr
    
    private FindClassClassLoader realParent;

    static {
        registerAsParallelCapable();
    }
    
    /**
     * Creates an instance with delegation to the real parent class loader.
     * 
     * @param urls the URLs to load classes from
     * @param realParent
     */
    public ChildURLClassLoader(URL[] urls, FindClassClassLoader realParent) {
        super(urls, null);
        this.realParent = realParent;
    }
    
    @Override
    public URL getResource(String name) {
        URL result = findResource(name);
        if (null == result) {
            result = realParent.getResource(name);
        }
        return result;
    }
    
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> result = findResources(name);
        if (null == result) {
            result = realParent.getResources(name);
        }
        return result;
    }
    
    // the other getResource methods in ClassLoader rely on these two
    
    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> result = findLoadedClass(name); 
        if (null == result) {
            result = findClassIntern(name);
        }
        return result;
    }
        
    /**
     * Finds a class (no caching).
     * 
     * @param name the qualified class name
     * @return the class object
     * @throws ClassNotFoundException if the class cannot be found
     */
    public Class<?> findClassIntern(String name) throws ClassNotFoundException {
        boolean isJava = name.startsWith("java.") || name.startsWith("javax."); // java is java
        try {
            if (isJava) {
                return realParent.loadClass(name);
            }
        } catch (ClassNotFoundException e) {
            // may also fail if there is eg no logger, the try super
        }
        try {
            // first try to use the URLClassLoader findClass
            return super.findClass(name);
        } catch (ClassNotFoundException e) {
            // if that fails, we ask our real parent classloader to load the class (we give up)
            return realParent.loadClass(name);
        }
    }
        
}    

/**
 * A delegating child-first classloader.
 * 
 * @author Stackoverflow
 * @author Holger Eichelberger, SSE
 */
public class ChildFirstURLClassLoader extends ChildFirstClassLoader {
    
    //https://stackoverflow.com/questions/5445511/how-do-i-create-a-parent-last-child-first-classloader
    // -in-java-or-how-to-overr
    
    static {
        registerAsParallelCapable();
    }
    
    /**
     * Creates a child-first classloader using the context class loader of the current thread as parent.
     * 
     * @param urls the URLs to load classes from
     */
    public ChildFirstURLClassLoader(URL[] urls) {
        this(urls, null);
    }        

    /**
     * Creates a child-first classloader.
     * 
     * @param urls the URLs to load classes from
     * @param parent the parent class loader
     */
    public ChildFirstURLClassLoader(URL[] urls, ClassLoader parent) {
        super(p -> new ChildURLClassLoader(urls, p), parent);
    }
    
}    
