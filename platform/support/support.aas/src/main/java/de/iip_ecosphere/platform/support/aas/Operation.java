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

import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAction;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.Role;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;

/**
 * Represents an AAS operation.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Operation extends Element, SubmodelElement {

    /**
     * Builds an AAS operation.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface OperationBuilder extends Builder<Operation>, RbacReceiver<OperationBuilder> {
        
        public static final String DEFAULT_RETURN_VAR_NAME = "result";
        
        /**
         * Returns the parent builder.
         * 
         * @return the parent builder
         */
        public SubmodelElementContainerBuilder getParentBuilder();

        /**
         * Sets the semantic ID of the property in terms of a reference.
         * 
         * @param semanticId the reference value (supported: irdi:<i>irdiValue</i>)
         * @return <b>this</b>
         */
        public OperationBuilder setSemanticId(String semanticId);

        /**
         * Adds an input variable to the operation.
         * 
         * @param idShort the short id of the variable
         * @param type the type of the variable (may be <b>null</b> for left undefined)
         * @return <b>this</b>
         */
        public default OperationBuilder addInputVariable(String idShort, Type type) {
            return addInputVariable(idShort, type, null);
        }
        
        /**
         * Adds an output variable to the operation.
         * 
         * @param idShort the short id of the variable
         * @param type the type of the variable (may be <b>null</b> for left undefined)
         * @return <b>this</b>
         */
        public default OperationBuilder addOutputVariable(String idShort, Type type) {
            return addOutputVariable(idShort, type, null);
        }
        
        /**
         * Adds an input-output variable to the operation.
         * 
         * @param idShort the short id of the variable
         * @param type the type of the variable (may be <b>null</b> for left undefined)
         * @return <b>this</b>
         */
        public default OperationBuilder addInOutVariable(String idShort, Type type) {
            return addInOutVariable(idShort, type, null);
        }

        /**
         * Adds an input variable to the operation.
         * 
         * @param idShort the short id of the variable
         * @param type the type of the variable (may be <b>null</b> for left undefined)
         * @param init optional initializer in builder style, may be <b>null</b> for none
         * @return <b>this</b>
         */
        public OperationBuilder addInputVariable(String idShort, Type type, Consumer<PropertyBuilder> init);
        
        /**
         * Adds an output variable to the operation.
         * 
         * @param idShort the short id of the variable
         * @param type the type of the variable (may be <b>null</b> for left undefined)
         * @param init optional initializer in builder style, may be <b>null</b> for none
         * @return <b>this</b>
         */
        public OperationBuilder addOutputVariable(String idShort, Type type, Consumer<PropertyBuilder> init);
        
        /**
         * Adds an input-output variable to the operation.
         * 
         * @param idShort the short id of the variable
         * @param type the type of the variable (may be <b>null</b> for left undefined)
         * @param init optional initializer in builder style, may be <b>null</b> for none
         * @return <b>this</b>
         */
        public OperationBuilder addInOutVariable(String idShort, Type type, Consumer<PropertyBuilder> init);
        
        /**
         * Sets the description in terms of language strings.
         * 
         * @param description the description
         * @return <b>this</b>
         */
        public OperationBuilder setDescription(LangString... description);
        
        /**
         * Sets the invocable of this operation. May apply tests to avoid known failures, 
         * e.g., regarding the type of the {@code invocable}. Use {@link #setInvocableLazy(Invokable)}
         * to avoid such tests and to take the responsibility for potential later runtime errors.
         * 
         * @param invocable the invocable
         * @return <b>this</b>
         * @throws IllegalArgumentException if the tests on {@code invocable} fail for some reason
         */
        public OperationBuilder setInvocable(Invokable invocable);

        /**
         * Sets the invocable of this operation.
         * 
         * @param invocable the invocable
         * @return <b>this</b>
         * @see #setInvocableLazy(Invokable)
         */
        public default OperationBuilder setInvocableLazy(Invokable invocable) {
            return setInvocable(invocable);
        }

        /**
         * Convenience method to add the output variable {@value #DEFAULT_RETURN_VAR_NAME} with 
         * the given type and to call {@link #build()}.
         * 
         * @param type the type of the output/return variable
         * @return return of {@link #build()}.
         */
        public default Operation build(Type type) {
            addOutputVariable(DEFAULT_RETURN_VAR_NAME, type);
            return build();
        }

        /**
         * Creates default RBAC rules and builds this operation.
         * 
         * @param auth the authentication descriptor, may be <b>null</b>, ignored then
         * @return return of {@link #build(Type)}.
         * @see #rbac(AuthenticationDescriptor)
         */
        public default Operation build(Type type, AuthenticationDescriptor auth) {
            rbac(auth);
            return build(type);
        }

        /**
         * Creates default RBAC rules and builds this operation.
         * 
         * @param auth the authentication descriptor, may be <b>null</b>, ignored then
         * @return return of {@link #build()}.
         * @see #rbac(AuthenticationDescriptor)
         */
        public default Operation build(AuthenticationDescriptor auth) {
            rbac(auth);
            return build();
        }
        
        /**
         * Creates RBAC rules for the submodel under creation and adds the roles to {@code auth}.
         * 
         * @param auth the authentication descriptor, may be <b>null</b>, ignored then
         * @param roles the roles to create the rules for
         * @param actions the permitted actions
         * @return <b>this</b> for chaining
         */
        public default OperationBuilder rbac(AuthenticationDescriptor auth, Role[] roles, RbacAction... actions) {
            return RbacRoles.rbac(this, auth, roles, actions);
        }
        
    }
    
    /**
     * Returns the number of incoming arguments/variables.
     * 
     * @return the number of incoming arguments/variables
     */
    public int getInArgsCount();

    /**
     * Returns the number of outgoing arguments/variables.
     * 
     * @return the number of outgoing arguments/variables
     */
    public int getOutArgsCount();

    /**
     * Returns the number of incoming/outgoing arguments/variables.
     * 
     * @return the number of incoming/outgoing arguments/variables
     */
    public int getInOutArgsCount();

    /**
     * Returns the number of arguments/variables regardless whether they are in/out/inout.
     * 
     * @return the number of arguments/variables
     */
    public int getArgsCount();

    /**
     * Invokes this operation.
     * 
     * @param args the arguments to the operation (must match the in/out/inout arguments) in terms of plain 
     *   Java instances 
     * @return the output of the operation in terms of a plain Java instance 
     * @throws ExecutionException in case that execution fails for some reason
     */
    public Object invoke(Object... args) throws ExecutionException;
    
}
