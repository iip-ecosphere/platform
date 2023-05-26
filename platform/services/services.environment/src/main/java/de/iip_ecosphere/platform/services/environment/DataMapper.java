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
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;
import de.iip_ecosphere.platform.transport.serialization.Serializer;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;

/**
 * Maps data from a stream to input instances for a service. This class is intended as a basis for testing (here 
 * avoiding the test scope for generated code). The idea is that all input types are represented as attributes of a 
 * generated class (given in terms of a JSON file/stream). The generated service test calls this class providing a 
 * consumer to take over the data.
 * 
 * As we read JSON through Jackson, currently the fields must comply with camel case Java naming convention 
 * irrespective how the fields are written in the generated Java class.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DataMapper {
    
    /**
    * Interface to represent all potential inputs to the service and the JSON input format.
    * Defines the meta attributes (thus $ prefixes), needs to be refined with actual attributes 
    * by using class. Can be used for dynamic class proxying.
    *
    * @author Holger Eichelberger, SSE
    */
    public interface BaseDataUnitFunctions {

        // checkstyle: stop names check

        /**
         * Returns the delay period between this and the next data unit.
         *
         * @return the period in ms, use default/last value if zero or negative
         */
        public int get$period();

        /**
         * Returns the number of repeats of this data unit.
         *
         * @return the number of repeats, negative for infinite
         */
        public int get$repeats();

        /**
        * Changes the delay period between this and the next data unit. [snakeyaml]
        *
        * @param $period the period in ms, default/last value if zero or negative
        */
        public void set$period(int $period);
        
        /**
         * Changes the number of repeats of this data unit. [snakeyaml]
         *
         * @param $repeats the number of repeats, negative for infinite
         */
        public void set$repeats(int $repeats);

        // checkstyle: resume names check

    }
    
    /**
     * Creates a dynamic class extending {@code cls} and implementing {@link BaseDataUnitFunctions}.
     * 
     * @param <T> the type of the class
     * @param cls the class to extend (assuming it'S a generated data class)
     * @return the created class
     */
    public static <T> Class<? extends T> createBaseDataUnitClass(Class<T> cls) {
        AnnotationDescription jsonFilter = AnnotationDescription.Latent.Builder.ofType(JsonFilter.class)
            .define("value", "iipFilter").build();
        Class<? extends T> result = new ByteBuddy()
            .subclass(cls)
            .name("iip.mock." + cls.getSimpleName() + "Mock")
            .implement(BaseDataUnitFunctions.class)
            .annotateType(jsonFilter)
            .defineProperty("$period", Integer.TYPE)
            .defineProperty("$repeats", Integer.TYPE)
            .make()
            .load(MockingConnectorServiceWrapper.class.getClassLoader())
            .getLoaded();
        
        // and we need an ad-hoch serializer that represents the new type and behaves as the existing type
        final Serializer<T> ser = SerializerRegistry.getSerializer(cls);
        if (null != ser) {
            Serializer<T> newSer = new Serializer<T>() {

                @Override
                public T from(byte[] data) throws IOException {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonUtils.handleIipDataClasses(objectMapper); // only if nested?
                        return objectMapper.readValue(data, cls);
                    } catch (JsonProcessingException e) {
                        throw new IOException(e);
                    }
                }

                @Override
                public byte[] to(T source) throws IOException {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        SimpleBeanPropertyFilter theFilter = SimpleBeanPropertyFilter
                            .serializeAllExcept("$period", "$repeats");
                        FilterProvider filters = new SimpleFilterProvider()
                            .addFilter("iipFilter", theFilter);
                        return objectMapper.writer(filters).writeValueAsBytes(source);
                    } catch (JsonProcessingException e) {
                        throw new IOException(e);
                    }
                }

                @Override
                public T clone(T origin) throws IOException {
                    return ser.clone(origin); // ignore additional fields
                }

                @SuppressWarnings("unchecked")
                @Override
                public Class<T> getType() {
                    return (Class<T>) result;
                }
                
            };
            SerializerRegistry.registerSerializer(newSer);
        }
        return result;
    }
    
    /**
    * Base class to represent all potential inputs to the service and the JSON input format.
    * Just defines the meta attributes (thus $ prefixes), needs to be refined with actual attributes 
    * by using class.
    *
    * @author Holger Eichelberger, SSE
    */
    public abstract static class BaseDataUnit implements BaseDataUnitFunctions {
        
        // checkstyle: stop names check
    
        private int $period = 0;
        private int $repeats = 0;
        
        @Override
        public int get$period() {
            return $period;
        }

        @Override
        public int get$repeats() {
            return $repeats;
        }

        @Override
        public void set$period(int $period) {
            this.$period = $period;
        }

        @Override
        public void set$repeats(int $repeats) {
            this.$repeats = $repeats;
        }

        // checkstyle: resume names check
    }
    
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
         * @return {@code true} if {@code instance} was passed on to a translator, {@code false} else
         */
        private boolean accept(T instance) {
            boolean accepted = false;
            try {
                Object data = getter.invoke(instance);
                if (null != data && null != translator) { // null is ok as data is alternative
                    translator.accept(data);
                    accepted = true;
                } else {
                    LoggerFactory.getLogger(DataMapper.class).warn(
                        "No data ({}) obtained from {} or no translator found ({})", data, instance, translator);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                LoggerFactory.getLogger(DataMapper.class).error("Cannot process {}/{}: {}", instance, 
                    getter.getName(), e.getMessage());
            }
            return accepted;
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
                } else {
                    LoggerFactory.getLogger(DataMapper.class).warn(
                        "No access mapping for class {}. Handler will be ignored", cls.getName());
                }
            }
        }
        
        @Override
        public void accept(T value) {
            if (null != value) {
                boolean accepted = false;
                for (MapperEntry<T> e: mapping.values()) {
                    accepted |= e.accept(value);
                }
                if (!accepted) {
                    LoggerFactory.getLogger(DataMapper.class).warn(
                        "Data {} was not processed further. {} mapper(s) available, but none reacted. If null, this "
                        + "could be a standard sink and normal but not expected.", value, mapping.size());
                }
            }
        }
        
    }
    
    /**
     * Extended {@link MappingConsumer} to take {@link BaseDataUnit#$period} and {@link BaseDataUnit#$repeats} into 
     * account.
     * 
     * @param <B> the mapped type
     * @author Holger Eichelberger, SSE
     */
    public static class BaseMappingConsumer <B extends BaseDataUnit> extends DataMapper.MappingConsumer<B> {
    
        private int period;
        
        /**
         * Creates a timed mapping consumer.
         * 
         * @param cls the type used for data input
         * @param period the initial time period between two tuples, usually 0
         */
        public BaseMappingConsumer(Class<B> cls, int period) {
            super(cls);
            this.period = period;
        }
        
        /**
         * Informs that data is available for testing and data ingestion may start.
         * Default is output on System.out. May be overridden.
         * 
         * @param value the data value
         */
        protected void infoGotData(B value) {
            LoggerFactory.getLogger(getClass()).info("Test data: {}", value);
        }
    
        @Override
        public void accept(B value) {
            boolean endless = value.get$repeats() < 0;
            boolean once = value.get$repeats() == 0;
            int count = 0;
            while (endless || once || count < value.get$repeats()) {
                infoGotData(value);
                super.accept(value);
                period = value.get$period();
                if (period > 0) {
                    TimeUtils.sleep(period);
                }
                count++;
                if (once) {
                    break;
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
        mapJsonData(stream, cls, cons, failOnUnknownProperties, null);
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
     * @param continueFunction optional function that tells the data mapper to go on reading the input, 
     *     may be <b>null</b> for none
     * @throws IOException if I/O or JSON parsing errors occur
     */
    public static <T> void mapJsonData(InputStream stream, Class<T> cls, Consumer<T> cons, 
        boolean failOnUnknownProperties, Supplier<Boolean> continueFunction) throws IOException {
        try {
            IOIterator<T> iter = mapJsonDataToIterator(stream, cls, failOnUnknownProperties);
            while (iter.hasNext() && (null == continueFunction || continueFunction.get())) {
                cons.accept(iter.next());
            }
        } catch (JsonProcessingException e) {
            throw new IOException(e);
        } finally {
            if (null != stream) {
                stream.close();
            }
        }
    }

    /**
     * An iterator that can throw {@link IOException}.
     * 
     * @param <T> the type of element
     * @author Holger Eichelberger, SSE
     */
    public interface IOIterator<T> {

        /**
         * Returns {@code true} if the iteration has more elements.
         *
         * @return {@code true} if the iteration has more elements
         * @throws IOException if providing the next element caused an I/O problem
         */
        public boolean hasNext() throws IOException;

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration
         * @throws NoSuchElementException if the iteration has no more elements
         * @throws IOException if checking for the next element caused an I/O problem
         */
        public T next() throws IOException;
        
    }

    /**
     * Maps the data in {@code stream} to instances of {@code cls}, one instance per line, returned in terms of an 
     * iterator. Ignores unknown attributes in {@code cls}.
     *  
     * @param <T> the type of data to read
     * @param stream the stream to read (may be <b>null</b> for none)
     * @param cls the type of data to read
     * @return the data iterator
     * @throws IOException if I/O or JSON parsing errors occur
     */
    public static <T> IOIterator<T> mapJsonDataToIterator(InputStream stream, Class<T> cls) 
        throws IOException {
        return mapJsonDataToIterator(stream, cls, false);
    }
    
    /**
     * Maps the data in {@code stream} to instances of {@code cls}, one instance per line, returned in terms of an 
     * iterator. Ignores unknown attributes in {@code cls}.
     *  
     * @param <T> the type of data to read
     * @param stream the stream to read (may be <b>null</b> for none)
     * @param cls the type of data to read
     * @param failOnUnknownProperties whether parsing shall be tolerant or not, the latter may be helpful for debugging
     * @return the data iterator
     * @throws IOException if I/O or JSON parsing errors occur
     */
    public static <T> IOIterator<T> mapJsonDataToIterator(InputStream stream, Class<T> cls, 
        boolean failOnUnknownProperties) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failOnUnknownProperties);
        JsonUtils.handleIipDataClasses(objectMapper);
        
        JsonFactory jf = new JsonFactory();
        JsonParser jp = jf.createParser(stream);
        jp.setCodec(objectMapper);
        jp.nextToken();
        return new IOIterator<T>() {

            @Override
            public boolean hasNext() throws IOException {
                return jp.hasCurrentToken();
            }

            @Override
            public T next() throws IOException {
                T data = jp.readValueAs(cls);
                jp.nextToken();
                return data;
            }
            
        };
    }

}
