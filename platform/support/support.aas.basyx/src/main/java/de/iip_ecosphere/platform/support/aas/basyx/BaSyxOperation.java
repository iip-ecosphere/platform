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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.eclipse.basyx.submodel.metamodel.api.qualifier.haskind.ModelingKind;
import org.eclipse.basyx.submodel.metamodel.api.reference.IReference;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.operation.IOperation;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.operation.IOperationVariable;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetype.ValueType;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.OperationVariable;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAction;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.Role;
import de.iip_ecosphere.platform.support.aas.Invokable;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
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
         * @param init optional initializer in builder style, may be <b>null</b> for none
         * @return the operation variable
         */
        private OperationVariable createOperationVariable(String idShort, Type type, 
            Consumer<PropertyBuilder> init) {
            Property prop = new Property();
            prop.setIdShort(idShort);
            VersionAdjustment.setPropertyKind(prop, ModelingKind.TEMPLATE); // required with BaSyx 1.0.0
            if (null != type) { // let's see whether this makes sense
                prop.setValueType(Tools.translate(type));
            }
            if (null != init) {
                PropertyBuilder builder = new BaSyxProperty.BaSyxPropertyBuilder(null, new BaSyxProperty(prop));
                init.accept(builder);
            }
            return new OperationVariable(prop);
        }
        
        @Override
        public OperationBuilder addInputVariable(String idShort, Type type, Consumer<PropertyBuilder> init) {
            if (null == inputVariables) {
                inputVariables = new ArrayList<>();                
            }
            inputVariables.add(createOperationVariable(idShort, type, init));
            return this;
        }

        @Override
        public OperationBuilder addOutputVariable(String idShort, Type type, Consumer<PropertyBuilder> init) {
            if (null == outputVariables) {
                outputVariables = new ArrayList<>();                
            }
            outputVariables.add(createOperationVariable(idShort, type, init));
            return this;
        }

        @Override
        public OperationBuilder addInOutVariable(String idShort, Type type, Consumer<PropertyBuilder> init) {
            if (null == outputVariables) {
                outputVariables = new ArrayList<>();                
            }
            inOutVariables.add(createOperationVariable(idShort, type, init));
            return this;
        }

        @Override
        public OperationBuilder setInvocable(Invokable invocable) {
            if (invocable != null && !(invocable instanceof Serializable)) {
                throw new IllegalArgumentException("'invocable' for operation '" + operation.getIdShort() 
                    + "' must be Serializable.");
            }
            return setInvocableLazy(invocable);
        }

        @Override
        public OperationBuilder setInvocableLazy(Invokable invocable) {
            operation.setInvokable(invocable.getOperation());
            return this;
        }

        @Override
        public OperationBuilder setDescription(LangString... description) {
            operation.setDescription(Tools.translate(description));
            return this;
        }
                
        @Override
        public Operation build() {
            if (operation.getIdShort().contains("_")) {
                LoggerFactory.getLogger(BaSyxOperation.class).warn("IdShort of operation '{}' contains '_'. "
                    + "Invocation may fail.", operation.getIdShort());
            }
            if (null != inputVariables) {
                operation.setInputVariables(inputVariables);
            }
            // since BaSyx 1.0.0 there must be at least an output variable; from the BaSyx code it looks as if
            // only output variables are considered, not InOut-Variables
            if (null == outputVariables) {
                addOutputVariable(DEFAULT_RETURN_VAR_NAME, Type.NONE);
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

        @Override
        public OperationBuilder setSemanticId(String refValue) {
            IReference ref = Tools.translateReference(refValue);
            if (ref != null) {
                operation.setSemanticId(ref);
            }
            return this;
        }

        @Override
        public OperationBuilder rbac(AuthenticationDescriptor auth, Role role, RbacAction... actions) {
            return AuthenticationDescriptor.elementRbac(this, auth, role, 
                parentBuilder.composeRbacPath(operation.getIdShort()), actions);
        }

        @Override
        public OperationBuilder rbac(AuthenticationDescriptor auth) {
            // must be set explicitly in BaSyx
            return AuthenticationDescriptor.parentRbac(this, auth, parentBuilder.parents(), 
                parentBuilder.composeRbacPath(operation.getIdShort()), 
                RbacAction.READ, RbacAction.EXECUTE);
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
        int result = operation.getOutputVariables().size();
        if (isVoid()) { // added implicitly
            result = 0;
        }
        return result;
    }
    
    /**
     * Returns whether this operation is void, i.e., no return type.
     * 
     * @return {@code true} for void, {@code false} else
     */
    private boolean isVoid() {
        int noneCount = 0;
        for (IOperationVariable v: operation.getOutputVariables()) {
            if (v.getValue() instanceof Property) {
                ValueType type = ((Property) v.getValue()).getValueType();
                if (type == null || type == ValueType.None) {
                    noneCount++;
                }
            }
        }
        return operation.getOutputVariables().size() == 1 && noneCount == 1;
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
            ValueType type = null; 
            if (operation.getOutputVariables().size() > 0) {
                ISubmodelElement outVar = operation.getOutputVariables().iterator().next().getValue();
                if (outVar instanceof Property) {
                    type = ((Property) outVar).getValueType();
                }
            }
            // TODO param translate needed but sequence of in/inout unclear
            return Tools.translateValueFromBaSyx(VersionAdjustment.operationInvoke(operation, args), type);
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

    @Override
    public String getSemanticId(boolean stripPrefix) {
        return Tools.translateReference(operation.getSemanticId(), stripPrefix);
    }
    
    @Override
    public void setSemanticId(String semanticId) {
        IReference ref = Tools.translateReference(semanticId);
        if (ref != null && operation instanceof org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation
                .Operation) {
            ((org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.Operation) 
                operation).setSemanticId(ref);
        }
    }

}
