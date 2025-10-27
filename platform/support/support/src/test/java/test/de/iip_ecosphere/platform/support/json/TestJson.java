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

package test.de.iip_ecosphere.platform.support.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import de.iip_ecosphere.platform.support.json.IOIterator;
import de.iip_ecosphere.platform.support.json.Json;
import de.iip_ecosphere.platform.support.json.JsonArrayBuilder;
import de.iip_ecosphere.platform.support.json.JsonIterator;
import de.iip_ecosphere.platform.support.json.JsonObject;
import de.iip_ecosphere.platform.support.json.JsonObjectBuilder;
import de.iip_ecosphere.platform.support.json.JsonUtils;
import de.iip_ecosphere.platform.support.json.JsonUtils.JacksonEnumMapping;

/**
 * Implements the JSON interface by Jackson.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestJson extends de.iip_ecosphere.platform.support.json.Json {
    
    /**
     * Self-configuring Json implementation based on provided types.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class TestJson4All extends TestJson {
        
        private Set<Class<?>> configured = new HashSet<>();

        /**
         * Configures this instance for {@code cls}.
         * 
         * @param <T> the actual type
         * @param cls the class to configure for
         * @return cls
         */
        private <T> Class<T> cfg(Class<T> cls) {
            if (!configured.contains(cls)) {
                configureFor(cls);
            }
            return cls;
        }

        /**
         * Configures this instance for the class of {@code obj}.
         * 
         * @param obj the object to configure for, ignored if <b>null</b>
         * @return obj
         */
        private Object cfg(Object obj) {
            if (null != obj) {
                cfg(obj.getClass());
            }
            return obj;
        }

        @Override
        public String toJson(Object obj) throws IOException {
            return super.toJson(cfg(obj));
        }
        
        @Override
        public <R> R fromJson(Object json, Class<R> cls) throws IOException {
            return super.fromJson(json, cfg(cls));
        }

        @Override
        public <R> List<R> listFromJson(Object json, Class<R> cls) {
            return super.listFromJsonDflt(json, cfg(cls));
        }
        
        @Override
        public <K, V> Map<K, V> mapFromJson(Object json, Class<K> keyCls, Class<K> valueCls) {
            return super.mapFromJson(json, cfg(keyCls), cfg(valueCls));
        }
        
        @Override
        public <T> T readValue(String src, Class<T> cls) throws IOException {
            return super.readValue(src, cfg(cls));
        }

        @Override
        public <T> T readValue(byte[] src, Class<T> valueType) throws IOException {
            return super.readValue(src, cfg(valueType));
        }
        
        @Override
        public <T> T convertValue(Object value, Class<T> cls) throws IllegalArgumentException {
            cfg(cls);
            return super.convertValue(value, cfg(cls));
        }

        @Override
        public <T> EnumMapping<T> createEnumMapping(Class<T> type, Map<String, T> mapping) {
            return super.createEnumMapping(cfg(type), mapping);
        }
        
        @Override
        public byte[] writeValueAsBytes(Object value) throws IOException {
            return super.writeValueAsBytes(cfg(value));
        }    
        
        @Override
        public String writeValueAsString(Object value) throws IOException {
            return super.writeValueAsString(cfg(value));
        }        
        
    }
    
    private ObjectMapper mapper = new ObjectMapper();
    private ObjectWriter writer; 
    
    @Override
    public Json createInstanceImpl(boolean considerAnnotations) {
        return considerAnnotations ? new TestJson4All() : new TestJson();
    }

    @Override
    public String toJson(Object obj) throws IOException {
        String result = "";
        if (null != obj) {
            try {
                if (null != writer) {
                    result = writer.writeValueAsString(obj);
                } else {
                    result = mapper.writeValueAsString(obj);
                }
            } catch (JsonProcessingException e) {
                throw new IOException(e);
            }
        } 
        return result;
    }
    
    @Override
    public <R> R fromJson(Object json, Class<R> cls) throws IOException {
        R result = null;
        if (null != json) {
            try {
                result = mapper.readValue(json.toString(), cls);
            } catch (JsonProcessingException e) {
                throw new IOException(e);
            }
        }
        return result; 
    }
    
    @Override
    public <R> List<R> listFromJson(Object json, Class<R> cls) {
        List<R> result = null;
        if (null != json) {
            try {
                result = mapper.readValue(json.toString(), new ListTypeReference<R>());
            } catch (JsonProcessingException e) {
                //result = null;
            }            
        }
        return result; 
    }

    @Override
    public <K, V> Map<K, V> mapFromJson(Object json, Class<K> keyCls, Class<K> valueCls) {
        Map<K, V> result = null;
        if (null != json) {
            try {
                result = mapper.readValue(json.toString(), new MapTypeReference<K, V>());
            } catch (JsonProcessingException e) {
                //result = null;
            }            
        }
        return result; 
    }

    /**
     * Internal type to obtain a typed List from JSON.
     *
     * @param <T> the element type
     * @author Holger Eichelberger, SSE
     */
    private static class ListTypeReference<T> extends TypeReference<java.util.List<T>> {
    }
    
    /**
     * Internal type to obtain a typed Map from JSON.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @author Holger Eichelberger, SSE
     */
    private static class MapTypeReference<K, V> extends TypeReference<Map<K, V>> {
    }

    @Override
    public <T> T readValue(String src, Class<T> cls) throws IOException {
        try {
            return mapper.readValue(src, cls);
        } catch (JsonProcessingException e) {
            throw new IOException(e);
        }
    }
    
    @Override
    public <T> T readValue(byte[] src, Class<T> valueType) throws IOException {
        try {
            return mapper.readValue(src, valueType);
        } catch (JsonProcessingException e) {
            throw new IOException(e);
        }
    }
    
    @Override
    public byte[] writeValueAsBytes(Object value) throws IOException {
        try {
            if (writer != null) {
                return writer.writeValueAsBytes(value);
            } else {
                return mapper.writeValueAsBytes(value);
            }
        } catch (JsonProcessingException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Json configureFor(Class<?> cls) {
        mapper = JsonUtils.configureFor(mapper, cls);
        return this;
    }

    @Override
    public Json handleIipDataClasses() {
        mapper = JsonUtils.handleIipDataClasses(mapper);
        return this;
    }
    
    @Override
    public Json defineOptionals(Class<?> cls, String... fieldNames) {
        mapper = JsonUtils.defineOptionals(mapper, cls, fieldNames);
        return this;
    }

    @Override
    public Json defineFields(String... fieldNames) {
        mapper = JsonUtils.defineFields(mapper, fieldNames);
        return this;
    }
    
    @Override
    public Json exceptFields(String... fieldNames) {
        mapper = JsonUtils.exceptFields(mapper, fieldNames);
        return this;
    }
    
    @Override
    public Json filterAllExceptFields(String... fieldNames) {
        SimpleBeanPropertyFilter theFilter = SimpleBeanPropertyFilter
            .serializeAllExcept(fieldNames);
        SimpleFilterProvider filters = new SimpleFilterProvider()
            .setDefaultFilter(theFilter);
        writer = mapper.writer(filters);
        return this;
    }

    @Override
    public <T> T convertValue(Object value, Class<T> cls) throws IllegalArgumentException {
        return mapper.convertValue(value, cls);
    }

    @Override
    public Json failOnUnknownProperties(boolean fail) {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return this;
    }

    @Override
    public <T> EnumMapping<T> createEnumMapping(Class<T> type, Map<String, T> mapping) {
        return new JacksonEnumMapping<T>(type, mapping);
    }
    
    @Override
    public Json declareEnums(EnumMapping<?>... mappings) {
        JsonUtils.declareEnums(mapper, mappings);
        return this;
    }
    
    @Override
    public Json configureLazy(Set<Object> ignore) { 
        JsonUtils.configureLazy(mapper, ignore);
        return this;
    }
    
    @Override
    public <T> IOIterator<T> createIterator(InputStream stream, Class<T> cls) throws IOException {
        return JsonUtils.createIterator(mapper, stream, cls);
    }

    @Override
    protected JsonObject createObjectImpl(Reader reader) throws IOException {
        return TestJsonObject.createObject(reader);
    }

    @Override
    protected JsonObjectBuilder createObjectBuilderImpl() {
        return TestJsonObject.createObjectBuilder();
    }

    @Override
    protected JsonArrayBuilder createArrayBuilderImpl() {
        return TestJsonObject.createArrayBuilder();
    }

    @Override
    protected JsonIterator parseImpl(String text) {
        return null;
    }

    @Override
    protected JsonIterator parseImpl(byte[] data) {
        return null;
    }

    @Override
    public String writeValueAsString(Object value) throws IOException {
        return mapper.writeValueAsString(value);
    }
    
}
