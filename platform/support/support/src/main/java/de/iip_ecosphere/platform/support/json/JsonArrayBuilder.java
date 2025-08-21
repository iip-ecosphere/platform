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
 * Creates Json arrays. Abstracted from JavaEE/glassfisch.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface JsonArrayBuilder extends Builder<JsonArray> {
    
    /**
     * Adds a value to the JSON object associated with this array builder. 
     * 
     * @param value the value to add
     * @return <b>this</b> for chaining
     */
    public JsonArrayBuilder add(String value);

    /**
     * Adds a value to the JSON object associated with this array builder. 
     * 
     * @param value the value to add
     * @return <b>this</b> for chaining
     */
    public JsonArrayBuilder add(int value);

    /**
     * Adds a value to the JSON object associated with this array builder. 
     * 
     * @param value the value to add
     * @return <b>this</b> for chaining
     */
    public JsonArrayBuilder add(double value);

    /**
     * Adds a value to the JSON object associated with this array builder. 
     * 
     * @param value the value to add
     * @return <b>this</b> for chaining
     */
    public JsonArrayBuilder add(boolean value);

    /**
     * Adds a value to the JSON object associated with this array builder. 
     * 
     * @param value the value to add
     * @return <b>this</b> for chaining
     */
    public JsonArrayBuilder add(JsonString value);

    /**
     * Adds a value to the JSON object associated with this array builder. 
     * 
     * @param value the value to add
     * @return <b>this</b> for chaining
     */
    public JsonArrayBuilder add(JsonNumber value);

    /**
     * Adds a value to the JSON object associated with this array builder. 
     * 
     * @param value the value to add
     * @return <b>this</b> for chaining
     */
    public JsonArrayBuilder add(JsonArray value);

    /**
     * Adds a value to the JSON object associated with this array builder. 
     * 
     * @param value the value to add
     * @return <b>this</b> for chaining
     */
    public JsonArrayBuilder add(JsonObject value);

    /**
     * Adds a null value to the JSON object associated with this array builder. 
     * 
     * @return <b>this</b> for chaining
     */
    public JsonArrayBuilder addNull();

    /**
     * Adds a value to the JSON object associated with this array builder via an array builder. 
     * 
     * @param builder the object builder
     * @return <b>this</b> for chaining
     */
    public JsonArrayBuilder add(JsonArrayBuilder builder);

    /**
     * Adds a value to the JSON object associated with this array builder via an object builder. 
     * 
     * @param builder the object builder
     * @return <b>this</b> for chaining
     */
    public JsonArrayBuilder add(JsonObjectBuilder builder);
    
}
