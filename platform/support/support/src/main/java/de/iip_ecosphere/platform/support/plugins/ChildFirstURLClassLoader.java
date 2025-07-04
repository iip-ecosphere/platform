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
import java.util.NoSuchElementException;

/**
 * A to make internal methods accessible.
 * 
 * @author Stackoverflow
 */
class FindClassClassLoader extends ClassLoader {

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

/**
 * A delegating child classloader to make internal methods accessible.
 * 
 * @author Stackoverflow
 */
class ChildURLClassLoader extends URLClassLoader {
    
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
    public Class<?> findClass(String name) throws ClassNotFoundException {
        boolean isJava = name.startsWith("java.") || name.startsWith("javax."); // java is java
        boolean isLogger = name.startsWith("org.slf4j.") // otherwise LinkageError
            || name.startsWith("ch.qos.logback."); // often instanceof required
        if (isJava || isLogger) {
            return realParent.loadClass(name);
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
public class ChildFirstURLClassLoader extends ClassLoader {
    
    //https://stackoverflow.com/questions/5445511/how-do-i-create-a-parent-last-child-first-classloader
    // -in-java-or-how-to-overr
    
    private ChildURLClassLoader childClassLoader;
    
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
        super(null == parent ? Thread.currentThread().getContextClassLoader() : parent);
        childClassLoader = new ChildURLClassLoader( urls, new FindClassClassLoader(this.getParent()) );
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
        @SuppressWarnings("unchecked")
        Enumeration<URL>[] tmp = (Enumeration<URL>[]) new Enumeration<?>[2];
        int index = 0;
        IOException ex1 = null;
        IOException ex2 = null;
        try {
            tmp[index++] = childClassLoader.getResources(name);
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
        InputStream result = childClassLoader.getResourceAsStream(name);
        if (null == result) {
            result = super.getResourceAsStream(name);
        }
        return result;
    }

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
    public void setPackageAssertionStatus(String packageName,
            boolean enabled) {
        childClassLoader.setPackageAssertionStatus(packageName, enabled);
        super.setPackageAssertionStatus(packageName, enabled);
    }
    
}

/**
 * A compound enumeration.
 * 
 * @param <E> the element type
 * @author SUN/Oracle (sun.misc)
 */
class CompoundEnumeration<E> implements Enumeration<E> {

    private Enumeration<E>[] enums;
    private int index = 0;

    /**
     * Creates a compound enumeration.
     * 
     * @param enums the enumerations to enumerated over.
     */
    public CompoundEnumeration(Enumeration<E>[] enums) {
        this.enums = enums;
    }

    /**
     * Determines the next element and returns whether it exists.
     * 
     * @return does the next element exist
     */
    private boolean next() {
        while (index < enums.length) {
            if (enums[index] != null && enums[index].hasMoreElements()) {
                return true;
            }
            index++;
        }
        return false;
    }

    @Override
    public boolean hasMoreElements() {
        return next();
    }

    @Override
    public E nextElement() {
        if (!next()) {
            throw new NoSuchElementException();
        }
        return enums[index].nextElement();
    }
}    
