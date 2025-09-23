/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
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
 * A resource resolver for oktoflow resources.
 * 
 * @author Holger Eichelberger, SSE
 */
public class OktoflowResourceResolver implements ResourceResolver {
    
    private File base;
    private String folder;

    /**
     * Creates a resource resolver for the actual work directory.
     */
    public OktoflowResourceResolver() {
        this(new File(""), null);
    }

    /**
     * Creates resource resolver for the given {@code base} directory.
     * 
     * @param base the base directory
     */
    public OktoflowResourceResolver(File base) {
        this(base, null);
    }

    /**
     * Creates resource resolver for the given {@code base} directory.
     * 
     * @param base the base directory
     * @param folder specific folder to took in, e.g., "software"
     */
    public OktoflowResourceResolver(File base, String folder) {
        this.base = base;
        this.folder = folder;
    }
    
    @Override
    public String getName() {
        return "oktoflow resources";
    }
    
    @Override
    public InputStream resolve(ClassLoader loader, String resource) {
        InputStream result = resolve(loader, resource, new File(base, "resources.ipr"));
        if (null == result) {
            result = resolve(loader, resource, new File(base, "resources"));
        }
        return result;
    }

    /**
     * Resolves resource within the given {@code baseFolder}.
     * 
     * @param loader
     * @param resource
     * @param baseFolder
     * @return
     */
    private InputStream resolve(ClassLoader loader, String resource, File baseFolder) {
        InputStream result = null;
        if (baseFolder.isDirectory()) {
            File[] folders;
            if (null != this.folder) {
                folders = new File[] {new File(baseFolder, this.folder)};
            } else {
                folders = baseFolder.listFiles();
            }
            if (null != folders) {
                for (File fo: folders) {
                    if (fo.isDirectory()) {
                        File f = new File(fo, resource);
                        if (f.exists()) {
                            try {
                                result = new FileInputStream(f);
                                break;
                            } catch (IOException e) {
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

}
