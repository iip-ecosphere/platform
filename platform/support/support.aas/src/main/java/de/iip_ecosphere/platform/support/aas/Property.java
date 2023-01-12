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

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.support.Builder;

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
    public interface PropertyBuilder extends Builder<Property> {

        /**
         * A getter implementation that does nothing.
         */
        public static final Supplier<Object> WRITE_ONLY = () -> null;

        /**
         * A setter implementation that does nothing.
         */
        @SuppressWarnings("unchecked")
        public static final Consumer<Object> READ_ONLY = ((Consumer<Object> & Serializable) (o) -> { });
        
        /**
         * Returns the parent builder.
         * 
         * @return the parent builder
         */
        public SubmodelElementContainerBuilder getParentBuilder();
        
        /**
         * Sets the description in terms of language strings.
         * 
         * @param description the description
         * @return <b>this</b>
         */
        public PropertyBuilder setDescription(LangString... description);
        
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
         * Returns the value of the property.
         * 
         * @return the value, may be <b>null</b> if not present
         * @throws ExecutionException if obtaining the property value fails
         */
        public Object getValue() throws ExecutionException;
        
        /**
         * Sets the actual type and value. [convenience]
         * 
         * @param type the type
         * @param value the value
         * @return <b>this</b>
         */
        public PropertyBuilder setValue(Type type, Object value);
        
        /**
         * Sets the semantic ID of the property in terms of a reference.
         * 
         * @param refValue the reference value (supported: irdi:<i>irdiValue</i>)
         * @return <b>this</b>
         */
        public PropertyBuilder setSemanticId(String refValue);

        /**
         * Binds the value of the property against functions, e.g., accessing an underlying object.
         * 
         * @param get the getter function (use {@link #WRITE_ONLY} for write-only)
         * @param set the setter function called when the setter of the property is called (may be <b>bull</b>,
         *   then a new value may override the getter and hold the value locally, use {@link #READ_ONLY} for read-only))
         * @return <b>this</b>
         * @throws IllegalArgumentException may be thrown if {@link #setType(Type)} was not called before
         */
        public PropertyBuilder bind(Supplier<Object> get, Consumer<Object> set);

    }

    /**
     * Returns the value of this property.
     * 
     * @return the value
     * @throws ExecutionException if accessing the value fails
     */
    public Object getValue() throws ExecutionException;

    /**
     * Returns the semantic id of the element.
     * 
     * @return the semantic id in textual format, e.g., with identifier prefix, or <b>null</b> if there is no 
     *     semantic id or no translation to string
     * @see #getSemanticId(boolean)
     */
    public default String getSemanticId() {
        return getSemanticId(false);
    }

    /**
     * Returns the semantic id of the element.
     * 
     * @param stripPrefix if the plain semantic id or the prefix shall also be emitted
     * @return the semantic id in textual format, e.g., with/out identifier prefix, or <b>null</b> if there is no 
     *     semantic id or no translation to string
     * @see #getSemanticId()
     */
    public String getSemanticId(boolean stripPrefix);
    
    /**
     * Changes the value of this property.
     * 
     * @param value the new value
     * @throws ExecutionException if accessing the value fails
     */
    public void setValue(Object value) throws ExecutionException;
    
}
