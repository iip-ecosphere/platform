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
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import com.jsoniter.JsonIterator;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.jsoniter.any.Any.EntryIterator;
import com.jsoniter.spi.JsonException;

import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;
import de.iip_ecosphere.platform.connectors.formatter.FormatCache;
import de.iip_ecosphere.platform.support.function.IOConsumer;

/**
 * Implements the default input parser for JSON data. Name-based access shall be rather fast, however, 
 * index-based access is currently a limited compromise.
 * 
 * <b>Warning:</b> This implementation is not stable and may change during performance optimization.
 * 
 * @author Holger Eichelberger, SSE
 */
@MachineParser
public final class JsonInputParser implements InputParser<Any> {
    
    private static final JsonInputConverter CONVERTER = new JsonInputConverter();

    /**
     * Defines a parse result instance for JSON.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static final class JsonParseResult implements ParseResult<Any> {

        private Any any;
        private byte[] data;
        private JsonParseResult parent;

        /**
         * Creates a parse result instance.
         * 
         * @param data the data to parse/deserialize
         */
        private JsonParseResult(byte[] data) {
            this(data, JsonIterator.deserialize(data), null);
        }

        /**
         * Creates a parse result instance.
         * 
         * @param data the data to parse/deserialize
         * @param any the jsoniter object representing the context
         * @param parent the parent parse result to jump back to in {@link #stepOut()}.
         */
        private JsonParseResult(byte[] data, Any any, JsonParseResult parent) {
            this.any = any;
            this.data = data;
            this.parent = parent;
        }

        @Override
        public int getDataCount() {
            return any.size();
        }

        @Override
        public String getFieldName(IOConsumer<Any> valueCons, int... indexes) throws IOException {
            String result = "";
            EntryIterator it = findBy(indexes);
            if (null != it) {
                result = it.key();
                if (null != valueCons) {
                    valueCons.accept(it.value());
                }
            }
            return result;
        }
        
        /**
         * Returns an entry iterator to the element denoted by (nested) {@code indexes}.
         * 
         * @param indexes the path of (nested) 0-based indexes to the field, the sum must be less than 
         *     {@link #getDataCount()}
         * @return the input iterator, <b>null</b> for not found
         */
        private EntryIterator findBy(int[] indexes) {
            EntryIterator result = null;
            if (indexes.length > 0) {
                Any tmp = JsonIterator.deserialize(data); // ensure (lazy) iterator :(
                for (int i = 0; i < indexes.length; i++) {
                    int pos = indexes[i];
                    EntryIterator it = tmp.entries();
                    while (pos >= 0 && it.next()) {
                        if (pos == 0) {
                            if (i < indexes.length - 1) {
                                tmp = it.value();
                                if (tmp.valueType() == ValueType.STRING) { // index assumes an object, try to parse
                                    tmp = JsonIterator.deserialize(tmp.toString());
                                }
                            } else {
                                result = it;
                            }
                        }
                        pos--;
                    }                    
                }
            }
            return result;
        }
        
        /**
         * Searches for the entry at position {@code index}.
         * 
         * @param index the index to search for
         * @return the entry iterator pointing to the index or <b>null</b> for none
         */
        private EntryIterator findBy(int index) {
            EntryIterator result = null;
            Any tmp = JsonIterator.deserialize(data); // ensure (lazy) iterator :(
            EntryIterator it = tmp.entries();
            while (index >= 0 && it.next()) {
                if (index == 0) {
                    result = it;
                }
                index--;
            }                    
            return result;
        }

        @Override
        public Any getData(String name, int... indexes) {
            Any result = null;
            Any obj = any;
            int start = 0;
            int end = 0;
            do {
                if (obj.valueType() == ValueType.STRING) { // index assumes an object, try to parse
                    obj = JsonIterator.deserialize(obj.toString());
                }
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
            if (obj.valueType() == ValueType.INVALID) { // fallback
                EntryIterator it = findBy(indexes);
                if (null != it) {
                    result = it.value();
                } else {
                    throw new IndexOutOfBoundsException("No entry found for " + Arrays.toString(indexes));
                }
            } else if (obj != any) {
                result = obj;
            } else {
                throw new IndexOutOfBoundsException("No entry found for " + name);
            }
            return result;
        }

        @Override
        public JsonParseResult stepInto(String name, int index) throws IOException {
            Any nested = any.get(name);
            boolean deserialized = false;
            if (nested.valueType() == ValueType.INVALID) { // fallback
                EntryIterator it = findBy(index);
                if (null != it) {
                    nested = it.value();
                } else {
                    throw new IndexOutOfBoundsException("No entry found for " + index);
                }
            }
            if (nested.valueType() != ValueType.INVALID) { // fallback
                if (nested.valueType() == ValueType.STRING) { // step into assumes an object, try to parse
                    nested = JsonIterator.deserialize(nested.toString());
                    deserialized = true;
                }
                byte[] topData = getTopData();
                byte[] actData = topData; // jsoniter parsing happens on the same array
                try { // no direct access but we need the correct slice of the underlying input data for indexes
                    Class<?> cls = Class.forName("com.jsoniter.any.LazyAny");
                    if (cls.isInstance(nested)) {
                        Field headField = cls.getDeclaredField("head");
                        headField.setAccessible(true);
                        Field tailField = cls.getDeclaredField("tail");
                        tailField.setAccessible(true);
                        int head = (int) headField.get(nested);
                        int tail = (int) tailField.get(nested);
                        actData = new byte[tail - head]; // exclude tail
                        System.arraycopy(topData, head, actData, 0, actData.length);
                        if (deserialized) {
                            String tmp = JsonUtils.unescape(new String(actData));
                            actData = tmp.getBytes();
                        }
                    }
                } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                    throw new IOException("Cannot determine head/tail to slice input data: " + e.getMessage());
                }
                return new JsonParseResult(actData, nested, this);
            } else {
                throw new IOException("Cannot determine element for " + name + " index: " + index);
            }
        }

        /**
         * Returns the original input data array from the top-level parser result. 
         * @return the data
         */
        private byte[] getTopData() {
            JsonParseResult ptr = this;
            while (ptr.parent != null) {
                ptr = ptr.parent;
            }
            return ptr.data;
        }

        @Override
        public JsonParseResult stepOut() {
            return parent;
        }
        
    }
    
    /**
     * Implements a JSON input converted.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static final class JsonInputConverter implements InputConverter<Any> {

        @Override
        public int toInteger(Any data) throws IOException {
            try {
                return data.toInt();
            } catch (JsonException e) { // wrong format, we cannot read that
                throw new IOException(e);
            }
        }

        @Override
        public long toLong(Any data) throws IOException {
            try {
                return data.toLong();
            } catch (JsonException e) { // wrong format, we cannot read that
                throw new IOException(e);
            }
        }

        @Override
        public String toString(Any data) throws IOException {
            try {
                return data.toString();
            } catch (JsonException e) { // wrong format, we cannot read that
                throw new IOException(e);
            }
        }

        @Override
        public double toDouble(Any data) throws IOException {
            try {
                return data.toDouble();
            } catch (JsonException e) { // wrong format, we cannot read that
                throw new IOException(e);
            }
        }

        @Override
        public float toFloat(Any data) throws IOException {
            try {
                return data.toFloat();
            } catch (JsonException e) { // wrong format, we cannot read that
                throw new IOException(e);
            }
        }

        @Override
        public boolean toBoolean(Any data) throws IOException {
            try {
                return data.toBoolean();
            } catch (JsonException e) { // wrong format, we cannot read that
                throw new IOException(e);
            }
        }

        @Override
        public int[] toIntegerArray(Any data) throws IOException {
            int[] dta = new int[data.size()];
            for (int j = 0; j < dta.length; j++) {
                dta[j] = data.get(j).toInt();
            }
            return dta; // exception?
        }

        @Override
        public double[] toDoubleArray(Any data) throws IOException {
            double[] dta = new double[data.size()]; 
            for (int j = 0; j < dta.length; j++) {
                dta[j] = Double.parseDouble(data.get(j).toString());
            }
            return dta; // exception?
        }

        @Override
        public Object toObject(Any data) throws IOException {
            return null; // preliminary
        }
        
        @Override
        public Date toDate(Any data, String format) throws IOException {
            SimpleDateFormat f = FormatCache.getDateFormatter(format);
            try {
                return f.parse(data.toString());
            } catch (ParseException e) {
                throw new IOException(e);
            }
        }
        
    }
    
    @Override
    public JsonParseResult parse(byte[] data) throws IOException {
        return new JsonParseResult(data);
    }

    @Override
    public JsonInputConverter getConverter() {
        return CONVERTER;
    }

}
