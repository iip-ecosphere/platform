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
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.Invokable;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAction;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.Role;

/**
 * Wraps a BaSyx property.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxProperty extends BaSyxSubmodelElement implements Property {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaSyxOperation.class);
    private org.eclipse.digitaltwin.aas4j.v3.model.Property property;

    /**
     * Builder for {@link BaSyxProperty}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxPropertyBuilder implements PropertyBuilder {

        private BaSyxSubmodelElementContainerBuilder<?> parentBuilder;
        private BaSyxProperty instance;
        private org.eclipse.digitaltwin.aas4j.v3.model.Property property;
        private DataTypeDefXsd typeDef;
        private boolean isNew = true;

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
            property = new org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty();
            property.setIdShort(Tools.checkId(idShort));

            // following not necessary, but leads to NPE when writing AAS to file
            property.setDescription(new ArrayList<>()); // TODO check
            property.setEmbeddedDataSpecifications(new ArrayList<>());
        }
        
        /**
         * Creates an instance for modifying an existing property. Prevents external creation.
         * 
         * @param parentBuilder the parent builder
         * @param instance the existing property
         */
        BaSyxPropertyBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, BaSyxProperty instance) {
            isNew = false;
            this.parentBuilder = parentBuilder;
            this.instance = instance;
            this.property = (org.eclipse.digitaltwin.aas4j.v3.model.Property) instance.property;
        }
        
        /**
         * Creates a property builder, if possible from {@code instance} else from {@code idShort}.
         * 
         * @param parentBuilder the parent builder
         * @param idShort the id short
         * @param instance the optional property instance
         * @return the builder
         */
        static BaSyxPropertyBuilder create(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, String idShort, 
            Property instance) {
            if (instance instanceof BaSyxProperty) {
                return new BaSyxPropertyBuilder(parentBuilder, (BaSyxProperty) instance);
            } else {
                return new BaSyxPropertyBuilder(parentBuilder, idShort);
            }
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
            return bindLazy(get, set);
        }

        @Override
        public PropertyBuilder bindLazy(Invokable get, Invokable set) {
            LOGGER.warn("Creating AAS operation {} with bindings not supported in AAS metamodel v3", 
                property.getIdShort()); 
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
            property.setValue(Tools.translateValueToBaSyx(property.getValueType(), value));
            return this;
        }

        @Override
        public PropertyBuilder setSemanticId(String refValue) {
            return Tools.setSemanticId(this, refValue, property);
        }
        
        @Override
        public Property build() {
            instance.property = property;
            return updateInBuild(isNew, null != parentBuilder ? parentBuilder.register(instance) : instance);
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
    public BaSyxProperty(org.eclipse.digitaltwin.aas4j.v3.model.Property property) {
        this.property = property;
    }

    // checkstyle: stop exception type check
    
    @Override
    public Object getValue() throws ExecutionException {
        DataTypeDefXsd type = null;
        if (property instanceof org.eclipse.digitaltwin.aas4j.v3.model.Property) {
            type = ((org.eclipse.digitaltwin.aas4j.v3.model.Property) property).getValueType();
        }
        return Tools.translateValueFromBaSyx(property.getValue(), type);
    }
    
    // checkstyle: resume exception type check
    
    @Override
    public void setValue(Object value) throws ExecutionException {
        property.setValue(Tools.translateValueToBaSyx(property.getValueType(), value));
        updateConnectedSubmodelElement();
    }

    @Override
    org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement getSubmodelElement() {
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
    
}
