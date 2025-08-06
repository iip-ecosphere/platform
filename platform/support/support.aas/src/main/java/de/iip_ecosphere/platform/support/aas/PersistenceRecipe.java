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

package de.iip_ecosphere.platform.support.aas;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import de.iip_ecosphere.platform.support.FileFormat;
import de.iip_ecosphere.platform.support.FileUtils;

/**
 * A receipe to read/write AAS from/to files.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface PersistenceRecipe {
    
    /**
     * Represents a packageable resource.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class FileResource {
        
        private byte[] fileContent;
        private String path;

        /**
         * Creates a file resource.
         * 
         * @param file the file to be turned into a resource
         * @param path the local (target) path
         * @throws IOException if {@code file} cannot be read
         */
        public FileResource(File file, String path) throws IOException {
            this(FileUtils.readFileToByteArray(file), path);
        }

        /**
         * Creates a file resource.
         * 
         * @param fileContent the file content to be used as a resource
         * @param path the local (target) path
         */
        public FileResource(byte[] fileContent, String path) {
            this.fileContent = fileContent;
            this.path = path;
        }

        /**
         * Returns the file content.
         * 
         * @return the file content
         */
        public byte[] getFileContent() {
            return fileContent;
        }

        /**
         * Returns the relative path.
         * 
         * @return the relative path
         */
        public String getPath() {
            return path;
        }
        
    }

    /**
     * Returns the supported file formats.
     * 
     * @return the supported file formats
     */
    public Collection<FileFormat> getSupportedFormats();

    /**
     * Writes the given AAS to {@code file}.
     * 
     * @param aas the AAS to write
     * @param file the file to write to
     * @throws IOException in case of I/O problems
     * @throws IllegalArgumentException if {@code file} represents an unknown format, see {@link #getSupportedFormats()}
     */
    public default void writeTo(List<Aas> aas, File file) throws IOException {
        writeTo(aas, null, null, file);
    }
    
    /**
     * Writes the given AAS to {@code file}.
     * 
     * @param aas the AAS to write
     * @param file the file to write to
     * @param thumbnail optional file to a PNG/JPG/JPEG thumbnail
     * @param resources file resources to be stored in the AAS, resolving the file elements, may be <b>null</b> for none
     * @throws IOException in case of I/O problems
     * @throws IllegalArgumentException if {@code file} represents an unknown format, see {@link #getSupportedFormats()}
     */
    public void writeTo(List<Aas> aas, File thumbnail, List<FileResource> resources, File file) throws IOException;
    
    /**
     * Reads AAS from the given {@code file}.
     * 
     * @param file the file to read from
     * @return the read AAS
     * @throws IOException in case of I/O problems
     * @throws IllegalArgumentException if {@code file} represents an unknown format, see {@link #getSupportedFormats()}
     */
    public List<Aas> readFrom(File file) throws IOException;

}
