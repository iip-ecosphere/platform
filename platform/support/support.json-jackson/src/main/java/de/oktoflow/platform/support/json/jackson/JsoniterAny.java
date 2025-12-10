/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.oktoflow.platform.support.json.jackson;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import com.jsoniter.any.Any;
import com.jsoniter.spi.JsonException;

import de.iip_ecosphere.platform.support.json.JsonIterator;
import de.iip_ecosphere.platform.support.json.JsonUtils;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Wraps a Jsoniter into a JsonIterator. May need performance optimization, e.g., pooling.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JsoniterAny implements JsonIterator {
    
    private static final Class<?> LAZY_ANY_CLS;
    private static final Field LAZY_ANY_HEAD_FIELD;
    private static final Field LAZY_ANY_TAIL_FIELD;
    private static final Map<com.jsoniter.ValueType, ValueType> IMPL_TYPE2TYPE = new HashMap<>();

    private Any any;

    static {
        Class<?> cls = JsoniterAny.class; // a class that is not instance of LazyAny
        Field hf = null;
        Field tf = null;
        try {
            cls = Class.forName("com.jsoniter.any.LazyAny");
            hf = cls.getDeclaredField("head");
            hf.setAccessible(true);
            tf = cls.getDeclaredField("tail");
            tf.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            LoggerFactory.getLogger(JsoniterAny.class).error("Cannot find LazyAny class or its fields: " 
                + e.getMessage() + " Disabling JSON stepIn-slicing.");
        }
        if (tf == null) {
            cls = JsoniterAny.class; // a class that is not instance of LazyAny
            tf = null;
        }
        LAZY_ANY_CLS = cls;
        LAZY_ANY_HEAD_FIELD = hf;
        LAZY_ANY_TAIL_FIELD = tf;
        
        IMPL_TYPE2TYPE.put(com.jsoniter.ValueType.INVALID, ValueType.INVALID);
        IMPL_TYPE2TYPE.put(com.jsoniter.ValueType.STRING, ValueType.STRING);
        IMPL_TYPE2TYPE.put(com.jsoniter.ValueType.NUMBER, ValueType.NUMBER);
        IMPL_TYPE2TYPE.put(com.jsoniter.ValueType.NULL, ValueType.NULL);
        IMPL_TYPE2TYPE.put(com.jsoniter.ValueType.BOOLEAN, ValueType.BOOLEAN);
        IMPL_TYPE2TYPE.put(com.jsoniter.ValueType.ARRAY, ValueType.ARRAY);
        IMPL_TYPE2TYPE.put(com.jsoniter.ValueType.OBJECT, ValueType.OBJECT);
    }
    
    /**
     * Creates a wrapping instance.
     * 
     * @param any the instance to wrap
     */
    JsoniterAny(Any any) {
        this.any = any;
    }

    @Override
    public ValueType valueType() {
        return IMPL_TYPE2TYPE.get(any.valueType());
    }

    @Override
    public boolean containsKey(String key) {
        return any.keys().contains(key);
    }

    @Override
    public JsonIterator get(String key) {
        Any tmp = any.get(key);
        return null == tmp ? null : new JsoniterAny(tmp);
    }

    @Override
    public byte[] slice(byte[] data, JsonIterator iter) throws IOException {
        boolean deserialized = false;
        Any nested = ((JsoniterAny) iter).any;
        if (nested.valueType() == com.jsoniter.ValueType.STRING) { // step into assumes an object, try to parse
            nested = com.jsoniter.JsonIterator.deserialize(nested.toString());
            deserialized = true;
        }

        byte[] topData = data;
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
        return actData;
    }

    @Override
    public int size() {
        int size = 0;
        try {
            size = any.size();
        } catch (ArrayIndexOutOfBoundsException e) { // occurs if JSON syntax is erroneous
            size = 0;
            LoggerFactory.getLogger(this).warn("Cannot read JSON due to syntax errors: {}", 
                LoggerFactory.classAndMessage(e)); // ArrayIndexOutOfBoundsException just has the index as message
        }
        return size;
    }

    @Override
    public JsonIterator get(int index) {
        return new JsoniterAny(any.get(index));
    }

    @Override
    public EntryIterator entries() {
        final com.jsoniter.any.Any.EntryIterator iter = any.entries();
        return new EntryIterator() {

            @Override
            public boolean next() {
                return iter.next();
            }

            @Override
            public String key() {
                return iter.key();
            }

            @Override
            public JsonIterator value() {
                return new JsoniterAny(iter.value());
            }
            
        };
    }

    @Override
    public String getAnyKey() {
        String result;
        try {
            result = any.asMap().keySet().iterator().next().toString(); // :(
        } catch (ClassCastException e) {
            result = null;
        }
        return result;
    }

    @Override
    public String toString() {
        return any.toString();
    }

    @Override
    public String toStringValue() throws IOException {
        try {
            return any.toString();
        } catch (JsonException e) { // wrong format, we cannot read that
            throw new IOException(e);
        }
    }

    @Override
    public double toDoubleValue() throws IOException {
        try {
            return any.toDouble();
        } catch (JsonException e) { // wrong format, we cannot read that
            throw new IOException(e);
        }
    }

    @Override
    public float toFloatValue() throws IOException {
        try {
            return any.toFloat();
        } catch (JsonException e) { // wrong format, we cannot read that
            throw new IOException(e);
        }
    }

    @Override
    public long toLongValue() throws IOException {
        try {
            return any.toLong();
        } catch (JsonException e) { // wrong format, we cannot read that
            throw new IOException(e);
        }
    }

    @Override
    public boolean toBooleanValue() throws IOException {
        try {
            return any.toBoolean();
        } catch (JsonException e) { // wrong format, we cannot read that
            throw new IOException(e);
        }
    }

    @Override
    public int toIntValue() throws IOException {
        try {
            return any.toInt();
        } catch (JsonException e) { // wrong format, we cannot read that
            throw new IOException(e);
        }
    }
    
    @Override
    public BigInteger toBigIntegerValue() throws IOException {
        try {
            return any.toBigInteger();
        } catch (JsonException e) { // wrong format, we cannot read that
            throw new IOException(e);
        }
    }

    @Override
    public BigDecimal toBigDecimalValue() throws IOException {
        try {
            return any.toBigDecimal();
        } catch (JsonException e) { // wrong format, we cannot read that
            throw new IOException(e);
        }
    }

    @Override
    public Map<String, Object> asMap() throws IOException {
        Map<String, Object> result = new HashMap<>();
        final com.jsoniter.any.Any.EntryIterator iter = any.entries();
        while (iter.next()) {
            String key = iter.key();
            Any value = iter.value();
            collectAll(key, value, result);
        }
        return result;
    }

    /**
     * Collects all (nested) entries for field {@code name}-{@code any} in {@code result}.
     * 
     * @param name the name of the field
     * @param any the value of the field
     * @param fields the fields representing the parent of this field, to be modified as a side effect
     */
    private void collectAll(String name, Any any, Map<String, Object> fields) {
        final com.jsoniter.any.Any.EntryIterator iter = any.entries();
        Map<String, Object> nested = null;
        while (iter.next()) {
            if (null == nested) {
                nested = new HashMap<>();
            }
            String key = iter.key();
            Any value = iter.value();
            collectAll(key, value, nested);
            fields.put(name, nested);
        }
        if (null == nested) {
            fields.put(name, any.object());
        }
    }

}
