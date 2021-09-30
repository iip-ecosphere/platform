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

import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.basyx.AbstractAas.BaSyxAbstractAasBuilder;
import de.iip_ecosphere.platform.support.aas.basyx.AbstractAas.BaSyxSubmodelParent;

/**
 * Wraps a BaSyx sub-model.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxSubmodel extends AbstractSubmodel<org.eclipse.basyx.submodel.metamodel.map.Submodel> {

    private BaSyxSubmodelParent parent;
    
    /**
     * Builder for {@link BaSyxSubmodel}.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class BaSyxSubmodelBuilder extends BaSyxSubmodelElementContainerBuilder
        <org.eclipse.basyx.submodel.metamodel.map.Submodel> implements SubmodelBuilder {

        private BaSyxAbstractAasBuilder parentBuilder;
        private BaSyxSubmodel instance;
        private org.eclipse.basyx.submodel.metamodel.map.Submodel submodel;
        private boolean isNew = true;

        /**
         * Creates an instance. Prevents external creation.
         * 
         * @param parentBuilder the parent builder (may be <b>null</b> for a standalone sub-model)
         * @param idShort the short id of the sub-model
         * @param identifier the identifier of the sub-model (may be <b>null</b> or empty for an identification based on
         *    {@code idShort}, interpreted as an URN if this starts with {@code urn})
         * @throws IllegalArgumentException may be thrown if {@code idShort} is not given
         */
        BaSyxSubmodelBuilder(BaSyxAbstractAasBuilder parentBuilder, String idShort, String identifier) {
            this(parentBuilder, idShort, Tools.translateIdentifier(identifier, idShort));
        }
        
        /**
         * Creates an instance. Prevents external creation.
         * 
         * @param parentBuilder the parent builder (may be <b>null</b> for a standalone sub-model)
         * @param idShort the short id of the sub-model
         * @param identifier the identifier of the model
         * @throws IllegalArgumentException may be thrown if {@code idShort} is not given
         */
        BaSyxSubmodelBuilder(BaSyxAbstractAasBuilder parentBuilder, String idShort, IIdentifier identifier) {
            this.parentBuilder = parentBuilder;
            submodel = new org.eclipse.basyx.submodel.metamodel.map.Submodel(Tools.checkId(idShort), identifier);
            instance = new BaSyxSubmodel(submodel);
            instance.parent = null == parentBuilder ? null : parentBuilder.getSubmodelParent();
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
            this.isNew = false;
        }

        @Override
        public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort, boolean ordered, 
            boolean allowDuplicates) {
            SubmodelElementCollectionBuilder result = instance.getDeferred(idShort, 
                SubmodelElementCollectionBuilder.class);
            if (null == result) {
                result = instance.obtainSubmodelElementCollectionBuilder(this, idShort, ordered, allowDuplicates);
            }
            return result;
        }
        
        @Override
        public void defer() {
            parentBuilder.defer(instance.getIdShort(), this);
        }

        @Override
        public void buildDeferred() {
            parentBuilder.buildMyDeferred();
        }

        @Override
        public Submodel build() {
            buildMyDeferred();
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
        protected AbstractSubmodel<org.eclipse.basyx.submodel.metamodel.map.Submodel> getInstance() {
            return instance;
        }

        @Override
        public boolean isNew() {
            return isNew;
        }

    }

    /**
     * Creates an instance. Prevents external creation.
     * 
     * @param subModel the sub-model instance
     */
    private BaSyxSubmodel(org.eclipse.basyx.submodel.metamodel.map.Submodel subModel) {
        super(subModel);
    }
    
    /**
     * Creates an instance based on a given instance.
     * 
     * @param parent the parent instance
     * @param instance the BaSyx submodel instance
     */
    BaSyxSubmodel(BaSyxSubmodelParent parent, org.eclipse.basyx.submodel.metamodel.map.Submodel instance) {
        super(instance);
        this.parent = parent;
        BaSyxElementTranslator.registerSubmodelElements(instance.getSubmodelElements(), this);
    }
    
    /**
     * Creates a builder for a contained sub-model element collection. Calling this method again with the same name 
     * shall lead to a builder that allows for modifying the sub-model.
     * 
     * @param parent the parent builder
     * @param idShort the short name of the reference element
     * @param ordered whether the collection is ordered
     * @param allowDuplicates whether the collection allows duplicates
     * @return the builder
     * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty; or if modification is not possible
     */
    private SubmodelElementCollectionBuilder obtainSubmodelElementCollectionBuilder(
        BaSyxSubmodelElementContainerBuilder<?> parent, String idShort, boolean ordered, boolean allowDuplicates) {
        SubmodelElementCollectionBuilder result = getDeferred(idShort, SubmodelElementCollectionBuilder.class);
        if (null == result) {
            SubmodelElementCollection sub = getSubmodelElementCollection(idShort);
            if (null == sub) {
                result = new BaSyxSubmodelElementCollection.BaSyxSubmodelElementCollectionBuilder(parent, idShort, 
                    ordered, allowDuplicates);
            } else {
                result = new BaSyxSubmodelElementCollection.BaSyxSubmodelElementCollectionBuilder(parent, 
                   (BaSyxSubmodelElementCollection) sub);
            }
        }
        return result;
    }
    
    @Override
    public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort, boolean ordered,
        boolean allowDuplicates) {
        LoggerFactory.getLogger(getClass()).warn("Adding a submodel to a deployed AAS currently does not lead to "
            + "the deployment of the new submodel (as for initial AAS). If possible, create the submodel in advance.");
        return obtainSubmodelElementCollectionBuilder(new BaSyxSubmodelBuilder(parent.createAasBuilder(), this), 
            idShort, ordered, allowDuplicates);
    }

    @Override
    public void update() {
    }

}
