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

import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxAas.BaSyxAasBuilder;

/**
 * Wraps a BaSyx sub-model.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxSubmodel extends AbstractSubmodel<org.eclipse.basyx.submodel.metamodel.map.SubModel> {

    /**
     * Builder for {@link BaSyxSubmodel}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxSubmodelBuilder extends BaSyxSubmodelElementContainerBuilder implements SubmodelBuilder {

        private BaSyxAasBuilder parentBuilder;
        private BaSyxSubmodel instance;
        private org.eclipse.basyx.submodel.metamodel.map.SubModel submodel;
        
        /**
         * Creates an instance. Prevents external creation.
         * 
         * @param parentBuilder the parent builder (may be <b>null</b> for a standalone sub-model)
         * @param idShort the short id of the sub-model
         * @throws IllegalArgumentException may be thrown if {@link #setType(Type)} was not called before
         */
        BaSyxSubmodelBuilder(BaSyxAasBuilder parentBuilder, String idShort) {
            if (null == idShort || 0 == idShort.length()) {
                throw new IllegalArgumentException("idShort must be given");
            }
            this.parentBuilder = parentBuilder;
            submodel = new org.eclipse.basyx.submodel.metamodel.map.SubModel();
            submodel.setIdShort(idShort);
            submodel.setIdentification(IdentifierType.CUSTOM, idShort); // preliminary
            instance = new BaSyxSubmodel(submodel);
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

        @Override
        public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort, boolean ordered, 
            boolean allowDuplicates) {
            return new BaSyxSubmodelElementCollection.BaSyxSubmodelElementCollectionBuilder(this, idShort, ordered, 
                allowDuplicates);
        }

        @Override
        BaSyxOperation register(BaSyxOperation operation) {
            instance.getSubModel().addSubModelElement(operation.getSubmodelElement());
            return instance.register(operation);
        }
        
        @Override
        BaSyxProperty register(BaSyxProperty property) {
            instance.getSubModel().addSubModelElement(property.getSubmodelElement());
            return instance.register(property);
        }

        @Override
        BaSyxReferenceElement register(BaSyxReferenceElement reference) {
            instance.getSubModel().addSubModelElement(reference.getSubmodelElement());
            return instance.register(reference);
        }

        @Override
        BaSyxSubmodelElementCollection register(BaSyxSubmodelElementCollection collection) {
            instance.getSubModel().addSubModelElement(collection.getSubmodelElement());
            return instance.register(collection);
        }

        @Override
        public Submodel build() {
            return null == parentBuilder ? instance : parentBuilder.register(instance);
        }

        @Override
        public Reference createReference() {
            return new BaSyxReference(new org.eclipse.basyx.submodel.metamodel.map.reference.Reference(
                submodel.getIdentification(), KeyElements.SUBMODEL, true));
        }

    }

    /**
     * Creates an instance. Prevents external creation.
     * 
     * @param subModel the sub-model instance
     */
    private BaSyxSubmodel(org.eclipse.basyx.submodel.metamodel.map.SubModel subModel) {
        super(subModel);
    }
    
}
