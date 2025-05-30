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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.aasx.InMemoryFile;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;

import de.iip_ecosphere.platform.support.FileFormat;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.PersistenceRecipe;
import de.iip_ecosphere.platform.support.aas.Submodel;

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
     * Transforms an environment and related files to a list of {@link Aas} instances of the
     * abstraction.
     * 
     * @param env the environment
     * @param relatedFiles the optional related files, may be <b>null</b>
     * @return the resulting {@link Aas} instances (to be modified as a side effect)
     * @throws IOException in case that something goes wrong
     */
    protected List<Aas> transform(Environment env, List<InMemoryFile> relatedFiles) throws IOException {
        List<Aas> result = new ArrayList<Aas>();
        transform(env.getAssetAdministrationShells(), env.getSubmodels(), result);
        //env.getConceptDescriptions();
        return result;
    }
            
    /**
     * Transforms a list of related {@code aas} and {@code submodels} to a list of {@link Aas} instances of the
     * abstraction.
     * 
     * @param aas the AAS to transform
     * @param submodels the sub-models to transform/link to {@code aas}
     * @param result the resulting {@link Aas} instances (to be modified as a side effect)
     * @throws IOException in case that something goes wrong
     */
    protected void transform(List<? extends org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell> aas, 
        List<? extends org.eclipse.digitaltwin.aas4j.v3.model.Submodel> submodels, List<Aas> result) 
        throws IOException {
        
        Map<String, org.eclipse.digitaltwin.aas4j.v3.model.Submodel> subMapping = new HashMap<>();
        for (org.eclipse.digitaltwin.aas4j.v3.model.Submodel sm : submodels) { // we do not read back connected AAS 
            subMapping.put(sm.getId(), sm);
        }
        for (org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell a : aas) {
            BaSyxAas bAas = new BaSyxAas(a, null);
            for (org.eclipse.digitaltwin.aas4j.v3.model.Reference r : a.getSubmodels()) {
                if (!r.getKeys().isEmpty()) {
                    org.eclipse.digitaltwin.aas4j.v3.model.Submodel submodel = null;
                    for (org.eclipse.digitaltwin.aas4j.v3.model.Key k : r.getKeys()) {
                        if (org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes.SUBMODEL == k.getType()) {
                            submodel = subMapping.get(k.getValue());
                        }
                        if (null != submodel) {
                            bAas.register(new BaSyxSubmodel(bAas, submodel, null));
                            break;
                        }
                    }
                }
            }
            result.add(bAas);
        }
    }
    
    /**
     * Creates an AAS environment for writing.
     * 
     * @param aas the AAS to write
     * @return the environment
     */
    protected Environment buildEnvironment(List<Aas> aas) {
        DefaultEnvironment.Builder envBuilder = new DefaultEnvironment.Builder();
        for (Aas a: aas) {
            envBuilder.assetAdministrationShells(((AbstractAas<?>) a).getAas());
            for (Submodel s : a.submodels()) {
                envBuilder.submodels(((AbstractSubmodel<?>) s).getSubmodel());
            }
        }
        // envBuilder.conceptDescriptions()
        return envBuilder.build();
    }
    
}
