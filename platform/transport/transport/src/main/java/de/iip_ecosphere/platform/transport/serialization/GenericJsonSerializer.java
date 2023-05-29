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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;

/**
 * A simple, generic, reusable JSON serializer.
 * 
 * @param <T> the class to be serialized
 * @author Holger Eichelberger, SSE
 */
public class GenericJsonSerializer<T> implements Serializer<T> {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private Class<T> cls;
    
    static {
        JsonUtils.handleIipDataClasses(MAPPER);
    }

    /**
     * Implements a simple generic JSON serializer.
     * 
     * @param cls the class to be serialized
     */
    public GenericJsonSerializer(Class<T> cls) {
        this.cls = cls;
    }
    
    @Override             
    public T from(byte[] data) throws IOException {
        try {
            return MAPPER.readValue(data, cls);
        } catch (JsonProcessingException e) {
            throw new IOException(e);
        }
    }

    @Override    
    public byte[] to(T source) throws IOException {
        try {
            return MAPPER.writeValueAsBytes(source);
        } catch (JsonProcessingException e) {
            throw new IOException(e);
        }
    }

    @Override
    public T clone(T origin) throws IOException {
        T result = null;
        Constructor<T> c;
        try {
            c = cls.getConstructor(cls);
            result = c.newInstance(origin);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException 
            | IllegalArgumentException | InvocationTargetException e) {
            // ignore
        }
        if (null == result) {
            try {
                c = cls.getConstructor();
                result = c.newInstance();
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException 
                | IllegalArgumentException | InvocationTargetException e) {
                LoggerFactory.getLogger(getClass()).error("Cannot create cloned instance of {}: {}", origin, 
                    e.getMessage());
            }
        }
        return result;
    }

    @Override
    public Class<T> getType() {
        return cls;
    }

}
