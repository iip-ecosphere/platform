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

package de.iip_ecosphere.platform.support.aas.basyx;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.eclipse.basyx.submodel.metamodel.api.submodelelement.operation.IOperation;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.OperationVariable;

import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.SubModel.SubModelBuilder;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxSubModel.BaSyxSubModelBuilder;

/**
 * Implements an AAS Operation wrapper for BaSyx.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxOperation implements Operation {
    
    private IOperation operation;
    
    /**
     * Creates an instance. Prevents from external creation.
     */
    private BaSyxOperation() {
    }
    
    /**
     * Creates an instance while retrieving an AAS.
     * 
     * @param operation the operation
     */
    BaSyxOperation(IOperation operation) {
        this.operation = operation;
    }

    /**
     * Implements the operation builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSxyOperationBuilder implements OperationBuilder {
        
        private BaSyxSubModelBuilder parentBuilder;
        private BaSyxOperation instance;
        private org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.Operation operation;
        private List<OperationVariable> inputVariables;
        private List<OperationVariable> outputVariables;
        private List<OperationVariable> inOutVariables;

        /**
         * Creates an instance. Prevents from external creation.
         * 
         * @param parentBuilder the parent builder
         * @param idShort the short name of the operation
         * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty
         */
        BaSxyOperationBuilder(BaSyxSubModelBuilder parentBuilder, String idShort) {
            this.parentBuilder = parentBuilder;
            instance = new BaSyxOperation();
            operation = new org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.Operation();
            operation.setIdShort(idShort);
            if (null == idShort || 0 == idShort.length()) {
                throw new IllegalArgumentException("idShort must be given");
            }
        }

        @Override
        public SubModelBuilder getParentBuilder() {
            return parentBuilder;
        }
        
        /**
         * Creates an operation variable. Just in case that we somewhen need name and type.
         * 
         * @return the operation variable
         */
        private OperationVariable createOperationVariable() {
            return new OperationVariable();
        }
        
        @Override
        public OperationBuilder addInputVariable() {
            if (null == inputVariables) {
                inputVariables = new ArrayList<>();                
            }
            inputVariables.add(createOperationVariable());
            return this;
        }

        @Override
        public OperationBuilder addOutputVariable() {
            if (null == outputVariables) {
                outputVariables = new ArrayList<>();                
            }
            outputVariables.add(createOperationVariable());
            return this;
        }

        @Override
        public OperationBuilder addInOutVariable() {
            if (null == outputVariables) {
                outputVariables = new ArrayList<>();                
            }
            inOutVariables.add(createOperationVariable());
            return this;
        }

        @Override
        public OperationBuilder setInvocable(Function<Object[], Object> invocable) {
            operation.setInvocable(invocable);
            return this;
        }

        @Override
        public Operation build() {
            if (null != inputVariables) {
                operation.setInputVariables(inputVariables);
            }
            if (null != outputVariables) {
                operation.setInputVariables(outputVariables);
            }
            if (null != inOutVariables) {
                operation.setInOutputVariables(inOutVariables);
            }
            instance.operation = operation;
            return parentBuilder.register(instance);
        }
                
    }
    
    /**
     * Returns the BaSyx operation instance.
     * 
     * @return the operation instance
     */
    IOperation getOperation() {
        return operation;
    }

    @Override
    public String getIdShort() {
        return operation.getIdShort();
    }

    @Override
    public int getInArgsCount() {
        return operation.getInputVariables().size();
    }

    @Override
    public int getOutArgsCount() {
        return operation.getOutputVariables().size();
    }

    @Override
    public int getInOutArgsCount() {
        return operation.getInOutputVariables().size();
    }

    @Override
    public int getArgsCount() {
        return getInArgsCount() + getOutArgsCount() + getInOutArgsCount();
    }

    // checkstyle: stop exception type check
    
    @Override
    public Object invoke(Object... args) throws ExecutionException {
        try {
            return operation.invoke(args);
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }

    // checkstyle: resume exception type check

}
