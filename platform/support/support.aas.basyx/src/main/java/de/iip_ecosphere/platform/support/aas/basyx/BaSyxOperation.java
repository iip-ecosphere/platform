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

import org.eclipse.basyx.submodel.metamodel.api.qualifier.haskind.ModelingKind;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.operation.IOperation;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.OperationVariable;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Type;

/**
 * Implements an AAS Operation wrapper for BaSyx.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxOperation extends BaSyxSubmodelElement implements Operation {
    
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
        
        private BaSyxSubmodelElementContainerBuilder<?> parentBuilder;
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
        BaSxyOperationBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, String idShort) {
            this.parentBuilder = parentBuilder;
            instance = new BaSyxOperation();
            operation = new org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.Operation();
            operation.setIdShort(Tools.checkId(idShort));
        }

        @Override
        public BaSyxSubmodelElementContainerBuilder<?> getParentBuilder() {
            return parentBuilder;
        }
        
        /**
         * Creates an operation variable. Just in case that we somewhen need name and type.
         * 
         * @param idShort the short id of the variable
         * @param type the type of the variable (may be <b>null</b> for left undefined)
         * @return the operation variable
         */
        private OperationVariable createOperationVariable(String idShort, Type type) {
            Property prop = new Property();
            prop.setIdShort(idShort);
            prop.setModelingKind(ModelingKind.TEMPLATE); // required with BaSyx 1.0.0
            if (null != type) { // let's see whether this makes sense
                prop.setValueType(Tools.translate(type));
            }
            return new OperationVariable(prop);
        }
        
        @Override
        public OperationBuilder addInputVariable(String idShort, Type type) {
            if (null == inputVariables) {
                inputVariables = new ArrayList<>();                
            }
            inputVariables.add(createOperationVariable(idShort, type));
            return this;
        }

        @Override
        public OperationBuilder addOutputVariable(String idShort, Type type) {
            if (null == outputVariables) {
                outputVariables = new ArrayList<>();                
            }
            outputVariables.add(createOperationVariable(idShort, type));
            return this;
        }

        @Override
        public OperationBuilder addInOutVariable(String idShort, Type type) {
            if (null == outputVariables) {
                outputVariables = new ArrayList<>();                
            }
            inOutVariables.add(createOperationVariable(idShort, type));
            return this;
        }

        @Override
        public OperationBuilder setInvocable(Function<Object[], Object> invocable) {
            operation.setInvokable(invocable);
            return this;
        }

        @Override
        public Operation build() {
            if (null != inputVariables) {
                operation.setInputVariables(inputVariables);
            }
            // since BaSyx 1.0.0 there must be at least an output variable; from the BaSyx code it looks as if
            // only output variables are considered, not InOut-Variables
            if (null == outputVariables) {
                addOutputVariable(DEFAULT_RETURN_VAR_NAME, Type.NONE);
                LoggerFactory.getLogger(BaSyxOperation.class).warn("No result output variable specified for "
                    + "operation '{}'. Creating an implicit variable of type NONE.", operation.getIdShort());
            }
            if (null != outputVariables) {
                operation.setOutputVariables(outputVariables);
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
            // TODO param translate needed but sequence of in/inout unclear
            return Tools.translateValueFromBaSyx(operation.invoke(args));
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }

    // checkstyle: resume exception type check

    @Override
    IOperation getSubmodelElement() {
        return operation;
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitOperation(this);
    }

}
