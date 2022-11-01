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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * @see #getField(Object, String)
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
     * @see #getField(Object, String)
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
     * @see #getField(Object, String)
     * @see #asString(Object)
     */
    public static String getFieldAsString(Object data, String field, String dflt) {
        return asString(getField(data, field), dflt);
    }

}
