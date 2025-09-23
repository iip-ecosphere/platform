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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Predicate;

import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.plugins.PluginSetup;

/**
 * Support for class loading also in FAT jars. Resource resolvers can be added directly or via JLS. Resource
 * filtering is currently only supported on the implicit classpath resource resolver.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ResourceLoader {

    /**
     * Optional Maven resource resolver looking in {@code src/main/resources} and {@code src/test/resources} in this 
     * sequence.
     */
    public static final ResourceResolver MAVEN_RESOLVER = new MavenResourceResolver();
    
    private static List<ResourceResolver> resolvers = new ArrayList<>();
    private static List<Predicate<URI>> filters = null;
    
    static {
        registerResourceResolver(new ResourceResolver() {

            @Override
            public String getName() {
                return "Classloader";
            }

            @Override
            public InputStream resolve(ClassLoader loader, String resource) {
                InputStream result = null;
                if (hasFilters()) {
                    try {
                        Enumeration<URL> res = loader.getResources(resource);
                        while (res.hasMoreElements()) {
                            URL u = res.nextElement();
                            try {
                                if (matchesFilter(u.toURI())) {
                                    result = u.openStream();
                                }
                            } catch (URISyntaxException e) {
                                LoggerFactory.getLogger(this).warn("Cannot filter resource URL {}: {}", u, 
                                    e.getMessage());
                            }
                        }
                    } catch (IOException e) {
                        LoggerFactory.getLogger(this).warn("Cannot enumerate resources for {}: {}", resource, 
                            e.getMessage());
                    }
                } else {
                    result = loader.getResourceAsStream(resource);
                }
                return result;                
            }
        });
        
        ServiceLoader<ResourceResolver> loader = ServiceLoaderUtils.load(ResourceResolver.class);
        if (null != loader) { // whyever in tests, might be through mocking
            loader.forEach(r -> registerResourceResolver(r));
        }
    }

    /**
     * Registers a resources resolver.
     * 
     * @param resolver the resolver to be registered, ignored if <b>null</b>
     */
    public static void registerResourceResolver(ResourceResolver resolver) {
        registerResourceResolver(resolver, false);
    }

    /**
     * Registers a resources resolver.
     * 
     * @param resolver the resolver to be registered, ignored if <b>null</b>
     * @param prepend add the resolver to the front (handle with care)
     */
    public static void registerResourceResolver(ResourceResolver resolver, boolean prepend) {
        if (null != resolver) {
            if (prepend) {
                resolvers.add(0, resolver);
            } else {
                resolvers.add(resolver);
            }
            LoggerFactory.getLogger(ResourceLoader.class).info("Registered resource resolver {}", resolver.getName());
        }
    }
    
    /**
     * Returns whether the specific resolver is known.
     * 
     * @param resolver the resolver to look for
     * @return {@code true} for known, {@code false} else
     */
    public static boolean knowsResourceResolver(ResourceResolver resolver) {
        return resolvers.contains(resolver);
    }

    /**
     * Unregisters a resources resolver.
     * 
     * @param resolver the resolver to be unregistered, ignored if <b>null</b>
     */
    public static void unregisterResourceResolver(ResourceResolver resolver) {
        if (null != resolver) {
            resolvers.remove(resolver);
            LoggerFactory.getLogger(PluginSetup.getClassLoader()).info("Unregistered resource resolver {}", 
                resolver.getName());
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
        return getResourceAsStream(PluginSetup.getClassLoader(), name, optional);
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
    
    /**
     * Adds a URL-based resource filter. If filters are added, only resources that are accepted by the filter
     * are returned (if supported by the {@link ResourceResolver}. [testing]
     * 
     * @param filter the filter to add, ignored if <b>null</b>
     */
    public static void addFilter(Predicate<URI> filter) {
        if (null != filter) {
            if (null == filters) {
                filters = new ArrayList<>();
            }
            filters.add(filter);
        }
    }

    /**
     * Adds a set of default exclude filters to focus on app resources rather than test resources. [convenience]
     */
    public static void addTestExcludeFilters() {
     // may require a different naming for ZIP
        addFilter(u -> !u.toString().endsWith("-tests.jar!/identityStore.yml")); 
    }
    
    /**
     * Returns whether resource filters have been defined. [testing]
     * 
     * @return {@code true} for resource filters, {@code false} else
     */
    public static boolean hasFilters() {
        return null != filters && !filters.isEmpty();
    }
    
    /**
     * Returns if any filter matches the given {@code uri}. [testing]
     * 
     * @param uri the URI to test
     * @return {@code true} if at least one filter matches the given {@code uri}, {@code false} else. Consider 
     *     {@link #hasFilters()} to identify whether {@code false} is significant.
     */
    public static boolean matchesFilter(URI uri) {
        boolean matches = false;
        if (null != filters) {
            for (int f = 0; !matches && f < filters.size(); f++) {
                matches = filters.get(f).test(uri);
            }
        }
        return matches;
    }

}
