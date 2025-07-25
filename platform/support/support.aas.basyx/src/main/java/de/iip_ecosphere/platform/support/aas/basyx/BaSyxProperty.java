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
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.basyx.submodel.metamodel.api.reference.IReference;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IProperty;
import org.eclipse.basyx.submodel.metamodel.map.qualifier.LangStrings;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetype.ValueType;
import org.eclipse.basyx.vab.exception.provider.ResourceNotFoundException;
import org.eclipse.basyx.vab.modelprovider.lambda.VABLambdaProviderHelper;

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.Invokable;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAction;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.Role;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Wraps a BaSyx property.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxProperty extends BaSyxSubmodelElement implements Property {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaSyxOperation.class);
    private ISubmodelElement property;

    /**
     * Builder for {@link BaSyxProperty}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxPropertyBuilder implements PropertyBuilder {

        private BaSyxSubmodelElementContainerBuilder<?> parentBuilder;
        private BaSyxProperty instance;
        private org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property property;
        private ValueType typeDef;

        /**
         * Creates an instance. Prevents external creation.
         * 
         * @param parentBuilder the parent builder
         * @param idShort the short name of the property
         * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty
         */
        BaSyxPropertyBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, String idShort) {
            this.parentBuilder = parentBuilder;
            instance = new BaSyxProperty();
            property = new org.eclipse.basyx.submodel.metamodel.map.submodelelement
                .dataelement.property.Property();
            property.setIdShort(Tools.checkId(idShort));

            // following not necessary, but leads to NPE when writing AAS to file
            property.setDescription(new LangStrings()); 
            property.setEmbeddedDataSpecifications(new ArrayList<>());
        }
        
        /**
         * Creates an instance for modifying an existing property. Prevents external creation.
         * 
         * @param parentBuilder the parent builder
         * @param instance the existing property
         */
        BaSyxPropertyBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, BaSyxProperty instance) {
            this.parentBuilder = parentBuilder;
            this.instance = instance;
            this.property = (org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property)
                instance.property;
        }
        
        @Override
        public BaSyxSubmodelElementContainerBuilder<?> getParentBuilder() {
            return parentBuilder;
        }

        @Override
        public PropertyBuilder setType(Type type) {
            typeDef = Tools.translate(type);
            property.setValueType(typeDef);
            return this;
        }

        @Override
        public PropertyBuilder bind(Invokable get, Invokable set) {
            if (null == typeDef) {
                throw new IllegalArgumentException("setType was not called before");
            }
            if (get != null && !(get instanceof Serializable)) {
                throw new IllegalArgumentException("'get' for " + property.getIdShort() + " must be serializable.");
            }
            if (set != null && !(set instanceof Serializable)) {
                throw new IllegalArgumentException("'set' for " + property.getIdShort() + " must be serializable.");
            }
            return bindLazy(get, set);
        }

        @Override
        public PropertyBuilder bindLazy(Invokable get, Invokable set) {
            if (null != get && null == set) {
                LOGGER.warn("Creating AAS operation " + property.getIdShort() + " with only a bound getter "
                    + "can lead to runtime inconsistencies as setting the value will change the value in the "
                    + "property rather than the value in the underlying representation object.");
            }
            property.set(VABLambdaProviderHelper.createSimple(
                null == get ? null : get.getGetter(), 
                null == set ? null : set.getSetter()), typeDef);
            return this;
        }

        @Override
        public PropertyBuilder setValue(Type type, Object value) {
            setType(type);
            return setValue(value);
        }
        
        @Override
        public PropertyBuilder setDescription(LangString... description) {
            property.setDescription(Tools.translate(description));
            return this;
        }

        @Override
        public PropertyBuilder setValue(Object value) {
            property.setValue(Tools.translateValueToBaSyx(Tools.getType(property), value));
            return this;
        }

        @Override
        public PropertyBuilder setSemanticId(String refValue) {
            IReference ref = Tools.translateReference(refValue);
            if (ref != null) {
                property.setSemanticId(ref);
            }
            return this;
        }
        
        @Override
        public Property build() {
            instance.property = property;
            return null != parentBuilder ? parentBuilder.register(instance) : instance;
        }

        @Override
        public Object getValue() throws ExecutionException {
            return property.getValue();
        }
        
        @Override
        public PropertyBuilder rbac(AuthenticationDescriptor auth, Role role, RbacAction... actions) {
            return AuthenticationDescriptor.elementRbac(this, auth, role, 
                parentBuilder.composeRbacPath(property.getIdShort()), actions);
        }

    }
    
    /**
     * Creates an instance. Prevents external creation.
     */
    private BaSyxProperty() {
    }

    /**
     * Creates an instance while retrieving an AAS.
     * 
     * @param property the property
     */
    public BaSyxProperty(IProperty property) {
        this.property = property;
    }

    // checkstyle: stop exception type check

    @Override
    public String getIdShort() {
        try {
            return property.getIdShort();
        } catch (ResourceNotFoundException e) { // TODO check BaSyx Bug 0.1.0-SNAPSHOT for dynamic properties
            return "";
        }
    }
    
    @Override
    public String getSemanticId(boolean stripPrefix) {
        return Tools.translateReference(property.getSemanticId(), stripPrefix);
    }
    
    @Override
    public Object getValue() throws ExecutionException {
        try {
            ValueType type = null;
            if (property instanceof IProperty) {
                type = ((IProperty) property).getValueType();
            }
            return Tools.translateValueFromBaSyx(property.getValue(), type);
        } catch (ResourceNotFoundException e) { // TODO check BaSyx Bug 0.1.0-SNAPSHOT for dynamic properties
            throw new ExecutionException(e);
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }
    
    // checkstyle: resume exception type check
    
    @Override
    public void setValue(Object value) throws ExecutionException {
        try {
            property.setValue(Tools.translateValueToBaSyx(Tools.getType(property), value));
        } catch (ResourceNotFoundException e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    ISubmodelElement getSubmodelElement() {
        return property;
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitProperty(this);
    }
    
    @Override
    public Map<String, LangString> getDescription() {
        return Tools.translate(property.getDescription());
    }
    
    @Override
    public void setSemanticId(String semanticId) {
        IReference ref = Tools.translateReference(semanticId);
        if (ref != null && property instanceof org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.
            property.Property) {
            ((org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property) property)
                .setSemanticId(ref);
        }
    }
    
}
