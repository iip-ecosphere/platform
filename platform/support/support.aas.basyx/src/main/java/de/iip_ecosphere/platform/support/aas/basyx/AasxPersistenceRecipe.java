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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.basyx.aas.factory.aasx.AASXFactory;
import org.eclipse.basyx.aas.factory.aasx.InMemoryFile;
import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.api.parts.asset.IAsset;
import org.eclipse.basyx.components.aas.aasx.AASXPackageManager;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.parts.IConceptDescription;
import org.eclipse.basyx.support.bundle.AASBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import de.iip_ecosphere.platform.support.ExtensionBasedFileFormat;
import de.iip_ecosphere.platform.support.FileFormat;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Submodel;

/**
 * Persistence recipe for AASX.
 * 
 * @author Holger Eichelberger, SSE
 */
class AasxPersistenceRecipe extends AbstractPersistenceRecipe {

    private static final Logger LOGGER = LoggerFactory.getLogger(AasxPersistenceRecipe.class);
    private static final FileFormat AASX = new ExtensionBasedFileFormat("aasx", "AASX", "AASX package");
    
    /**
     * Creates a recipe instance.
     */
    AasxPersistenceRecipe() {
        super(AASX);
    }
    
    @Override
    public void writeTo(List<Aas> aas, File file) throws IOException {
        if (aas.size() > 1) {
            LOGGER.warn("Writing multiple AAS to a single file may not be read back as "
                + "BaSyx currently just supports one AAS to be read from an AASX package.");
        }        
        List<IAssetAdministrationShell> basyxAas = new ArrayList<IAssetAdministrationShell>();
        List<ISubmodel> basyxSubmodels = new ArrayList<ISubmodel>();
        Collection<IAsset> assetList = new ArrayList<IAsset>();
        Collection<IConceptDescription> conceptDescriptionList = new ArrayList<IConceptDescription>();
        for (Aas a : aas) {
            IAssetAdministrationShell origAas = ((AbstractAas<?>) a).getAas();
            if (null == origAas.getAsset()) {  // as of BaSyx 0.0.1
                LOGGER.warn("AAS '" + a.getIdShort() 
                    + "' may not be read back correctly as it does not have an Asset.");
            } else {
                assetList.add(origAas.getAsset());
            }
            if (null == origAas.getAssetReference()) { // as of BaSyx 0.1.0
                LOGGER.warn("AAS '" + a.getIdShort() + "' may not be read back correctly as it does not have "
                    + "an Asset Reference.");
            }
            basyxAas.add(origAas);
            for (Submodel s : a.submodels()) {
                basyxSubmodels.add(((AbstractSubmodel<?>) s).getSubmodel());
            }
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            AASXFactory.buildAASX(basyxAas, assetList, conceptDescriptionList, basyxSubmodels, 
                new ArrayList<InMemoryFile>(), out);
            out.close();
        } catch (TransformerException | ParserConfigurationException e) {
            throw new IOException(e);
        }
    }
    
    @Override
    public List<Aas> readFrom(File file) throws IOException {
        List<Aas> result = new ArrayList<Aas>();
        try {
            AASXPackageManager apm = new AASXPackageManager(file.getAbsolutePath());
            Set<AASBundle> bundles = apm.retrieveAASBundles();
            List<IAssetAdministrationShell> aas = new ArrayList<>();
            List<ISubmodel> submodels = new ArrayList<>();
            List<IAsset> assets = new ArrayList<>();
            for (AASBundle b : bundles) {
                aas.add(b.getAAS());
                submodels.addAll(b.getSubmodels());
                // TODO BaSyx, unclear how to get the assets here
                transform(aas, submodels, assets, result);
                aas.clear();
                submodels.clear();
            }
        } catch (SAXException | ParserConfigurationException | InvalidFormatException e) {
            e.printStackTrace();            
            throw new IOException(e);
        }
        return result;
    }

}
