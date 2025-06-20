/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx2;

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.AssetInformation;
import de.iip_ecosphere.platform.support.aas.AssetKind;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.basyx2.AbstractAas.BaSyxAbstractAasBuilder;

/**
 * Implements the wrapper for BaSyx assets.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxAssetInformation implements AssetInformation {

    private org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation asset;
    
    /**
     * Implements the asset builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class BaSyxAssetInformationBuilder implements AssetInformationBuilder {
        
        private BaSyxAbstractAasBuilder parent;
        private BaSyxAssetInformation instance;
        private org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation asset;
        
        /**
         * Creates an asset builder.
         * 
         * @param parent the parent builder
         * @param idShort the short id of the asset
         * @param identifier the identifier of the AAS (may be <b>null</b> or empty for an identification based on 
         *    {@code idShort}, interpreted as an URN if this starts with {@code urn})
         * @param kind the asset kind
         */
        BaSyxAssetInformationBuilder(BaSyxAbstractAasBuilder parent, String idShort, String identifier, 
            AssetKind kind) {
            this.parent = parent;
            this.instance = new BaSyxAssetInformation();
            this.asset = new org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetInformation();
            this.asset.setGlobalAssetId(idShort); // initial value, may be overwritten
            this.asset.setAssetKind(Tools.translate(kind));
            // Tools.translateIdentifier(identifier, idShort); // TODO SpecificAssetId
            this.instance.asset = this.asset;
        }

        @Override
        public AssetInformation build() {
            parent.setAsset(this.instance);
            return this.instance;
        }

        @Override
        public AssetInformationBuilder setDescription(LangString... description) {
            // this.asset.setDescription(Tools.translate(description)); // TODO does not exist anymore
            return this;
        }

        @Override
        public Reference createReference() {
            return null; // new BaSyxReference(asset.getReference()); // TODO does not exist anymore
        }

    }

    /**
     * Creates an instance, prevents external creation.
     */
    private BaSyxAssetInformation() {
    }
    
    /**
     * Creates an instance based on an asset.
     * 
     * @param asset the asset
     */
    BaSyxAssetInformation(org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation asset) {
        this.asset = asset;
    }
    
    @Override
    public AssetKind getAssetKind() {
        return Tools.translate(asset.getAssetKind());
    }

    @Override
    public String getIdShort() {
        // idShort does not exist anymore, same by initialization if not overwritten
        return asset.getGlobalAssetId(); 
    }
    
    /**
     * Returns the BaSyx instance.
     * 
     * @return the BaSyx instance
     */
    org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation getAsset() {
        return asset;
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitAsset(this);
    }

    @Override
    public Reference createReference() {
        return null; // TODO does not exist anymore new BaSyxReference(asset.getReference());
    }

}
