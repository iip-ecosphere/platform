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

package test.de.iip_ecosphere.platform.support.aas;

import de.iip_ecosphere.platform.support.aas.Asset;
import de.iip_ecosphere.platform.support.aas.AssetKind;
import test.de.iip_ecosphere.platform.support.aas.FakeAas.FakeAasBuilder;

/**
 * Implements a fake asset.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeAsset implements Asset {

    private String shortId;
    private AssetKind kind;
    
    static class FakeAssetBuilder implements AssetBuilder {

        private FakeAasBuilder parent;
        private FakeAsset instance;
        
        /**
         * Creates an asset builder.
         * 
         * @param parent the parent builder
         * @param idShort the short id of the asset
         * @param urn the URN of the asset
         * @param kind the asset kind
         */
        FakeAssetBuilder(FakeAasBuilder parent, String idShort, String urn, AssetKind kind) {
            this.parent = parent;
            this.instance = new FakeAsset(idShort, kind);
        }

        @Override
        public Asset build() {
            parent.getInstance().setAsset(instance);
            return instance;
        }
        
    }
    
    /**
     * Creates an instance.
     * 
     * @param shortId the short id
     * @param kind the asset kind
     */
    private FakeAsset(String shortId, AssetKind kind) {
        this.shortId = shortId;
        this.kind = kind;
    }
    
    @Override
    public AssetKind getAssetKind() {
        return kind;
    }

    @Override
    public String getIdShort() {
        return shortId;
    }

}
