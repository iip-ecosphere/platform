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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.api.parts.asset.IAsset;
import org.eclipse.basyx.vab.exception.provider.ResourceNotFoundException;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.DeferredBuilder;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.basyx.types.technicaldata.BaSyxTechnicalDataSubmodel;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalDataSubmodel;

/**
 * Abstract implementation of the {@link Aas} interface.
 * 
 * @param <A> the BaSyx AAS type to wrap
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractAas<A extends IAssetAdministrationShell> implements Aas {

    /**
     * Represents the parent instance of a sub-model. Due to the two different AAS types in BaSyx, this
     * cannot just be an AAS instance rather than a pseudo instance being able to provide the correct 
     * operations.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface BaSyxSubmodelParent {
        
        /**
         * Creates an AAS builder on parent level.
         * 
         * @return the AAS builder
         */
        public BaSyxAbstractAasBuilder createAasBuilder();
        
    }
    
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
         */
        abstract Submodel register(BaSyxSubmodel submodel);
        
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
        abstract void setAsset(BaSyxAsset asset);
        
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
    private BaSyxAsset asset;
    private Map<String, Builder<?>> deferred;

    /**
     * Creates an instance. Prevents external creation.
     * 
     * @param aas the BaSyx AAS instance
     */
    protected AbstractAas(A aas) {
        this.aas = aas;
        IAsset asset = aas.getAsset();
        if (null != asset) {
            this.asset = new BaSyxAsset(asset);
        }
    }

    /**
     * Returns the AAS instance.
     * 
     * @return the AAS instance
     */
    A getAas() {
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
        try {
            return submodels.get(idShort);
        } catch (ResourceNotFoundException e) {
            return null;
        }
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
        for (Submodel sm : submodels.values()) {
            sm.accept(visitor);
        }
        visitor.endAas(this);
    }

    @Override
    public Reference createReference() {
        return new BaSyxReference(getAas().getReference());
    }

    @Override
    public de.iip_ecosphere.platform.support.aas.Asset getAsset() {
        return asset;
    }

    /**
     * Defines the asset.
     * 
     * @param asset the asset
     */
    protected void setAsset(BaSyxAsset asset) {
        this.asset = asset;
    }

    @Override
    public void delete(Submodel submodel) {
        if (null != submodel) {
            try {
                submodels.remove(submodel.getIdShort());
                getAas().removeSubmodel(((AbstractSubmodel<?>) submodel).getSubmodel().getIdentification());
            } catch (ResourceNotFoundException e) {
                LoggerFactory.getLogger(getClass()).error("Deleting submodel %s: %s", 
                    submodel.getIdShort(), e.getMessage());
            }
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

    @Override
    public TechnicalDataSubmodel getTechnicalDataSubmodel() {
        return (TechnicalDataSubmodel) getSubmodel(BaSyxTechnicalDataSubmodel.ID_SHORT);
    }

    /**
     * Returns the identification of the identifiable.
     * 
     * @return the identification (for now as string, like URNs are passed into)
     */
    //@Override // JDK8
    public String getIdentification() {
        return Tools.translateIdentifier(aas.getIdentification());
    }
    
}
