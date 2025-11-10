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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.IOUtils;
import de.iip_ecosphere.platform.support.ZipUtils;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.support.resources.ResourceResolver;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Default resource-based plugin setup descriptor, reading the required URLs from a classpath file. Tries to resolve and
 * localize the individual classpath entries. Typically, a specific descriptor inherits from this class and sets up the 
 * required information in a constructor without arguments.
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
     * Loads a resource in classpath format and returns the specified classpath entries as URLs. Tries to resolve and 
     * localize the individual classpath entries. Logs errors and exceptions.
     * 
     * @param resourceName the name of the resource
     * @param resolvers optional further, optional on-the fly resolvers
     * @return the URLs, may be empty
     */
    public static URL[] loadResourceSafe(String resourceName, ResourceResolver... resolvers) {
        URL[] result = null;
        String folderName = resourceName;
        int pos = folderName.lastIndexOf('.');
        if (pos > 0) {
            folderName = folderName.substring(0, pos);
        }
        File dir = new File(FileUtils.getTempDirectory(), folderName);
        dir.deleteOnExit();
        dir.mkdirs();
        boolean zipExtracted = false;
        InputStream in = ResourceLoader.getResourceAsStream(resourceName, resolvers);
        if (null != in && resourceName.endsWith(".zip")) {
            try {
                ZipUtils.extractZip(in, dir.toPath());
                de.iip_ecosphere.platform.support.FileUtils.closeQuietly(in);
                in = new FileInputStream(new File(dir, "classpath"));
                zipExtracted = true;
            } catch (IOException e) {
                in = null;
                LoggerFactory.getLogger(URLPluginSetupDescriptor.class).error(
                    "While reading resource, extracting ZIP '{}': {} Ignoring.", resourceName, e.getMessage());
            }
        }
        if (null != in) {
            try {
                List<File> entries = new ArrayList<File>();
                String contents = IOUtils.toString(in);
                StringTokenizer tokenizer = new StringTokenizer(contents, ":;");
                while (tokenizer.hasMoreTokens()) {
                    String tok = tokenizer.nextToken();
                    File file = new File(tok);
                    File target = new File(dir, tok);
                    if (!zipExtracted && file.exists()) { // local, relative
                        entries.add(file);
                    } else if (zipExtracted && target.exists()) { // unpacked
                        entries.add(target);
                    } else if (!file.exists() && !target.exists()) { // else, try unpacking
                        InputStream tis = ResourceLoader.getResourceAsStream(tok, resolvers);
                        if (tis != null) {
                            try {
                                FileUtils.copyInputStreamToFile(tis, target);
                                entries.add(target);
                            } catch (IOException e1) {
                                LoggerFactory.getLogger(URLPluginSetupDescriptor.class).error(
                                    "While stpring resource '{}': {} Ignoring.", resourceName, e1.getMessage());
                            }
                        }
                    }
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
