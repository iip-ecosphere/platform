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
 * A file format just based on extensions. Although not nice, still many file formats are just identified by 
 * extensions, also as this rather easy to check.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ExtensionBasedFileFormat extends FileFormat {

    private String extension;
    
    /**
     * Creates an instance.
     * 
     * @param extension the file name extension to check for, without the "." (normalized out)
     * @param name the name of the file format
     * @param description a free-text description of the file format
     */
    public ExtensionBasedFileFormat(String extension, String name, String description) {
        super(name, description);
        this.extension = null == extension ? "" : extension;
        while (this.extension.startsWith(".")) {
            this.extension = this.extension.substring(1);
        }
    }

    /**
     * Returns the extension.
     * 
     * @return the extension (without leading ".")
     */
    public String getExtension() {
        return extension;
    }

    @Override
    public boolean matches(File file) {
        boolean result;
        if (extension.length() > 0) {
            result = file.getName().endsWith("." + extension);
        } else {
            result = true;
        }
        return result;
    }

    @Override
    public String toString() {
        String result = getName();
        String desc = getExtension();
        if (desc.length() > 0) {
            desc = "*." + desc;
        }
        if (getDescription().length() > 0) {
            if (desc.length() > 0) {
                desc = desc + ", ";
            }
            desc += getDescription();
        }
        if (desc.length() > 0) {
            result += " (" + desc + ")";
        }
        return result;
    }

}
