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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.iip_ecosphere.platform.support.json.Json;
import de.iip_ecosphere.platform.support.json.JsonIterator;
import de.iip_ecosphere.platform.support.json.JsonIterator.EntryIterator;
import de.iip_ecosphere.platform.support.json.JsonIterator.ValueType;

import de.iip_ecosphere.platform.transport.serialization.QualifiedElement;
import de.iip_ecosphere.platform.transport.serialization.QualifiedElementFactory;
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
public final class JsonInputParser implements InputParser<JsonIterator> {
    
    private static final JsonInputConverter CONVERTER = new JsonInputConverter();
    
    /**
     * Emulates a one-element entry "iterator".
     * 
     * @author Holger Eichelberger, SSE
     */
    private static final class OneElementEntryIterator implements EntryIterator {

        private String key;
        private JsonIterator value;
        
        /**
         * Creates the instance.
         * 
         * @param key the key of the only element
         * @param value the value of the only element
         */
        private OneElementEntryIterator(String key, JsonIterator value) {
            this.key = key;
            this.value = value;
        }
        
        @Override
        public boolean next() {
            return false;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public JsonIterator value() {
            return value;
        }
        
    }
    
    /**
     * Defines a parse result instance for JSON.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static final class JsonParseResult implements ParseResult<JsonIterator> {

        private JsonIterator any;
        private byte[] data;
        private JsonParseResult parent;

        /**
         * Creates a parse result instance.
         * 
         * @param data the data to parse/deserialize
         */
        private JsonParseResult(byte[] data) {
            this(data, Json.parse(data), null);
        }

        /**
         * Creates a parse result instance.
         * 
         * @param data the data to parse/deserialize
         * @param any the jsoniter object representing the context
         * @param parent the parent parse result to jump back to in {@link #stepOut()}.
         */
        private JsonParseResult(byte[] data, JsonIterator any, JsonParseResult parent) {
            this.any = any;
            this.data = data;
            this.parent = parent;
        }

        @Override
        public int getDataCount() {
            return any.size();
        }

        @Override
        public String getFieldName(IOConsumer<JsonIterator> valueCons, int... indexes) throws IOException {
            String result = "";
            if (any.size() == 1 && indexes.length == 1) {
                result = any.getAnyKey();
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
                JsonIterator tmp = Json.parse(data);
                for (int i = 0; i < indexes.length; i++) {
                    int pos = indexes[i];
                    if (tmp.valueType() == ValueType.ARRAY) {
                        result = new OneElementEntryIterator("", tmp.get(pos));
                    } else {
                        EntryIterator it = tmp.entries();
                        while (pos >= 0 && it.next()) {
                            if (pos == 0) {
                                if (i < indexes.length - 1) {
                                    tmp = deserializeIfString(it.value());
                                } else {
                                    result = it;
                                }
                            }
                            pos--;
                        }
                    }
                }
            }
            return result;
        }
        
        /**
         * Deserializes {@code tmp} if it looks like a string.
         * 
         * @param tmp the any to be considered
         * @return {@code tmp} or deserialized any
         */
        private JsonIterator deserializeIfString(JsonIterator tmp) {
            if (tmp.valueType() == ValueType.STRING) { // index assumes an object, try to parse
                tmp = Json.parse(tmp.toString());
            }
            return tmp;
        }
        
        /**
         * Searches for the entry at position {@code index}.
         * 
         * @param index the index to search for
         * @return the entry iterator pointing to the index or <b>null</b> for none
         */
        private EntryIterator findBy(int index) {
            EntryIterator result = null;
            JsonIterator tmp = Json.parse(data);
            if (tmp.valueType() == ValueType.ARRAY) {
                result = new OneElementEntryIterator("", tmp.get(index));
            } else {
                EntryIterator it = tmp.entries();
                while (index >= 0 && it.next()) {
                    if (index == 0) {
                        result = it;
                    }
                    index--;
                }           
            }
            return result;
        }

        @Override
        public JsonIterator getData(String name, int... indexes) throws IOException {
            JsonIterator result = get(name, indexes);
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
        private JsonIterator get(String name, int... indexes) {
            JsonIterator result = null;
            JsonIterator obj = any;
            int start = 0;
            int end = 0;
            do {
                if (obj.valueType() == ValueType.STRING) { // index assumes an object, try to parse
                    obj = Json.parse(obj.toString());
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
        public void getData(IOConsumer<JsonIterator> ifPresent, String name, int... indexes) throws IOException {
            JsonIterator result = get(name, indexes);
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
        private JsonIterator getLocal(String name, int[] indexes) {
            JsonIterator result = null;
            if (any.containsKey(name)) { // seems to be faster than direct access
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
        public JsonIterator getLocalData(String name, int... indexes) throws IOException {
            JsonIterator result = getLocal(name, indexes);
            if (null == result) {
                throw new IOException("No entry found for " + name + " / " + Arrays.toString(indexes));
            }
            return result;
        }

        @Override
        public void getLocalData(IOConsumer<JsonIterator> ifPresent, String name, int... indexes) throws IOException {
            JsonIterator result = getLocal(name, indexes);
            if (null != result) {
                ifPresent.accept(result);
            }
        }

        @Override
        public JsonParseResult stepInto(String name, int index) throws IOException {
            JsonIterator nested = any.get(name);
            if (nested.valueType() == ValueType.INVALID) { // fallback
                EntryIterator it = findBy(index);
                if (null != it) {
                    nested = it.value();
                } else {
                    throw new IndexOutOfBoundsException("No entry found for " + index);
                }
            }
            if (nested.valueType() != ValueType.INVALID) { // fallback
                byte[] actData = any.slice(getTopData(), nested);
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

        @Override
        public int getArraySize() {
            return any.size();
        }
        
    }
    
    /**
     * Implements a JSON input converter.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static final class JsonInputConverter implements InputConverter<JsonIterator> {

        @Override
        public int toInteger(JsonIterator data) throws IOException {
            return data.toIntValue();
        }

        @Override
        public byte toByte(JsonIterator data) throws IOException {
            return (byte) data.toIntValue();
        }

        @Override
        public long toLong(JsonIterator data) throws IOException {
            return data.toLongValue();
        }

        @Override
        public short toShort(JsonIterator data) throws IOException {
            return (short) data.toIntValue();
        }

        @Override
        public String toString(JsonIterator data) throws IOException {
            return data.toStringValue();
        }

        @Override
        public double toDouble(JsonIterator data) throws IOException {
            return data.toDoubleValue();
        }

        @Override
        public float toFloat(JsonIterator data) throws IOException {
            return data.toFloatValue();
        }

        @Override
        public boolean toBoolean(JsonIterator data) throws IOException {
            return data.toBooleanValue();
        }

        @Override
        public int[] toIntegerArray(JsonIterator data) throws IOException {
            int[] dta = new int[data.size()];
            for (int j = 0; j < dta.length; j++) {
                dta[j] = data.get(j).toIntValue();
            }
            return dta; // exception?
        }
        
        @Override
        public String[] toStringArray(JsonIterator data) throws IOException {
            String[] dta = new String[data.size()];
            for (int j = 0; j < dta.length; j++) {
                dta[j] = data.get(j).toStringValue();
            }
            return dta;
        }        

        @Override
        public double[] toDoubleArray(JsonIterator data) throws IOException {
            double[] dta = new double[data.size()]; 
            for (int j = 0; j < dta.length; j++) {
                dta[j] = Double.parseDouble(data.get(j).toStringValue());
            }
            return dta;
        }

        @Override
        public byte[] toByteArray(JsonIterator data) throws IOException {
            byte[] dta = new byte[data.size()];
            for (int j = 0; j < dta.length; j++) {
                dta[j] = (byte) data.get(j).toIntValue();
            }
            return dta;
        }
        
        @Override
        public Object toObject(JsonIterator data) throws IOException {
            return null; // preliminary
        }

        @Override
        public <E> List<E> toList(JsonIterator data, Class<E> eltCls) throws IOException {
            List<E> result = new ArrayList<>(data.size());
            for (int i = 0; i < data.size(); i++) {
                Object obj = data.get(i);
                if (obj instanceof QualifiedElement) {
                    obj = ((QualifiedElement<?>) obj).getValue();
                }
                if (eltCls.isInstance(obj)) {
                    result.add(eltCls.cast(obj));
                } else {
                    throw new IOException("Element " + i + " " + obj + " is not of type " + eltCls.getName());
                }
            }
            return result;
        }

        @Override
        public <E> List<QualifiedElement<E>> toElementList(JsonIterator data, Class<E> eltCls) throws IOException {
            List<QualifiedElement<E>> result = new ArrayList<>(data.size());
            for (int i = 0; i < data.size(); i++) {
                Object obj = data.get(i);
                if (obj instanceof QualifiedElement) { // AAS element conversion
                    obj = ((QualifiedElement<?>) obj).getValue();
                }
                if (eltCls.isInstance(obj)) {
                    QualifiedElement<E> elt = QualifiedElementFactory.createElement(eltCls);
                    elt.setValue(eltCls.cast(obj));
                    result.add(elt);                    
                } else {
                    throw new IOException("Element " + i + " " + obj + " is not of type " + eltCls.getName());
                }
            }
            return result;
        }

        @Override
        public BigInteger toBigInteger(JsonIterator data) throws IOException {
            return data.toBigIntegerValue();
        }

        @Override
        public BigDecimal toBigDecimal(JsonIterator data) throws IOException {
            return data.toBigDecimalValue();
        }
        
        @Override
        public long toLongIndex(JsonIterator data) throws IOException {
            return data.toLongValue();
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
