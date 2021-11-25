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
import java.io.IOException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.basyx.vab.protocol.api.IConnectorFactory;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnectorFactory;
import org.eclipse.basyx.vab.protocol.https.HTTPSConnectorProvider;
import org.slf4j.LoggerFactory;

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
import de.iip_ecosphere.platform.support.net.SslUtils;

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
        public InvocablesCreator createInvocablesCreator(String host, int port, File keyPath, String keyPass) {
            return new VabTcpInvocablesCreator(host, port); 
        }

        @Override
        public ProtocolServerBuilder createProtocolServerBuilder(int port, File keyPath, String keyPass) {
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
        public InvocablesCreator createInvocablesCreator(String host, int port, File keyPath, String keyPass) {
            return new VabHttpInvocablesCreator("http://" + host + ":" + port);
        }

        @Override
        public ProtocolServerBuilder createProtocolServerBuilder(int port, File keyPath, String keyPass) {
            return new VabOperationsProvider.VabHttpOperationsBuilder(port, Schema.HTTP, null, null);
        }
        
    }
    
    /**
     * The VAB-HTTPS Protocol creator.
     * 
     * @author Holger Eichelberger, SSE
     */
    @SuppressWarnings("unused")
    private static class VabHttpsProtocolCreator implements ProtocolCreator {

        private BaSyxJerseyHttpsClientFactory cFactory;
        
        @Override
        public InvocablesCreator createInvocablesCreator(String host, int port, File keyPath, String keyPass) {
            if (null == cFactory) {
                try {
                    KeyStore ks = SslUtils.openKeyStore(keyPath, keyPass);
                    TrustManagerFactory tmf = SslUtils.createTrustManagerFactory(ks);
                    KeyManager[] kms = SslUtils.createKeyManagers(ks, keyPass, "VAB");
                    cFactory = new BaSyxJerseyHttpsClientFactory("TLSv1",  new HostnameVerifier() {
        
                        @Override
                        public boolean verify(String hostname, SSLSession sslSession) {
                            return true;
                        }
                        
                    }, kms, new SecureRandom(), tmf.getTrustManagers());
                } catch (IOException e) {
                    LoggerFactory.getLogger(BaSyxAasFactory.class).error(
                        "Creating VAB-HTTPS client factory: " + e.getMessage());
                }
            }
            
            return new VabHttpsInvocablesCreator("https://" + host + ":" + port, cFactory);
        }

        @Override
        public ProtocolServerBuilder createProtocolServerBuilder(int port, File keyPath, String keyPass) {
            return new VabOperationsProvider.VabHttpOperationsBuilder(port, Schema.HTTPS, keyPath, keyPass);
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
        return obtainRegistry(endpoint, Schema.HTTP);
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
    public DeploymentRecipe createDeploymentRecipe(Endpoint endpoint) {
        return new BaSyxDeploymentRecipe(endpoint);
    }
    
    @Override
    public DeploymentRecipe createDeploymentRecipe(Endpoint endpoint, File keyPath, String keyPass) {
        return new BaSyxDeploymentRecipe(endpoint, keyPath, keyPass);
    }

    @Override
    public String getName() {
        return "AAS/BaSyx v1.0.1 (10/2021)";
    }

    @Override
    public PersistenceRecipe createPersistenceRecipe() {
        return new BaSyxPersistenceRecipe();
    }

}
