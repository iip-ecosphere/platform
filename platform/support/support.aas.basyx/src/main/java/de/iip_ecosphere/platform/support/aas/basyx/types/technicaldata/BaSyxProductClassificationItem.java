/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx.types.technicaldata;

import org.eclipse.basyx.submodel.types.technicaldata.submodelelementcollections.productclassifications.ProductClassificationItem;
import org.eclipse.basyx.vab.exception.provider.ResourceNotFoundException;

import de.iip_ecosphere.platform.support.aas.basyx.BaSyxProperty;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxSubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxSubmodelElementContainerBuilder;

/**
 * Wraps a product classification item.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxProductClassificationItem extends BaSyxSubmodelElementCollection implements 
    de.iip_ecosphere.platform.support.aas.types.technicaldata.ProductClassificationItem {

    /**
     * The sub-model element collection builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxProductClassificationItemBuilder extends BaSyxSubmodelElementCollectionBuilder 
        implements ProductClassificationItemBuilder {

        /**
         * Creates a sub-model element collection builder. The parent builder must be set by the calling
         * constructor.
         * 
         * @param parentBuilder the parent builder
         * @param idShort the short id of this item
         * @param productClassificationSystem the common name of the product classification system, e.g., 
         *   "ECLASS" or "IEC CDD".
         * @param productClassId the class of the associated product or industrial equipment in the classification 
         *   system according to the notation of the system. Ideally, the Property/valueId is used to reference the 
         *   IRI/ IRDI of the product class.
         * @throws IllegalArgumentException may be thrown if {@code idShort} is not given
         */
        BaSyxProductClassificationItemBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, String idShort, 
            String productClassificationSystem, String productClassId) {
            super(parentBuilder, idShort, 
                () -> new BaSyxProductClassificationItem(), 
                () -> new ProductClassificationItem(idShort, productClassificationSystem, productClassId)); 
        }
    
        /**
         * Creates an instance from an existing BaSyx instance.
         * 
         * @param parentBuilder the parent builder
         * @param instance the BaSyx instance
         */
        BaSyxProductClassificationItemBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder,
            BaSyxSubmodelElementCollection instance) {
            super(parentBuilder, instance);
        }
        
        @Override
        protected ProductClassificationItem getCollection() {
            return (ProductClassificationItem) super.getCollection();
        }
    
        @Override
        public BaSyxProductClassificationItemBuilder setClassificationSystemVersion(String version) {
            ProductClassificationItem item = getCollection();
            item.setClassificationSystemVersion(version);
            register(new BaSyxProperty(item.getClassificationSystemVersion()));
            return this;
        }

    }

    /**
     * Creates an instance. Prevents external creation.
     */
    private BaSyxProductClassificationItem() {
        super();
    }

    /**
     * Creates an instance and sets the BaSyx instance directly.
     * 
     * @param collection the collection instance
     */
    BaSyxProductClassificationItem(ProductClassificationItem collection) {
        super(collection);
    }

    @Override
    public ProductClassificationItem getSubmodelElement() {
        return (ProductClassificationItem) super.getSubmodelElement();
    }
    
    @Override
    public String getProductClassId() {
        return (String) getSubmodelElement().getProductClassId().getValue();
    }

    @Override
    public String getProductClassificationSystem() {
        return (String) getSubmodelElement().getProductClassificationSystem().getValue();
    }

    @Override
    public String getClassificationSystemVersion() {
        String result;
        try {
            result = (String) getSubmodelElement().getClassificationSystemVersion().getValue();
        } catch (ResourceNotFoundException e) {
            result = null; // optional, not there
        }
        return result;
    }

}