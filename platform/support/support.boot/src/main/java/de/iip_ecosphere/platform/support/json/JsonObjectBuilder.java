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

import de.iip_ecosphere.platform.support.Builder;

/**
 * Creates Json objects. Abstracted from JavaEE/glassfisch.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface JsonObjectBuilder extends Builder<JsonObject> {
    
    /**
     * Adds a name/value pair to the JSON object associated with this object builder. 
     * If the object contains a mapping for the specified name, this method replaces the old value with the 
     * specified value.
     * 
     * @param name name in the name/value pair
     * @param value value in the name/value pair
     * @return <b>this</b> for chaining
     * @throws NullPointerException if the specified name is <b>null</b>
     */
    public JsonObjectBuilder add(String name, String value);

    /**
     * Adds a name/value pair to the JSON object associated with this object builder. 
     * If the object contains a mapping for the specified name, this method replaces the old value with the 
     * specified value.
     * 
     * @param name name in the name/value pair
     * @param value value in the name/value pair
     * @return <b>this</b> for chaining
     * @throws NullPointerException if the specified name is <b>null</b>
     */
    public JsonObjectBuilder add(String name, int value);

    /**
     * Adds a name/value pair to the JSON object associated with this object builder. 
     * If the object contains a mapping for the specified name, this method replaces the old value with the 
     * specified value.
     * 
     * @param name name in the name/value pair
     * @param value value in the name/value pair
     * @return <b>this</b> for chaining
     * @throws NullPointerException if the specified name is <b>null</b>
     */
    public JsonObjectBuilder add(String name, double value);

    /**
     * Adds a name/value pair to the JSON object associated with this object builder. 
     * If the object contains a mapping for the specified name, this method replaces the old value with the 
     * specified value.
     * 
     * @param name name in the name/value pair
     * @param value value in the name/value pair
     * @return <b>this</b> for chaining
     * @throws NullPointerException if the specified name is <b>null</b>
     */
    public JsonObjectBuilder add(String name, boolean value);

    /**
     * Adds a name/value pair to the JSON object associated with this object builder. 
     * If the object contains a mapping for the specified name, this method replaces the old value with the 
     * specified value.
     * 
     * @param name name in the name/value pair
     * @param value value in the name/value pair
     * @return <b>this</b> for chaining
     * @throws NullPointerException if the specified name is <b>null</b>
     */
    public JsonObjectBuilder add(String name, JsonString value);

    /**
     * Adds a name/value pair to the JSON object associated with this object builder. 
     * If the object contains a mapping for the specified name, this method replaces the old value with the 
     * specified value.
     * 
     * @param name name in the name/value pair
     * @param value value in the name/value pair
     * @return <b>this</b> for chaining
     * @throws NullPointerException if the specified name is <b>null</b>
     */
    public JsonObjectBuilder add(String name, JsonNumber value);

    /**
     * Adds a name/value pair to the JSON object associated with this object builder. 
     * If the object contains a mapping for the specified name, this method replaces the old value with the 
     * specified value.
     * 
     * @param name name in the name/value pair
     * @param value value in the name/value pair
     * @return <b>this</b> for chaining
     * @throws NullPointerException if the specified name is <b>null</b>
     */
    public JsonObjectBuilder add(String name, JsonArray value);

    /**
     * Adds a name/value pair to the JSON object associated with this object builder. 
     * If the object contains a mapping for the specified name, this method replaces the old value with the 
     * specified value.
     * 
     * @param name name in the name/value pair
     * @param value value in the name/value pair
     * @return <b>this</b> for chaining
     * @throws NullPointerException if the specified name is <b>null</b>
     */
    public JsonObjectBuilder add(String name, JsonObject value);

    /**
     * Adds a name/null pair to the JSON object associated with this object builder. 
     * If the object contains a mapping for the specified name, this method replaces the old value with the 
     * specified value.
     * 
     * @param name name in the name/value pair
     * @return <b>this</b> for chaining
     * @throws NullPointerException if the specified name is <b>null</b>
     */
    public JsonObjectBuilder addNull(String name);

    /**
     * Adds a name/value pair to the JSON object associated with this object builder via an array builder. 
     * If the object contains a mapping for the specified name, this method replaces the old value with the 
     * specified value.
     * 
     * @param name name in the name/value pair
     * @param builder the array builder
     * @return <b>this</b> for chaining
     * @throws NullPointerException if the specified name is <b>null</b>
     */
    public JsonObjectBuilder add(String name, JsonArrayBuilder builder);

    /**
     * Adds a name/value pair to the JSON object associated with this object builder via an object builder. 
     * If the object contains a mapping for the specified name, this method replaces the old value with the 
     * specified value.
     * 
     * @param name name in the name/value pair
     * @param builder the object builder
     * @return <b>this</b> for chaining
     * @throws NullPointerException if the specified name is <b>null</b>
     */
    public JsonObjectBuilder add(String name, JsonObjectBuilder builder);

    /**
     * Remove the name/value pair from the JSON object associated with this object builder if it is present.
     * 
     * @param name the name in the name/value pair to be removed
     * @return <b>this</b> for chaining
     * @throws NullPointerException if the specified name is <b>null</b>
     */
    public JsonObjectBuilder remove(String name);

}
