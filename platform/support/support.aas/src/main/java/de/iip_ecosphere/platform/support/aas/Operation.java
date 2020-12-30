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
import java.util.function.Function;

import de.iip_ecosphere.platform.support.Builder;

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
    public interface OperationBuilder extends Builder<Operation> {
        
        /**
         * Returns the parent builder.
         * 
         * @return the parent builder
         */
        public SubmodelElementContainerBuilder getParentBuilder();
        
        /**
         * Adds an input variable to the operation.
         * 
         * @param idShort the short id of the variable
         * @param type the type of the variable (may be <b>null</b> for left undefined)
         * @return <b>this</b>
         */
        public OperationBuilder addInputVariable(String idShort, Type type);
        
        /**
         * Adds an output variable to the operation.
         * 
         * @param idShort the short id of the variable
         * @param type the type of the variable (may be <b>null</b> for left undefined)
         * @return <b>this</b>
         */
        public OperationBuilder addOutputVariable(String idShort, Type type);

        /**
         * Adds an input-output variable to the operation.
         * 
         * @param idShort the short id of the variable
         * @param type the type of the variable (may be <b>null</b> for left undefined)
         * @return <b>this</b>
         */
        public OperationBuilder addInOutVariable(String idShort, Type type);
        
        /**
         * Sets the invocable of this operation.
         * 
         * @param invocable the invocable
         * @return <b>this</b>
         */
        public OperationBuilder setInvocable(Function<Object[], Object> invocable);
        
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
