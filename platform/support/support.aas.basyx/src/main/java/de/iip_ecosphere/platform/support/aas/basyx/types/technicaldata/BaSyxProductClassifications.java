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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.types.technicaldata.submodelelementcollections.productclassifications.ProductClassifications;

import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxSubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxSubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.ProductClassificationItem;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.ProductClassificationItem.ProductClassificationItemBuilder;

/**
 * Wrapper for the BaSyx product classifications class.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxProductClassifications extends BaSyxSubmodelElementCollection implements 
    de.iip_ecosphere.platform.support.aas.types.technicaldata.ProductClassifications {

    public static final String ID_SHORT = ProductClassifications.IDSHORT;

    /**
     * The sub-model element collection builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxProductClassificationsBuilder extends BaSyxSubmodelElementCollectionBuilder 
        implements ProductClassificationsBuilder {

        /**
         * Creates a sub-model element collection builder. The parent builder must be set by the calling
         * constructor.
         * 
         * @param parentBuilder the parent builder
         * @throws IllegalArgumentException may be thrown if {@code idShort} is not given
         */
        BaSyxProductClassificationsBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder) {
            super(parentBuilder, ID_SHORT, 
                () -> new BaSyxProductClassifications(), 
                () -> new ProductClassifications()); 
            ((BaSyxProductClassifications) getCollectionInstance()).productClassificationItems = new HashMap<>();
        }
        
        /**
         * Creates an instance from an existing BaSyx instance.
         * 
         * @param parentBuilder the parent builder
         * @param instance the BaSyx instance
         */
        BaSyxProductClassificationsBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder,
            BaSyxSubmodelElementCollection instance) {
            super(parentBuilder, instance);
        }
        
        @Override
        protected ProductClassifications getCollection() {
            return (ProductClassifications) super.getCollection();
        }
        
        @Override
        public ProductClassificationItemBuilder createProductClassificationItemBuilder(String idShort, 
            String productClassificationSystem, String productClassId) {
            ProductClassificationItemBuilder result;
            SubmodelElementCollection sub = getCollectionInstance().getSubmodelElementCollection(idShort);
            if (null == sub) {
                result = new BaSyxProductClassificationItem.BaSyxProductClassificationItemBuilder(this, idShort, 
                    productClassificationSystem, productClassId);
            } else {
                result = new BaSyxProductClassificationItem.BaSyxProductClassificationItemBuilder(this, 
                   (BaSyxSubmodelElementCollection) sub);
            }
            return result;
        }

    }
    
    private Map<String, ProductClassificationItem> productClassificationItems;

    /**
     * Creates an instance. Prevents external creation.
     */
    private BaSyxProductClassifications() {
        super();
    }
    
    /**
     * Creates an instance and sets the BaSyx instance directly.
     * 
     * @param collection the collection instance
     */
    BaSyxProductClassifications(org.eclipse.basyx.submodel.types.technicaldata.submodelelementcollections
        .generalinformation.GeneralInformation collection) {
        super(collection);
    }
    
    /**
     * Dynamically initializes the product classification items structure.
     */
    protected void initialize() {
        if (null == productClassificationItems) {
            productClassificationItems = new HashMap<String, ProductClassificationItem>();
            for (ISubmodelElement se : getSubmodelElement().getSubmodelElements().values()) {
                if (se instanceof org.eclipse.basyx.submodel.types.technicaldata
                    .submodelelementcollections.productclassifications.ProductClassificationItem) {
                    BaSyxProductClassificationItem tmp = new BaSyxProductClassificationItem((org.eclipse.basyx.submodel
                        .types.technicaldata.submodelelementcollections.productclassifications
                        .ProductClassificationItem) se);
                    productClassificationItems.put(se.getIdShort(), tmp);
                }
            }
        }
    }

    @Override
    public ProductClassifications getSubmodelElement() {
        return (ProductClassifications) super.getSubmodelElement();
    }

    @Override
    public BaSyxSubmodelElementCollection register(BaSyxSubmodelElementCollection collection) {
        if (collection instanceof ProductClassificationItem) {
            productClassificationItems.put(collection.getIdShort(), (ProductClassificationItem) collection);
        } else {
            super.register(collection);
        }
        return collection;
    }

    @Override
    public int getProductClassificationItemsCount() {
        initialize();
        return productClassificationItems.size();
    }

    @Override
    public ProductClassificationItem getProductClassificationItem(String shortId) {
        initialize();
        return productClassificationItems.get(shortId);
    }

    @Override
    public Iterable<ProductClassificationItem> productClassificationItems() {
        initialize();
        return productClassificationItems.values();
    }

}
