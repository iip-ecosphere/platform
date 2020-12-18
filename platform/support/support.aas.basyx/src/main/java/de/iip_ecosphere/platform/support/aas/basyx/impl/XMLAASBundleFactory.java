// credits to the BaSys 4.0/4.2 and the Eclipse BaSyx project. Thanks for all your work. We had to take 
// over this file in order to be able to read AASX from file. We will try to reflect the modifications
// into your tasks (if not already addressed) and may remove this file when we upgrade to a newer version
// of BaSyx in the future.

package de.iip_ecosphere.platform.support.aas.basyx.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.basyx.aas.factory.xml.XMLToMetamodelConverter;
import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.api.parts.asset.IAsset;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.parts.Asset;
import org.eclipse.basyx.submodel.metamodel.api.ISubModel;
import org.eclipse.basyx.submodel.metamodel.api.qualifier.IIdentifiable;
import org.eclipse.basyx.submodel.metamodel.api.reference.IKey;
import org.eclipse.basyx.submodel.metamodel.api.reference.IReference;
import org.eclipse.basyx.support.bundle.AASBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Creates multiple {@link AASBundle} from an XML containing several AAS and
 * Submodels <br />
 * TODO: ConceptDescriptions
 * 
 * @author schnicke
 *
 */
public class XMLAASBundleFactory {
    private static Logger logger = LoggerFactory.getLogger(XMLAASBundleFactory.class);

    private String content;

    /**
     * Internal exception used to indicate that the resource was not found
     * 
     * @author schnicke
     *
     */
    private class ResourceNotFoundException extends Exception {
        private static final long serialVersionUID = -1247012719907370743L;
    }

    /**
     * 
     * @param xmlContent
     *            the content of the XML
     */
    public XMLAASBundleFactory(String xmlContent) {
        this.content = xmlContent;
    }

    public XMLAASBundleFactory(Path xmlFile) throws IOException {
        content = new String(Files.readAllBytes(xmlFile));
    }

    /**
     * Creates the set of {@link AASBundle} contained in the XML string.
     * 
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public Set<AASBundle> create() throws ParserConfigurationException, SAXException, IOException {
        XMLToMetamodelConverter converter = new XMLToMetamodelConverter(content);

        List<IAssetAdministrationShell> shells = converter.parseAAS();
        List<ISubModel> submodels = converter.parseSubmodels();

        List<IAsset> assets = converter.parseAssets();

        Set<AASBundle> bundles = new HashSet<>();

        for (IAssetAdministrationShell shell : shells) {
            // Retrieve asset
            try {
                IReference assetRef = shell.getAssetReference();
                if (null != assetRef) { // IIP-Ecosphere add: XML writing allows to go without assets
                    IAsset asset = getByReference(assetRef, assets);
                    ((AssetAdministrationShell) shell).setAsset((Asset) asset);
                }
            } catch (ResourceNotFoundException e) {
                logger.warn("Can't find asset with id " + shell.getAssetReference().getKeys().get(0).getValue() + " for AAS " + shell.getIdShort() + "; If the asset is not provided in another way, this is an error!");
            }

            // Retrieve submodels
            Set<ISubModel> currentSM = retrieveSubmodelsForAAS(submodels, shell);
            bundles.add(new AASBundle(shell, currentSM));
        }

        return bundles;
    }

    /**
     * Retrieves the Submodels belonging to an AAS
     * 
     * @param submodels
     * @param shell
     * @return
     */
    private Set<ISubModel> retrieveSubmodelsForAAS(List<ISubModel> submodels, IAssetAdministrationShell shell) {
        Set<ISubModel> currentSM = new HashSet<>();

        for (IReference submodelRef : shell.getSubmodelReferences()) {
            try {
                ISubModel sm = getByReference(submodelRef, submodels);
                currentSM.add(sm);
                logger.debug("Found Submodel " + sm.getIdShort() + " for AAS " + shell.getIdShort());
            } catch (ResourceNotFoundException e) {
                // If there's no match, the submodel is assumed to be provided by different
                // means, e.g. it is already being hosted
                logger.warn("Could not find Submodel " + submodelRef.getKeys().get(0).getValue() + " for AAS " + shell.getIdShort() + "; If it is not hosted elsewhere this is an error!");
            }
        }
        return currentSM;
    }

    /**
     * Retrieves an identifiable from a list of identifiable by its reference
     * 
     * @param ref
     * @param submodels
     * @return
     * @throws ResourceNotFoundException
     */
    private <T extends IIdentifiable> T getByReference(IReference ref, List<T> submodels) throws ResourceNotFoundException {
        // It may be that only one key fits to the Submodel contained in the XML
        for (IKey key : ref.getKeys()) {
            // There will only be a single submodel matching the identification at max
            Optional<T> match = submodels.stream().filter(s -> s.getIdentification().getId().equals(key.getValue())).findFirst();
            if (match.isPresent()) {
                return match.get();
            }
        }

        // If no identifiable is found, indicate it by throwing an exception
        throw new ResourceNotFoundException();
    }
}
