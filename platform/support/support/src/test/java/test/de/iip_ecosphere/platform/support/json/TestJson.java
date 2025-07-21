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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.json.Json;
import de.iip_ecosphere.platform.support.json.JsonUtils.MappingPropertyNamingStrategy;
import de.iip_ecosphere.platform.support.json.JsonUtils.OptionalFieldsDeserializationProblemHandler;

/**
 * Implements the JSON interface by Jackson.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestJson extends de.iip_ecosphere.platform.support.json.Json {

    /**
     * A type resolver for "Impl" classes in "iip.".
     */
    public static final SimpleAbstractTypeResolver IIP_TYPE_RESOLVER = new SimpleAbstractTypeResolver() {
        
        private static final long serialVersionUID = -3746467806797935401L;
        // no instance data here

        @Override
        public JavaType findTypeMapping(DeserializationConfig config, JavaType type) {
            JavaType result = null;
            // for generated IIP-Ecosphere data interfaces, we can try it with Impl classes
            String className = type.getRawClass().getName();
            if (type.isInterface() && className.startsWith("iip.")) {
                String name = className + "Impl";
                try {
                    Class<?> cls = Class.forName(name);
                    result = config.getTypeFactory().constructType(cls);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            if (null == result) {
                result = super.findTypeMapping(config, type);
            }
            return result;
        }
            
    };    
    
    private ObjectMapper mapper = new ObjectMapper();
    
    @Override
    public Json createInstanceImpl() {
        return new TestJson();
    }

    @Override
    public String toJson(Object obj) throws IOException {
        String result = "";
        if (null != obj) {
            try {
                result = mapper.writeValueAsString(obj);
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
    public Json handleIipDataClasses() {
        SimpleModule iipModule = new SimpleModule();
        iipModule.setAbstractTypes(IIP_TYPE_RESOLVER);
        mapper.registerModule(iipModule);
        return this;
    }
    
    @Override
    public Json defineOptionals(Class<?> cls, String... fieldNames) {
        mapper.addHandler(new OptionalFieldsDeserializationProblemHandler(cls, fieldNames));
        return this;
    }

    @Override
    public Json defineFields(String... fieldNames) {
        Map<String, String> mapping = new HashMap<>();
        for (String fn : fieldNames) {
            String javaField = fn;
            if (javaField.length() > 0) {
                javaField = Character.toUpperCase(javaField.charAt(0)) + javaField.substring(1);
            }
            mapping.put(javaField, fn);
        }
        mapper.setPropertyNamingStrategy(new MappingPropertyNamingStrategy(mapping));
        return this;
    }
    
    @Override
    public Json exceptFields(String... fieldNames) {
        final Set<String> exclusions = CollectionUtils.addAll(new HashSet<String>(), fieldNames);
        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {

            private static final long serialVersionUID = -6485293464445674590L;

            @Override
            public boolean hasIgnoreMarker(final AnnotatedMember member) {
                return exclusions.contains(member.getName()) || super.hasIgnoreMarker(member);
            }
        });
        return this;
    }

}
