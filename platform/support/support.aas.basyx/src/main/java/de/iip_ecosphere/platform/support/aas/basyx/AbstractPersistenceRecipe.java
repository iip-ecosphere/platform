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
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.submodel.metamodel.api.ISubModel;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.api.reference.IKey;
import org.eclipse.basyx.submodel.metamodel.api.reference.IReference;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyElements;
import org.eclipse.basyx.submodel.metamodel.map.SubModel;

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
     * @param submodels the sub-models for {@code aas} to transform
     * @param result the resulting {@link Aas} instances (to be modified as a side effect)
     * @throws IOException in case that something goes wrong
     */
    protected void transform(List<? extends IAssetAdministrationShell> aas, List<? extends ISubModel> submodels, 
        List<Aas> result) throws IOException {
        Map<String, SubModel> subMapping = new HashMap<>();
        for (ISubModel sm : submodels) {
            if (sm instanceof SubModel) {
                IIdentifier id = sm.getIdentification();
                subMapping.put(id.getIdType() + "/" + id.getId(), (SubModel) sm);
            }
        }
        for (IAssetAdministrationShell a : aas) {
            if (a instanceof AssetAdministrationShell) {
                BaSyxAas bAas = new BaSyxAas((AssetAdministrationShell) a);
                for (IReference r : a.getSubmodelReferences()) {
                    if (!r.getKeys().isEmpty()) {
                        SubModel submodel = null;
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
                result.add(bAas);
            }
        }
    }

}
