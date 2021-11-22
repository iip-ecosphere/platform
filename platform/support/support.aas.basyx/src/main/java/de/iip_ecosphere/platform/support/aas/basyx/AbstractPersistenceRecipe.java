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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.api.parts.asset.IAsset;
import org.eclipse.basyx.aas.metamodel.connected.ConnectedAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.parts.Asset;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.api.reference.IKey;
import org.eclipse.basyx.submodel.metamodel.api.reference.IReference;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyElements;
import org.eclipse.basyx.submodel.metamodel.connected.ConnectedSubmodel;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;

import de.iip_ecosphere.platform.support.FileFormat;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.PersistenceRecipe;

/**
 * Basic supporting functions for persistency.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractPersistenceRecipe implements PersistenceRecipe {

    private List<FileFormat> formats;
    
    /**
     * Creates a persistence recipe instance with given file formats.
     * 
     * @param formats the supported formats
     */
    protected AbstractPersistenceRecipe(FileFormat... formats) {
        this.formats = new ArrayList<FileFormat>();
        for (FileFormat f : formats) {
            this.formats.add(f);
        }
        this.formats = Collections.unmodifiableList(this.formats);
    }

    @Override
    public Collection<FileFormat> getSupportedFormats() {
        return formats; // this is unmodifiable
    }

    /**
     * Transforms a list of related {@code aas} and {@code submodels} to a list of {@link Aas} instances of the
     * abstraction.
     * 
     * @param aas the AAS to transform
     * @param submodels the sub-models to transform/link to {@code aas}
     * @param assets the assets to transform/link to {@code aas}
     * @param result the resulting {@link Aas} instances (to be modified as a side effect)
     * @throws IOException in case that something goes wrong
     */
    protected void transform(List<? extends IAssetAdministrationShell> aas, List<? extends ISubmodel> submodels, 
        List<? extends IAsset> assets, List<Aas> result) throws IOException {
        Map<String, Submodel> subMapping = new HashMap<>();
        Map<String, Asset> assetMapping = new HashMap<>();
        for (ISubmodel sm : submodels) { // we do not read back connected AAS 
            if (sm instanceof Submodel) {
                IIdentifier id = sm.getIdentification();
                subMapping.put(id.getIdType() + "/" + id.getId(), (Submodel) sm);
            }
        }
        for (IAsset asset : assets) {
            if (asset instanceof Asset) {
                IIdentifier id = asset.getIdentification();
                assetMapping.put(id.getIdType() + "/" + id.getId(), (Asset) asset);
            }
        }
        for (IAssetAdministrationShell a : aas) {
            if (a instanceof AssetAdministrationShell) {
                BaSyxAas bAas = new BaSyxAas((AssetAdministrationShell) a);
                for (IReference r : a.getSubmodelReferences()) {
                    if (!r.getKeys().isEmpty()) {
                        Submodel submodel = null;
                        for (IKey k : r.getKeys()) {
                            if (KeyElements.SUBMODEL == k.getType()) {
                                submodel = subMapping.get(k.getIdType() + "/" + k.getValue());
                            }
                            if (null != submodel) {
                                bAas.register(new BaSyxSubmodel(bAas, submodel));
                                break;
                            }
                        }
                    }
                }
                IReference r = a.getAssetReference();
                if (null != r && !r.getKeys().isEmpty()) {
                    Asset asset = null;
                    for (IKey k : r.getKeys()) {
                        if (KeyElements.ASSET == k.getType()) {
                            asset = assetMapping.get(k.getIdType() + "/" + k.getValue());
                        }
                    }
                    if (null != asset) {
                        bAas.registerAsset(new BaSyxAsset(asset));
                    }
                }
                result.add(bAas);
            }
        }
    }
    
    /**
     * Adds the asset from {@code aas} to {@code assetList} if feasible.
     * 
     * @param <T> the type of the asset
     * @param aas the AAS to take the asset from
     * @param assetList the asset list to be modified as a side effect
     * @param assetCls the asset class
     * @see #isValidForWriting(IAsset)
     * @throws IllegalArgumentException if the wrong asset instance comes in
     */
    protected static <T> void addAsset(Aas aas, Collection<T> assetList, Class<T> assetCls) {
        if (aas.getAsset() instanceof BaSyxAsset) {
            IAsset asset = ((BaSyxAsset) aas.getAsset()).getAsset();
            if (asset instanceof Asset) {
                if (isValidForWriting(asset)) { // default assets created by BaSyx, cause problems in reading 
                    assetList.add(assetCls.cast(asset));
                }
            } else {
                throw new IllegalArgumentException("Can only write real Asset instances");
            }
        }
    }
    
    /**
     * Returns whether the given {@code asset} is valid for writing.
     * 
     * @param asset the asset to check
     * @return {@code true} if valid, {@code false} else
     */
    protected static boolean isValidForWriting(IAsset asset) {
        return asset.getIdShort().length() > 0;
    }
    
    /**
     * Returns that the result is a local AAS, e.g., a connected AAS is turned transparently into a local copy.
     *  
     * @param aas the AAS to check
     * @return the local AAS (<b>null</b> if <code>aas</code> is neither a local nor a connected AAS)
     */
    protected static AssetAdministrationShell ensureLocal(IAssetAdministrationShell aas) {
        AssetAdministrationShell result;
        if (aas instanceof ConnectedAssetAdministrationShell) {
            result = ((ConnectedAssetAdministrationShell) aas).getLocalCopy();
        } else if (aas instanceof AssetAdministrationShell) {
            result = (AssetAdministrationShell) aas;
        } else {
            result = null; // shall not occur
        }
        return result;
    }

    /**
     * Returns that the result is a local submodel, e.g., a connected submodel is turned transparently into 
     * a local copy.
     *  
     * @param submodel the submodel to check
     * @return the local submodel (<b>null</b> if <code>submodel</code> is neither a local nor a connected submodel)
     */
    protected static Submodel ensureLocal(ISubmodel submodel) {
        Submodel result;
        if (submodel instanceof ConnectedSubmodel) {
            result = ((ConnectedSubmodel) (submodel)).getLocalCopy(); 
        } else if (submodel instanceof Submodel) {
            result = (Submodel) submodel;
        } else {
            result = null;
        }
        return result;
    }

}
