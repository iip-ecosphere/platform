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

/**
 * An array-based parse result.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ArrayParseResult extends AbstractParseResult {

    private String[] data;

    /**
     * Creates an array-based parse result.
     * 
     * @param data the parsed data
     * @param mapping the name-index mapping (may be empty)
     */
    protected ArrayParseResult(String[] data, Map<String, Integer> mapping) {
        super(mapping);
        this.data = data;
    }
    
    @Override
    public int getDataCount() {
        return data.length;
    }

    @Override
    public Object getData(int index) {
        return data[index];
    }

}
