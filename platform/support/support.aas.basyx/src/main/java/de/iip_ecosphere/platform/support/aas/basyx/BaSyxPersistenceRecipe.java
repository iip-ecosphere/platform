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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.basyx.aas.factory.xml.MetamodelToXMLConverter;
import org.eclipse.basyx.aas.factory.xml.XMLToMetamodelConverter;
import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.api.parts.asset.IAsset;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.submodel.metamodel.api.ISubModel;
import org.eclipse.basyx.submodel.metamodel.api.parts.IConceptDescription;
import org.eclipse.basyx.submodel.metamodel.api.reference.IReference;
import org.eclipse.basyx.submodel.metamodel.map.SubModel;
import org.xml.sax.SAXException;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.PersistenceRecipe;
import de.iip_ecosphere.platform.support.aas.Submodel;

/**
 * A persistence recipe for BaSyx AAS. This implementation is internally based on short ids. Might be this is
 * not enough for reading models uniquely back.
 * 
 * @author Holger Eichelberger, SSE
 */
class BaSyxPersistenceRecipe implements PersistenceRecipe {

    // TODO ZIP
    @Override
    public void writeTo(List<Aas> aas, File file) throws IOException {
        List<IAssetAdministrationShell> basyxAas = new ArrayList<IAssetAdministrationShell>();
        List<ISubModel> basyxSubmodelle = new ArrayList<ISubModel>();
        Collection<IAsset> assetList = new ArrayList<IAsset>();
        Collection<IConceptDescription> conceptDescriptionList = new ArrayList<IConceptDescription>();
        for (Aas a : aas) {
            basyxAas.add(((BaSyxAas) a).getAas());
            for (Submodel s : a.submodels()) {
                basyxSubmodelle.add(((BaSyxSubmodel) s).getSubmodel());
            }
        }
        
        try (FileWriter result = new FileWriter(file)) {
            MetamodelToXMLConverter.convertToXML(basyxAas, assetList, conceptDescriptionList, basyxSubmodelle, 
                new StreamResult(result));
        } catch (IOException e) {
            throw e;
        } catch (ParserConfigurationException | TransformerException e) {
            throw new IOException(e);
        }
    }
    
    @Override
    public List<Aas> readFrom(File file) throws IOException {
        // TODO ZIP
        List<Aas> result = new ArrayList<Aas>();
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            XMLToMetamodelConverter conv = new XMLToMetamodelConverter(content);
            List<IAssetAdministrationShell> aas = conv.parseAAS();
            List<ISubModel> submodels = conv.parseSubmodels();
            Map<String, SubModel> subMapping = new HashMap<>();
            for (ISubModel sm : submodels) {
                if (sm instanceof SubModel) {
                    subMapping.put(sm.getIdShort(), (SubModel) sm);
                }
            }
            for (IAssetAdministrationShell a : aas) {
                if (a instanceof AssetAdministrationShell) {
                    BaSyxAas bAas = new BaSyxAas((AssetAdministrationShell) a);
                    for (IReference r : a.getSubmodelReferences()) {
                        String name = r.getKeys().get(0).getValue();
                        SubModel submodel = subMapping.get(name);
                        if (null != submodel) {
                            bAas.register(new BaSyxSubmodel(bAas, submodel));
                        }
                    }
                    result.add(bAas);
                }
            }
        } catch (SAXException | ParserConfigurationException e) {
            throw new IOException(e);
        }
        return result;
    }

}
