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

package test.de.iip_ecosphere.platform.support.fakeAas;

import java.util.HashMap;
import java.util.Map;

import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.Asset;
import de.iip_ecosphere.platform.support.aas.Asset.AssetBuilder;
import de.iip_ecosphere.platform.support.aas.AssetKind;
import de.iip_ecosphere.platform.support.aas.DeferredBuilder;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalDataSubmodel;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalDataSubmodel.TechnicalDataSubmodelBuilder;

/**
 * Implements a fake AAS for testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeAas extends FakeElement implements Aas {

    private Map<String, Submodel> submodels = new HashMap<String, Submodel>();
    private Asset asset;
    private Map<String, Builder<?>> deferred;
    
    /**
     * The Fake AAS builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class FakeAasBuilder implements AasBuilder {

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
         * @param instance an existing instance
         */
        FakeAasBuilder(FakeAas instance) {
            this.instance = instance;
        }

        @Override
        public SubmodelBuilder createSubmodelBuilder(String idShort, String identifier) {
            SubmodelBuilder result = instance.getDeferred(idShort, SubmodelBuilder.class);
            if (null == result) {
                result = new FakeSubmodel.FakeSubmodelBuilder(this, idShort, identifier);
            }
            return result;
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
            buildMyDeferred();
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
        
        /**
         * Registers a sub-build as deferred.
         * 
         * @param shortId the shortId of the element
         * @param builder the sub-builder to be registered
         * @see #buildMyDeferred()
         */
        void defer(String shortId, Builder<?> builder) {
            getInstance().defer(shortId, builder);
        }

        /**
         * Calls {@link Builder#build()} on all deferred builders.
         * 
         * @see #defer(String, Builder)
         */
        void buildMyDeferred() {
            getInstance().buildDeferred();
        }

        @Override
        public TechnicalDataSubmodelBuilder createTechnicalDataSubmodelBuilder(String identifier) {
            return null; 
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
        if (null != asset) {
            asset.accept(visitor);
        }
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
    public SubmodelBuilder createSubmodelBuilder(String idShort, String urn) {
        SubmodelBuilder result = getDeferred(idShort, SubmodelBuilder.class);
        if (null == result) {
            result = new FakeSubmodel.FakeSubmodelBuilder(new FakeAasBuilder(this), idShort);
        }
        return result;
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

    /**
     * Registers a sub-build as deferred.
     * 
     * @param shortId the shortId of the element
     * @param builder the sub-builder to be registered
     * @see #buildDeferred()
     */
    void defer(String shortId, Builder<?> builder) {
        deferred = DeferredBuilder.defer(shortId, builder, deferred);
    }

    /**
     * Calls {@link Builder#build()} on all deferred builders.
     * 
     * @see #defer(String, Builder)
     */
    public void buildDeferred() {
        DeferredBuilder.buildDeferred(deferred);
    }

    /**
     * Returns a deferred builder.
     * 
     * @param <B> the builder type
     * @param shortId the short id
     * @param cls the builder type
     * @return the builder or <b>null</b> if no builder for {@code shortId} with the respective type is registered
     */
    <B extends Builder<?>> B getDeferred(String shortId, Class<B> cls) {
        return DeferredBuilder.getDeferred(shortId, cls, deferred);
    }

    @Override
    public AasBuilder createAasBuilder() {
        return new FakeAasBuilder(this);
    }

    @Override
    public TechnicalDataSubmodel getTechnicalDataSubmodel() {
        return null;
    }

}
