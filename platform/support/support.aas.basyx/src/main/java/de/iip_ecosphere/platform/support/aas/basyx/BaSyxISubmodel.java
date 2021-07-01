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

import org.eclipse.basyx.submodel.metamodel.api.ISubModel;

import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxConnectedAas.BaSyxConnectedAasBuilder;

/**
 * Represents a generic sub-model just given in terms of the BaSyx interface.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxISubmodel extends AbstractSubmodel<ISubModel> {

    private BaSyxConnectedAas parent;

    /**
     * The builder, just for adding elements.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class BaSyxISubmodelBuilder extends BaSyxSubmodelElementContainerBuilder<ISubModel> 
        implements SubmodelBuilder {
        
        private BaSyxConnectedAasBuilder parentBuilder;
        private BaSyxISubmodel instance;
        
        /**
         * Creates an instance from an existing BaSyx instance.
         * 
         * @param parentBuilder the parent builder
         * @param instance the BaSyx instance
         */
        BaSyxISubmodelBuilder(BaSyxConnectedAasBuilder parentBuilder, BaSyxISubmodel instance) {
            this.parentBuilder = parentBuilder;
            this.instance = instance;
        }

        @Override
        public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort, boolean ordered,
            boolean allowDuplicates) {
            SubmodelElementCollectionBuilder result = instance.getDeferred(idShort, 
                SubmodelElementCollectionBuilder.class);
            if (null == result) {
                SubmodelElementCollection sub = instance.getSubmodelElementCollection(idShort);
                if (null == sub) {
                    result = new BaSyxSubmodelElementCollection.BaSyxSubmodelElementCollectionBuilder(this, idShort, 
                        ordered, allowDuplicates);
                } else {
                    result = new BaSyxSubmodelElementCollection.BaSyxSubmodelElementCollectionBuilder(this, 
                       (BaSyxSubmodelElementCollection) sub);                
                }
            }
            return result;
        }

        @Override
        public SubmodelElementContainerBuilder getParentBuilder() {
            return null;
        }

        @Override
        public AasBuilder getAasBuilder() {
            return parentBuilder;
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
            // do not register, this already exists/is registered
            return instance;
        }

        @Override
        protected AbstractSubmodel<ISubModel> getInstance() {
            return instance;
        }

        @Override
        public boolean isNew() {
            return false; // see constructor
        }

    }
    
    /**
     * Creates sub-model instance.
     * 
     * @param parent the parent AAS
     * @param submodel the instance
     */
    public BaSyxISubmodel(BaSyxConnectedAas parent, ISubModel submodel) {
        super(submodel);
        this.parent = parent;
        BaSyxElementTranslator.registerSubmodelElements(submodel.getSubmodelElements(), this);
    }

    @Override
    public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort, boolean ordered,
        boolean allowDuplicates) {
        SubmodelElementCollectionBuilder result = getDeferred(idShort, SubmodelElementCollectionBuilder.class);
        if (null == result) {
            BaSyxSubmodelElementContainerBuilder<ISubModel> secb = new BaSyxISubmodelBuilder(
                new BaSyxConnectedAasBuilder(parent), this);
    
            SubmodelElementCollection sub = getSubmodelElementCollection(idShort);
            if (null == sub) {
                result = new BaSyxSubmodelElementCollection.BaSyxSubmodelElementCollectionBuilder(
                    secb, idShort, ordered, allowDuplicates);
            } else {
                result = new BaSyxSubmodelElementCollection.BaSyxSubmodelElementCollectionBuilder(secb, 
                   (BaSyxSubmodelElementCollection) sub);
            }
        }
        return result;
    }
    
    @Override
    public void update() {
    }

}
