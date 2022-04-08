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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

import de.iip_ecosphere.platform.transport.serialization.Serializer;

/**
 * A simple, generic status serializer. Additional enum constants must be registered here.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TraceRecordSerializer implements Serializer<TraceRecord> {

    /**
     * Creates a specific object mapper that allows for lazy default serialization of unknown types
     * as it is the case for the payload in {@link TraceRecord}.
     * 
     * @return the object mapper
     */
    private static ObjectMapper createMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL, 
            JsonTypeInfo.As.WRAPPER_ARRAY);
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

}
