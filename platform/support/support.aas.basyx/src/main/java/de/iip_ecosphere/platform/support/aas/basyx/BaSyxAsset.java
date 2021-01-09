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

import org.eclipse.basyx.aas.metamodel.api.parts.asset.IAsset;
import org.eclipse.basyx.aas.metamodel.map.descriptor.ModelUrn;

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.Asset;
import de.iip_ecosphere.platform.support.aas.AssetKind;
import de.iip_ecosphere.platform.support.aas.basyx.AbstractAas.BaSyxAbstractAasBuilder;

/**
 * Implements the wrapper for BaSyx assets.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxAsset implements Asset {

    private IAsset asset;
    
    /**
     * Implements the asset builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class BaSyxAssetBuilder implements AssetBuilder {
        
        private BaSyxAbstractAasBuilder parent;
        private BaSyxAsset instance;
        
        /**
         * Creates an asset builder.
         * 
         * @param parent the parent builder
         * @param idShort the short id of the asset
         * @param urn the URN of the asset
         * @param kind the asset kind
         */
        BaSyxAssetBuilder(BaSyxAbstractAasBuilder parent, String idShort, String urn, AssetKind kind) {
            this.parent = parent;
            this.instance = new BaSyxAsset();
            this.instance.asset = new org.eclipse.basyx.aas.metamodel.map.parts.Asset(idShort, 
                new ModelUrn(Tools.checkUrn(urn)), Tools.translate(kind));
        }

        @Override
        public Asset build() {
            parent.setAsset(this.instance);
            return this.instance;
        }

    }

    /**
     * Creates an instance, prevents external creation.
     */
    private BaSyxAsset() {
    }
    
    /**
     * Creates an instance based on an asset.
     * 
     * @param asset the asset
     */
    BaSyxAsset(IAsset asset) {
        this.asset = asset;
    }
    
    @Override
    public AssetKind getAssetKind() {
        return Tools.translate(asset.getAssetKind());
    }

    @Override
    public String getIdShort() {
        return asset.getIdShort();
    }
    
    /**
     * Returns the BaSyx instance.
     * 
     * @return the BaSyx instance
     */
    IAsset getAsset() {
        return asset;
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitAsset(this);
    }

}
