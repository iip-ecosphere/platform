/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support;

import java.io.File;

/**
 * Represents a file format, e.g., to indicate which formats are supported by an importer/exporter. 
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class FileFormat {
    
    private String name;
    private String description;
    
    /**
     * Creates an instance.
     * 
     * @param name the name of the file format
     * @param description a free-text description of the file format (may be <b>null</b>, turned into an empty string 
     *     then)
     * @throws IllegalArgumentException if {@code name} is not given
     */
    public FileFormat(String name, String description) {
        if (null == name || name.length() == 0) {
            throw new IllegalArgumentException("name must be given");
        }
        this.name = name;
        this.description = description == null ? "" : description;
    }

    /**
     * Returns the name of the file format.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the free-text description of the file format.
     * 
     * @return the free-text description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Returns whether the given file matches the file format. This may be based on an anlysis of magic numbers,
     * the file extension, etc.
     * 
     * @param file the file to check
     * @return {@code true} if the given {@code file} matches this file format, {@code false} else
     */
    public abstract boolean matches(File file);

    @Override
    public String toString() {
        String result = name;
        if (description.length() > 0) {
            result += " (" + description + ")";
        }
        return result;
    }

}
