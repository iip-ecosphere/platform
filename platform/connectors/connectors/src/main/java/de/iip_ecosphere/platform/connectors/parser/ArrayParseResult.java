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

import de.iip_ecosphere.platform.connectors.parser.InputParser.ParseResult;
import de.iip_ecosphere.platform.support.function.IOConsumer;

/**
 * An array-based parse result for data parsed to strings.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ArrayParseResult implements ParseResult<String> {

    private String[] data;
    private ArrayParseResult parent;
    private int baseIndex;

    /**
     * Creates an array-based parse result.
     * 
     * @param data the parsed data
     */
    protected ArrayParseResult(String[] data) {
        this(data, 0, null);
    }

    /**
     * Creates an array-based parse result.
     * 
     * @param data the parsed data
     * @param baseIndex the base index set as context root, {code 0} for top-level
     * @param parent the parent result representing the context where a {@code #stepInto(String, int)} happened, 
     *     <b>null</b> for the top context
     */
    protected ArrayParseResult(String[] data, int baseIndex, ArrayParseResult parent) {
        this.data = data;
        this.baseIndex = baseIndex;
        this.parent = parent;
    }

    @Override
    public int getDataCount() {
        return data.length;
    }

    @Override
    public String getFieldName(IOConsumer<String> valueCons, int... index) {
        return "";
    }

    @Override
    public String getData(String name, int... indexes) throws IOException {
        return getLocalData(name, indexes); // anyway no name interpretation here
    }
    
    @Override
    public String getLocalData(String name, int... indexes) throws IOException {
        int index = getIndex(indexes);
        if (index >= 0 && index < data.length) { // prevent exception, call consumer
            return data[index];
        } else {
            throw new IOException();
        }
    }
    
    /**
     * Returns the one-dimensional array index into the data.
     * 
     * @param indexes the path of (nested) 0-based indexes to the field, the sum must be less than 
     *     {@link #getDataCount()}
     * @return the 0-based index
     */
    private int getIndex(int[] indexes) {
        int index;
        if (indexes.length == 1) {
            index = baseIndex + indexes[0];
        } else {
            index = baseIndex;
            for (int i = indexes.length - 1; i >= 0; i--) {
                index += indexes[i];
            }
        }
        return index;
    }

    @Override
    public void getLocalData(IOConsumer<String> ifPresent, String name, int... indexes) throws IOException {
        int index = getIndex(indexes);
        if (index >= 0 && index < data.length) { // prevent exception, call consumer
            ifPresent.accept(data[index]);
        }
    }
    
    @Override
    public void getData(IOConsumer<String> ifPresent, String name, int... indexes) throws IOException {
        getLocalData(ifPresent, name, indexes); // anyway no name interpretation here
    }

    @Override
    public ArrayParseResult stepInto(String name, int index) {
        return new ArrayParseResult(data, index, this);
    }

    @Override
    public ArrayParseResult stepOut() {
        return parent;
    }
    
    /**
     * Returns the data object.
     * 
     * @return the data object
     */
    protected String[] getData() {
        return data;
    }

    @Override
    public int getArraySize() {
        return -1; // not supported?
    }

}
