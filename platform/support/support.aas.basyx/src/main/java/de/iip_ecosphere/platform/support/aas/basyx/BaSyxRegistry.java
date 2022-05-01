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

import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.map.descriptor.AASDescriptor;
import org.eclipse.basyx.aas.metamodel.map.descriptor.SubmodelDescriptor;
import org.eclipse.basyx.aas.registration.api.IAASRegistry;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.vab.exception.provider.ProviderException;
import org.eclipse.basyx.vab.protocol.api.IConnectorFactory;

import de.iip_ecosphere.platform.support.Endpoint;
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
        return obtainAas(Tools.translateIdentifier(identifier, ""));
    }
    
    /**
     * Obtains an AAS for a given identifier.
     * 
     * @param aasId the AAS identifier
     * @return the AAS (may be <b>null</b> in case that the AAS cannot be obtained)
     * @throws IOException in case that the access fails
     */
    private BaSyxConnectedAas obtainAas(IIdentifier aasId) throws IOException {
        return new BaSyxConnectedAas(manager.retrieveAAS(aasId));
    }

    @Override
    public Submodel retrieveSubmodel(String aasIdentifier, String submodelIdentifier) throws IOException {
        IIdentifier aasId = Tools.translateIdentifier(aasIdentifier, "");
        IIdentifier submodelId = Tools.translateIdentifier(submodelIdentifier, "");
        return new BaSyxISubmodel(obtainAas(aasId), manager.retrieveSubmodel(aasId, submodelId));
    }

    @Override
    public void createAas(Aas aas, String endpointURL) {
        if (!(aas instanceof BaSyxAas)) {
            throw new IllegalArgumentException("The aas must be created by the AasFactory.");
        }
        BaSyxAas a = (BaSyxAas) aas;
        a.registerRegistry(this);
        manager.createAAS(a.getAas(), endpointURL);
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
        manager.createSubmodel(aasIdentifier, ((BaSyxSubmodel) submodel).getSubmodel());
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
        registry.register(aasIdentifier, new SubmodelDescriptor(submodel.getIdShort(), 
            ((BaSyxSubmodel) submodel).getSubmodel().getIdentification(), endpointUrl));
    }
    
    @Override
    public String getEndpoint(String aasIdShort) {
        String result = null;
        for (AASDescriptor desc : registry.lookupAll()) {
            if (desc.getIdShort().equals(aasIdShort)) {
                result = desc.getFirstEndpoint();
            }
        }
        return result;
    }

    @Override
    public String getEndpoint(Aas aas) {
        String result = null;
        if (aas instanceof BaSyxAas) {
            try {
                AASDescriptor desc = registry.lookupAAS(((BaSyxAas) aas).getAas().getIdentification());
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

    // TODO delete
    
}
