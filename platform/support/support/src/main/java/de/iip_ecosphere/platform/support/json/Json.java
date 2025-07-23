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

package de.iip_ecosphere.platform.support.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import de.iip_ecosphere.platform.support.plugins.PluginManager;

/**
 * Json interface. Requires an implementing plugin of type {@link Json} or an active {@link JsonProviderDescriptor}.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class Json {
    
    private static Json prototype;
    
    static {
        prototype = PluginManager.getPluginInstance(Json.class, JsonProviderDescriptor.class);
    }
    
    /**
     * Creates a configurable instance.
     * 
     * @return the Json instance
     */
    public static Json createInstance() {
        return prototype.createInstanceImpl();
    }
    
    /**
     * Manually sets the instance. Shall not be needed, but may be required in some tests.
     * 
     * @param json the Json instance
     */
    public static void setPrototype(Json json) {
        if (null != json) {
            prototype = json;
        }
    }

    /**
     * Creates the actual instance.
     * 
     * @return the instance
     */
    protected abstract Json createInstanceImpl();
    
    /**
     * Turns an {@code object} to JSON.
     * 
     * @param obj the object (may be <b>null</b>), must have getters/setters for all attributes and a no-arg constructor
     *   no-arg constructor
     * @return the JSON string or an empty string in case of problems/no address
     * @see #fromJson(Object, Class)
     */
    public abstract String toJson(Object obj) throws IOException;

    /**
     * Reads an Object from a JSON string.
     * 
     * @param <R> the object type, must have getters/setters for all attributes and a no-arg constructor
     * @param json the JSON value (usually a String)
     * @param cls the class of the type to read
     * @return the object or <b>null</b> if reading fails
     * @see #toJson(Object)
     */
    public abstract <R> R fromJson(Object json, Class<R> cls) throws IOException;

    /**
     * Reads a value from a string.
     * 
     * @param <T> the result type
     * @param src the value as string
     * @param cls the value type
     * @throws IOException if reading fails
     */
    public abstract <T> T readValue(String src, Class<T> cls) throws IOException;
    
    /**
     * Reads a value from a byte array.
     * 
     * @param <T> the result type
     * @param src the value as byte array
     * @param cls the value type
     * @throws IOException if reading fails
     */
    public abstract <T> T readValue(byte[] src, Class<T> cls) throws IOException;

    /**
     * Writes a given values into a byte array representation.
     * 
     * @param value the value to write
     * @return the byte array representation
     * @throws IOException if writing fails
     */
    public abstract byte[] writeValueAsBytes(Object value) throws IOException;
    
    /**
     * Convenience method for doing two-step conversion from given value, into
     * instance of given value type, by writing value into temporary buffer
     * and reading from the buffer into specified target type.
     * 
     * @param value the value to convert
     * @param cls the target type
     *      
     * @throws IllegalArgumentException If conversion fails due to incompatible type;
     *    if so, root cause will contain underlying checked exception data binding
     *    functionality threw
     */
    public abstract <T> T convertValue(Object value, Class<T> cls) throws IllegalArgumentException;
    
    /**
     * Turns an {@code object} to JSON on the default instance.
     * 
     * @param obj the object (may be <b>null</b>), must have getters/setters for all attributes and a no-arg constructor
     *   no-arg constructor
     * @return the JSON string or an empty string in case of problems/no address
     * @see #fromJson(Object, Class)
     */
    public static String toJsonDflt(Object obj) throws IOException {
        return prototype.toJson(obj);
    }

    /**
     * Reads an Object from a JSON string on the default instance.
     * 
     * @param <R> the object type, must have getters/setters for all attributes and a no-arg constructor
     * @param json the JSON value (usually a String)
     * @param cls the class of the type to read
     * @return the object or <b>null</b> if reading fails
     * @see #toJson(Object)
     */
    public static <R> R fromJsonDflt(Object json, Class<R> cls) throws IOException {
        return prototype.fromJson(json, cls);
    }
    
    /**
     * Configures this instance for {@code cls} by considering the annotations in {@code cls}.
     * 
     * @param cls the class
     * @return <b>this</b> for chaining
     */
    public abstract Json configureFor(Class<?> cls);
    
    /**
     * Configures this instance for IIP conventions.
     * 
     * @param mapper the mapper to be configured
     * @return <b>this</b> for chaining
     */
    public abstract Json handleIipDataClasses();
    
    /**
     * Configures this instance for the given {@code fieldNames} as optional during deserialization.
     * 
     * @param cls the cls the class {@code fieldNames} are member of
     * @param fieldNames the field names (names of Java fields)
     * @return <b>this</b> for chaining
     */
    public abstract Json defineOptionals(Class<?> cls, String... fieldNames);

    /**
     * Configures this instance so that Java field names map exactly to the given names.
     * 
     * @param fieldNames the field names (names of JSON/Java fields)
     * @return <b>this</b> for chaining
     */
    public abstract Json defineFields(String... fieldNames);
    
    /**
     * Configures this instance so that it excludes the {@code fieldNames} to be excluded.
     * 
     * @param fieldNames the field names
     * @return <b>this</b> for chaining
     */
    public abstract Json exceptFields(String... fieldNames);

    /**
     * Configures this instance to filter to all given field names. 
     * 
     * @param fieldNames the field names that shall be excluded
     * @return <b>this</b> for chaining
     */
    public abstract Json filterAllExceptFields(String... fieldNames);
    
    /**
     * Configures this instance to fail/fail not on unknown fields/properties.
     * 
     * @param fail to fail or not to fail
     * @return <b>this</b> for chaining
     */
    public abstract Json failOnUnknownProperties(boolean fail);

    /**
     * Specifies the mapping of an enumeration for serialization/deserialization.
     * 
     * @param <T> the enumeration type to map
     * @author Holger Eichelberger, SSE
     */
    public interface EnumMapping<T> {
        
        /**
         * Adds a mapping.
         * 
         * @param name the name
         * @param value the mapped value
         */
        public void addMapping(String name, T value);
        
        /**
         * Returns the enum type.
         * 
         * @return the enum type
         */
        public Class<T> getType();
        
    }

    /**
     * Creates a mapping specification, with no mapping, for incremental creation.
     * 
     * @param type the type to map
     */
    public <T> EnumMapping<T> createEnumMapping(Class<T> type) {
        return createEnumMapping(type, null);
    }

    /**
     * Creates a mapping specification, with mapping.
     * 
     * @param type the type to map
     * @param mapping the mapping of values
     */
    public abstract <T> EnumMapping<T> createEnumMapping(Class<T> type, Map<String, T> mapping);
    
    /**
     * Declares enums and their mappings.
     * 
     * @param mappings the mappings
     * @return <b>this</b> for chaining
     */
    public abstract Json declareEnums(EnumMapping<?>... mappings);

    /**
     * Configures this instance for lazy serialization ignoring given classes and members.
     * 
     * @param ignore, classes (also as return types) and (reflection) fields that shall be ignored
     * @return <b>this</b> for chaining
     */
    public abstract Json configureLazy(Set<Object> ignore); 

    /**
     * Creates an iterator over the information in {@code stream} assuming a heterogeneous collection of {@code cls}.
     * 
     * @param <T> the element type
     * @param stream the input stream to read
     * @param cls the element type
     * @return an iterator over the element types
     * @throws IOException if accessing the stream fails
     */
    public abstract <T> IOIterator<T> createIterator(InputStream stream, Class<T> cls) throws IOException;

}
