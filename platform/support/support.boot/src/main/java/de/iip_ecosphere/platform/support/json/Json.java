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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.iip_ecosphere.platform.support.Filter;
import de.iip_ecosphere.platform.support.plugins.PluginManager;

/**
 * Json interface. Requires an implementing plugin of type {@link Json} or an active {@link JsonProviderDescriptor}.
 * The default instances, e.g., for {@link #toJsonDflt(Object)} does not necessarily consider annotations. For 
 * annotations, please create a configured instance, e.g., via {@link #createInstance(Class)} 
 * or {@link #createInstance4All()}.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class Json {
    
    private static Json prototype;
    
    static {
        prototype = PluginManager.getPluginInstance(Json.class, JsonProviderDescriptor.class);
    }
    
    /**
     * Creates a configurable instance, no annotations are considered by default.
     * 
     * @return the Json instance
     */
    public static Json createInstance() {
        return prototype.createInstanceImpl(false);
    }

    /**
     * Creates a configurable instance, all annotations are considered by default.
     * 
     * @return the Json instance
     */
    public static Json createInstance4All() {
        return prototype.createInstanceImpl(true);
    }

    /**
     * Creates a pre-configured configurable instance by applying {@link Json#configureFor(Class)} so that
     * usual annotations are considered.
     * 
     * @param cls the class to configure for
     * @return the Json instance
     */
    public static Json createInstance(Class<?> cls) {
        return prototype.createInstanceImpl(false).configureFor(cls);
    }

    /**
     * Creates a pre-configured configurable instance by applying {@link Json#configureFor(Class)} so that
     * usual annotations are considered.
     * 
     * @param cls the classes to configure for
     * @return the Json instance
     */
    public static Json createInstance(Class<?>... cls) {
        Json result = prototype.createInstanceImpl(false);
        for (Class<?> c: cls) {
            result.configureFor(c);
        }
        return result;
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
     * @param considerAnnotations whether annotations shall be considered automatically
     * @return the instance
     */
    protected abstract Json createInstanceImpl(boolean considerAnnotations);
    
    /**
     * Turns an {@code object} to JSON.
     * 
     * @param obj the object (may be <b>null</b>), must have getters/setters for all attributes and a no-arg constructor
     *   no-arg constructor
     * @return the JSON string or an empty string in case of problems/no address
     * @throws IOException in case of serious issues
     * @see #fromJson(Object, Class)
     */
    public abstract String toJson(Object obj) throws IOException;

    /**
     * Turns an {@code object} to JSON without throwing exceptions.
     * 
     * @param obj the object (may be <b>null</b>), must have getters/setters for all attributes and a no-arg constructor
     *   no-arg constructor
     * @return the JSON string or an empty string in case of problems/no address
     */
    public String toJsonQuiet(Object obj) {
        String result;
        try {
            result = toJson(obj);
        } catch (IOException e) {
            result = "";
        }
        return result;
    }

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
     * Reads a typed List from a JSON string.
     * 
     * @param <R> the entity type
     * @param json the JSON value (usually a String)
     * @param cls the class of the entity type to read
     * @return the list or <b>null</b> if reading fails
     * @see #toJson(Object)
     */
    public abstract <R> java.util.List<R> listFromJson(Object json, Class<R> cls);
    
    /**
     * Reads a typed Map from a JSON string.
     * 
     * @param <K> the key type
     * @param <V> the value type
     * @param json the JSON value (usually a String)
     * @param keyCls the class of the key type to read
     * @param valueCls the class of the value type to read
     * @return the map or <b>null</b> if reading fails
     * @see #toJson(Object)
     */
    public abstract <K, V> Map<K, V> mapFromJson(Object json, Class<K> keyCls, Class<V> valueCls);
    
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
     * Reads a value from a string using the default instance.
     * 
     * @param <T> the result type
     * @param src the value as string
     * @param cls the value type
     * @throws IOException if reading fails
     */
    public static <T> T readValueDflt(String src, Class<T> cls) throws IOException {
        return prototype.readValue(src, cls);
    }
    
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
     * Reads a value from a byte array using the default instance.
     * 
     * @param <T> the result type
     * @param src the value as byte array
     * @param cls the value type
     * @throws IOException if reading fails
     */
    public static <T> T readValueDflt(byte[] src, Class<T> cls) throws IOException {
        return prototype.readValue(src, cls);
    }

    /**
     * Writes a given values into a byte array representation.
     * 
     * @param value the value to write
     * @return the byte array representation
     * @throws IOException if writing fails
     */
    public abstract byte[] writeValueAsBytes(Object value) throws IOException;
    
    /**
     * Writes a given values into a string representation.
     * 
     * @param value the value to write
     * @return the string representation
     * @throws IOException if writing fails
     */
    public abstract String writeValueAsString(Object value) throws IOException;
    
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
     * @see #toJson(Object)
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
     * @see #fromJson(Object, Class)
     */
    public static <R> R fromJsonDflt(Object json, Class<R> cls) throws IOException {
        return prototype.fromJson(json, cls);
    }

    /**
     * Reads a typed List from a JSON string.
     * 
     * @param <R> the entity type
     * @param json the JSON value (usually a String)
     * @param cls the class of the entity type to read
     * @return the list or <b>null</b> if reading fails
     * @see #listFromJson(Object, Class)
     */
    public static <R> java.util.List<R> listFromJsonDflt(Object json, Class<R> cls) {
        return prototype.listFromJson(json, cls);
    }
    
    /**
     * Reads a typed Map from a JSON string.
     * 
     * @param <K> the key type
     * @param <V> the value type
     * @param json the JSON value (usually a String)
     * @param keyCls the class of the key type to read
     * @param valueCls the class of the value type to read
     * @return the map or <b>null</b> if reading fails
     * @see #mapFromJson(Object, Class, Class)
     */
    public static <K, V> Map<K, V> mapFromJsonDflt(Object json, Class<K> keyCls, Class<V> valueCls) {
        return prototype.mapFromJson(json, keyCls, valueCls);
    }    
    
    /**
     * Writes a given values into a byte array representation.
     * 
     * @param value the value to write
     * @return the byte array representation
     * @throws IOException if writing fails
     */
    public static byte[] writeValueAsBytesDflt(Object value) throws IOException {
        return prototype.writeValueAsBytes(value);
    }
    
    /**
     * Writes a given values into a string representation.
     * 
     * @param value the value to write
     * @return the string representation
     * @throws IOException if writing fails
     */
    public static String writeValueAsStringDflt(Object value) throws IOException {
        return prototype.writeValueAsString(value);
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
     * Enables a filter for the given id (as declared by {@link Filter}) that excepts the given fields. Combination
     * of annotation and configured filter in case that excepts cannot be setup directly on classes or the class
     * object is not available/accessible for some reason. Similar to {@link #filterAllExceptFields(String...)}, which
     * is applied to all classes to be serialized.
     * 
     * @param filterId the filter id
     * @param fieldNames the fields to except
     * @return <b>this</b> for chaining
     */
    public abstract Json configureExceptFieldsFilter(String filterId, String... fieldNames);
    
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
     * @param <T> the (enum) type
     * @param type the type to map
     */
    public <T> EnumMapping<T> createEnumMapping(Class<T> type) {
        return createEnumMapping(type, null);
    }

    /**
     * Creates a mapping specification, with mapping.
     * 
     * @param <T> the (enum) type
     * @param type the type to map
     * @param mapping the mapping of values
     */
    public abstract <T> EnumMapping<T> createEnumMapping(Class<T> type, Map<String, T> mapping);

    /**
     * Creates a mapping specification based on {@link #createEnumValueMap(Class)}.
     * 
     * @param <T> the enum type
     * @param type the enum to map
     */
    public <T extends Enum<T>> EnumMapping<T> createEnumValueMapping(Class<T> type) {
        return createEnumMapping(type, createEnumValueMap(type));
    }

    /**
     * Declares enums and their mappings.
     * 
     * @param mappings the mappings
     * @return <b>this</b> for chaining
     */
    public abstract Json declareEnums(EnumMapping<?>... mappings);

    /**
     * Creates a default value mapping for the given enum.
     * 
     * @param <T> the enum type
     * @param enm the enum to map/take values from
     * @return the name-value map for {@code enum}
     */
    public static <T extends Enum<T>> Map<String, T> createEnumValueMap(Class<? extends T> enm) {
        Map<String, T> result = new HashMap<>();
        if (enm.isEnum()) {
            for (T c : enm.getEnumConstants()) {
                result.put(c.name(), c);
            }
        }
        return result;
    }
    
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

    /**
     * Creates a JSON object for individual access from {@code string}.
     * 
     * @param string the string
     * @return the JSON object
     * @throws IOException if the object cannot be read/constructed
     * @see #createObject(Reader)
     */
    public static JsonObject createObject(String string) throws IOException {
        return createObject(new StringReader(string));
    }

    /**
     * Creates a JSON object for individual access from the byte array {@code data}.
     * 
     * @param data the byte array
     * @return the JSON object
     * @throws IOException if the object cannot be read/constructed
     * @see #createObject(Reader)
     */
    public static JsonObject createObject(byte[] data) throws IOException {
        return createObject(new InputStreamReader(new ByteArrayInputStream(data))); // or new String(data)??
    }

    /**
     * Creates a JSON object for individual access from {@code reader}.
     * 
     * @param reader the reader
     * @return the JSON object
     * @throws IOException if the object cannot be read/constructed
     */
    public static JsonObject createObject(Reader reader) throws IOException {
        return prototype.createObjectImpl(reader);
    }

    /**
     * Creates a JSON object for individual access from {@code reader}.
     * 
     * @param reader the reader
     * @return the JSON object
     * @throws IOException if the object cannot be read/constructed
     */
    protected abstract JsonObject createObjectImpl(Reader reader) throws IOException;

    /**
     * Creates a JSON object builder.
     * 
     * @return the JSON object builder
     */
    public static JsonObjectBuilder createObjectBuilder() {
        return prototype.createObjectBuilderImpl();
    }
    
    /**
     * Creates a JSON object builder.
     * 
     * @return the JSON object builder
     */
    protected abstract JsonObjectBuilder createObjectBuilderImpl();

    /**
     * Creates a JSON array builder.
     * 
     * @return the JSON array builder
     */
    public static JsonArrayBuilder createArrayBuilder() {
        return prototype.createArrayBuilderImpl();
    }
    
    /**
     * Creates a JSON array builder.
     * 
     * @return the JSON array builder
     */
    protected abstract JsonArrayBuilder createArrayBuilderImpl();

    /**
     * Parses text into an interatively parsable structure.
     * 
     * @param text the JSON text to parse
     * @return the iterator, may indicate invalid data
     */
    protected abstract JsonIterator parseImpl(String text);

    /**
     * Parses data into an interatively parsable structure.
     * 
     * @param data the JSON data to parse
     * @return the iterator, may indicate invalid data
     */
    protected abstract JsonIterator parseImpl(byte[] data);

    /**
     * Parses text into an interatively parsable structure.
     * 
     * @param text the JSON text to parse
     * @return the iterator, may indicate invalid data
     */
    public static JsonIterator parse(String text) {
        return prototype.parseImpl(text);
    }

    /**
     * Parses data into an interatively parsable structure.
     * 
     * @param data the JSON data to parse
     * @return the iterator, may indicate invalid data
     */
    public static JsonIterator parse(byte[] data) {
        return prototype.parseImpl(data);
    }

    /**
     * Creates a generator instance for {@code writer}.
     * 
     * @param writer the writer
     * @return the generator
     * @throws IOException if the generator cannot be created
     */
    protected abstract JsonGenerator createGeneratorImpl(Writer writer) throws IOException;

    /**
     * Creates a generator instance for {@code writer}.
     * 
     * @param writer the writer
     * @return the generator
     * @throws IOException if the generator cannot be created
     */
    public static JsonGenerator createGenerator(Writer writer) throws IOException {
        return prototype.createGeneratorImpl(writer);
    }

}
