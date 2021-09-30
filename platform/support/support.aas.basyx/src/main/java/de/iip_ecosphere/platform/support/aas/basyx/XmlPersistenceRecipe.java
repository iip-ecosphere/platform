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
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.basyx.aas.factory.xml.MetamodelToXMLConverter;
import org.eclipse.basyx.aas.factory.xml.XMLToMetamodelConverter;
import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.api.parts.asset.IAsset;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.parts.IConceptDescription;
import org.xml.sax.SAXException;

import de.iip_ecosphere.platform.support.ExtensionBasedFileFormat;
import de.iip_ecosphere.platform.support.FileFormat;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Submodel;

/**
 * An XML persistence recipe.
 * 
 * @author Holger Eichelberger, SSE
 */
class XmlPersistenceRecipe extends AbstractPersistenceRecipe {

    private static final FileFormat XML = new ExtensionBasedFileFormat("xml", "AAS XML", "AAS in XML");

    /**
     * Creates an instance.
     */
    XmlPersistenceRecipe() {
        super(XML);
    }

    @Override
    public void writeTo(List<Aas> aas, File file) throws IOException {
        List<IAssetAdministrationShell> basyxAas = new ArrayList<IAssetAdministrationShell>();
        List<ISubmodel> basyxSubmodels = new ArrayList<ISubmodel>();
        Collection<IAsset> assetList = new ArrayList<IAsset>();
        Collection<IConceptDescription> conceptDescriptionList = new ArrayList<IConceptDescription>();
        for (Aas a : aas) {
            basyxAas.add(((AbstractAas<?>) a).getAas());
            for (Submodel s : a.submodels()) {
                basyxSubmodels.add(((AbstractSubmodel<?>) s).getSubmodel());
            }
            addAsset(a, assetList, IAsset.class);
        }
        
        try (FileWriter result = new FileWriter(file)) {
            MetamodelToXMLConverter.convertToXML(basyxAas, assetList, conceptDescriptionList, basyxSubmodels, 
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
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            XMLToMetamodelConverter conv = new XMLToMetamodelConverter(content);
            transform(conv.parseAAS(), conv.parseSubmodels(), conv.parseAssets(), result);
        } catch (SAXException | ParserConfigurationException e) {
            throw new IOException(e);
        }
        return result;
    }

}
