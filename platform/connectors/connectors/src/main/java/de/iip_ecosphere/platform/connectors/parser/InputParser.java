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

/**
 * Interfaces for generic named/indexed input parsers.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface InputParser {
    
    /**
     * Result of parsing data.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface ParseResult {
        
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
        public Object getData(int index);
        
        /**
         * Returns the value of the data field for the given field name.
         * 
         * @param name the name of the data field
         * @return the data value
         * @throws NoSuchFieldException if there is no such field
         */
        public Object getData(String name);
        
    }
    
    /**
     * Parses a chunk of data received from a source.
     * 
     * @param data the data
     * @return parsing result
     * @throws IOException if parsing fails for some reasons
     */
    public ParseResult parse(byte[] data) throws IOException;

}
