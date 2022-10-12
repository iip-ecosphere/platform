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
import java.util.Arrays;

import org.slf4j.LoggerFactory;

import com.jsoniter.JsonIterator;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.jsoniter.any.Any.EntryIterator;
import com.jsoniter.spi.JsonException;

import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;
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

    private static final Class<?> LAZY_ANY_CLS;
    private static final Field LAZY_ANY_HEAD_FIELD;
    private static final Field LAZY_ANY_TAIL_FIELD;
    
    static {
        Class<?> cls = JsonInputConverter.class; // a class that is not instance of LazyAny
        Field hf = null;
        Field tf = null;
        try {
            cls = Class.forName("com.jsoniter.any.LazyAny");
            hf = cls.getDeclaredField("head");
            hf.setAccessible(true);
            tf = cls.getDeclaredField("tail");
            tf.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            LoggerFactory.getLogger(JsonInputParser.class).error("Cannot find LazyAny class or its fields: " 
                + e.getMessage() + " Disabling JSON stepIn-slicing.");
        }
        if (tf == null) {
            cls = JsonInputConverter.class; // a class that is not instance of LazyAny
            tf = null;
        }
        LAZY_ANY_CLS = cls;
        LAZY_ANY_HEAD_FIELD = hf;
        LAZY_ANY_TAIL_FIELD = tf;
    }
    
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
            if (any.size() == 1 && indexes.length == 1) {
                result = any.asMap().keySet().iterator().next().toString(); // :(
                if (null != valueCons) {
                    valueCons.accept(any.get(result));
                }
            } else {
                EntryIterator it = findBy(indexes);
                if (null != it) {
                    result = it.key();
                    if (null != valueCons) {
                        valueCons.accept(it.value());
                    }
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
                // ensure (lazy) iterator :( // LazyIterator#parse ??
                Any tmp = Any.lazyObject(data, 0, data.length - 1);
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
            // ensure (lazy) iterator :( // LazyIterator#parse ??
            Any tmp = Any.lazyObject(data, 0, data.length - 1);
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
        public Any getData(String name, int... indexes) throws IOException {
            Any result = get(name, indexes);
            if (result != null) {
                return result;
            } else {
                throw new IOException("No entry found for " + name + " / " + Arrays.toString(indexes));
            }
        }

        /**
         * Returns the JSON object representing {@code name} or {@code indexes}.
         * 
         * @param name the name of the data field, may contain hierarchical names separated by 
         *     {@link InputParser#SEPARATOR}, may be based on the scope set by {@link #stepInto(String, int)}
         * @param indexes the path of (nested) 0-based indexes to the field, the sum must be less than 
         *     {@link #getDataCount()}
         * @return the JSON object, may be <b>null</b> for not found
         */
        private Any get(String name, int... indexes) {
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
            if (obj.valueType() == ValueType.INVALID && indexes.length > 0) { // fallback
                EntryIterator it = findBy(indexes);
                if (null != it) {
                    result = it.value();
                }
            } else if (obj.valueType() != ValueType.INVALID && obj != any) {
                result = obj;
            } 
            return result;
        }
        
        @Override
        public void getData(IOConsumer<Any> ifPresent, String name, int... indexes) throws IOException {
            Any result = get(name, indexes);
            if (null != result) {
                ifPresent.accept(result);
            }
        }

        /**
         * Returns the JSON object representing {@code name} or {@code indexes} without hierarchical name 
         * interpretation.
         * 
         * @param name the name of the data field
         * @param indexes the path of (nested) 0-based indexes to the field, the sum must be less than 
         *     {@link #getDataCount()}
         * @return the JSON object, may be <b>null</b> for not found
         */
        private Any getLocal(String name, int[] indexes) {
            Any result = null;
            if (any.keys().contains(name)) { // seems to be faster than direct access
                result = any.get(name);    
            } else {
                if (indexes.length > 0) {
                    EntryIterator it = findBy(indexes);
                    if (null != it) {
                        result = it.value();
                    }
                }
            }
            return result;
        }

        @Override
        public Any getLocalData(String name, int... indexes) throws IOException {
            Any result = getLocal(name, indexes);
            if (null == result) {
                throw new IOException("No entry found for " + name + " / " + Arrays.toString(indexes));
            }
            return result;
        }

        @Override
        public void getLocalData(IOConsumer<Any> ifPresent, String name, int... indexes) throws IOException {
            Any result = getLocal(name, indexes);
            if (null != result) {
                ifPresent.accept(result);
            }
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
                if (LAZY_ANY_CLS.isInstance(nested)) { // LazyIterator#parse -> JsonIterator may help but not available
                    try { // no direct access but we need the correct slice of the underlying input data for indexes
                        int head = (int) LAZY_ANY_HEAD_FIELD.get(nested);
                        int tail = (int) LAZY_ANY_TAIL_FIELD.get(nested);
                        actData = new byte[tail - head]; // exclude tail
                        System.arraycopy(topData, head, actData, 0, actData.length);
                        if (deserialized) {
                            String tmp = JsonUtils.unescape(new String(actData));
                            actData = tmp.getBytes();
                        }
                    } catch (IllegalAccessException e) {
                        throw new IOException("Cannot determine head/tail to slice input data: " + e.getMessage());
                    }
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
        public byte toByte(Any data) throws IOException {
            try {
                return (byte) data.toInt();
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
        public short toShort(Any data) throws IOException {
            try {
                return (short) data.toInt();
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
        public byte[] toByteArray(Any data) throws IOException {
            byte[] dta = new byte[data.size()];
            for (int j = 0; j < dta.length; j++) {
                dta[j] = (byte) data.get(j).toInt();
            }
            return dta; // exception?
        }

        @Override
        public Object toObject(Any data) throws IOException {
            return null; // preliminary
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
