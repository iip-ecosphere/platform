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

import org.eclipse.basyx.submodel.metamodel.api.submodelelement.SubmodelElementIdShortBlacklist;
import org.eclipse.basyx.vab.protocol.api.IConnectorFactory;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnectorFactory;
import org.eclipse.basyx.vab.protocol.https.HTTPSConnectorProvider;

import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasFactoryDescriptor;
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.OperationsProvider;
import de.iip_ecosphere.platform.support.aas.PersistenceRecipe;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.SimpleLocalProtocolCreator;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;

/**
 * AAS factory for BaSyx. Do not rename, this class is referenced in {@code META-INF/services}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxAasFactory extends AasFactory {

    // public for testing, do not reference from outside
    public static final String PROTOCOL_VAB_TCP = "VAB-TCP";
    public static final String PROTOCOL_VAB_HTTP = "VAB-HTTP";
    public static final String PROTOCOL_VAB_HTTPS = "VAB-HTTPS";
    
    /**
     * Factory descriptor for Java Service Loader.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Descriptor implements AasFactoryDescriptor {

        @Override
        public AasFactory createInstance() {
            return new BaSyxAasFactory();
        }
        
    }
    
    /**
     * The VAB-TCP Protocol creator.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class VabTcpProtocolCreator implements ProtocolCreator {

        @Override
        public InvocablesCreator createInvocablesCreator(String host, int port, KeyStoreDescriptor kstore) {
            return new VabTcpInvocablesCreator(host, port); 
        }

        @Override
        public ProtocolServerBuilder createProtocolServerBuilder(int port, KeyStoreDescriptor kstore) {
            return new VabOperationsProvider.VabTcpOperationsBuilder(port);
        }
        
    }
    
    /**
     * The VAB-HTTP Protocol creator.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class VabHttpProtocolCreator implements ProtocolCreator {

        @Override
        public InvocablesCreator createInvocablesCreator(String host, int port, KeyStoreDescriptor kstore) {
            return new VabHttpInvocablesCreator("http://" + host + ":" + port);
        }

        @Override
        public ProtocolServerBuilder createProtocolServerBuilder(int port, KeyStoreDescriptor kstore) {
            return new VabOperationsProvider.VabHttpOperationsBuilder(port, Schema.HTTP, null);
        }
        
    }
    
    /**
     * The VAB-HTTPS Protocol creator.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class VabHttpsProtocolCreator implements ProtocolCreator {
        
        @Override
        public InvocablesCreator createInvocablesCreator(String host, int port, KeyStoreDescriptor kstore) {
            return new VabHttpsInvocablesCreator("https://" + host + ":" + port, kstore);
        }

        @Override
        public ProtocolServerBuilder createProtocolServerBuilder(int port, KeyStoreDescriptor kstore) {
            return new VabOperationsProvider.VabHttpOperationsBuilder(port, Schema.HTTPS, kstore);
        }
        
    }
    
    /**
     * Creates an instance.
     */
    public BaSyxAasFactory() {
        registerProtocolCreator(LOCAL_PROTOCOL, new SimpleLocalProtocolCreator() {
            
            @Override
            protected OperationsProvider createOperationsProvider() {
                return new VabOperationsProvider();
            }
            
        });
        
        VabTcpProtocolCreator tcp = new VabTcpProtocolCreator();
        registerProtocolCreator(DEFAULT_PROTOCOL, tcp);
        registerProtocolCreator(PROTOCOL_VAB_TCP, tcp);
        registerProtocolCreator(PROTOCOL_VAB_HTTP, new VabHttpProtocolCreator());
        registerProtocolCreator(PROTOCOL_VAB_HTTPS, new VabHttpsProtocolCreator());
    }
    
    @Override
    public AasBuilder createAasBuilder(String idShort, String identifier) {
        return new BaSyxAas.BaSyxAasBuilder(idShort, identifier);
    }

    @Override
    public SubmodelBuilder createSubmodelBuilder(String idShort, String identifier) {
        return new BaSyxSubmodel.BaSyxSubmodelBuilder(null, idShort, identifier);
    }

    @Override
    protected ServerRecipe createDefaultServerRecipe() {
        return new BaSyxServerRecipe();
    }
    
    @Override
    public Registry obtainRegistry(Endpoint endpoint) throws IOException {
        return obtainRegistry(endpoint, endpoint.getSchema());
    }
    
    @Override
    public Registry obtainRegistry(Endpoint endpoint, Schema aasSchema) throws IOException {
        IConnectorFactory cFactory;
        if (Schema.HTTPS == aasSchema) {
            cFactory = new HTTPSConnectorProvider();
        } else {
            cFactory = new HTTPConnectorFactory();
        }
        return new BaSyxRegistry(endpoint, cFactory);
    }
    
    @Override
    public String getFullRegistryUri(Endpoint regEndpoint) {
        return regEndpoint.toUri() + "/api/v1/registry";
    }

    @Override
    public DeploymentRecipe createDeploymentRecipe(Endpoint endpoint) {
        return new BaSyxDeploymentRecipe(endpoint);
    }
    
    @Override
    public DeploymentRecipe createDeploymentRecipe(Endpoint endpoint, KeyStoreDescriptor kstore) {
        return new BaSyxDeploymentRecipe(endpoint, kstore);
    }

    @Override
    public String getName() {
        return "AAS/BaSyx v1.3.0 (2022/12/15)";
    }

    @Override
    public PersistenceRecipe createPersistenceRecipe() {
        return new BaSyxPersistenceRecipe();
    }

    @Override
    protected boolean needsIdFix(String id) {
        // for now it's ok that it may mapply more global than just to submodel element
        return SubmodelElementIdShortBlacklist.isBlacklisted(id);
    }

}
