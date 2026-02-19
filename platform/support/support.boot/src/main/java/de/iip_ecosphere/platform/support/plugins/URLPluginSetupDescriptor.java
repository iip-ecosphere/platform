/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.plugins;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.OsUtils;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.oktoflow.platform.tools.lib.loader.IndexClassloader;
import de.oktoflow.platform.tools.lib.loader.LoaderIndex;

/**
 * Default URL-based plugin setup descriptor. Typically, a specific descriptor inherits from this class and sets
 * up the required information in a constructor without arguments.
 * 
 * @author Holger Eichelberger, SSE
 */
public class URLPluginSetupDescriptor implements PluginSetupDescriptor {

    private URL[] urls;

    /**
     * Creates an URL plugin setup descriptor.
     * 
     * @param urls the URLs to load classes from
     * @see #toURL(String[])
     */
    public URLPluginSetupDescriptor(URL[] urls) {
        this.urls = urls;
    }
    
    /**
     * Turns all {@code urls} to {@link URL} instances.
     * 
     * @param urls the URLs to convert
     * @return the converted URLs
     * @throws IllegalArgumentException if one of the {@code urls} has invalid syntax
     */
    public static URL[] toURL(String... urls) throws IllegalArgumentException {
        URL[] result = new URL[urls.length];
        for (int u = 0; u < urls.length; u++) {
            result[u] = NetUtils.createURL(urls[u]);
        }
        return result;
    }
    
    /**
     * Turns all {@code urls} to {@link URL} instances, logs errors.
     * 
     * @param urls the URLs to convert
     * @return the converted URLs
     * @see #toURL(String[])
     */
    public static URL[] toURLSafe(String... urls) {
        URL[] result;
        try {
            result = toURL(urls);
        } catch (IllegalArgumentException e) {
            LoggerFactory.getLogger(URLPluginSetupDescriptor.class).error(
                "While converting URLs {}: {} Ignoring.", urls, e.getMessage());
            result = new URL[0];
        }
        return result;
    }

    /**
     * Turns all {@code files} to {@link URL} instances.
     * 
     * @param files the files to convert
     * @return the converted URLs
     * @throws MalformedURLException if one of the {@code urls} has invalid syntax
     */
    public static URL[] toURL(File... files) throws MalformedURLException {
        URL[] result = new URL[files.length];
        for (int u = 0; u < files.length; u++) {
            result[u] = files[u].toURI().toURL();
        }
        return result;
    }
    
    /**
     * Turns all {@code files} to {@link URL} instances, logs errors.
     * 
     * @param files the files to convert
     * @return the converted URLs
     * @see #toURL(String[])
     */
    public static URL[] toURLSafe(File... files) {
        URL[] result;
        try {
            result = toURL(files);
        } catch (MalformedURLException e) {
            LoggerFactory.getLogger(URLPluginSetupDescriptor.class).error(
                "While converting URLs {}: {} Ignoring.", files, e.getMessage());
            result = new URL[0];
        }
        return result;
    }
    
    @Override
    public ClassLoader createClassLoader(ClassLoader parent) {
        LoggerFactory.getLogger(URLPluginSetupDescriptor.class)
            .debug("Creating classpath for {}", Arrays.toString(urls));
        return createClassLoader(urls, parent);
    }

    /**
     * Actually creates the classloader.
     * 
     * @param urls the URLs to create the classloader from
     * @param parent the parent class loader
     * @return the created classloader
     */
    protected ClassLoader createClassLoader(URL[] urls, ClassLoader parent) {
        ClassLoader result = null;
        if (ChildFirstClassLoader.useChildFirst()) {
            File idxFile = getIndexFile();
            if (null != idxFile && OsUtils.getBooleanEnv("OKTO_USE_PLUGIN_INDEXES", true)) {
                try {
                    LoaderIndex index = LoaderIndex.fromFile(idxFile);
                    Map<String, String> urlMapping = new HashMap<>();
                    for (URL url : urls) {
                        String u = LoaderIndex.normalize(Paths.get(url.toURI()).toString());
                        for (String loc : index.getLocations()) {
                            String l = LoaderIndex.normalize(loc);
                            if (u.endsWith(l)) {
                                urlMapping.put(loc, u);
                            }
                        }
                    }
                    index.substituteLocations(urlMapping);
                    result = new ChildFirstIndexClassLoader(index, parent);
                } catch (IOException | URISyntaxException e) {
                    LoggerFactory.getLogger(URLPluginSetupDescriptor.class).warn(
                        "Cannot create {}, falling back to {}. Reason: {}", 
                        IndexClassloader.class.getSimpleName(), 
                        ChildFirstURLClassLoader.class.getSimpleName(), 
                        e.getMessage());
                }
            }
            if (null == result) {
                result = new ChildFirstURLClassLoader(urls, parent);
            }
        } else {
            result = new URLClassLoader(urls, parent);
        }
        return result;
    }
    
    /**
     * Returns the index file for classloading.
     * 
     * @return the index file
     */
    protected File getIndexFile() {
        return null;
    }

    /**
     * Returns the URLs.
     * 
     * @return the URLs
     */
    protected URL[] getURLs() {
        return urls;
    }

}
