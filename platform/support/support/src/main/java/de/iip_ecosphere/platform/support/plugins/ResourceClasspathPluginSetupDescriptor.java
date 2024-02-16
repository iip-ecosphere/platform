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
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.support.resources.ResourceResolver;

/**
 * Default resource-based plugin setup descriptor, reading the required URLs from a classpath file. Typically, a 
 * specific descriptor inherits from this class and sets up the required information in a constructor without arguments.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ResourceClasspathPluginSetupDescriptor extends URLPluginSetupDescriptor {

    /**
     * Creates an instance based on loading a resource in classpath format.
     * 
     * @param resourceName the name of the resource
     * @param resolvers optional further, optional on-the fly resolvers
     */
    public ResourceClasspathPluginSetupDescriptor(String resourceName, ResourceResolver... resolvers) {
        super(loadResourceSafe(resourceName, resolvers));
    }

    /**
     * Loads a resource in classpath format and returns the specified classpath entries as URLs. Logs errors and 
     * exceptions.
     * 
     * @param resourceName the name of the resource
     * @param resolvers optional further, optional on-the fly resolvers
     * @return the URLs, may be empty
     */
    public static URL[] loadResourceSafe(String resourceName, ResourceResolver... resolvers) {
        URL[] result = null;
        InputStream in = ResourceLoader.getResourceAsStream(resourceName, resolvers);
        if (null != in) {
            try {
                List<File> entries = new ArrayList<File>();
                String contents = IOUtils.toString(in, Charset.defaultCharset());
                StringTokenizer tokenizer = new StringTokenizer(contents, ":;");
                while (tokenizer.hasMoreTokens()) {
                    entries.add(new File(tokenizer.nextToken()));
                }
                result = toURLSafe(entries.toArray(new File[entries.size()]));
            } catch (IOException e) {
                LoggerFactory.getLogger(URLPluginSetupDescriptor.class).error(
                    "While reading resource '{}': {} Ignoring.", resourceName, e.getMessage());
            }
        } else {
            LoggerFactory.getLogger(URLPluginSetupDescriptor.class).error(
                "Resource '{}' not found. Ignoring.", resourceName);
        }
        if (null == result) {
            result = new URL[0];
        }
        return result;
    }

}
