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
import java.util.List;
import java.util.function.Function;

import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.map.descriptor.AASDescriptor;
import org.eclipse.basyx.aas.metamodel.map.descriptor.SubmodelDescriptor;
import org.eclipse.basyx.aas.registration.api.IAASRegistry;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.vab.exception.provider.ProviderException;
import org.eclipse.basyx.vab.exception.provider.ResourceNotFoundException;
import org.eclipse.basyx.vab.protocol.api.IConnectorFactory;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.Submodel;

/**
 * Implements a registry recipe for BaSyx.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxRegistry implements Registry {

    private Endpoint endpoint;
    private IAASRegistry registry;
    private ConnectedAssetAdministrationShellManager manager;

    // checkstyle: stop exception type check

    /**
     * Creates a registry recipe.
     * 
     * @param endpoint the registry endpoint
     * @param connectorFactory connector factory, e.g., HTTP, HTTPS
     * @throws IOException if connecting the registry fails
     */
    BaSyxRegistry(Endpoint endpoint, IConnectorFactory connectorFactory) throws IOException {
        this.endpoint = endpoint;
        try {
            registry = new AASRegistryProxy(this.endpoint.toUri());
            manager = new ConnectedAssetAdministrationShellManager(
                registry, connectorFactory);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    // checkstyle: resume exception type check

    @Override
    public Aas retrieveAas(String identifier) throws IOException {
        return retrieveAas(identifier, true);
    }
    
    @Override
    public Aas retrieveAas(String identifier, boolean populate) throws IOException {
        Aas result = null;
        IIdentifier id = null;
        if (null != identifier) {
            if ((identifier.startsWith(Schema.HTTP.toUri()) || identifier.startsWith(Schema.HTTPS.toUri()))) {
                List<AASDescriptor> descs = registry.lookupAll();
                for (int d = 0; null == id && d < descs.size(); d++) {
                    if (identifier.equals(descs.get(d).getFirstEndpoint())) { // map is unclear
                        id = descs.get(d).getIdentifier();
                    }
                }
            } else {
                id = Tools.translateIdentifier(identifier, "");
            }
            if (null != id) {
                result = obtainAas(id, populate);
            }
        }
        return result;
    }

    @Override
    public List<String> getAasIdShorts() {
        return getStrings(d -> d.getIdShort());
    }

    @Override
    public List<String> getAasIdentifiers() {
        return getStrings(d -> Tools.translateIdentifier(d.getIdentifier()));
    }

    /**
     * Retrieves strings from registered AAS descriptors.
     * 
     * @param func returns the output for a given descriptor, skip descriptor if result is <b>null</b>
     * @return the strings
     */
    private List<String> getStrings(Function<AASDescriptor, String> func) {
        List<String> result = new ArrayList<String>();
        try {
            List<AASDescriptor> desc = registry.lookupAll();
            for (AASDescriptor d : desc) {
                String tmp = func.apply(d);
                if (tmp != null) {
                    result.add(tmp);
                }
            }
        } catch (ProviderException e) {
            LoggerFactory.getLogger(getClass()).error("Cannot obtain AAS descriptor: " + e.getMessage());
        }
        return result;
    }
    
    /**
     * Obtains an AAS for a given identifier.
     * 
     * @param aasId the AAS identifier
     * @param populate the submodels with elements (performance!)
     * @return the AAS (may be <b>null</b> in case that the AAS cannot be obtained)
     * @throws IOException in case that the access fails
     */
    private BaSyxConnectedAas obtainAas(IIdentifier aasId, boolean populate) throws IOException {
        try {
            return new BaSyxConnectedAas(manager.retrieveAAS(aasId), populate);
        } catch (ProviderException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Submodel retrieveSubmodel(String aasIdentifier, String submodelIdentifier) throws IOException {
        try {
            IIdentifier aasId = Tools.translateIdentifier(aasIdentifier, "");
            IIdentifier submodelId = Tools.translateIdentifier(submodelIdentifier, "");
            return new BaSyxISubmodel(obtainAas(aasId, true), manager.retrieveSubmodel(aasId, submodelId), true);
        } catch (ProviderException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void createAas(Aas aas, String endpointURL) {
        if (!(aas instanceof BaSyxAas)) {
            throw new IllegalArgumentException("The aas must be created by the AasFactory.");
        }
        BaSyxAas a = (BaSyxAas) aas;
        a.registerRegistry(this);
        try {
            manager.createAAS(a.getAas(), endpointURL);
        } catch (ProviderException e) {
            LoggerFactory.getLogger(getClass()).error("Cannot create AAS: " + e.getMessage());
        }
    }

    @Override
    public void createSubmodel(Aas aas, Submodel submodel) {
        if (!(aas instanceof BaSyxAas)) {
            throw new IllegalArgumentException("The aas must be created by the AasFactory.");
        }
        if (!(submodel instanceof BaSyxSubmodel)) {
            throw new IllegalArgumentException("The submodel must be created by the AasFactory.");
        }
        IIdentifier aasIdentifier = ((BaSyxAas) aas).getAas().getIdentification();
        try {
            manager.createSubmodel(aasIdentifier, ((BaSyxSubmodel) submodel).getSubmodel());
        } catch (ProviderException e) {
            LoggerFactory.getLogger(getClass()).error("Cannot create submodel: " + e.getMessage());
        }
    }

    @Override
    public void register(Aas aas, Submodel submodel, String endpointUrl) {
        if (!(aas instanceof BaSyxAas)) {
            throw new IllegalArgumentException("The aas must be created by the AasFactory.");
        }
        if (!(submodel instanceof BaSyxSubmodel)) {
            throw new IllegalArgumentException("The submodel must be created by the AasFactory.");
        }
        
        if (null == endpointUrl) {
            endpointUrl = AbstractSubmodel.getSubmodelEndpoint(endpoint, aas, submodel);
        }
        IIdentifier aasIdentifier = ((BaSyxAas) aas).getAas().getIdentification();
        try {
            registry.register(aasIdentifier, new SubmodelDescriptor(submodel.getIdShort(), 
                ((BaSyxSubmodel) submodel).getSubmodel().getIdentification(), endpointUrl));
        } catch (ProviderException e) {
            LoggerFactory.getLogger(getClass()).error("Cannot register submodel: " + e.getMessage());
        }
    }
    
    @Override
    public String getEndpoint(String aasIdShort) {
        String result = null;
        try {
            for (AASDescriptor desc : registry.lookupAll()) {
                if (desc.getIdShort().equals(aasIdShort)) {
                    result = desc.getFirstEndpoint();
                }
            }
        } catch (ResourceNotFoundException e) {
            // for now
        }
        return result;
    }

    @Override
    public String getEndpoint(Aas aas) {
        String result = null;
        if (aas instanceof AbstractAas) {
            try {
                AASDescriptor desc = registry.lookupAAS(((AbstractAas<?>) aas).getAas().getIdentification());
                result = desc.getFirstEndpoint();
            } catch (ProviderException e) {
            }
        }
        return result;
    }

    @Override
    public String getEndpoint(Aas aas, Submodel submodel) {
        String result = null;
        if (aas instanceof BaSyxAas && submodel instanceof BaSyxSubmodel) {
            try {
                SubmodelDescriptor desc = registry.lookupSubmodel(
                    ((BaSyxAas) aas).getAas().getIdentification(), 
                    ((BaSyxSubmodel) submodel).getSubmodel().getIdentification());
                result = desc.getFirstEndpoint();
            } catch (ProviderException e) {
            }
        }
        return result;
    }
    
    /**
     * Returns the internal registry instance.
     * 
     * @return the internal registry instance
     */
    IAASRegistry getRegistry() {
        return registry;
    }

    // TODO delete
    
}
