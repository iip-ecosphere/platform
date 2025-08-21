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

/**
 * Represents a Json array. Abstracted from javax.json (J2EE) and Jersey.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface JsonArray extends JsonValue {

    /**
    * Returns the number of elements in this array. If this array contains more than Integer.MAX_VALUE elements, 
    * returns Integer.MAX_VALUE.
    *
    * @return the number of elements in this array
    */
    public int size();
    
    /**
     * Returns the object value at the specified position in this array. 
     * 
     * @param index index of the value to be returned
     * @return the value at the specified position in this array
     * @throws IndexOutOfBoundsException if the index is out of range
     * @throws ClassCastException if the value at the specified position is not assignable to the {@link JsonObject} 
     *     type
     */
    public JsonObject getJsonObject(int index);

    /**
     * Returns the arrray value at the specified position in this array. 
     * 
     * @param index index of the value to be returned
     * @return the value at the specified position in this array
     * @throws IndexOutOfBoundsException if the index is out of range
     * @throws ClassCastException if the value at the specified position is not assignable to the {@link JsonArray} type
     */
    public JsonArray getJsonArray(int index);

    /**
     * Returns the number value at the specified position in this array. 
     * 
     * @param index index of the value to be returned
     * @return the value at the specified position in this array
     * @throws IndexOutOfBoundsException if the index is out of range
     * @throws ClassCastException if the value at the specified position is not assignable to the {@link JsonNumber} 
     *     type
     */
    public JsonNumber getJsonNumber(int index);

    /**
     * Returns the string value at the specified position in this array. 
     * 
     * @param index index of the value to be returned
     * @return the value at the specified position in this array
     * @throws IndexOutOfBoundsException if the index is out of range
     * @throws ClassCastException if the value at the specified position is not assignable to the {@link JsonString} 
     *     type
     */
    public JsonString getJsonString(int index);
    
    /**
    * Returns the string value at the specified position. 
    * 
    * @param index index of the JSON string value
    * @return the string value at the specified position
    * @throws IndexOutOfBoundsException if the index is out of range
    * @throws ClassCastException if the value at the specified position is not assignable
    */
    public String getString(int index);

    /**
    * Returns the int value at the specified position. 
    * 
    * @param index index of the JSON int value
    * @return the int value at the specified position
    * @throws IndexOutOfBoundsException if the index is out of range
    * @throws ClassCastException if the value at the specified position is not assignable
    */
    public int getInt(int index);

    /**
    * Returns the boolean value at the specified position. 
    * 
    * @param index index of the JSON boolean value
    * @return the boolean value at the specified position
    * @throws IndexOutOfBoundsException if the index is out of range
    * @throws ClassCastException if the value at the specified position is not assignable
    */
    public boolean getBoolean(int index);

    /**
     * Returns the element at the specified position in this list.
     * 
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException - if the index is out of range
     */
    public JsonValue getValue(int index);

    /**
     * Returns {@code true} if this array contains no elements.
     *
     * @return {@code true} if this array contains no elements
     */
    public boolean isEmpty();

    /**
     * Returns {@code true} if the value at the specified location in this array is Json null.
     * 
     * @param index index of the JSON null value
     * @return {@code true} if the value at the specified location is Json null, otherwise {@code false}
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public boolean isNull(int index);

}
