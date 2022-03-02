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

import de.iip_ecosphere.platform.connectors.parser.InputParser.ParseResult;

/**
 * An array-based parse result for data parsed to strings.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ArrayParseResult implements ParseResult<String> {

    private String[] data;

    /**
     * Creates an array-based parse result.
     * 
     * @param data the parsed data
     */
    protected ArrayParseResult(String[] data) {
        this.data = data;
    }
    
    @Override
    public int getDataCount() {
        return data.length;
    }

    @Override
    public String getFieldName(int... index) {
        return "";
    }

    @Override
    public String getData(String name, int... indexes) {
        int index;
        if (indexes.length == 1) {
            index = indexes[0];
        } else {
            index = 0;
            for (int i = indexes.length - 1; i >= 0; i--) {
                index += indexes[i];
            }
        }
        return data[index];
    }

}
