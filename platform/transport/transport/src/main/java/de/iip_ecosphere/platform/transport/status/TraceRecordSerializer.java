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

package de.iip_ecosphere.platform.transport.status;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

import de.iip_ecosphere.platform.transport.serialization.Serializer;

/**
 * A simple, generic JSON status serializer. Additional enum constants must be registered here.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TraceRecordSerializer implements Serializer<TraceRecord> {

    private static Set<Object> ignore = new HashSet<>();
    
    /**
     * Creates a specific object mapper that allows for lazy default serialization of unknown types
     * as it is the case for the payload in {@link TraceRecord}.
     * 
     * @return the object mapper
     */
    private static ObjectMapper createMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // may become empty through ignores
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL, 
            JsonTypeInfo.As.WRAPPER_ARRAY);
        if (!ignore.isEmpty()) {
            objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
    
                private static final long serialVersionUID = 7445592829151624983L;
    
                @Override
                public boolean hasIgnoreMarker(final AnnotatedMember member) {
                    return ignore.contains(member.getType().getRawClass()) || ignore.contains(member.getMember()) 
                        || super.hasIgnoreMarker(member); 
                }
            });
        }
        return objectMapper;
    }
    
    @Override
    public TraceRecord from(byte[] data) throws IOException {
        return createMapper().readValue(data, TraceRecord.class);
    }

    @Override
    public byte[] to(TraceRecord source) throws IOException {
        return createMapper().writeValueAsBytes(source);
    }

    @Override
    public TraceRecord clone(TraceRecord origin) throws IOException {
        return new TraceRecord(origin.getSource(), origin.getTimestamp(), origin.getAction(), origin.getPayload());
    }

    @Override
    public Class<TraceRecord> getType() {
        return TraceRecord.class;
    }
    
    /**
     * Registers a type to be ignored when serializing trace record (payloads).
     * 
     * @param cls the class representing the type to be ignored
     */
    public static void ignoreClass(Class<?> cls) {
        ignore.add(cls);
    }

    /**
     * Registers a field to be ignored when serializing trace record (payloads).
     * 
     * @param cls the class representing the type containing the field
     * @param field the field to be ignored
     */
    public static void ignoreField(Class<?> cls, String field) {
        try {
            Field f = cls.getDeclaredField(field);
            ignore.add(f);
        } catch (NoSuchFieldException e) {
            LoggerFactory.getLogger(TraceRecordSerializer.class).warn("Field {} not found on class {}", 
                field, cls.getName());
        }
    }
    
    /**
     * Clears all ignored types and fields.
     */
    public static void clearIgnores() {
        ignore.clear();
    }

}
