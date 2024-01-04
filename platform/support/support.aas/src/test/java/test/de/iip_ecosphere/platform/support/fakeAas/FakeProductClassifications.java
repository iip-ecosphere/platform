/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.fakeAas;

import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.ProductClassificationItem;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.ProductClassificationItem.ProductClassificationItemBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.ProductClassifications;

/**
 * Represents product classifications.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeProductClassifications extends FakeSubmodelElementCollection implements ProductClassifications {
    
    /**
     * A fake builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class FakeProductClassificationsBuilder extends FakeSubmodelElementCollectionBuilder 
        implements ProductClassificationsBuilder {

        /**
         * Creates an instance.
         * 
         * @param parent the parent builder
         */
        protected FakeProductClassificationsBuilder(FakeSubmodelElementContainerBuilder parent) {
            super(parent, "ProductClassifications", false, false);
        }
        
        @Override
        <T extends SubmodelElement> T registerElement(T elt) {
            if (!elt.getIdShort().equals("ProductClassification")) { // bug in BaSyx sync
                super.registerElement(elt);
            }
            return elt;
        }

        @Override
        protected FakeSubmodelElementCollection createInstance(String idShort) {
            return new FakeProductClassifications(idShort);
        }

        @Override
        public ProductClassificationItemBuilder createProductClassificationItemBuilder(String idShort,
            String productClassificationSystem, String productClassId) {
            return new FakeProductClassificationItem.FakeProductClassificationItemBuilder(this, idShort, 
                productClassificationSystem, productClassId);
        }
        
    }

    /**
     * Creates an instance.
     * 
     * @param idShort the idshort
     */
    protected FakeProductClassifications(String idShort) {
        super(idShort);
    }

    @Override
    public int getProductClassificationItemsCount() {
        return 0; // ignore for now
    }

    @Override
    public ProductClassificationItem getProductClassificationItem(String shortId) {
        return null; // ignore for now
    }

    @Override
    public Iterable<ProductClassificationItem> productClassificationItems() {
        return null; // ignore for now
    }

}
