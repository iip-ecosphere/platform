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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.DeferredBuilder;
import de.iip_ecosphere.platform.support.aas.IdentifierType;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.Submodel;

/**
 * Abstract implementation of the {@link Aas} interface.
 * 
 * @param <A> the BaSyx AAS type to wrap
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractAas<A extends org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell> 
    implements Aas, BaSyxSubmodelParent {

    /**
     * An abstract builder for two concrete AAS types in BaSyx.
     * 
     * @author Holger Eichelberger, SSE
     */
    public abstract static class BaSyxAbstractAasBuilder implements AasBuilder {
        
        /**
         * Registers a sub-model.
         * 
         * @param submodel the sub-model
         * @return {@code submodel}
         * @throws IOException if the submodel cannot be registered, e.g. due to permission issues
         */
        abstract Submodel register(BaSyxSubmodel submodel) throws IOException;
        
        /**
         * Returns the sub-model parent.
         * 
         * @return the sub-model parent
         */
        abstract BaSyxSubmodelParent getSubmodelParent();

        /**
         * Returns the instance under creation.
         * 
         * @return the instance
         */
        abstract Aas getInstance();
        
        /**
         * Defines the asset for the AAS being under construction.
         * 
         * @param asset the asset
         */
        abstract void setAsset(BaSyxAssetInformation asset);
        
        @Override
        public Reference createReference() {
            return getInstance().createReference();
        }
        
        /**
         * Registers a sub-build as deferred.
         * 
         * @param shortId the shortId of the element
         * @param builder the sub-builder to be registered
         * @see #buildMyDeferred()
         */
        abstract void defer(String shortId, Builder<?> builder);

        /**
         * Calls {@link Builder#build()} on all deferred builders.
         * 
         * @see #defer(String, Builder)
         */
        abstract void buildMyDeferred();
        
    }
    
    private A aas;
    private Map<String, Submodel> submodels = new HashMap<>();
    private BaSyxAssetInformation asset;
    private Map<String, Builder<?>> deferred;

    /**
     * Creates an instance. Prevents external creation.
     * 
     * @param aas the BaSyx AAS instance
     */
    protected AbstractAas(A aas) {
        this.aas = aas;
        org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation asset = aas.getAssetInformation();
        if (null != asset) {
            this.asset = new BaSyxAssetInformation(asset);
        }
    }

    /**
     * Returns the AAS instance.
     * 
     * @return the AAS instance
     */
    public A getAas() {
        return aas;
    }
    
    @Override
    public String getIdShort() {
        return aas.getIdShort();
    }

    @Override
    public Iterable<Submodel> submodels() {
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

    /**
     * Registers a sub-model.
     * 
     * @param <S> the actual sub-model type
     * @param submodel the sub-model to register
     * @return {@code subModel}
     */
    <S extends Submodel> S register(S submodel) {
        submodels.put(submodel.getIdShort(), submodel);
        return submodel;
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitAas(this);
        if (null != asset) {
            asset.accept(visitor);
        }
        for (Submodel sm : visitor.sortSubmodels(submodels.values())) {
            sm.accept(visitor);
        }
        visitor.endAas(this);
    }

    @Override
    public Reference createReference() {
        return BaSyxReference.createModelReference(getAas());
    }

    @Override
    public de.iip_ecosphere.platform.support.aas.AssetInformation getAsset() {
        return asset;
    }

    /**
     * Defines the asset.
     * 
     * @param asset the asset
     */
    protected void setAsset(BaSyxAssetInformation asset) {
        this.asset = asset;
    }

    @Override
    public void delete(Submodel submodel) {
        if (null != submodel) {
            submodels.remove(submodel.getIdShort());
            org.eclipse.digitaltwin.aas4j.v3.model.Reference sRef = Tools.createModelReference(
                ((AbstractSubmodel<?>) submodel).getSubmodel());
            aas.setSubmodels(Tools.removeElements(aas.getSubmodels(), s -> s.equals(sRef)));
        }
    }

    /**
     * Returns an AAS endpoint URI according to the BaSyx naming schema. [public for testing, debugging]
     * 
     * @param server the server address
     * @param aas the AAS
     * @return the endpoint URI
     */
    public static String getAasEndpoint(ServerAddress server, Aas aas) {
        return server.toServerUri() + "/" + Tools.idToUrlPath(aas.getIdShort()) + "/aas";
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

    /**
     * Returns the identification of the AAS.
     * 
     * @return the identification (prefixed according to {@link IdentifierType}, custom if none matches). Can e.g. be 
     *     used with {@link Registry} if not <b>null</b>.
     */
    //@Override // JDK8
    public String getIdentification() {
        return aas.getId();
    }
    
}
