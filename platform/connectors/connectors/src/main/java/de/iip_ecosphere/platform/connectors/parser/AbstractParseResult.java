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
import java.util.NoSuchElementException;

import de.iip_ecosphere.platform.connectors.parser.InputParser.ParseResult;

/**
 * Basic implementation for parse results with name-index mapping.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractParseResult implements ParseResult {

    private Map<String, Integer> mapping;

    /**
     * Creates an abstract parse result based on a given mapping.
     * 
     * @param mapping the name-index mapping (may be empty)
     */
    protected AbstractParseResult(Map<String, Integer> mapping) {
        this.mapping = mapping;
    }
    
    @Override
    public Object getData(String name) {
        Integer index = mapping.get(name);
        if (null != index) {
            try {
                return getData(index.intValue());
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException("Field name '" + name + "' mapped to " + index 
                    + ": " + e.getMessage());
            }
        } else {
            throw new NoSuchElementException("Field name '" + name + "' has no mapping");
        }
    }

    
}
