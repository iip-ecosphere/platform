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

/**
 * An array-based parse result for data parsed to strings.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ArrayParseResult extends AbstractParseResult<String> {

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
    public String getData(int index) {
        return data[index];
    }

}
