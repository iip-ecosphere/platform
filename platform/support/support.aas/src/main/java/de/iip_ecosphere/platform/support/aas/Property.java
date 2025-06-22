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
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAction;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.Role;

/**
 * Represents an AAS Property.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Property extends Element, DataElement {

    /**
     * Encapsulated logic to build an AAS property.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface PropertyBuilder extends Builder<Property>, RbacReceiver<PropertyBuilder> {

        /**
         * A getter implementation that does nothing.
         */
        public static final Supplier<Object> WRITE_ONLY_FUNC = () -> null;

        /**
         * A setter implementation that does nothing.
         */
        @SuppressWarnings("unchecked")
        public static final Consumer<Object> READ_ONLY_FUNC = ((Consumer<Object> & Serializable) (o) -> { });        
        
        /**
         * A getter implementation that does nothing.
         */
        public static final Invokable WRITE_ONLY = Invokable.createSerializableInvokable(WRITE_ONLY_FUNC);

        /**
         * A setter implementation that does nothing.
         */
        public static final Invokable READ_ONLY = Invokable.createSerializableInvokable(READ_ONLY_FUNC);
        
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
         * Binds the value of the property against functions, e.g., accessing an underlying object. May apply tests 
         * to avoid known failures, e.g., regarding the type of the {@code invocable}. Use 
         * {@link #bindLazy(Invokable, Invokable)} to avoid such tests and to take the responsibility for potential 
         * later runtime errors.
         * 
         * @param get the getter function (use {@link #WRITE_ONLY} for write-only)
         * @param set the setter function called when the setter of the property is called (may be <b>bull</b>,
         *   then a new value may override the getter and hold the value locally, use {@link #READ_ONLY} for read-only))
         * @return <b>this</b>
         * @throws IllegalArgumentException may be thrown if {@link #setType(Type)} was not called before
         */
        public PropertyBuilder bind(Invokable get, Invokable set);

        /**
         * Binds the value of the property against functions, e.g., accessing an underlying object.
         * 
         * @param get the getter function (use {@link #WRITE_ONLY} for write-only)
         * @param set the setter function called when the setter of the property is called (may be <b>bull</b>,
         *   then a new value may override the getter and hold the value locally, use {@link #READ_ONLY} for read-only))
         * @return <b>this</b>
         * @see #bind(Invokable, Invokable)
         */
        public default PropertyBuilder bindLazy(Invokable get, Invokable set) {
            return bind(get, set);
        }

        /**
         * Creates RBAC rules for the submodel under creation and adds the roles to {@code auth}.
         * 
         * @param auth the authentication descriptor, may be <b>null</b>, ignored then
         * @param roles the roles to create the rules for
         * @param actions the permitted actions
         * @return <b>this</b> for chaining
         */
        public default PropertyBuilder rbac(AuthenticationDescriptor auth, Role[] roles, RbacAction... actions) {
            return RbacRoles.rbac(this, auth, roles, actions);
        }

        /**
         * Creates default RBAC rules based on the rules of the parent builder, i.e., the context this operation
         * is created within. The implementation decides whether/how this operation shall be implemented or whether
         * the respective rules are implicit.
         * 
         * @param auth the authentication descriptor, may be <b>null</b>, ignored then
         * @return <b>this</b> for chaining
         */
        public default PropertyBuilder rbac(AuthenticationDescriptor auth) {
            return this;
        }

        /**
         * Creates default RBAC rules and builds this operation.
         * 
         * @param auth the authentication descriptor, may be <b>null</b>, ignored then
         * @return return of {@link #build()}.
         * @see #rbac(AuthenticationDescriptor)
         */
        public default Property build(AuthenticationDescriptor auth) {
            rbac(auth);
            return build();
        }

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

    /**
     * Returns the description of this property, potentially in different languages.
     * 
     * @return the description, may be empty or <b>null</b>
     */
    public Map<String, LangString> getDescription();
    
}
