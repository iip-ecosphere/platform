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

import java.util.HashMap;
import java.util.Map;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.Asset;
import de.iip_ecosphere.platform.support.aas.Asset.AssetBuilder;
import de.iip_ecosphere.platform.support.aas.AssetKind;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;

/**
 * Implements a fake AAS for testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeAas extends FakeElement implements Aas {

    private Map<String, Submodel> submodels = new HashMap<String, Submodel>();
    private Asset asset;
    
    /**
     * The Fake AAS builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class FakeAasBuilder implements AasBuilder {

        private FakeAas instance;
        
        /**
         * Creates an instance.
         * 
         * @param idShort the short id
         * @param urn the URN
         */
        FakeAasBuilder(String idShort, String urn) {
            instance = new FakeAas(idShort); // we do not return the URN so far, so we ignore it here
        }

        /**
         * Creates an instance.
         * 
         * @param instance an axisting instance
         */
        FakeAasBuilder(FakeAas instance) {
            this.instance = instance;
        }

        @Override
        public SubmodelBuilder createSubmodelBuilder(String idShort, String identifier) {
            return new FakeSubmodel.FakeSubmodelBuilder(this, idShort, identifier);
        }

        /**
         * Registers a sub-model.
         * 
         * @param submodel the sub-model
         * @return {@code submodel}
         */
        Submodel register(Submodel submodel) {
            instance.submodels.put(submodel.getIdShort(), submodel);
            return submodel;
        }

        @Override
        public Aas build() {
            return instance;
        }
        
        /**
         * Returns the instance.
         * 
         * @return the instance
         */
        FakeAas getInstance() {
            return instance;
        }
        
        @Override
        public Reference createReference() {
            return new FakeReference();
        }

        @Override
        public AssetBuilder createAssetBuilder(String idShort, String urn, AssetKind kind) {
            return new FakeAsset.FakeAssetBuilder(this, idShort, urn, kind);
        }
        
    }
    
    /**
     * Creates the instance.
     * 
     * @param idShort the short id.
     */
    FakeAas(String idShort) {
        super(idShort);
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitAas(this);
        for (Submodel sm : submodels()) {
            sm.accept(visitor);
        }
        visitor.endAas(this);
    }

    @Override
    public Iterable<? extends Submodel> submodels() {
        return submodels.values();
    }

    @Override
    public int getSubmodelCount() {
        return submodels.size();
    }

    @Override
    public Submodel getSubmodel(String idShort) {
        return submodels.get(idShort);
    }

    @Override
    public SubmodelBuilder addSubmodel(String idShort, String urn) {
        return new FakeSubmodel.FakeSubmodelBuilder(new FakeAasBuilder(this), idShort);
    }

    @Override
    public Reference createReference() {
        return new FakeReference();
    }
    
    /**
     * Defines the asset.
     * 
     * @param asset the asset
     */
    void setAsset(FakeAsset asset) {
        this.asset = asset;
    }
    
    @Override
    public Asset getAsset() {
        return asset;
    }

    @Override
    public void delete(Submodel submodel) {
        submodels.remove(submodel.getIdShort());
    }

}
