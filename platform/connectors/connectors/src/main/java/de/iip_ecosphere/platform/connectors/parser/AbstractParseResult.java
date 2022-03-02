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

import java.util.Map;

import de.iip_ecosphere.platform.connectors.parser.InputParser.ParseResult;

/**
 * Basic implementation of {@link ParseResult}.
 * 
 * @param <T> the type of data produced by parsing
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractParseResult<T> implements ParseResult<T> {

    /**
     * Returns the value of the data field at position {@code index}.
     * 
     * @param index the 0-based position of the data field
     * @return the data value
     * @throws IndexOutOfBoundsException if {@code index}&lt;0 || index &gt;= {@link #getDataCount()}
     */
    protected abstract T getData(int index);
    
    /**
     * Returns the value of the data field for the given field {@code name} from {@code mapping} or with 
     * via the given {@code index}. May be overridden if direct access to names is provided by the parsed
     * structure, e.g., in JSON.
     * 
     * @param name the name of the data field, may contain hierarchical names separated by 
     *     {@link InputParser#SEPARATOR}
     * @param index the 0-based position of the data field
     * @param mapping the name-index mapping (may be empty or <b>null</b> for none, then fallback to 
     *     index-based access)
     * @return the data value
     * @throws IndexOutOfBoundsException if the mapped index or the given 
     *     {@code index}&lt;0 || index &gt;= {@link #getDataCount()}
     */
    @Override
    public T getData(String name, int index, Map<String, Integer> mapping) {
        Integer idx = null == mapping ? null : mapping.get(name);
        if (null != idx) {
            return getData(idx.intValue());
        } else {
            return getData(index);
        }
    }

}