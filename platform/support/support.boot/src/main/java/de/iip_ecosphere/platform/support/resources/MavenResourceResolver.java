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
 * A resource resolver for typical Maven paths. Looks for {@code src/main/resources} and {@code src/test/resources} in 
 * this sequence.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MavenResourceResolver implements ResourceResolver {
    
    private File base;

    /**
     * Creates a resource resolver for the actual work directory.
     */
    public MavenResourceResolver() {
        this(null);
    }
    
    /**
     * Creates resource resolver for the given {@code base} directory.
     * 
     * @param base the base directory (may be <b>null</b> for none)
     */
    public MavenResourceResolver(File base) {
        this.base = base;
    }
    
    @Override
    public String getName() {
        return "Maven resources";
    }
    
    /**
     * Creates a file depending on whether a {@link #base} is given.
     * 
     * @param child the child path
     * @return the file object
     */
    private File createFile(String child) {
        return base == null ? new File(child) : new File(base, child);
    }
    
    @Override
    public InputStream resolve(ClassLoader loader, String resource) {
        InputStream result = null;
        File f = createFile("src/main/resources/" + resource);
        if (f.exists()) {
            try {
                result = new FileInputStream(f);
            } catch (IOException e) {
            }
        } else {
            try {
                f = createFile("src/test/resources/" + resource);
                if (f.exists()) {
                    result = new FileInputStream(f);    
                }
            } catch (IOException e) {
            }
        }
        return result;
    }

}
