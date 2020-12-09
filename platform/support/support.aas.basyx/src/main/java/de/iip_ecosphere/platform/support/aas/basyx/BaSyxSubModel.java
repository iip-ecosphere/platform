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

import de.iip_ecosphere.platform.support.aas.Operation.OperationBuilder;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;

import org.eclipse.basyx.submodel.metamodel.api.identifier.IdentifierType;

import de.iip_ecosphere.platform.support.aas.SubModel;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxAas.BaSyxAasBuilder;

/**
 * Wraps a BaSyx sub-model.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxSubModel extends AbstractSubModel<org.eclipse.basyx.submodel.metamodel.map.SubModel> {

    /**
     * Builder for {@link BaSyxSubModel}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxSubModelBuilder implements SubModelBuilder {

        private BaSyxAasBuilder parentBuilder;
        private BaSyxSubModel instance;
        
        /**
         * Creates an instance. Prevents external creation.
         * 
         * @param parentBuilder the parent builder (may be <b>null</b> for a standalone sub-model)
         * @param idShort the short id of the sub-model
         * @throws IllegalArgumentException may be thrown if {@link #setType(Type)} was not called before
         */
        BaSyxSubModelBuilder(BaSyxAasBuilder parentBuilder, String idShort) {
            if (null == idShort || 0 == idShort.length()) {
                throw new IllegalArgumentException("idShort must be given");
            }
            this.parentBuilder = parentBuilder;
            org.eclipse.basyx.submodel.metamodel.map.SubModel subModel 
                = new org.eclipse.basyx.submodel.metamodel.map.SubModel();
            subModel.setIdShort(idShort);
            subModel.setIdentification(IdentifierType.CUSTOM, idShort); // preliminary
            instance = new BaSyxSubModel(subModel);
        }

        @Override
        public PropertyBuilder createPropertyBuilder(String shortName) {
            return new BaSyxProperty.BaSyxPropertyBuilder(this, shortName);
        }
        
        @Override
        public OperationBuilder createOperationBuilder(String shortName) {
            return new BaSyxOperation.BaSxyOperationBuilder(this, shortName);
        }
        
        /**
         * Registers an operation.
         * 
         * @param operation the operation
         * @return {@code operation}
         */
        BaSyxOperation register(BaSyxOperation operation) {
            instance.getSubModel().addSubModelElement(operation.getOperation());
            return instance.register(operation);
        }
        
        /**
         * Registers a property.
         * 
         * @param property the property
         * @return {@code property}
         */
        BaSyxProperty register(BaSyxProperty property) {
            instance.getSubModel().addSubModelElement(property.getProperty());
            return instance.register(property);
        }

        @Override
        public SubModel build() {
            return null == parentBuilder ? instance : parentBuilder.register(instance);
        }

    }

    /**
     * Creates an instance. Prevents external creation.
     * 
     * @param subModel the sub-model instance
     */
    private BaSyxSubModel(org.eclipse.basyx.submodel.metamodel.map.SubModel subModel) {
        super(subModel);
    }
    
}
