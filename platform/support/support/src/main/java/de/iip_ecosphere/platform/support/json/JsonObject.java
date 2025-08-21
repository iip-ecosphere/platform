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

import java.util.Set;

/**
 * Represents a Json object. Abstracted from javax.json (J2EE) and Jersey.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface JsonObject extends JsonValue {

    /** 
     * Returns the object value to which the specified name is mapped. 
     * 
     * @param name the name whose associated value is to be returned
     * @return the object value to which the specified name is mapped, or <b>null</b> if this object contains no 
     *     mapping for {@code name}
     * @throws ClassCastException if the value to which the specified {@code name} is mapped is not assignable to 
     *     {@link JsonObject} type
     */
    public JsonObject getJsonObject(String name);

    /** 
     * Returns the object array to which the specified name is mapped. 
     * 
     * @param name the name whose associated array is to be returned
     * @return the array value to which the specified name is mapped, or <b>null</b> if this object contains no 
     *     mapping for {@code name}
     * @throws ClassCastException if the value to which the specified {@code name} is mapped is not assignable to 
     *     {@link JsonArray} type
     */
    public JsonArray getJsonArray(String name);

    /** 
     * Returns the number to which the specified name is mapped. 
     * 
     * @param name the name whose associated number is to be returned
     * @return the number to which the specified name is mapped, or <b>null</b> if this object contains no 
     *     mapping for {@code name}
     * @throws ClassCastException if the value to which the specified {@code name} is mapped is not assignable to 
     *     {@link JsonNumber} type
     */
    public JsonNumber getJsonNumber(String name);

    /** 
     * Returns the string to which the specified name is mapped. 
     * 
     * @param name the name whose associated string is to be returned
     * @return the string to which the specified name is mapped, or <b>null</b> if this object contains no 
     *     mapping for {@code name}
     * @throws ClassCastException if the value to which the specified {@code name} is mapped is not assignable to 
     *     {@link JsonString} type
     */
    public JsonString getJsonString(String name);

    /**
     * Returns the string value of the associated mapping for the specified name. 
     * 
     * @param name whose associated value is to be returned as string
     * @return the string value to which the specified name is mapped
     * @throws NullPointerException if the specified name doesn't have any mapping
     * @throws ClassCastException if the value for specified name mapping is not assignable
     */
    public String getString(String name);

    /**
     * Returns the int value of the associated mapping for the specified name. 
     * 
     * @param name whose associated value is to be returned as int
     * @return the int value to which the specified name is mapped
     * @throws NullPointerException if the specified name doesn't have any mapping
     * @throws ClassCastException if the value for specified name mapping is not assignable
     */
    public int getInt(String name);

    /**
     * Returns the boolean value of the associated mapping for the specified name. 
     * 
     * @param name whose associated value is to be returned as boolean
     * @return the boolean value to which the specified name is mapped
     * @throws NullPointerException if the specified name doesn't have any mapping
     * @throws ClassCastException if the value for specified name mapping is not assignable
     */
    public boolean getBoolean(String name);

    /**
     * Returns {@code true} if the associated value for the specified name is a JSON null.
     * 
     * @param name name whose associated value is checked
     * @return return {@code true} if the associated value is JSON null, otherwise {@code false}
     * @throws NullPointerException if the specified name doesn't have anymapping
     */
    public boolean isNull(String name);

    /**
     * Returns the value of the associated mapping for the specified name. 
     * 
     * @param name whose associated value is to be returned as boolean
     * @return the value to which the specified name is mapped
     * @throws NullPointerException if the specified name doesn't have any mapping
     * @throws ClassCastException if the value for specified name mapping is not assignable
     */
    public JsonValue getValue(String name);

    /**
     * Returns {@code true} if this object contains no key-value mappings.
     *
     * @return {@code true} if this object contains no key-value mappings
     */
    public boolean isEmpty();
    
    /**
     * Returns the keys of the key-value pairs of this object.
     * 
     * @return the keys
     */
    public Set<String> keys();

}
