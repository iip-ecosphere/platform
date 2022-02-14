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

package de.iip_ecosphere.platform.connectors.formatter;

import java.io.IOException;

/**
 * Generic output formatter. You may add information to one chunk until {@link #chunkCompleted()} is called.
 * Custom implementations must have a constructor with a single String argument, the character encoding name.
 * 
 * @param <T> the output format type
 * @author Holger Eichelberger, SSE
 */
public interface OutputFormatter<T> {
    
    /**
     * Converts primitive types to the output format. Must be stateless.
     * 
     * @param <T> the output format type
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface OutputConverter<T> {
        
        /**
         * Converts data from int to the output format.
         * 
         * @param data the data
         * @return the converted output format
         * @throws IOException if conversion fails
         */
        public T fromInt(int data) throws IOException;

        /**
         * Converts data from int to the output format.
         * 
         * @param data the data
         * @return the converted output format
         * @throws IOException if conversion fails
         */
        public T fromInteger(int data) throws IOException;

        /**
         * Converts data from long to the output format.
         * 
         * @param data the data
         * @return the converted output format
         * @throws IOException if conversion fails
         */
        public T fromLong(long data) throws IOException;

        /**
         * Converts data from String to the output format.
         * 
         * @param data the data
         * @return the converted output format
         * @throws IOException if conversion fails
         */
        public T fromString(String data) throws IOException;

        /**
         * Converts data from double to the output format.
         * 
         * @param data the data
         * @return the converted output format
         * @throws IOException if conversion fails
         */
        public T fromDouble(double data) throws IOException;

        /**
         * Converts data from float to the output format.
         * 
         * @param data the data
         * @return the converted output format
         * @throws IOException if conversion fails
         */
        public T fromFloat(float data) throws IOException;

        /**
         * Converts data from Boolean to the output format.
         * 
         * @param data the data
         * @return the converted output format
         * @throws IOException if conversion fails
         */
        public T fromBoolean(boolean data) throws IOException;
    }

    /**
     * Adds information to one chunk of output.
     * 
     * @param name optional data name field (may be <b>null</b> for none)
     * @param data the data to be added
     * @throws IOException if adding the data fails for some reason
     */
    public void add(String name, T data) throws IOException;

    /**
     * Completes a chunk of output data.
     * 
     * @return the chunk
     * @throws IOException if creating the chunk fails for some reason
     */
    public byte[] chunkCompleted() throws IOException;
    
    /**
     * Returns the output converter.
     * 
     * @return the output converter
     */
    public OutputConverter<T> getConverter();
    
}
