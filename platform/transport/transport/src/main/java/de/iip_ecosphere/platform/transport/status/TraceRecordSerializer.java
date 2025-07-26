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
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import de.iip_ecosphere.platform.support.json.JsonUtils;
import de.iip_ecosphere.platform.transport.serialization.GenericJsonToStringTranslator;
import de.iip_ecosphere.platform.transport.serialization.Serializer;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * A simple, generic JSON status serializer. Additional enum constants must be registered here.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TraceRecordSerializer implements Serializer<TraceRecord> {

    private static Set<Object> ignore = new HashSet<>();
    private static TraceRecordFilter filter = new TraceRecordFilter() { };
    
    static {
        Optional<TraceRecordFilter> opt = ServiceLoaderUtils.findFirst(TraceRecordFilter.class);
        if (opt.isPresent()) {
            filter = opt.get();
            LoggerFactory.getLogger(TraceRecordSerializer.class).info(
                "Installed trace record filter {}. Initializing...", filter.getClass().getName());
            filter.initialize();
        }
    }
    
    /**
     * Returns the trace record filter.
     * 
     * @return the filter
     */
    public static TraceRecordFilter getFilter() {
        return filter;
    }
    
    /**
     * Creates a specific object mapper that allows for lazy default serialization of unknown types
     * as it is the case for the payload in {@link TraceRecord}.
     * 
     * @return the object mapper
     */
    private static ObjectMapper createMapper() {
        return JsonUtils.configureLazy(new ObjectMapper(), ignore);
    }

    /**
     * Creates a type translator based on the serialization approach in this class.
     * 
     * @return the type translator
     */
    public static TypeTranslator<TraceRecord, String> createTypeTranslator() {
        return new GenericJsonToStringTranslator<TraceRecord>(TraceRecord.class, createMapper());
    }

    @Override
    public TraceRecord from(byte[] data) throws IOException {
        return createMapper().readValue(data, TraceRecord.class);
    }

    @Override
    public byte[] to(TraceRecord source) throws IOException {
        source.setPayload(filter.filterPayload(source.getPayload())); // may require a new instance
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
     * Registers fields to be ignored when serializing trace record (payloads).
     * 
     * @param cls the class representing the type containing the field
     * @param fields the fields to be ignored
     * @see #ignoreField(Class, String)
     */
    public static void ignoreFields(Class<?> cls, String... fields) {
        for (String field: fields) {
            ignoreField(cls, field);
        }
    }
    
    /**
     * Clears all ignored types and fields.
     */
    public static void clearIgnores() {
        ignore.clear();
    }

}
