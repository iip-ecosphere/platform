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
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.basyx.aas.factory.xml.MetamodelToXMLConverter;
import org.eclipse.basyx.aas.factory.xml.XMLToMetamodelConverter;
import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.api.parts.asset.IAsset;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.components.aasx.AASXPackageManager;
import org.eclipse.basyx.submodel.metamodel.api.ISubModel;
import org.eclipse.basyx.submodel.metamodel.api.parts.IConceptDescription;
import org.eclipse.basyx.submodel.metamodel.api.reference.IReference;
import org.eclipse.basyx.submodel.metamodel.map.SubModel;
import org.eclipse.basyx.support.bundle.AASBundle;
import org.xml.sax.SAXException;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.PersistenceRecipe;
import de.iip_ecosphere.platform.support.aas.Submodel;

/**
 * A persistence recipe for BaSyx AAS. This implementation is internally based on short ids. 
 * 
 * Limitations:
 * <ul>
 *   <li>This class does not consider concept descriptions or assets when reading directly from XML.</li>
 *   <li>Might be this is not enough for reading models uniquely back directly from XML.</li>
 * </ul>
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
        List<Aas> result = new ArrayList<Aas>();
        if (file.getName().toLowerCase().endsWith(".aasx")) {
            readFromAasx(file, result);
        } else {
            readFromXml(file, result);
        }
        return result;
    }

    /**
     * Reads AAS from an AAS ZIP package in {@code file}.
     * 
     * @param file the file to read
     * @param result the resulting {@link Aas} instances (to be modified as a side effect)
     * @throws IOException in case of I/O reading problems
     */
    private void readFromAasx(File file, List<Aas> result) throws IOException {
        try {
            AASXPackageManager apm = new AASXPackageManager(file.getAbsolutePath());
            Set<AASBundle> bundles = apm.retrieveAASBundles();
            List<IAssetAdministrationShell> aas = new ArrayList<>();
            List<ISubModel> submodels = new ArrayList<>();
            for (AASBundle b : bundles) {
                aas.add(b.getAAS());
                submodels.addAll(b.getSubmodels());
                
                aas.clear();
                submodels.clear();
            }
        } catch (SAXException | ParserConfigurationException e) {
            throw new IOException(e);
        }
    }

    /**
     * Reads AAS from XML in {@code file}.
     * 
     * @param file the file to read
     * @param result the resulting {@link Aas} instances (to be modified as a side effect)
     * @throws IOException in case of I/O reading problems
     */
    private void readFromXml(File file, List<Aas> result) throws IOException {
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            XMLToMetamodelConverter conv = new XMLToMetamodelConverter(content);
            transform(conv.parseAAS(), conv.parseSubmodels(), result);
        } catch (SAXException | ParserConfigurationException e) {
            throw new IOException(e);
        }
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
    private void transform(List<IAssetAdministrationShell> aas, List<ISubModel> submodels, List<Aas> result) 
        throws IOException {
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
                    if (!r.getKeys().isEmpty()) {
                        String name = r.getKeys().get(0).getValue(); // really the first??
                        SubModel submodel = subMapping.get(name);
                        if (null != submodel) {
                            bAas.register(new BaSyxSubmodel(bAas, submodel));
                        }
                    }
                }
                result.add(bAas);
            }
        }
    }

}
