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
import java.io.InputStream;
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
        URL result = realParent.getResource(name);
        if (null == result) {
            result = super.getResource(name);
        }
        return result;
    }
    
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        @SuppressWarnings("unchecked")
        Enumeration<URL>[] tmp = (Enumeration<URL>[]) new Enumeration<?>[2];
        int index = 0;
        IOException ex1 = null;
        IOException ex2 = null;
        try {
            tmp[index++] = realParent.getResources(name);
        } catch (IOException ex) {
            ex1 = ex;
        }
        try {
            tmp[index++] = super.getResources(name);
        } catch (IOException ex) {
            ex2 = ex;
        }
        if (ex1 != null && ex2 != null) {
            throw ex1;
        }
        return new CompoundEnumeration<>(tmp);        
    }
    
    @Override
    public InputStream getResourceAsStream(String name) {
        InputStream result = realParent.getResourceAsStream(name);
        if (null == result) {
            result = super.getResourceAsStream(name);
        }
        return result;
    }
    

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        boolean isJava = name.startsWith("java.") || name.startsWith("javax."); // java is java
        boolean isLogger = false; 
//        name.startsWith("org.slf4j.") // otherwise LinkageError
//            || name.startsWith("ch.qos.logback."); // often instanceof required
        try {
            if (isJava || isLogger) {
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
