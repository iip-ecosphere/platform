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

package de.iip_ecosphere.platform.support.aas.basyx2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.QualifierKind;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultQualifier;
import org.eclipse.digitaltwin.basyx.client.internal.ApiException;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.digitaltwin.basyx.core.exceptions.NotInvokableException;
import org.eclipse.digitaltwin.basyx.core.exceptions.OperationDelegationException;
import org.eclipse.digitaltwin.basyx.submodelrepository.client.ConnectedSubmodelRepository;
import org.eclipse.digitaltwin.basyx.submodelrepository.feature.operation.delegation.HTTPOperationDelegation;

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAction;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.Role;

/**
 * Implements an AAS Operation wrapper for BaSyx.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxOperation extends BaSyxSubmodelElement implements Operation {
    
    public static final String INVOCATION_DELEGATION_SUBMODELID_TYPE 
        = HTTPOperationDelegation.INVOCATION_DELEGATION_TYPE + "-submodelId";
    public static final String INVOCATION_DELEGATION_SUBMODELREGISTRYURL_TYPE 
        = HTTPOperationDelegation.INVOCATION_DELEGATION_TYPE + "-submodelRegistryUrl";
    
    private org.eclipse.digitaltwin.aas4j.v3.model.Operation operation;
    
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
    BaSyxOperation(org.eclipse.digitaltwin.aas4j.v3.model.Operation operation) {
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
        private org.eclipse.digitaltwin.aas4j.v3.model.Operation operation;
        private List<org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable> inputVariables;
        private List<org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable> outputVariables;
        private List<org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable> inOutVariables;
        private boolean isNew = true;

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
            operation = new org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperation();
            operation.setIdShort(Tools.checkId(idShort));
        }
        
        /**
         * Creates an instance for modifying an existing property. Prevents external creation.
         * 
         * @param parentBuilder the parent builder
         * @param instance the existing property
         */
        BaSxyOperationBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, BaSyxOperation instance) {
            isNew = false;
            this.parentBuilder = parentBuilder;
            this.instance = instance;
            this.operation = (org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperation) instance.operation;
        }        

        /**
         * Creates an operation builder, if possible from {@code instance} else from {@code idShort}.
         * 
         * @param parentBuilder the parent builder
         * @param idShort the id short
         * @param instance the optional operation instance
         * @return the builder
         */
        static BaSxyOperationBuilder create(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, String idShort, 
            Operation instance) {
            if (instance instanceof BaSyxOperation) {
                return new BaSxyOperationBuilder(parentBuilder, (BaSyxOperation) instance);
            } else {
                return new BaSxyOperationBuilder(parentBuilder, idShort);
            }
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
        private org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable createOperationVariable(String idShort, 
            Type type, Consumer<PropertyBuilder> init) {
            Property prop = new DefaultProperty();
            prop.setIdShort(idShort);
            prop.setValueType(Tools.translate(type));
            if (null != init) {
                PropertyBuilder builder = new BaSyxProperty.BaSyxPropertyBuilder(null, new BaSyxProperty(prop));
                init.accept(builder);
            }
            return new DefaultOperationVariable.Builder().value(prop).build();
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
            if (null == inOutVariables) {
                inOutVariables = new ArrayList<>();
            }
            inOutVariables.add(createOperationVariable(idShort, type, init));
            return this;
        }

        @Override
        public OperationBuilder setInvocable(de.iip_ecosphere.platform.support.aas.Invokable invocable) {
            return setInvocableLazy(invocable);
        }

        /**
         * Adds an invocation qualifier.
         * 
         * @param type the qualifier type
         * @param value the qualifier value
         */
        private void addInvocationQualifier(String type, String value) {
            DefaultQualifier qual = new DefaultQualifier();
            qual.setKind(QualifierKind.CONCEPT_QUALIFIER);
            qual.setType(type);
            qual.setValueType(DataTypeDefXsd.STRING);
            qual.setValue(value);
            operation.setQualifiers(Tools.addElement(operation.getQualifiers(), qual));
        }

        @Override
        public OperationBuilder setInvocableLazy(de.iip_ecosphere.platform.support.aas.Invokable invocable) {
            if (null != invocable) {
                String url = invocable.getUrl();
                String submodelRepoUrl = invocable.getSubmodelRepositoryUrl();
                if (null != url && null != submodelRepoUrl) {
                    addInvocationQualifier(HTTPOperationDelegation.INVOCATION_DELEGATION_TYPE, url);
                    final String submodelId = parentBuilder.getInstance().getIdentification();
                    if (null == submodelId || submodelId.isEmpty()) {
                        LoggerFactory.getLogger(this).warn("Operation execution of {} may fail as no submodelId is "
                            + "known.", operation.getIdShort());
                    }
                    addInvocationQualifier(INVOCATION_DELEGATION_SUBMODELID_TYPE, submodelId);
                    addInvocationQualifier(INVOCATION_DELEGATION_SUBMODELREGISTRYURL_TYPE, submodelRepoUrl);
                } else {
                    LoggerFactory.getLogger(this).info("Cannot set invokable of {} as urls are missing "
                        + "(url: {}, submodelRepository {})", operation.getIdShort(), url, submodelRepoUrl);
                }
            } else {
                LoggerFactory.getLogger(this).info("Cannot set invokable of {} as none is given (null)", 
                    operation.getIdShort());
            }
            return this;
        }

        @Override
        public OperationBuilder setDescription(LangString... description) {
            operation.setDescription(Tools.translate(description));
            return this;
        }
                
        @Override
        public Operation build() {
            if (null != inputVariables) {
                operation.setInputVariables(inputVariables);
            }
            if (null != outputVariables) {
                operation.setOutputVariables(outputVariables);
            }
            if (null != inOutVariables) {
                operation.setInoutputVariables(inOutVariables);
            }
            instance.operation = operation;
            return updateInBuild(isNew, parentBuilder.register(instance));
        }
        
        @Override
        public OperationBuilder setSemanticId(String refValue) {
            return Tools.setSemanticId(this, refValue, operation);
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
    org.eclipse.digitaltwin.aas4j.v3.model.Operation getOperation() {
        return operation;
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
        return operation.getInoutputVariables().size();
    }

    @Override
    public int getArgsCount() {
        return getInArgsCount() + getOutArgsCount() + getInOutArgsCount();
    }

    // checkstyle: stop exception type check
    
    @Override
    public Object invoke(Object... args) throws ExecutionException {
        StringBuilder submodelId = new StringBuilder();
        StringBuilder submodelRegistryUrl = new StringBuilder();
        operation.getQualifiers().forEach(q -> {
            if (INVOCATION_DELEGATION_SUBMODELID_TYPE.equals(q.getType())) {
                submodelId.append(q.getValue());
            } else if (INVOCATION_DELEGATION_SUBMODELREGISTRYURL_TYPE.equals(q.getType())) {
                submodelRegistryUrl.append(q.getValue());
            }
        });
        if (submodelId.isEmpty()) {
            throw new ExecutionException("Cannot invoke operation " + operation.getIdShort() 
                + " as submodelId is missing", null);
        }
        if (submodelRegistryUrl.isEmpty()) {
            throw new ExecutionException("Cannot invoke operation " + operation.getIdShort() 
                + " as submodelRegistryUrl is missing", null);
        }
        ConnectedSubmodelRepository smRepo = getRepo();
        if (null == smRepo) { // registered, retrieved; else fallback, potentially without TLS/auth information
            smRepo = new ConnectedSubmodelRepository(submodelRegistryUrl.toString());
        }
        DataTypeDefXsd type = null;
        List<OperationVariable> params = operation.getInputVariables(); // TODO inout
        OperationVariable[] opArgs = new OperationVariable[params.size()];
        for (int a = 0; a < opArgs.length; a++) {
            SubmodelElement paramElt = params.get(a).getValue();
            String argValue = "";
            DataTypeDefXsd argType = DataTypeDefXsd.STRING;
            String argName = "unknown";
            if (a < args.length) {
                if (args[a] != null) {
                    argValue = args[a].toString();
                }
            }
            if (paramElt instanceof Property) {
                Property param = ((Property) paramElt);
                argName = param.getIdShort();
                argType = param.getValueType();
                
            } // TODO others, adjust generic REST Service implementation
            SubmodelElement tmp = new DefaultProperty.Builder()
                .value(argValue)
                .valueType(argType)
                .idShort(argName)
                .build();
            opArgs[a] = new DefaultOperationVariable.Builder().value(tmp).build();
        }
        if (operation.getOutputVariables().size() > 0) {
            SubmodelElement outVar = operation.getOutputVariables().iterator().next().getValue();
            if (outVar instanceof Property) {
                type = ((Property) outVar).getValueType();
            }
        }
        try {
            String path = operation.getIdShort();
            BaSyxSubmodelElementParent parent = getParent();
            while (parent != null && !(parent instanceof Submodel)) {
                path = parent.getIdShort() + "." + path;
                parent = parent.getParent();
            }
            OperationVariable[] result = smRepo.invokeOperation(submodelId.toString(), path, opArgs);
            return Tools.translateValueFromBaSyx(result == null || result.length == 0 
                ? null : result[0].getValue(), type);
        } catch (ElementDoesNotExistException | OperationDelegationException | NotInvokableException | ApiException e) {
            throw new ExecutionException("Invoking operation '" + getIdShort() + "': " +  e.getMessage(), e);
        } 
    }

    // checkstyle: resume exception type check

    @Override
    org.eclipse.digitaltwin.aas4j.v3.model.Operation getSubmodelElement() {
        return operation;
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitOperation(this);
    }

}
