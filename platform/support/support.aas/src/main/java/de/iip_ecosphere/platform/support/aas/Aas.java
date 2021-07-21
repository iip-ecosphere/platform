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

package de.iip_ecosphere.platform.support.aas;

import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.aas.Asset.AssetBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;

/**
 * Represents an AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Aas extends Element, Identifiable, HasDataSpecification, DeferredParent {
    
    /**
     * Used to build an AAS. For creating an instance/type AAS, use 
     * {@link #createAssetBuilder(String, String, AssetKind)}. 
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface AasBuilder extends Builder<Aas> {

        /**
         * Creates a builder for a contained sub-model. Calling this method again with the same name shall
         * lead to a builder that allows for modifying the sub-model. If the parent AAS was deployed before via 
         * {@link Registry#createAas(Aas, String)}, the {@link SubmodelBuilder#build()} will automatically deploy
         * this submodel into the parent AAS via the parent registry.
         * 
         * @param idShort the short id of the sub-model
         * @param identifier the identifier of the sub-model (may be <b>null</b> or empty for an identification based on
         *    {@code idShort}, interpreted as an URN if this starts with {@code urn})
         * @return the builder
         * @throws IllegalArgumentException if {@code idShort} or {@code urn} is <b>null</b> or empty; or if 
         *   modification is not possible
         */
        public SubmodelBuilder createSubmodelBuilder(String idShort, String identifier);

        /**
         * Returns the reference to the AAS.
         * 
         * @return the reference
         */
        public Reference createReference();
        
        /**
         * Creates an asset builder for this AAS.
         * 
         * @param idShort the short id of the asset
         * @param urn the URN of the asset
         * @param kind the asset kind
         * @return the asset builder
         * @throws IllegalArgumentException if creating an asset builder is not possible, e.g., because {@code idShort} 
         *     or {@code urn} are <b>null</b> or empty or because creating an asset on an already deployed AAS does not 
         *     work
         */
        public AssetBuilder createAssetBuilder(String idShort, String urn, AssetKind kind);
        
    }
    
    /**
     * Returns the sub-models.
     * 
     * @return the sub-models
     */
    public Iterable<? extends Submodel> submodels();
    
    /**
     * Returns the number of sub-models.
     * 
     * @return the number of sub-models
     */
    public int getSubmodelCount();
    
    /**
     * Returns the sub-model with the specified name.
     * 
     * @param idShort the short name to search for
     * @return the sub-model or <b>null</b> if there was none
     */
    public Submodel getSubmodel(String idShort);

    /**
     * Creates an AAS builder for this AAS.
     * 
     * @return the AAS builder
     */
    public AasBuilder createAasBuilder();
    
    /**
     * Returns a sub-model builder either by providing access to an existing sub-model or by a builder instance to 
     * create a new one (only if finally {@link Builder#build()} is called). However, added sub-models are
     * not automatically deployed as the AAS just maintains a reference to the sub-model (in contrast to initial
     * deployment where we can consider sub-models). If a late sub-model shall be deployed/made available, keep
     * the instance of the {@link AasServer} and explicitly deploy the new sub-model via 
     * {@link AasServer#deploy(Aas, Submodel)}.
     * 
     * @param idShort the short id of the sub-model
     * @param identifier the identifier of the sub-model (may be <b>null</b> or empty for an identification based on 
     *    {@code idShort}, interpreted as an URN if this starts with {@code urn})
     * @return the sub-model builder
     */
    public SubmodelBuilder createSubmodelBuilder(String idShort, String identifier);

    /**
     * Returns the reference to the AAS.
     * 
     * @return the reference
     */
    public Reference createReference();

    /**
     * Returns the attached asset.
     * 
     * @return the asset (may be <b>null</b> for none)
     */
    public Asset getAsset();

    /**
     * Deletes the given sub-model.
     * 
     * @param submodel the sub-model to delete
     */
    public void delete(Submodel submodel);
    
}
