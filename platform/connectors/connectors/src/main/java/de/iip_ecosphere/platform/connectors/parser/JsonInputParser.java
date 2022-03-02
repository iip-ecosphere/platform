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

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import com.jsoniter.any.Any.EntryIterator;

/**
 * Implements the default input parser for JSON data.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JsonInputParser implements InputParser<Any> {
    
    private static final JsonInputConverter CONVERTER = new JsonInputConverter();

    /**
     * Defines a parse result instance for JSON.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class JsonParseResult extends AbstractParseResult<Any> {

        private Any any;

        /**
         * Creates a parse result instance.
         * 
         * @param any the parsed intermediary data
         */
        private JsonParseResult(Any any) {
            this.any = any;
        }
        
        @Override
        public int getDataCount() {
            return any.size();
        }

        @Override
        protected Any getData(int index) {
            return any.get(index);
        }
        
        @Override
        public Any getData(String name, int index, Map<String, Integer> mapping) {
            Any obj = any;
            int start = 0;
            int end = 0;
            do {
                end = name.indexOf(SEPARATOR, start);
                if (end > 0) {
                    obj = obj.get(name.substring(start, end));
                    start = end + 1;
                } else {
                    if (0 == start) {
                        obj = obj.get(name);
                    } else {
                        obj = obj.get(name.substring(start, name.length()));
                    }
                }
            } while (end > 0);
            return obj;
        }

        @Override
        public String getFieldName(int... index) {
            String result = "";
            if (index.length > 0) {
                Any tmp = any;
                for (int i = 0; i < index.length; i++) {
                    tmp = JsonIterator.deserialize(tmp.toString()); // ensure (lazy) iterator :(
                    int pos = index[i];
                    EntryIterator it = tmp.entries();
                    while (it.next() && pos >= 0) {
                        if (pos == 0) {
                            if (i < index.length - 1) {
                                tmp = it.value();
                            } else {
                                result = it.key();        
                            }
                        }
                        pos--;
                    }                    
                }
            }
            return result;
        }
        
    }
    
    /**
     * Implements a JSON input converted.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class JsonInputConverter implements InputConverter<Any> {

        @Override
        public int toInteger(Any data) throws IOException {
            return data.toInt();
        }

        @Override
        public long toLong(Any data) throws IOException {
            return data.toLong();
        }

        @Override
        public String toString(Any data) throws IOException {
            return data.toString();
        }

        @Override
        public double toDouble(Any data) throws IOException {
            return data.toDouble();
        }

        @Override
        public double toFloat(Any data) throws IOException {
            return data.toFloat();
        }

        @Override
        public boolean toBoolean(Any data) throws IOException {
            return data.toBoolean();
        }

        @Override
        public int[] toIntegerArray(Any data) throws IOException {
            int[] dta = new int[data.size()];
            for (int j = 0; j < dta.length; j++) {
                dta[j] = data.get(j).toInt();
            }
            return dta;
        }

        @Override
        public double[] toDoubleArray(Any data) throws IOException {
            // Preliminary, performance?
            double[] dta = new double[data.size()]; 
            for (int j = 0; j < dta.length; j++) {
                dta[j] = data.get(j).toDouble();
            }
            return dta;
        }

        @Override
        public Object toObject(Any data) throws IOException {
            return null; // preliminary
        }
        
    }
    
    @Override
    public ParseResult<Any> parse(byte[] data) throws IOException {
        return new JsonParseResult(JsonIterator.deserialize(data));
    }

    @Override
    public InputConverter<Any> getConverter() {
        return CONVERTER;
    }

}
