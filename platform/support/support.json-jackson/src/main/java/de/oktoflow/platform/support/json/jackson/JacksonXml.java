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
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.xml.Xml;

/**
 * Implements the XML interface using Jackson.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JacksonXml extends Xml {

    private XmlMapper mapper = new XmlMapper();
    private OktoAnnotationIntrospector introspector;
    
    { // basic configuraiton for all mappers, independend whether specific classes shall be considered
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }
    
    /**
     * Self-configuring Json implementation based on provided types.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class JacksonXml4All extends JacksonXml {
        
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
        public <T> T readValue(String src, Class<T> cls) throws IOException {
            return super.readValue(src, cfg(cls));
        }

        @Override
        public <T> T readValue(byte[] src, Class<T> cls) throws IOException {
            return super.readValue(src, cfg(cls));
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
    
    @Override
    protected Xml createInstanceImpl(boolean considerAnnotations) {
        return considerAnnotations ? new JacksonXml4All() : new JacksonXml();
    }

    @Override
    public Xml configureFor(Class<?> cls) {
        introspector = JsonUtils.configureFor(mapper, introspector, cls);
        return this;
    }

    @Override
    public Xml defineOptionals(Class<?> cls, String... fieldNames) {
        mapper.addHandler(new OptionalFieldsDeserializationProblemHandler(cls, fieldNames));
        return this;
    }

    @Override
    public Xml defineFields(String... fieldNames) {
        mapper.setPropertyNamingStrategy(MappingPropertyNamingStrategy.createFor(fieldNames));
        return this;
    }

    @Override
    public Xml exceptFields(String... fieldNames) {
        final Set<String> exclusions = CollectionUtils.addAll(new HashSet<String>(), fieldNames);
        introspector = OktoAnnotationIntrospector.set(introspector, i -> mapper.setAnnotationIntrospector(i), 
            i -> i.setExclusions(exclusions));
        return this;
    }

    @Override
    public Xml handleIipDataClasses() {
        mapper.registerModule(JsonUtils.createIipDataClassesModule());
        return this;
    }
    
    @Override
    public Xml failOnUnknownProperties(boolean fail) {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return this;
    }

    @Override
    public <T> T readValue(String src, Class<T> cls) throws IOException {
        try {
            return mapper.readValue(src, cls);
        } catch (JsonProcessingException e) { // frontend exception although e is IOException
            throw new IOException(e);
        }
    }
    
    @Override
    public <T> T readValue(byte[] src, Class<T> valueType) throws IOException {
        try {
            return mapper.readValue(src, valueType);
        } catch (JsonProcessingException e) { // frontend exception although e is IOException
            throw new IOException(e);
        }
    }

    @Override
    public byte[] writeValueAsBytes(Object value) throws IOException {
        try {
            return mapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) { // frontend exception although e is IOException
            throw new IOException(e);
        }
    }

    @Override
    public String writeValueAsString(Object value) throws IOException {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) { // frontend exception although e is IOException
            throw new IOException(e);
        }
    }

}
