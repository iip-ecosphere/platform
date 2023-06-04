/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.transport.serialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;

/**
 * A simple, generic, reusable JSON serializer.
 * 
 * @param <T> the class to be serialized
 * @author Holger Eichelberger, SSE
 */
public class GenericJsonToStringTranslator<T> implements TypeTranslator<T, String> {
    
    private ObjectMapper mapper;
    private Class<T> cls;
    
    /**
     * Implements a simple generic JSON serializer.
     * 
     * @param cls the class to be serialized
     */
    public GenericJsonToStringTranslator(Class<T> cls) {
        this(cls, null);
    }

    /**
     * Implements a simple generic JSON serializer.
     * 
     * @param cls the class to be serialized
     * @param mapper the object mapper to use
     */
    public GenericJsonToStringTranslator(Class<T> cls, ObjectMapper mapper) {
        this.cls = cls;
        if (null == mapper) {
            this.mapper = new ObjectMapper();
            JsonUtils.handleIipDataClasses(this.mapper);
        } else {
            this.mapper = mapper;
        }
    }
    
    /**
     * Returns the object mapper, e.g., for further customization.
     * 
     * @return the mapper
     */
    public ObjectMapper getMapper() {
        return mapper;
    }

    @Override             
    public T from(String data) throws IOException {
        try {
            return mapper.readValue(data, cls);
        } catch (JsonProcessingException e) {
            throw new IOException(e);
        }
    }

    @Override    
    public String to(T source) throws IOException {
        try {
            return mapper.writeValueAsString(source);
        } catch (JsonProcessingException e) {
            throw new IOException(e);
        }
    }

}
