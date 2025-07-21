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

import de.iip_ecosphere.platform.support.plugins.PluginManager;

/**
 * Json interface.
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
     * Configures this instance for IIP conventions.
     * 
     * @param mapper the mapper to be configured
     * @return {@code mapper}
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
     * Configures thus instance so that Java field names map exactly to the given names.
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
    
}
