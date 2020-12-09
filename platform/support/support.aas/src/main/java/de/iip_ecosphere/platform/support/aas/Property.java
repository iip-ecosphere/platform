/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Represents an AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Property extends Element, DataElement {

    /**
     * Encapsulated logic to build an AAS property.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface PropertyBuilder {

        /**
         * Sets the type of the property (seems to be optional).
         * 
         * @param type the type
         * @return <b>this</b>
         */
        public PropertyBuilder setType(Type type);
        
        /**
         * Sets the actual value the property.
         * 
         * @param value the value
         * @return <b>this</b>
         */
        public PropertyBuilder setValue(Object value);

        /**
         * Binds the value of the property against functions, e.g., accessing an underlying object.
         * 
         * @param get the getter function
         * @param set the setter function called when the setter of the property is called (may be <b>bull</b>,
         *   then a new value may override the getter and hold the value locally)
         * @return <b>this</b>
         * @throws IllegalArgumentException may be thrown if {@link #setType(Type)} was not called before
         */
        public PropertyBuilder bind(Supplier<Object> get, Consumer<Object> set);

        /**
         * Builds the instance.
         * 
         * @return the property instance
         */
        public Property build();

    }

    /**
     * Returns the value of this property.
     * 
     * @return the value
     * @throws ExecutionException if accessing the value fails
     */
    public Object getValue() throws ExecutionException;
    
    /**
     * Changes the value of this property.
     * 
     * @param value the new value
     * @throws ExecutionException if accessing the value fails
     */
    public void setValue(Object value) throws ExecutionException;
    
}
