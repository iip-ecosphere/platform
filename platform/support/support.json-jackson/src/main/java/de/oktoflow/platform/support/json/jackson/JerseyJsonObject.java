package de.oktoflow.platform.support.json.jackson;

import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonException;

import de.iip_ecosphere.platform.support.json.JsonArray;
import de.iip_ecosphere.platform.support.json.JsonArrayBuilder;
import de.iip_ecosphere.platform.support.json.JsonNumber;
import de.iip_ecosphere.platform.support.json.JsonObject;
import de.iip_ecosphere.platform.support.json.JsonObjectBuilder;
import de.iip_ecosphere.platform.support.json.JsonString;
import de.iip_ecosphere.platform.support.json.JsonValue;

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

/**
 * Wraps an EE JsonObject (provided by Jersey).
 * 
 * @author Holger Eichelberger, SSE
 */
class JerseyJsonObject implements JsonObject {
    
    private javax.json.JsonObject object;

    /**
     * Wraps an EE JsonArray (provided by Jersey).
     * 
     * @author Holger Eichelberger, SSE
     */
    static class JerseyJsonArray implements JsonArray {

        private javax.json.JsonArray array;

        /**
         * Creates a wrapping JSON array.
         * 
         * @param array the JSON array to wrap
         */
        JerseyJsonArray(javax.json.JsonArray array) {
            this.array = array;
        }
        
        @Override
        public int size() {
            return array.size();
        }

        @Override
        public JsonObject getJsonObject(int index) {
            javax.json.JsonObject o = array.getJsonObject(index);
            return null == o ? null : new JerseyJsonObject(o);
        }

        @Override
        public JsonArray getJsonArray(int index) {
            javax.json.JsonArray o = array.getJsonArray(index);
            return null == o ? null : new JerseyJsonArray(o);
        }

        @Override
        public JsonNumber getJsonNumber(int index) {
            javax.json.JsonNumber o = array.getJsonNumber(index);
            return null == o ? null : new JerseyJsonNumber(o);
        }

        @Override
        public JsonString getJsonString(int index) {
            javax.json.JsonString o = array.getJsonString(index);
            return null == o ? null : new JerseyJsonString(o);
        }
        
        @Override
        public String getString(int index) {
            return array.getString(index);
        }

        @Override
        public int getInt(int index) {
            return array.getInt(index);
        }

        @Override
        public boolean getBoolean(int index) {
            return array.getBoolean(index);
        }

        @Override
        public JsonValue getValue(int index) {
            return JerseyJsonObject.map(array.get(index));
        }

        @Override
        public JsonObject asJsonObject() {
            return new JerseyJsonObject(array.asJsonObject());
        }

        @Override
        public boolean isEmpty() {
            return array.isEmpty();
        }

        @Override
        public boolean isNull(int index) {
            return array.isNull(index);
        }

        @Override
        public String toString() {
            return array.toString();
        }

        @Override
        public int hashCode() {
            return array.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            return other == this || array.equals(
                other instanceof JerseyJsonArray ? ((JerseyJsonArray) other).array : other);
        }

    }

    /**
     * Wraps an EE JsonNumber (provided by Jersey).
     * 
     * @author Holger Eichelberger, SSE
     */
    static class JerseyJsonNumber implements JsonNumber {

        private javax.json.JsonNumber number;

        /**
         * Creates a wrapping JSON number.
         * 
         * @param number the JSON number to wrap
         */
        JerseyJsonNumber(javax.json.JsonNumber number) {
            this.number = number;
        }

        @Override
        public double doubleValue() {
            return number.doubleValue();
        }

        @Override
        public int intValue() {
            return number.intValue();
        }

        @Override
        public long longValue() {
            return number.longValue();
        }

        @Override
        public JsonObject asJsonObject() {
            return new JerseyJsonObject(number.asJsonObject());
        }

        @Override
        public String toString() {
            return number.toString();
        }

        @Override
        public int hashCode() {
            return number.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            return other == this || number.equals(
                other instanceof JerseyJsonNumber ? ((JerseyJsonNumber) other).number : other);
        }

    }

    /**
     * Wraps an EE JsonString (provided by Jersey).
     * 
     * @author Holger Eichelberger, SSE
     */
    static class JerseyJsonString implements JsonString {

        private javax.json.JsonString string;

        /**
         * Creates a wrapping JSON number.
         * 
         * @param string the JSON number to wrap
         */
        JerseyJsonString(javax.json.JsonString string) {
            this.string = string;
        }
        
        @Override
        public String getString() {
            return string.getString();
        }

        @Override
        public JsonObject asJsonObject() {
            return new JerseyJsonObject(string.asJsonObject());
        }

        @Override
        public String toString() {
            return string.toString();
        }

        @Override
        public int hashCode() {
            return string.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            return other == this || string.equals(
                other instanceof JerseyJsonString ? ((JerseyJsonString) other).string : other);
        }
        
    }

    /**
     * Implements the object builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class JerseyJsonArrayBuilder implements JsonArrayBuilder {
        
        private javax.json.JsonArrayBuilder builder = Json.createArrayBuilder();

        @Override
        public JsonArray build() {
            return new JerseyJsonArray(builder.build());
        }

        @Override
        public JsonArrayBuilder add(String value) {
            builder.add(value);
            return this;
        }

        @Override
        public JsonArrayBuilder add(int value) {
            builder.add(value);
            return this;
        }

        @Override
        public JsonArrayBuilder add(double value) {
            builder.add(value);
            return this;
        }

        @Override
        public JsonArrayBuilder add(boolean value) {
            builder.add(value);
            return this;
        }

        @Override
        public JsonArrayBuilder add(JsonString value) {
            builder.add(value instanceof JerseyJsonString ? ((JerseyJsonString) value).string : null);
            return this;
        }

        @Override
        public JsonArrayBuilder add(JsonNumber value) {
            builder.add(value instanceof JerseyJsonNumber ? ((JerseyJsonNumber) value).number : null);
            return this;
        }

        @Override
        public JsonArrayBuilder add(JsonArray value) {
            builder.add(value instanceof JerseyJsonArray ? ((JerseyJsonArray) value).array : null);
            return this;
        }

        @Override
        public JsonArrayBuilder add(JsonObject value) {
            builder.add(value instanceof JerseyJsonObject ? ((JerseyJsonObject) value).object : null);
            return this;
        }

        @Override
        public JsonArrayBuilder addNull() {
            builder.addNull();
            return this;
        }

        @Override
        public JsonArrayBuilder add(JsonArrayBuilder builder) {
            this.builder.add(builder instanceof JerseyJsonArrayBuilder 
                ? ((JerseyJsonArrayBuilder) builder).builder : null);
            return this;
        }

        @Override
        public JsonArrayBuilder add(JsonObjectBuilder builder) {
            this.builder.add(builder instanceof JerseyJsonObjectBuilder 
                ? ((JerseyJsonObjectBuilder) builder).builder : null);
            return this;
        }

    }

    /**
     * Implements the object builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class JerseyJsonObjectBuilder implements JsonObjectBuilder {
        
        private javax.json.JsonObjectBuilder builder = Json.createObjectBuilder();

        @Override
        public JsonObject build() {
            return new JerseyJsonObject(builder.build());
        }

        @Override
        public JsonObjectBuilder add(String name, String value) {
            builder.add(name, value);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, int value) {
            builder.add(name, value);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, double value) {
            builder.add(name, value);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, boolean value) {
            builder.add(name, value);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, JsonString value) {
            builder.add(name, value instanceof JerseyJsonString ? ((JerseyJsonString) value).string : null);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, JsonNumber value) {
            builder.add(name, value instanceof JerseyJsonNumber ? ((JerseyJsonNumber) value).number : null);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, JsonArray value) {
            builder.add(name, value instanceof JerseyJsonArray ? ((JerseyJsonArray) value).array : null);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, JsonObject value) {
            builder.add(name, value instanceof JerseyJsonObject ? ((JerseyJsonObject) value).object : null);
            return this;
        }

        @Override
        public JsonObjectBuilder remove(String name) {
            builder.remove(name);
            return this;
        }

        @Override
        public JsonObjectBuilder addNull(String name) {
            builder.addNull(name);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, JsonArrayBuilder builder) {
            this.builder.add(name, builder instanceof JerseyJsonArrayBuilder 
                ? ((JerseyJsonArrayBuilder) builder).builder : null);
            return this;
        }

        @Override
        public JsonObjectBuilder add(String name, JsonObjectBuilder builder) {
            this.builder.add(name, builder instanceof JerseyJsonObjectBuilder 
                ? ((JerseyJsonObjectBuilder) builder).builder : null);
            return this;
        }

    }

    /**
     * Creates a wrapping JSON object.
     * 
     * @param object the JSON object to wrap
     */
    JerseyJsonObject(javax.json.JsonObject object) {
        this.object = object;
    }

    /**
     * Creates a JSON object for individual access from {@code reader}.
     * 
     * @param reader the reader
     * @return the JSON object
     * @throws IOException if the object cannot be read/constructed
     */
    static JsonObject createObject(Reader reader) throws IOException {
        try {
            return new JerseyJsonObject(javax.json.Json.createReader(reader).readObject());
        } catch (JsonException | IllegalStateException e) {
            throw new IOException(e.getMessage());
        }
    }
    
    /**
     * Creates a JSON object builder.
     * 
     * @return the JSON object builder
     */
    static JsonObjectBuilder createObjectBuilder() {
        return new JerseyJsonObjectBuilder();
    }
    
    /**
     * Creates a JSON array builder.
     * 
     * @return the JSON array builder
     */
    static JsonArrayBuilder createArrayBuilder() {
        return new JerseyJsonArrayBuilder();
    }

    @Override
    public JsonObject getJsonObject(String name) {
        javax.json.JsonObject o = object.getJsonObject(name);
        return null == o ? null : new JerseyJsonObject(o);
    }

    @Override
    public JsonArray getJsonArray(String name) {
        javax.json.JsonArray o = object.getJsonArray(name);
        return null == o ? null : new JerseyJsonArray(o);
    }

    @Override
    public JsonNumber getJsonNumber(String name) {
        javax.json.JsonNumber o = object.getJsonNumber(name);
        return null == o ? null : new JerseyJsonNumber(o);
    }

    @Override
    public JsonString getJsonString(String name) {
        javax.json.JsonString o = object.getJsonString(name);
        return null == o ? null : new JerseyJsonString(o);
    }

    @Override
    public JsonValue getValue(String name) {
        return map(object.get(name));
    }
    
    @Override
    public String getString(String name) {
        return object.getString(name);
    }

    @Override
    public int getInt(String name) {
        return object.getInt(name);
    }

    @Override
    public boolean getBoolean(String name) {
        return object.getBoolean(name);
    }

    @Override
    public boolean isNull(String name) {
        return object.isNull(name);
    }

    @Override
    public boolean isEmpty() {
        return object.isEmpty();
    }

    /**
     * Maps an implementation value to an abstracted value.
     * 
     * @param value the implementation value (may be <b>null</b>)
     * @return the abstracted value (may be <b>null</b>)
     */
    static JsonValue map(javax.json.JsonValue value) {
        JsonValue result;
        if (value == null) {
            result = null;
        } else if (value instanceof javax.json.JsonNumber) {
            result = new JerseyJsonNumber((javax.json.JsonNumber) value);
        } else if (value instanceof javax.json.JsonArray) {
            result = new JerseyJsonArray((javax.json.JsonArray) value);
        } else if (value instanceof javax.json.JsonString) {
            result = new JerseyJsonString((javax.json.JsonString) value);
        } else {
            result = new JerseyJsonObject((javax.json.JsonObject) value);
        }
        return result;
    }

    @Override
    public Set<String> keys() {
        return object.keySet();
    }

    @Override
    public JsonObject asJsonObject() {
        return new JerseyJsonObject(object.asJsonObject());
    }

    @Override
    public String toString() {
        return object.toString();
    }

    @Override
    public int hashCode() {
        return object.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || object.equals(
            other instanceof JerseyJsonObject ? ((JerseyJsonObject) other).object : other);
    }

}
