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

import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Operation.OperationBuilder;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.ReferenceElement.ReferenceElementBuilder;

import org.eclipse.basyx.submodel.metamodel.api.identifier.IdentifierType;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyElements;

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
        private org.eclipse.basyx.submodel.metamodel.map.SubModel subModel;
        
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
            subModel = new org.eclipse.basyx.submodel.metamodel.map.SubModel();
            subModel.setIdShort(idShort);
            subModel.setIdentification(IdentifierType.CUSTOM, idShort); // preliminary
            instance = new BaSyxSubModel(subModel);
        }
        
        @Override
        public AasBuilder getParentBuilder() {
            return parentBuilder;
        }

        @Override
        public PropertyBuilder createPropertyBuilder(String idShort) {
            return new BaSyxProperty.BaSyxPropertyBuilder(this, idShort);
        }

        @Override
        public ReferenceElementBuilder createReferenceElementBuilder(String idShort) {
            return new BaSyxReferenceElement.BaSyxReferenceElementBuilder(this, idShort);
        }
        
        @Override
        public OperationBuilder createOperationBuilder(String idShort) {
            return new BaSyxOperation.BaSxyOperationBuilder(this, idShort);
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

        /**
         * Registers a reference element.
         * 
         * @param reference the reference
         * @return {@code reference}
         */
        BaSyxReferenceElement register(BaSyxReferenceElement reference) {
            instance.getSubModel().addSubModelElement(reference.getReferenceElement());
            return instance.register(reference);
        }

        @Override
        public SubModel build() {
            return null == parentBuilder ? instance : parentBuilder.register(instance);
        }

        @Override
        public Reference createReference() {
            return new BaSyxReference(new org.eclipse.basyx.submodel.metamodel.map.reference.Reference(
                subModel.getIdentification(), KeyElements.SUBMODEL, true));
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
