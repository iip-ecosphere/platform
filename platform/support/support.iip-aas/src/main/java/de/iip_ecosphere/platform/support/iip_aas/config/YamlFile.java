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

package de.iip_ecosphere.platform.support.iip_aas.config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Low-level YAML file support.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlFile {
    
    /**
     * Prevents external instantiation.
     */
    private YamlFile() {
    }
    
    /**
     * Reads a YAML file into a generic object-based structure. Does not close {@code in}.
     * 
     * @param in the input stream to read from
     * @return the object structure
     * @throws IOException if reading from {@code in} fails
     */
    public static Object read(InputStream in) throws IOException {
        Yaml yaml = new Yaml();
        return yaml.load(in);
    }

    /**
     * Turns an object into a string.
     * 
     * @param data the data
     * @param dflt the default value if {@code data} is <b>null</b>
     * @return the string value, possibly {@code dflt}
     */
    public static String asString(Object data, String dflt) {
        String result;
        if (null == data) {
            result = dflt;
        } else {
            result = data.toString();
        }
        return result;
    }

    /**
     * Converts a plain object, e.g., returned from {@link #read(InputStream)} to a map.
     * 
     * @param data the data
     * @return the map, possibly empty if not convertible
     */
    @SuppressWarnings("unchecked")
    public static Map<Object, Object> asMap(Object data) {
        Map<Object, Object> result;
        if (data instanceof Map) {
            result = (Map<Object, Object>) data;
        } else {
            result = new HashMap<Object, Object>();
        }
        return result;
    }

    /**
     * Converts a plain object, e.g., returned from {@link #read(InputStream)} to a list.
     * 
     * @param data the data
     * @return the list, possibly empty if not convertible
     */
    @SuppressWarnings("unchecked")
    public static List<Object> asList(Object data) {
        List<Object> result;
        if (data instanceof List) {
            result = (List<Object>) data;
        } else {
            result = new ArrayList<Object>();
        }
        return result;
    }
    
    /**
     * Reads a field from a plain object, e.g., returned from {@link #read(InputStream)}.
     * 
     * @param data the data
     * @param field the field to read, may be a sequence of nested fields
     * @return the field, possibly <b>null</b> if not found
     */
    public static Object getField(Object data, String... field) {
        Object result = data;
        for (String f: field) {
            result = asMap(result).get(f);
        }
        return result;
    }

    /**
     * Reads a field as a map from a plain object, e.g., returned from {@link #read(InputStream)}.
     * 
     * @param data the data
     * @param field the field to read
     * @return the field as map, may be a sequence of nested fields, possibly empty if not found
     * @see #getField(Object, String...)
     * @see #asMap(Object)
     */
    public static Map<Object, Object> getFieldAsMap(Object data, String... field) {
        return asMap(getField(data, field));
    }

    /**
     * Reads a field as a map from a plain object, e.g., returned from {@link #read(InputStream)}.
     * 
     * @param data the data
     * @param field the field to read
     * @return the field as map, possibly empty if not found
     * @see #getField(Object, String...)
     * @see #asList(Object)
     */
    public static List<Object> getFieldAsList(Object data, String field) {
        return asList(getField(data, field));
    }

    /**
     * Reads a field as a string value from a plain object, e.g., returned from {@link #read(InputStream)}.
     * 
     * @param data the data
     * @param field the field to read
     * @param dflt what to return if there is no field/no convertible value
     * @return the field as string value, possibly {@code dflt} if not found
     * @see #getField(Object, String...)
     * @see #asString(Object, String)
     */
    public static String getFieldAsString(Object data, String field, String dflt) {
        return asString(getField(data, field), dflt);
    }

    /**
     * Somehow, Snakeyaml does not take up a generic type in a list and delivers a list of 
     * hashmaps instead of a list of objects of that type. This method fixes the instances
     * if the root cause cannot be determined. It also handles {@code list} is <b>null</b>
     * and logs potential exceptions.
     * 
     * @param <T> the expected type of objects
     * @param list the list
     * @param cls the class denoting the expected type
     * @return {@code list} eventually with modified entries
     */
    public static <T> List<T> fixListSafe(List<T> list, Class<T> cls) {
        List<T> result = list;
        if (list != null) {
            try {
                result = fixList(list, cls);
            } catch (ExecutionException e) {
                LoggerFactory.getLogger(YamlFile.class).error(e.getMessage());
            }
        }
        return result;
    }

    /**
     * Somehow, Snakeyaml does not take up a generic type in a list and delivers a list of 
     * hashmaps instead of a list of objects of that type. This method fixes the instances
     * if the root cause cannot be determined.
     * 
     * @param <T> the expected type of objects
     * @param list the list
     * @param cls the class denoting the expected type
     * @return {@code list} eventually with modified entries
     * @throws ExecutionException if creation of objects fails
     */
    public static <T> List<T> fixList(List<T> list, Class<T> cls) throws ExecutionException {
        List<T> result = new ArrayList<T>(); // force a "conversion"
        for (int i = 0; i < list.size(); i++) {
            Object o = list.get(i);
            if (o instanceof HashMap) {
                @SuppressWarnings("unchecked")
                HashMap<Object, Object> map = (HashMap<Object, Object>) o;
                T s = createInstance(cls);
                for (Map.Entry<Object, Object> e : map.entrySet()) {
                    Field f = findField(cls, e.getKey().toString());
                    if (f != null) {
                        f.setAccessible(true);
                        try {
                            f.set(s, e.getValue());
                        } catch (IllegalArgumentException | IllegalAccessException e1) {
                            LoggerFactory.getLogger(YamlFile.class).error("Cannot set field {} on YamlServer: {}", 
                                f.getName(), e1.getMessage());
                        }
                    }
                }
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Creates an instance of class {@code cls} and wraps all exceptions.
     * 
     * @param <T> the type of the instance
     * @param cls the class stating the type
     * @return the instance
     * @throws ExecutionException if the creation fails, e.g., no public no-arg constructor
     */
    private static <T> T createInstance(Class<T> cls) throws ExecutionException {
        try {
            return cls.getConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException 
            | IllegalArgumentException | InvocationTargetException | SecurityException e) {
            throw new ExecutionException("Cannot create instanceo of type " + cls.getName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Finds a field recursively in {@code cls}.
     * 
     * @param cls the class to start searching with
     * @param name the field name
     * @return the field or <b>null</b> if there is none
     */
    public static Field findField(Class<?> cls, String name) {
        Field result = null;
        try {
            result = cls.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            if (cls.getSuperclass() != Object.class) {
                result = findField(cls.getSuperclass(), name);
            }
        }
        return result;
    }

}
