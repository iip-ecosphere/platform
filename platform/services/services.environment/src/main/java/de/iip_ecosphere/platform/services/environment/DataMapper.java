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

package de.iip_ecosphere.platform.services.environment;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Maps data from a stream to input instances for a service. This class is intended as a basis for testing (here 
 * avoiding the test scope for generated code). The idea is that all input types are represented as attributes of a 
 * generated class (given in terms of a JSON file/stream). The generated service test calls this class providing a 
 * consumer to take over the data.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DataMapper {
    
    /**
     * Implements a mapper entry for {@code MappingConsumer}.
     *
     * @param <T> the containing mapped type
     * @author Holger Eichelberger, SSE
     */
    private static class MapperEntry<T> {
        
        private Method getter;
        private Consumer<Object> translator;
        
        /**
         * Creates a mapper entry for a given reflection getter method.
         * 
         * @param getter the getter method
         */
        private MapperEntry(Method getter) {
            this.getter = getter;
        }
        
        /**
         * Sets a configurable consumer for a given type. Exceptions are logged.
         * 
         * @param <A> the type
         * @param cls the type class
         * @param consumer the consumer to be added
         */
        private <A> void setConsumer(Class<A> cls, Consumer<A> consumer) {
            translator = o -> {
                try {
                    consumer.accept(cls.cast(o));
                } catch (ClassCastException e) {
                    LoggerFactory.getLogger(DataMapper.class).error("Cannot convert {} to {}", o, cls.getName());
                }
            };
        }
        
        /**
         * Accepts an instance of the mapped type by applying the {@link #getter} to {@code instance} and if the
         * result of the invocation is not <b>null</b>, calls the registered {@link #translator} to accept the 
         * value of the {@link #getter} call. Exceptions are logged.
         * 
         * @param instance the data instance to accept/process
         */
        private void accept(T instance) {
            try {
                Object data = getter.invoke(instance);
                if (null != data && null != translator) { // null is ok as data is alternative
                    translator.accept(data);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                LoggerFactory.getLogger(DataMapper.class).error("Cannot process {}: {}", instance, e.getMessage());
            }
        }
        
    }

    /**
     * Provides a default consumer implementation for {@link DataMapper#mapJsonData(InputStream, Class, Consumer)}
     * which maps attribute values to registered consumers.
     * 
     * @param <T> the mapped type
     * @author Holger Eichelberger, SSE
     */
    public static class MappingConsumer<T> implements Consumer<T> {

        private Map<Class<?>, MapperEntry<T>> mapping = new HashMap<>();
        
        /**
         * Creates a mapping consumer for the given {@code cls} type.
         * 
         * @param cls the class to do the mapping for
         */
        public MappingConsumer(Class<T> cls) {
            for (Method m: cls.getDeclaredMethods()) {
                if (m.getName().startsWith("get") && m.getParameterCount() == 0 && m.getReturnType() != Void.TYPE) {
                    mapping.put(m.getReturnType(), new MapperEntry<T>(m));
                }
            }
        }
        
        /**
         * Adds a handler for {@code cls} based on the consumer {@code cons}.
         * 
         * @param <A> the data type to be handled
         * @param cls the class to handle (may be <b>null</b>, then this call is ignored)
         * @param cons the corresponding consumer (may be <b>null</b>, then this call is ignored)
         */
        public <A> void addHandler(Class<A> cls, Consumer<A> cons) {
            if (null != cls && cons != null) {
                MapperEntry<T> entry = mapping.get(cls);
                if (entry != null) {
                    entry.setConsumer(cls, cons);
                }
            }
        }
        
        @Override
        public void accept(T value) {
            if (null != value) {
                for (MapperEntry<T> e: mapping.values()) {
                    e.accept(value);
                }
            }
        }
        
    }
    
    /**
     * Maps the data in {@code stream} to instances of {@code cls}, one instance per line. Calls {@code cons} per
     * instance/line. Closes {@code stream}. Ignores unknown attributes in {@code cls}.
     *  
     * @param <T> the type of data to read
     * @param stream the stream to read (may be <b>null</b> for none)
     * @param cls the type of data to read
     * @param cons the consumer to be called per instance
     * @throws IOException if I/O or JSON parsing errors occur
     */
    public static <T> void mapJsonData(InputStream stream, Class<T> cls, Consumer<T> cons) throws IOException {
        mapJsonData(stream, cls, cons, false);
    }

    /**
     * Maps the data in {@code stream} to instances of {@code cls}, one instance per line. Calls {@code cons} per
     * instance/line. Closes {@code stream}.
     *  
     * @param <T> the type of data to read
     * @param stream the stream to read (may be <b>null</b> for none)
     * @param cls the type of data to read
     * @param cons the consumer to be called per instance
     * @param failOnUnknownProperties whether parsing shall be tolerant or not, the latter may be helpful for debugging
     * @throws IOException if I/O or JSON parsing errors occur
     */
    public static <T> void mapJsonData(InputStream stream, Class<T> cls, Consumer<T> cons, 
        boolean failOnUnknownProperties) throws IOException {
        try {
            ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failOnUnknownProperties);
            
            JsonFactory jf = new JsonFactory();
            JsonParser jp = jf.createParser(stream);
            jp.setCodec(objectMapper);
            jp.nextToken();

            while (jp.hasCurrentToken()) {
                T data = jp.readValueAs(cls);
                jp.nextToken();
                cons.accept(data);
            }
        } catch (JsonProcessingException e) {
            throw new IOException(e);
        } finally {
            if (null != stream) {
                stream.close();
            }
        }
    }

}
