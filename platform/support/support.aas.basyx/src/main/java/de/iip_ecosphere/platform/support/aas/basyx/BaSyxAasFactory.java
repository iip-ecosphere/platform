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

/**
 * AAS factory for BaSyx. Do not rename, this class is referenced in {@code META-INF/services}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxAasFactory extends AasFactory {

    // package local, do not reference from outside
    static final String PROTOCOL_VAB_TCP = "VAB-TCP";
    static final String PROTOCOL_VAB_HTTP = "VAB-HTTP";
    static final String PROTOCOL_VAB_HTTPS = "VAB-HTTPS";
    
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
        public InvocablesCreator createInvocablesCreator(String host, int port) {
            return new VabTcpInvocablesCreator(host, port); 
        }

        @Override
        public ProtocolServerBuilder createProtocolServerBuilder(int port) {
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
        public InvocablesCreator createInvocablesCreator(String host, int port) {
            return new VabHttpInvocablesCreator("http://" + host + ":" + port);
        }

        @Override
        public ProtocolServerBuilder createProtocolServerBuilder(int port) {
            return new VabOperationsProvider.VabHttpOperationsBuilder(port, Schema.HTTP);
        }
        
    }
    
    /**
     * The VAB-HTTPS Protocol creator.
     * 
     * @author Holger Eichelberger, SSE
     */
    /*private static class VabHttpsProtocolCreator implements ProtocolCreator {

        @Override
        public InvocablesCreator createInvocablesCreator(String host, int port) {
            return new VabHttpsInvocablesCreator(new BaSyxHTTPSConnector("https://" + host + ":" + port, 
                new BaSyxJerseyHttpsClientFactory())); // TODO for now with self-signed
        }

        @Override
        public ProtocolServerBuilder createProtocolServerBuilder(int port) {
            return new VabOperationsProvider.VabHttpOperationsBuilder(port, Schema.HTTPS);
        }
        
    }*/
    
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
        //registerProtocolCreator(PROTOCOL_VAB_HTTPS, new VabHttpsProtocolCreator());
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
        return new BaSyxRegistry(endpoint);
    }

    @Override
    public DeploymentRecipe createDeploymentRecipe(Endpoint endpoint) {
        return new BaSyxDeploymentRecipe(endpoint);
    }

    @Override
    public String getName() {
        return "AAS/BaSyx v0.1.0-SNAPSHOT (Dec'20)";
    }

    @Override
    public PersistenceRecipe createPersistenceRecipe() {
        return new BaSyxPersistenceRecipe();
    }

}
