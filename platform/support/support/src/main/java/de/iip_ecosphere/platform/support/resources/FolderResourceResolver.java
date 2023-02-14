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

import de.iip_ecosphere.platform.support.FileUtils;

/**
 * A simple resource resolver that takes a given folder into account.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FolderResourceResolver implements ResourceResolver {
    
    private File basePath;
    private String removePrefix;
    
    /**
     * Creates a folder resource resolve for the system root directory. 
     */
    public FolderResourceResolver() {
        this(FileUtils.getSystemRoot());
    }

    /**
     * Creates a resolver.
     * 
     * @param basePath the base path to be taken into account
     */
    public FolderResourceResolver(String basePath) {
        this(new File(basePath));
    }
    
    /**
     * Creates a resolver.
     * 
     * @param basePath the base path to be taken into account
     * @param removePrefix prefix to be removed from requested resource name, may be empty or null for none
     */
    public FolderResourceResolver(String basePath, String removePrefix) {
        this(new File(basePath), removePrefix);
    }

    /**
     * Creates a resolver.
     * 
     * @param basePath the base path to be taken into account
     */
    public FolderResourceResolver(File basePath) {
        this(basePath, null);
    }
    
    /**
     * Creates a resolver.
     * 
     * @param basePath the base path to be taken into account
     * @param removePrefix prefix to be removed from requested resource name, may be empty or null for none
     */
    public FolderResourceResolver(File basePath, String removePrefix) {
        this.basePath = basePath;
        this.removePrefix = removePrefix;
    }

    @Override
    public InputStream resolve(ClassLoader loader, String resource) {
        if (null != removePrefix && removePrefix.length() > 0) {
            if (resource.startsWith(removePrefix)) {
                resource = resource.substring(removePrefix.length());
            }
        }
        try {
            return new FileInputStream(new File(basePath, resource));
        } catch (IOException e) {
            return null;
        }
    }
    
}