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

import de.iip_ecosphere.platform.support.aas.types.technicaldata.ProductClassificationItem;

/**
 * Fake product classification item.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeProductClassificationItem extends FakeSubmodelElementCollection implements ProductClassificationItem {

    /**
     * Fake builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class FakeProductClassificationItemBuilder extends FakeSubmodelElementCollectionBuilder 
        implements ProductClassificationItemBuilder {

        /**
         * Creates an instance.
         * 
         * @param parent the parent instance
         * @param idShort the id short
         * @param productClassificationSystem the product classification system
         * @param productClassId the product class id
         */
        FakeProductClassificationItemBuilder(FakeSubmodelElementContainerBuilder parent, String idShort,
            String productClassificationSystem, String productClassId) {
            super(parent, idShort, false, false);
        }

        
        @Override
        protected FakeSubmodelElementCollection createInstance(String idShort) {
            return new FakeProductClassificationItem(idShort);
        }


        @Override
        public ProductClassificationItemBuilder setClassificationSystemVersion(String version) {
            // ignore for now
            return this;
        }
        
    }

    /**
     * Creates an instance.
     * 
     * @param idShort the idShort
     */
    protected FakeProductClassificationItem(String idShort) {
        super(idShort);
    }

    @Override
    public String getProductClassId() {
        return null; // ignore for now
    }

    @Override
    public String getProductClassificationSystem() {
        return null; // ignore for now
    }

    @Override
    public String getClassificationSystemVersion() {
        return null; // ignore for now
    }

}
