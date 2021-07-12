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
import org.eclipse.basyx.aas.metamodel.map.descriptor.ModelUrn;
import org.eclipse.basyx.aas.metamodel.map.descriptor.SubmodelDescriptor;
import org.eclipse.basyx.aas.registration.api.IAASRegistryService;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnectorProvider;

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
    private IAASRegistryService registry;
    private ConnectedAssetAdministrationShellManager manager;

    // checkstyle: stop exception type check

    /**
     * Creates a registry recipe.
     * 
     * @param endpoint the registry endpoint
     * @throws IOException if connecting the registry fails
     */
    BaSyxRegistry(Endpoint endpoint) throws IOException {
        this.endpoint = endpoint;
        try {
            registry = new AASRegistryProxy(this.endpoint.toUri());
            HTTPConnectorProvider connectorProvider = new HTTPConnectorProvider(); // TODO HTTPS?
            manager = new ConnectedAssetAdministrationShellManager(
                registry, connectorProvider);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    // checkstyle: resume exception type check

    @Override
    public Aas retrieveAas(String aasUrn) throws IOException {
        return obtainAas(new ModelUrn(aasUrn));
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
    public Submodel retrieveSubmodel(String aasUrn, String submodelUrn) throws IOException {
        ModelUrn aasURN = new ModelUrn(aasUrn);
        return new BaSyxISubmodel(obtainAas(aasURN), manager.retrieveSubModel(aasURN, new ModelUrn(submodelUrn)));
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
        manager.createSubModel(aasIdentifier, ((BaSyxSubmodel) submodel).getSubmodel());
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

    // TODO delete
    
}
