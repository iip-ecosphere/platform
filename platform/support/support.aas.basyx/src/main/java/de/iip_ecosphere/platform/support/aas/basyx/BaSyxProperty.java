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

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IProperty;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetypedef.PropertyValueTypeDef;
import org.eclipse.basyx.vab.modelprovider.lambda.VABLambdaProviderHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxSubModel.BaSyxSubModelBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.SubModel.SubModelBuilder;

/**
 * Wraps a BaSyx property.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxProperty implements Property {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaSyxOperation.class);
    private IProperty property;

    // TODO can we distinguish the three types; static, dynamic, ?
    /**
     * Builder for {@lin BaSyxProperty}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxPropertyBuilder implements PropertyBuilder {

        private BaSyxSubModelBuilder parentBuilder;
        private BaSyxProperty instance;
        private org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property property;
        private PropertyValueTypeDef typeDef;

        /**
         * Creates an instance. Prevents external creation.
         * 
         * @param parentBuilder the parent builder
         * @param idShort the short name of the property
         * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty
         */
        BaSyxPropertyBuilder(BaSyxSubModelBuilder parentBuilder, String idShort) {
            if (null == idShort || 0 == idShort.length()) {
                throw new IllegalArgumentException("idShort must be given");
            }
            this.parentBuilder = parentBuilder;
            instance = new BaSyxProperty();
            property = new org.eclipse.basyx.submodel.metamodel.map.submodelelement
                .dataelement.property.Property();
            property.setIdShort(idShort);
        }
        
        @Override
        public SubModelBuilder getParentBuilder() {
            return parentBuilder;
        }

        @Override
        public PropertyBuilder setType(Type type) {
            typeDef = BaSyxAasFactory.translate(type);
            property.setValueType(typeDef);
            return this;
        }

        @Override
        public PropertyBuilder bind(Supplier<Object> get, Consumer<Object> set) {
            if (null == typeDef) {
                throw new IllegalArgumentException("setType was not called before");
            }
            if (null != get && null == set) {
                LOGGER.warn("Creating AAS operation " + property.getIdShort() + " with only a bound getter "
                    + "can lead to runtime inconsistencies as setting will change the value in the property rather "
                    + "than the value in the underlying representation object.");
            }
            property.set(VABLambdaProviderHelper.createSimple(get, set), typeDef);
            return this;
        }

        // TODO check validity?
        
        @Override
        public PropertyBuilder setValue(Object value) {
            property.set(value);
            return this;
        }

        @Override
        public Property build() {
            instance.property = property;
            return parentBuilder.register(instance);
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
    BaSyxProperty(IProperty property) {
        this.property = property;
    }

    /**
     * Returns the BaSyx property instance.
     * 
     * @return the property instance
     */
    IProperty getProperty() {
        return property;
    }

    @Override
    public String getIdShort() {
        return property.getIdShort();
    }
    
    // checkstyle: stop exception type check

    @Override
    public Object getValue() throws ExecutionException {
        try {
            return property.get();
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }
    
    @Override
    public void setValue(Object value) throws ExecutionException {
        property.set(value);
    }
    
    // checkstyle: resume exception type check
    
}
