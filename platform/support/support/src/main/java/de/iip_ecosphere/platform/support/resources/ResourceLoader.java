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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Support for class loading also in FAT jars. Resource resolvers can be added directly or via JLS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ResourceLoader {

    /**
     * Optional Maven resource resolver looking in src/main/resources and src/test/resources in this sequence.
     */
    public static final ResourceResolver MAVEN_RESOLVER = new ResourceResolver() {
        
        @Override
        public String getName() {
            return "Maven resources";
        }
        
        @Override
        public InputStream resolve(ClassLoader loader, String resource) {
            InputStream result = null;
            File f = new File("src/main/resources/" + resource);
            if (f.exists()) {
                try {
                    result = new FileInputStream(f);
                } catch (IOException e) {
                }
            } else {
                try {
                    f = new File("src/test/resources/" + resource);
                    if (f.exists()) {
                        result = new FileInputStream(f);    
                    }
                } catch (IOException e) {
                }
            }
            return result;
        }
    };
    
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
            LoggerFactory.getLogger(ResourceLoader.class).info("Registered resource resolver {}", resolver.getName());
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
            LoggerFactory.getLogger(ResourceLoader.class).info("Unregistered resource resolver {}", resolver.getName());
        }
    }

    /**
     * Returns a resource as string taking the class loader of this class.
     * 
     * @param name the name of the resource to load
     * @param optional further, optional on-the fly resolvers
     * @return the resource as input stream, may be <b>null</b> if the resource was not found
     */
    public static InputStream getResourceAsStream(String name, ResourceResolver... optional) {
        return getResourceAsStream(ResourceLoader.class, name, optional);
    }
    
    /**
     * Returns a resolver for all registered resolvers.
     * 
     * @param resolver additional resolver that shall be part of the result
     * @return a resolver for all registered resolvers 
     */
    public static ResourceResolver getAllRegisteredResolver(ResourceResolver... resolver) {
        List<ResourceResolver> res;
        if (resolver.length > 0) {
            res = new ArrayList<ResourceResolver>();
            res.addAll(resolvers);
            for (ResourceResolver r : resolver) {
                res.add(r);
            }
        } else {
            res = resolvers;
        }
        return new MultiResourceResolver(res);
    }

    /**
     * Returns a resource as string.
     * 
     * @param cls the class to take the class loader from 
     * @param name the name of the resource to load
     * @param optional further, optional on-the fly resolvers
     * @return the resource as input stream, may be <b>null</b> if the resource was not found
     */
    public static InputStream getResourceAsStream(Class<?> cls, String name, ResourceResolver... optional) {
        return getResourceAsStream(cls.getClassLoader(), name, optional);
    }

    /**
     * Returns a resource as string.
     * 
     * @param loader the class loader to use
     * @param name the name of the resource to load (shall not start with "/", used as fallback alternative)
     * @param optional further, optional on-the fly resolvers
     * @return the resource as input stream, may be <b>null</b> if the resource was not found
     */
    public static InputStream getResourceAsStream(ClassLoader loader, String name, ResourceResolver... optional) {
        InputStream result = null;
        while (name.startsWith("/")) {
            name = name.substring(1);
        }
        List<ResourceResolver> res = resolvers;
        if (null != optional && optional.length > 0) {
            res = new ArrayList<>();
            res.addAll(resolvers);
            Collections.addAll(res, optional);
        }
        for (ResourceResolver r: res) {
            result = r.resolve(loader, name);    
            if (result != null) {
                LoggerFactory.getLogger(ResourceLoader.class).info("Loading resource '{}' via {}", name, r.getName());
                break;
            }
            if (null == result && !name.startsWith("/")) {
                result = r.resolve(loader, "/" + name);
            }
            if (result != null) {
                LoggerFactory.getLogger(ResourceLoader.class).info("Loading {} via {}", name, r.getName());
                break;
            }
        }
        if (null == result) {
            LoggerFactory.getLogger(ResourceLoader.class).debug("Resource {} not found", name);
        }
        return result;
    }
    
    /**
     * Prepends a "/" if there is none at the beginning of {@code text}.
     * 
     * @param text the text to use as basis
     * @return test with "/" prepended
     */
    public static final String prependSlash(String text) {
        if (!text.startsWith("/")) {
            text = "/" + text;
        }
        return text;
    }

}
