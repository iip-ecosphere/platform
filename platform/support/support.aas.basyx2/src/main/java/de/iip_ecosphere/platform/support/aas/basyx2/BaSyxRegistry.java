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
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.basyx.aasregistry.client.ApiException;
import org.eclipse.digitaltwin.basyx.aasregistry.client.api.RegistryAndDiscoveryInterfaceApi;
import org.eclipse.digitaltwin.basyx.aasregistry.client.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.basyx.aasregistry.client.model.GetAssetAdministrationShellDescriptorsResult;
import org.eclipse.digitaltwin.basyx.aasrepository.client.ConnectedAasRepository;
import org.eclipse.digitaltwin.basyx.core.exceptions.CollidingIdentifierException;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.digitaltwin.basyx.submodelregistry.client.api.SubmodelRegistryApi;
import org.eclipse.digitaltwin.basyx.submodelregistry.client.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.basyx.submodelrepository.client.ConnectedSubmodelRepository;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.IdentifierType;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.SetupSpec;
import de.iip_ecosphere.platform.support.aas.Submodel;

/**
 * Implements a registry recipe for BaSyx.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxRegistry implements Registry {

    private static final Integer PAGINATION_UNLIMITED = null;

    private SetupSpec spec;
    private RegistryAndDiscoveryInterfaceApi aasRegistry;
    private SubmodelRegistryApi smRegistry;
    private ConnectedAasRepository aasRepo;
    private ConnectedSubmodelRepository smRepo;

    // checkstyle: stop exception type check

    /**
     * Creates a registry instance from the given deployment specification.
     * 
     * @param spec the deployment specification
     */
    BaSyxRegistry(SetupSpec spec) {
        this.spec = spec;
        aasRegistry = AasRegistryUtils.createRegistryApi(spec, null);
        smRegistry = SubmodelRegistryUtils.createRegistryApi(spec, null);
        aasRepo = AasRepositoryUtils.createRepositoryApi(spec, null);
        smRepo = SubmodelRepositoryUtils.createRepositoryApi(spec, null);
    }

    // checkstyle: resume exception type check

    /**
     * Returns the default AAS repository of this registry.
     * 
     * @return the AAS repository
     */
    ConnectedAasRepository getAasRepository() {
        return aasRepo;
    }

    /**
     * Returns the default submodel repository of this registry.
     * 
     * @return the submodel repository
     */
    ConnectedSubmodelRepository getSubmodelRepository() {
        return smRepo;
    }
    
    @Override
    public Aas retrieveAas(String identifier) throws IOException {
        return retrieveAas(identifier, true);
    }
    
    /**
     * Returns the descriptor for an AAS.
     * 
     * @param identifier the identifier of the AAS (may be <b>null</b> or empty for an identification based on 
     *    {@code idShort}, interpreted as an URN if this starts with {@code urn}, see {@link IdentifierType} for 
     *    others, or as an endpoint if it starts with {@code http://} or {@code https://})
     * @return the descriptor or <b>null</b> if the AAS cannot be found
     */
    AssetAdministrationShellDescriptor getAasDescriptor(String identifier) {
        AssetAdministrationShellDescriptor desc = null;
        if (null != identifier) {
            final String id = Tools.translateIdentifierToBaSyx(identifier, "");
            try {
                desc = aasRegistry.getAssetAdministrationShellDescriptorById(id);
            } catch (ApiException e) {
                Optional<AssetAdministrationShellDescriptor> o = allAas()
                    .filter(d -> (id.equals(d.getId()) || id.equals(d.getIdShort()) 
                        || AasRegistryUtils.hasEndpointUrl(d, id)))
                    .findFirst();
                if (o.isPresent()) {
                    desc = o.get();
                }
            }
        }        
        return desc;
    }
    
    @Override
    public BaSyxAas retrieveAas(String identifier, boolean populate) throws IOException {
        BaSyxAas result = null;
        if (null != identifier) {
            AssetAdministrationShellDescriptor desc = getAasDescriptor(identifier);
            AssetAdministrationShell aas = null;
            ConnectedAasRepository aasRepo;
            if (null != desc) {
                String epUrl = AasRegistryUtils.getFirstEndpointUrl(desc);
                aasRepo = getAasRepo(epUrl);
                aas = getAas(aasRepo, desc.getId());
                if (null == aas) {
                    aas = getAas(aasRepo, desc.getIdShort());
                }
                if (null == aas) {
                    aas = getAas(aasRepo, identifier);
                }
            } else {
                aasRepo = getAasRepo(null);
                aas = getAas(aasRepo, identifier);
            }
            if (null != aas) {
                result = new BaSyxAas(aas, this);
                if (populate) {
                    for (org.eclipse.digitaltwin.aas4j.v3.model.Reference r : result.getAas().getSubmodels()) {
                        Optional<Key> smk = r.getKeys().stream()
                            .filter(k -> k.getType() == KeyTypes.SUBMODEL)
                            .findFirst();
                        if (smk.isPresent()) {
                            Key k = smk.get();
                            result.register(retrieveSubmodel(result, k.getValue()));
                        }
                    }
                }
            } else {
                throw new IOException("AAS " + identifier + " not exist!");
            }
        }
        return result;
    }
    
    /**
     * Returns an AAS from a given repository catching potential exceptions.
     * 
     * @param aasRepo the AAS repository
     * @param aasId the AAS identifier, may be <b>null</b> or empty
     * @return the AAS or <b>null</b> for none
     * @throws IOException if the AAS cannot be accessed, e.g., as not permitted
     */
    private AssetAdministrationShell getAas(ConnectedAasRepository aasRepo, String aasId) throws IOException {
        AssetAdministrationShell result = null;
        if (null != aasRepo && !StringUtils.isBlank(aasId)) {
            try {
                result = aasRepo.getAas(aasId);
            } catch (ElementDoesNotExistException e) {
            } catch (org.eclipse.digitaltwin.basyx.client.internal.ApiException e) {
                if (allAas().anyMatch(a -> a.getId().equals(aasId))) {
                    throw new IOException("Obtaining AAS " + aasId + ": " + e.getMessage(), e);
                }  // it's there, we just cannot access it
            }
        }
        return result;
    }
    
    /**
     * Returns the (fallback) AAS repository for the given URL.
     * 
     * @param epUrl the repo URL
     * @return the repository or the fallback repository
     */
    private ConnectedAasRepository getAasRepo(String epUrl) {
        // we may cache those
        return StringUtils.isBlank(epUrl) ? aasRepo 
            : AasRepositoryUtils.createRepositoryApi(spec, toUriWithoutPath(epUrl));
    }
    
    /**
     * Returns a stream of all known. 
     * 
     * @return the known AAS
     */
    private Stream<AssetAdministrationShellDescriptor> allAas() {
        try {
            GetAssetAdministrationShellDescriptorsResult res = aasRegistry.getAllAssetAdministrationShellDescriptors(
                PAGINATION_UNLIMITED, null, null, null);
            return res.getResult().stream();
        } catch (ApiException e) {
            return Stream.<AssetAdministrationShellDescriptor>empty();
        }
    }

    @Override
    public List<String> getAasIdShorts() {
        return allAas().map(a -> a.getIdShort()).collect(Collectors.toList());
    }

    @Override
    public List<String> getAasIdentifiers() {
        return allAas().map(a -> a.getId()).collect(Collectors.toList());
    }

    @Override
    public Submodel retrieveSubmodel(String aasIdentifier, String submodelIdentifier) throws IOException {
        Submodel result = null;
        if (null != aasIdentifier && null != submodelIdentifier) {
            BaSyxAas aas = retrieveAas(aasIdentifier, true);
            String submodelId = Tools.translateIdentifierToBaSyx(submodelIdentifier, "");
            result = retrieveSubmodel(aas, submodelId);
        }
        return result;
    }

    /**
     * Retrieves a submodel for {@code aas}.
     * 
     * @param aas the AAS
     * @param submodelId the submodelId
     * @return the submodel or <b>null</b> if not found
     * @throws IOException if failed
     */
    private Submodel retrieveSubmodel(BaSyxAas aas, String submodelId) throws IOException {
        Submodel result = null;
        try {
            SubmodelDescriptor desc = smRegistry.getSubmodelDescriptorById(submodelId);
            String epUrl = SubmodelRegistryUtils.getFirstEndpointUrl(desc);
            ConnectedSubmodelRepository repo = getSubmodelRepo(epUrl);
            result = new BaSyxSubmodel(aas, repo.getSubmodel(submodelId), repo);
        } catch (org.eclipse.digitaltwin.basyx.submodelregistry.client.ApiException e) {
            throw new IOException(e);
        }
        return result;
    }

    /**
     * Returns the (fallback) submodel repository for the given URL.
     * 
     * @param epUrl the repo URL
     * @return the repository or the fallback repository
     */
    private ConnectedSubmodelRepository getSubmodelRepo(String epUrl) {
         // we may cache those
        return StringUtils.isBlank(epUrl) ? smRepo 
            : SubmodelRepositoryUtils.createRepositoryApi(spec, toUriWithoutPath(epUrl)); 
    }
    
    /**
     * Cuts the path of the given URI.
     * 
     * @param uri the URI
     * @return the URI scheme, host, port without path
     */
    private String toUriWithoutPath(String uri) {
        if (null != uri) {
            URI tmp = URI.create(uri);
            uri = tmp.getScheme() + "://" + tmp.getHost();
            if (tmp.getPort() > 0) {
                uri += ":" + tmp.getPort();
            }
        }
        return uri;
    }
    
    @Override
    public void createAas(Aas aas, String endpointURL) throws IOException { // TODO endpointURL??
        if (!(aas instanceof BaSyxAas)) {
            throw new IllegalArgumentException("The aas must be created by the AasFactory.");
        }
        BaSyxAas a = (BaSyxAas) aas;
        a.registerRegistry(this);
        if (null == endpointURL || endpointURL.isEmpty()) {
            endpointURL = aasRepo.getBaseUrl() + "/" 
                + StringUtils.defaultIfBlank(aas.getIdentification(), aas.getIdShort());
        }
        AssetAdministrationShellDescriptor desc = AasRegistryUtils.createDescriptor(aas, endpointURL, true);
        boolean known;
        try {
            aasRepo.getAas(a.getIdentification());
            known = true;
        } catch (ElementDoesNotExistException e) {
            known = false;
        } catch (org.eclipse.digitaltwin.basyx.client.internal.ApiException e) {
            throw new IOException(e);
        }
        if (known) {
            aasRepo.updateAas(a.getIdentification(), a.getAas());
            a.registerRegistry(this);
        } else {
            aasRepo.createAas(a.getAas());
            a.registerRegistry(this);
            try {
                aasRegistry.postAssetAdministrationShellDescriptor(desc);
            } catch (ApiException e) {
                LoggerFactory.getLogger(getClass()).error("Cannot deploy AAS '{}': {}", 
                    aas.getIdShort(), e.getMessage());
            }
        }
    }

    @Override
    public void createSubmodel(Aas aas, Submodel submodel) throws IOException {
        if (!(aas instanceof BaSyxAas)) {
            throw new IllegalArgumentException("The aas must be created by the AasFactory.");
        }
        if (!(submodel instanceof BaSyxSubmodel)) {
            throw new IllegalArgumentException("The submodel must be created by the AasFactory.");
        }
        try {
            BaSyxSubmodel bSub = (BaSyxSubmodel) submodel;
            try {
                smRepo.createSubmodel(bSub.getSubmodel());
            } catch (CollidingIdentifierException e) { // already there, ignore
            }
            bSub.setRepo(smRepo);
            SubmodelRegistryUtils.postDescriptor(submodel, smRegistry, smRepo, false);
            
            BaSyxAas a = (BaSyxAas) aas;
            aasRepo.updateAas(a.getIdentification(), a.getAas());
        } catch (org.eclipse.digitaltwin.basyx.client.internal.ApiException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void register(Aas aas, Submodel submodel, String endpointUrl) {
        if (!(aas instanceof BaSyxAas)) {
            throw new IllegalArgumentException("The aas must be created by the AasFactory.");
        }
        AasRegistryUtils.postDescriptor(aas, aasRegistry, aasRepo, false);
        if (null != submodel) {
            if (!(submodel instanceof BaSyxSubmodel)) {
                throw new IllegalArgumentException("The submodel must be created by the AasFactory.");
            }
            ((BaSyxSubmodel) submodel).setRepo(smRepo);
            SubmodelRegistryUtils.postDescriptor(submodel, smRegistry, smRepo, false);
        }
    }
    
    @Override
    public String getEndpoint(String aasIdShort) {
        String result = null;
        Optional<String> epUrl = allAas()
            .filter(d -> d.getIdShort().equals(aasIdShort))
            .map(d -> AasRegistryUtils.getFirstEndpointUrl(d))
            .findFirst();
        if (epUrl.isPresent()) {
            result = epUrl.get();
        }
        return result;
    }

    @Override
    public String getEndpoint(Aas aas) {
        String result = null;
        if (aas instanceof AbstractAas) {
            AssetAdministrationShell a = ((AbstractAas<?>) aas).getAas();
            String id = StringUtils.defaultIfBlank(a.getId(), "");
            String idShort = StringUtils.defaultIfBlank(a.getIdShort(), "");
            Optional<String> epUrl = allAas()
                .filter(d -> (id.equals(d.getId()) || idShort.equals(d.getIdShort())))
                .map(d -> AasRegistryUtils.getFirstEndpointUrl(d))
                .filter(d -> d != null)
                .findFirst();
            if (epUrl.isPresent()) {
                result = epUrl.get();
            }
        }
        return result;
    }

    @Override
    public String getEndpoint(Aas aas, Submodel submodel) {
        String result = null;
        if (aas instanceof BaSyxAas && submodel instanceof BaSyxSubmodel) {
            org.eclipse.digitaltwin.aas4j.v3.model.Submodel sub = ((BaSyxSubmodel) submodel).getSubmodel();
            String sId = StringUtils.defaultIfBlank(sub.getId(), "");
            String sIdShort = StringUtils.defaultIfBlank(sub.getIdShort(), "");
            try {
                Optional<String> epUrl = smRegistry.getAllSubmodelDescriptors(PAGINATION_UNLIMITED, null)
                    .getResult().stream()
                    .filter(s -> (sId.equals(s.getId()) || sIdShort.equals(s.getIdShort())))
                    .map(s -> SubmodelRegistryUtils.getFirstEndpointUrl(s))
                    .filter(s -> s != null)
                    .findFirst();
                if (epUrl.isPresent()) {
                    result = epUrl.get();
                }
            } catch (org.eclipse.digitaltwin.basyx.submodelregistry.client.ApiException e) {
            }            
        }
        return result;
    }
    
    /**
     * Returns the internal AAS registry instance.
     * 
     * @return the internal AAS registry instance
     */
    RegistryAndDiscoveryInterfaceApi getAasRegistry() {
        return aasRegistry;
    }

    /**
     * Returns the internal submodel registry instance.
     * 
     * @return the internal submodel registry instance
     */
    SubmodelRegistryApi getSubmodelRegistry() {
        return smRegistry;
    }

    // TODO delete
    
}
