/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.resources;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.LoggerFactory;

/**
 * Support for class loading also in FAT jars.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ResourceLoader {

    private static List<ResourceResolver> resolvers = new ArrayList<>();
    
    static {
        registerResourceResolver(new ResourceResolver() {

            @Override
            public String getName() {
                return "Classloader";
            }

            @Override
            public InputStream resolve(ClassLoader loader, String resource) {
                return loader.getResourceAsStream(resource);                
            }
        });
        
        ServiceLoader<ResourceResolver> loader = ServiceLoader.load(ResourceResolver.class);
        loader.forEach(r -> registerResourceResolver(r));
    }
    
    /**
     * Registers a resources resolver.
     * 
     * @param resolver the resolver to be registered, ignored if <b>null</b>
     */
    public static void registerResourceResolver(ResourceResolver resolver) {
        if (null != resolver) {
            resolvers.add(resolver);
        }
    }

    /**
     * Unregisters a resources resolver.
     * 
     * @param resolver the resolver to be unregistered, ignored if <b>null</b>
     */
    public static void unregisterResourceResolver(ResourceResolver resolver) {
        if (null != resolver) {
            resolvers.remove(resolver);
        }
    }

    /**
     * Returns a resource as string taking the class loader of this class.
     * 
     * @param name the name of the resource to load
     * @return the resource as input stream, may be <b>null</b> if the resource was not found
     */
    public static InputStream getResourceAsStream(String name) {
        return getResourceAsStream(ResourceLoader.class, name);
    }

    /**
     * Returns a resource as string.
     * 
     * @param cls the class to take the class loader from 
     * @param name the name of the resource to load
     * @return the resource as input stream, may be <b>null</b> if the resource was not found
     */
    public static InputStream getResourceAsStream(Class<?> cls, String name) {
        return getResourceAsStream(cls.getClassLoader(), name);
    }

    /**
     * Returns a resource as string.
     * 
     * @param loader the class loader to use
     * @param name the name of the resource to load (shall not start with "/", tested as fallback alternative)
     * @return the resource as input stream, may be <b>null</b> if the resource was not found
     */
    public static InputStream getResourceAsStream(ClassLoader loader, String name) {
        InputStream result = null;
        while (name.startsWith("/")) {
            name = name.substring(1);
        }
        for (ResourceResolver r: resolvers) {
            result = r.resolve(loader, name);    
            if (result != null) {
                LoggerFactory.getLogger(ResourceLoader.class).info("LOADING {} via {}", name, r.getName());
                break;
            }
            if (null == result && !name.startsWith("/")) {
                result = r.resolve(loader, "/" + name);
            }
            if (result != null) {
                LoggerFactory.getLogger(ResourceLoader.class).info("LOADING {} via {}", name, r.getName());
                break;
            }
        }
        return result;
    }

}
