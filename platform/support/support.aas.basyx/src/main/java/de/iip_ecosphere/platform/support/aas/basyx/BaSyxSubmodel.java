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

import org.eclipse.basyx.submodel.metamodel.api.identifier.IdentifierType;
import org.eclipse.basyx.submodel.metamodel.map.SubModel;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.basyx.AbstractAas.BaSyxAbstractAasBuilder;
import de.iip_ecosphere.platform.support.aas.basyx.AbstractAas.BaSyxSubmodelParent;

/**
 * Wraps a BaSyx sub-model.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxSubmodel extends AbstractSubmodel<SubModel> {

    private BaSyxSubmodelParent parent;
    
    /**
     * Builder for {@link BaSyxSubmodel}.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class BaSyxSubmodelBuilder extends BaSyxSubmodelElementContainerBuilder<SubModel> 
        implements SubmodelBuilder {

        private BaSyxAbstractAasBuilder parentBuilder;
        private BaSyxSubmodel instance;
        private SubModel submodel;
        
        /**
         * Creates an instance. Prevents external creation.
         * 
         * @param parentBuilder the parent builder (may be <b>null</b> for a standalone sub-model)
         * @param idShort the short id of the sub-model
         * @throws IllegalArgumentException may be thrown if {@link #setType(Type)} was not called before
         */
        BaSyxSubmodelBuilder(BaSyxAbstractAasBuilder parentBuilder, String idShort) {
            if (null == idShort || 0 == idShort.length()) {
                throw new IllegalArgumentException("idShort must be given");
            }
            this.parentBuilder = parentBuilder;
            submodel = new SubModel();
            submodel.setIdShort(idShort);
            submodel.setIdentification(IdentifierType.CUSTOM, idShort); // preliminary
            instance = new BaSyxSubmodel(submodel);
            instance.parent = parentBuilder.getSubmodelParent();
        }

        /**
         * Creates an instance from an existing BaSyx instance.
         * 
         * @param parentBuilder the parent builder (may be <b>null</b> for a standalone sub-model)
         * @param instance the BaSyx instance
         */
        BaSyxSubmodelBuilder(BaSyxAbstractAasBuilder parentBuilder, BaSyxSubmodel instance) {
            this.parentBuilder = parentBuilder;
            this.instance = instance;
        }

        @Override
        public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort, boolean ordered, 
            boolean allowDuplicates) {
            SubmodelElementCollectionBuilder result;
            SubmodelElementCollection sub = instance.getSubmodelElementCollection(idShort);
            if (null == sub) {
                result = new BaSyxSubmodelElementCollection.BaSyxSubmodelElementCollectionBuilder(this, idShort, 
                    ordered, allowDuplicates);
            } else {
                result = new BaSyxSubmodelElementCollection.BaSyxSubmodelElementCollectionBuilder(this, 
                   (BaSyxSubmodelElementCollection) sub);
            }
            return result;
        }

        @Override
        public Submodel build() {
            return null == parentBuilder ? instance : parentBuilder.register(instance);
        }

        @Override
        public AasBuilder getAasBuilder() {
            return parentBuilder;
        }

        @Override
        public SubmodelElementContainerBuilder getParentBuilder() {
            return null;
        }

        @Override
        protected AbstractSubmodel<SubModel> getInstance() {
            return instance;
        }

    }

    /**
     * Creates an instance. Prevents external creation.
     * 
     * @param subModel the sub-model instance
     */
    private BaSyxSubmodel(SubModel subModel) {
        super(subModel);
    }
    
    /**
     * Creates an instance based on a given instance.
     * 
     * @param parent the parent instance
     * @param instance the BaSyx submodel instance
     */
    BaSyxSubmodel(BaSyxSubmodelParent parent, SubModel instance) {
        super(instance);
        this.parent = parent;
        BaSyxElementTranslator.registerDataElements(instance.getDataElements(), this);
        BaSyxElementTranslator.registerOperations(instance.getOperations(), this);
        BaSyxElementTranslator.registerRemainingSubmodelElements(instance.getSubmodelElements(), this);
    }
    
    @Override
    public SubmodelElementCollectionBuilder addSubmodelElementCollection(String idShort, boolean ordered,
        boolean allowDuplicates) {
        LoggerFactory.getLogger(getClass()).warn("Adding a submodel to a deployed AAS currently does not lead to "
            + "the deployment of the new submodel (as for initial AAS). If possible, create the submodel in advance.");
        return new BaSyxSubmodelElementCollection.BaSyxSubmodelElementCollectionBuilder(
            new BaSyxSubmodelBuilder(parent.createAasBuilder(), this), idShort, ordered, allowDuplicates);
    }

}
