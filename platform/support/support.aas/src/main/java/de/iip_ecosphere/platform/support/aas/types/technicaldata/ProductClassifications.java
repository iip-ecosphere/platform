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

import static de.iip_ecosphere.platform.support.aas.types.common.Utils.*;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.types.common.DelegatingSubmodelElementCollection;

/**
 * Defines the interface to the product classifications.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ProductClassifications extends DelegatingSubmodelElementCollection {

    public static final String ID_SHORT = "ProductClassifications";

    /**
     * Creates an instance.
     * 
     * @param delegate the underlying delegate
     */
    ProductClassifications(SubmodelElementCollection delegate) {
        super(delegate);
    }

    /**
     * Returns the items as SME.
     * 
     * @return the items
     */
    private Stream<SubmodelElementCollection> itemsSME() {
        return stream(elements(), SubmodelElementCollection.class, 
            e -> ProductClassificationItem.SEMANTIC_ID.equals(e.getSemanticId()));
    }

    /**
     * Returns the items as {@link ProductClassificationItem}.
     * 
     * @return the items
     */
    private Stream<ProductClassificationItem> items() {
        return itemsSME().map(e -> new ProductClassificationItem(e));
    }

    /**
     * Returns the number of product classification items.
     * 
     * @return the number of product classification items
     */
    public int getProductClassificationItemsCount() {
        return (int) itemsSME().count();
    }

    /**
     * Returns a product classification item based on its short id.
     * 
     * @param nr the product classification number
     * @return the product classification item, may be <b>null</b> if the item does not exist
     */
    public ProductClassificationItem getProductClassificationItem(int nr) {
        return wrapSubmodelElementCollection(this, getCountingIdShort("ProductClassificationItem", nr), 
            s -> new ProductClassificationItem(s));
    }

    /**
     * Returns the product classification items.
     * 
     * @return the product classification items
     */
    public Iterable<ProductClassificationItem> productClassificationItems() {
        return items().collect(Collectors.toList());
    }
    
}
