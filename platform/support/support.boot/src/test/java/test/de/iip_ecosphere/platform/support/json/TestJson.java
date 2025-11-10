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
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.iip_ecosphere.platform.support.json.IOIterator;
import de.iip_ecosphere.platform.support.json.Json;
import de.iip_ecosphere.platform.support.json.JsonArrayBuilder;
import de.iip_ecosphere.platform.support.json.JsonGenerator;
import de.iip_ecosphere.platform.support.json.JsonIterator;
import de.iip_ecosphere.platform.support.json.JsonObject;
import de.iip_ecosphere.platform.support.json.JsonObjectBuilder;

/**
 * Implements the JSON interface by Jackson.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestJson extends de.iip_ecosphere.platform.support.json.Json {
    
    @Override
    public Json createInstanceImpl(boolean considerAnnotations) {
        return new TestJson();
    }

    @Override
    public String toJson(Object obj) throws IOException {
        return null;
    }
    
    @Override
    public <R> R fromJson(Object json, Class<R> cls) throws IOException {
        return null;
    }
    
    @Override
    public <R> List<R> listFromJson(Object json, Class<R> cls) {
        return null;
    }

    @Override
    public <K, V> Map<K, V> mapFromJson(Object json, Class<K> keyCls, Class<V> valueCls) {
        return null;
    }

    @Override
    public <T> T readValue(String src, Class<T> cls) throws IOException {
        ObjectMapper om = new ObjectMapper();
        return om.readValue(src, cls);
    }
    
    @Override
    public <T> T readValue(byte[] src, Class<T> valueType) throws IOException {
        return null;
    }
    
    @Override
    public byte[] writeValueAsBytes(Object value) throws IOException {
        return null;
    }
    
    @Override
    public Json configureFor(Class<?> cls) {
        return this;
    }
    
    @Override
    public Json handleIipDataClasses() {
        return this;
    }
    
    @Override
    public Json defineOptionals(Class<?> cls, String... fieldNames) {
        return this;
    }
    
    @Override
    public Json defineFields(String... fieldNames) {
        return this;
    }
    
    @Override
    public Json exceptFields(String... fieldNames) {
        return this;
    }
    
    @Override
    public Json configureExceptFieldsFilter(String filterId, String... fieldNames) {
        return this;
    }    
    
    @Override
    public Json filterAllExceptFields(String... fieldNames) {
        return this;
    }

    @Override
    public <T> T convertValue(Object value, Class<T> cls) throws IllegalArgumentException {
        ObjectMapper om = new ObjectMapper();
        return om.convertValue(value, cls);
    }

    @Override
    public Json failOnUnknownProperties(boolean fail) {
        return this;
    }

    @Override
    public <T> EnumMapping<T> createEnumMapping(Class<T> type, Map<String, T> mapping) {
        return null;
    }
    
    @Override
    public Json declareEnums(EnumMapping<?>... mappings) {
        return this;
    }
    
    @Override
    public Json configureLazy(Set<Object> ignore) { 
        return this;
    }
    
    @Override
    public <T> IOIterator<T> createIterator(InputStream stream, Class<T> cls) throws IOException {
        return null;
    }

    @Override
    protected JsonObject createObjectImpl(Reader reader) throws IOException {
        return null;
    }

    @Override
    protected JsonObjectBuilder createObjectBuilderImpl() {
        return null;
    }

    @Override
    protected JsonArrayBuilder createArrayBuilderImpl() {
        return null;
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
        ObjectMapper om = new ObjectMapper();
        return om.writeValueAsString(value);
    }
    
    @Override
    protected JsonGenerator createGeneratorImpl(Writer writer) {
        return null;
    }
    
}
