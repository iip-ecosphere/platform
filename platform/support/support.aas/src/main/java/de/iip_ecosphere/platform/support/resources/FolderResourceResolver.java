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

/**
 * A simple resource resolver that takes a given folder into account.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FolderResourceResolver implements ResourceResolver {
    
    private File basePath;

    /**
     * Creates a resolver.
     * 
     * @param basePath the base path to be taken into account
     */
    public FolderResourceResolver(String basePath) {
        this.basePath = new File(basePath);
    }

    /**
     * Creates a resolver.
     * 
     * @param basePath the base path to be taken into account
     */
    public FolderResourceResolver(File basePath) {
        this.basePath = basePath;
    }

    @Override
    public InputStream resolve(ClassLoader loader, String resource) {
        try {
            return new FileInputStream(new File(basePath, resource));
        } catch (IOException e) {
            return null;
        }
    }
    
}