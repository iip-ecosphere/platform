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
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.NetUtils;

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
        return new ChildFirstURLClassLoader(urls, parent);
    }

}
