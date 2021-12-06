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

package de.iip_ecosphere.platform.support.aas.types.technicaldata;

import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.ProductClassificationItem.ProductClassificationItemBuilder;

/**
 * Defines the interface to the product classifications.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ProductClassifications extends SubmodelElementCollection {

    /**
     * The general information builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface ProductClassificationsBuilder extends SubmodelElementCollectionBuilder {

        /**
         * Creates a product classification item builder. 
         * 
         * @param idShort the short id of this item
         * @param productClassificationSystem the common name of the product classification system, e.g., 
         *   "ECLASS" or "IEC CDD".
         * @param productClassId the class of the associated product or industrial equipment in the classification 
         *   system according to the notation of the system. Ideally, the Property/valueId is used to reference the 
         *   IRI/ IRDI of the product class.
         * @return the builder
         * @throws IllegalArgumentException may be thrown if {@code idShort} is not given
         */
        public ProductClassificationItemBuilder createProductClassificationItemBuilder(String idShort, 
            String productClassificationSystem, String productClassId);

    }

    /**
     * Returns the number of product classification items.
     * 
     * @return the number of product classification items
     */
    public int getProductClassificationItemsCount();

    /**
     * Returns a product classification item based on its short id.
     * 
     * @param shortId the short id
     * @return the product classification item
     */
    public ProductClassificationItem getProductClassificationItem(String shortId);

    /**
     * Returns the product classification items.
     * 
     * @return the product classification items
     */
    public Iterable<ProductClassificationItem> productClassificationItems();
    
}
