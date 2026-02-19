/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
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

import de.oktoflow.platform.tools.lib.loader.IndexClassloader;
import de.oktoflow.platform.tools.lib.loader.LoaderIndex;

/**
 * A delegating child classloader to make internal methods accessible.
 * 
 * @author Stackoverflow
 */
class ChildIndexClassLoader extends IndexClassloader implements ChildClassLoader {
    
    //https://stackoverflow.com/questions/5445511/how-do-i-create-a-parent-last-child-first-classloader
    // -in-java-or-how-to-overr
    
    private FindClassClassLoader realParent;

    static {
        registerAsParallelCapable();
    }
    
    /**
     * Creates an instance with delegation to the real parent class loader.
     * 
     * @param index the index to load classes from
     * @param realParent the real parent
     */
    public ChildIndexClassLoader(LoaderIndex index, FindClassClassLoader realParent) {
        super(index, null);
        this.realParent = realParent;
    }
    
    @Override
    public URL getResource(String name) {
        URL result = findResource(name);
        if (null == result) {
            result = realParent.getResource(name);
            if (null == result) {
                String modName = name.replace('$', '/'); // Spring 2.4 behavior package$Class for non-inner classes
                result = realParent.getResource(modName);
            }
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
            try {
                // first try to use the URLClassLoader findClass
                result = super.findClass(name);
            } catch (ClassNotFoundException e) {
                // if that fails, we ask our real parent classloader to load the class (we give up)
                result = realParent.loadClass(name);
            }
        }
        return result;
    }
        
}    

/**
 * A delegating child-first index classloader.
 * 
 * @author Stackoverflow
 * @author Holger Eichelberger, SSE
 */
public class ChildFirstIndexClassLoader extends ChildFirstClassLoader {

    //https://stackoverflow.com/questions/5445511/how-do-i-create-a-parent-last-child-first-classloader
    // -in-java-or-how-to-overr
    
    static {
        registerAsParallelCapable();
    }
    
    /**
     * Creates a child-first classloader using the context class loader of the current thread as parent.
     * 
     * @param index the index to load classes from
     */
    public ChildFirstIndexClassLoader(LoaderIndex index) {
        this(index, null);
    }        

    /**
     * Creates a child-first classloader.
     * 
     * @param index the index to load classes from
     * @param parent the parent class loader
     */
    public ChildFirstIndexClassLoader(LoaderIndex index, ClassLoader parent) {
        super(p -> new ChildIndexClassLoader(index, p), parent);
    }

}
