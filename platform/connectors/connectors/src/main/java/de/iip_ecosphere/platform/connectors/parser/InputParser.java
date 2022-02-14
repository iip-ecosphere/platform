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

package de.iip_ecosphere.platform.connectors.parser;

import java.io.IOException;
import java.util.Map;

/**
 * Interfaces for generic named/indexed input parsers. Custom implementations must have a constructor with a single 
 * String argument, the character encoding name.
 * 
 * @param <T> the type of data produced by parsing
 * @author Holger Eichelberger, SSE
 */
public interface InputParser<T> {
    
    /**
     * Result of parsing data.
     * 
     * @param <T> the type of data produced by parsing
     * @author Holger Eichelberger, SSE
     */
    public interface ParseResult<T> {
        
        /**
         * Returns the number of parsed data fields.
         * 
         * @return the number of data fields (non-negative)
         */
        public int getDataCount();
        
        /**
         * Returns the value of the data field at position {@code index}.
         * 
         * @param index the 0-based position of the data field
         * @return the data value
         * @throws IndexOutOfBoundsException if {@code index}&lt;0 || index &gt;= {@link #getDataCount()}
         */
        public T getData(int index);
        
        /**
         * Returns the value of the data field for the given field {@code name} from {@code mapping} or with 
         * via the given {@code index}.
         * 
         * @param name the name of the data field
         * @param index the 0-based position of the data field
         * @param mapping the name-index mapping (may be empty for none)
         * @return the data value
         * @throws IndexOutOfBoundsException if the mapped index or the given 
         *     {@code index}&lt;0 || index &gt;= {@link #getDataCount()}
         */
        public default T getData(String name, int index, Map<String, Integer> mapping) {
            Integer idx = mapping.get(name);
            if (null != idx) {
                return getData(idx.intValue());
            } else {
                return getData(index);
            }
        }
        
    }
    
    /**
     * Converts parsed data to primitive types. Implementations must be stateless.
     * 
     * @param <T> the type of data produced by parsing
     * @author Holger Eichelberger, SSE
     */
    public interface InputConverter<T> {

        /**
         * Converts parsed data returned by {@link ParseResult} to integer.
         * 
         * @param data the obtained data
         * @return the converted integer
         * @throws IOException if conversion fails
         */
        public int toInt(T data) throws IOException;

        /**
         * Converts parsed data returned by {@link ParseResult} to long.
         * 
         * @param data the obtained data
         * @return the converted long
         * @throws IOException if conversion fails
         */
        public long toLong(T data) throws IOException;

        /**
         * Converts parsed data returned by {@link ParseResult} to String.
         * 
         * @param data the obtained data
         * @return the converted String
         * @throws IOException if conversion fails
         */
        public String toString(T data) throws IOException;

        /**
         * Converts parsed data returned by {@link ParseResult} to double.
         * 
         * @param data the obtained data
         * @return the converted double
         * @throws IOException if conversion fails
         */
        public double toDouble(T data) throws IOException;

        /**
         * Converts parsed data returned by {@link ParseResult} to float.
         * 
         * @param data the obtained data
         * @return the converted float
         * @throws IOException if conversion fails
         */
        public double toFloat(T data) throws IOException;

        /**
         * Converts parsed data returned by {@link ParseResult} to Boolean.
         * 
         * @param data the obtained data
         * @return the converted Boolean
         * @throws IOException if conversion fails
         */
        public boolean toBoolean(T data) throws IOException;

    }
    
    /**
     * Parses a chunk of data received from a source.
     * 
     * @param data the data
     * @return parsing result
     * @throws IOException if parsing fails for some reasons
     */
    public ParseResult<T> parse(byte[] data) throws IOException;

    /**
     * Returns a type converter for parsed data.
     * 
     * @return the type converter
     */
    public InputConverter<T> getConverter();
    
}
