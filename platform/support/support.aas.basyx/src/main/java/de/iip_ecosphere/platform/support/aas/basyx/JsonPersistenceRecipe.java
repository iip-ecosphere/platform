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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.basyx.aas.factory.json.JSONToMetamodelConverter;
import org.eclipse.basyx.aas.factory.json.MetamodelToJSONConverter;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.parts.Asset;
import org.eclipse.basyx.submodel.metamodel.map.SubModel;
import org.eclipse.basyx.submodel.metamodel.map.parts.ConceptDescription;

import de.iip_ecosphere.platform.support.ExtensionBasedFileFormat;
import de.iip_ecosphere.platform.support.FileFormat;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Submodel;

/**
 * Implements the JSON persistence recipe. Unfortunately, so far, BaSyx only supports writing non-connected AAS as far 
 * as we can see.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JsonPersistenceRecipe extends AbstractPersistenceRecipe {

    private static final FileFormat JSON = new ExtensionBasedFileFormat("json", "AAS JSON", "AAS in JSON");

    /**
     * Creates a JSON persistence recipe.
     */
    JsonPersistenceRecipe() {
        super(JSON);
    }
    
    @Override
    public void writeTo(List<Aas> aas, File file) throws IOException {
        List<AssetAdministrationShell> basyxAas = new ArrayList<AssetAdministrationShell>();
        List<SubModel> basyxSubmodels = new ArrayList<SubModel>();
        Collection<Asset> assetList = new ArrayList<Asset>();
        Collection<ConceptDescription> conceptDescriptionList = new ArrayList<ConceptDescription>();
        for (Aas a : aas) {
            if (a instanceof BaSyxAas) {
                basyxAas.add(((BaSyxAas) a).getAas());
                for (Submodel s : a.submodels()) {
                    if (s instanceof BaSyxSubmodel) {
                        basyxSubmodels.add(((BaSyxSubmodel) s).getSubmodel());
                    } else {
                        throw new IllegalArgumentException("Can only write directly created submodels: " 
                            + a.getIdShort());
                    }
                }
                addAsset(a, assetList, Asset.class);
            } else {
                throw new IllegalArgumentException("Can only write directly created AAS: " + a.getIdShort());
            }
        }
        
        // TODO check whether BaSyx allows for the interfaces, i.e., connected AAS
        String json = MetamodelToJSONConverter.convertToJSON(basyxAas, assetList, conceptDescriptionList, 
            basyxSubmodels);
        FileUtils.writeStringToFile(file, json, Charset.defaultCharset());
    }

    @Override
    public List<Aas> readFrom(File file) throws IOException {
        List<Aas> result = new ArrayList<Aas>();
        String json = FileUtils.readFileToString(file, Charset.defaultCharset());
        JSONToMetamodelConverter conv = new JSONToMetamodelConverter(json);
        transform(conv.parseAAS(), conv.parseSubmodels(), conv.parseAssets(), result);
        return result;
    }

}
