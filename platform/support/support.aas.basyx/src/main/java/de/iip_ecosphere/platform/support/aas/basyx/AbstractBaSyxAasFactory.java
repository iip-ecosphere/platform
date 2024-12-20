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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.basyx.submodel.metamodel.api.submodelelement.SubmodelElementIdShortBlacklist;
import org.eclipse.basyx.vab.protocol.api.IConnectorFactory;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnectorFactory;
import org.eclipse.basyx.vab.protocol.https.HTTPSConnectorProvider;

import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.FileFormat;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
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
public abstract class AbstractBaSyxAasFactory extends AasFactory {

    // public for testing, do not reference from outside
    public static final String PROTOCOL_VAB_TCP = "VAB-TCP";
    public static final String PROTOCOL_VAB_HTTP = "VAB-HTTP";
    public static final String PROTOCOL_VAB_HTTPS = "VAB-HTTPS";
    
    private final Map<FileFormat, PersistenceRecipe> recipes = new HashMap<>();
    
    /**
     * The VAB-TCP Protocol creator.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected static class VabTcpProtocolCreator implements ProtocolCreator {

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
    protected static class VabHttpProtocolCreator implements ProtocolCreator {

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
    protected static class VabHttpsProtocolCreator implements ProtocolCreator {
        
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
    public AbstractBaSyxAasFactory() {
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
        
        registerPersistenceRecipe(new XmlPersistenceRecipe());
        registerPersistenceRecipe(new JsonPersistenceRecipe());
    }
    
    /**
     * Registers a recipe.
     * 
     * @param recipe the recipe to be registered
     */
    protected void registerPersistenceRecipe(PersistenceRecipe recipe) {
        if (null != recipe) {
            for (FileFormat f : recipe.getSupportedFormats()) {
                recipes.put(f, recipe);
            }
        }
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
    public String getServerBaseUri(Endpoint serverEndpoint) {
        return serverEndpoint.toUri() + "/shells";
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
    public PersistenceRecipe createPersistenceRecipe() {
        return new BaSyxPersistenceRecipe();
    }

    /**
     * A generic, delegating persistence recipe based on those registered in 
     * {@link AbstractBaSyxAasFactory#registerPersistenceRecipe(PersistenceRecipe)}.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class BaSyxPersistenceRecipe implements PersistenceRecipe {
        
        /**
         * Finds a matching {@link PersistenceRecipe} against the known recipes and their file formats.
         * 
         * @param file the file to look for
         * @return the persistence recipe
         * @throws IllegalArgumentException if there is no matching file format/recipe
         */
        private PersistenceRecipe findMatching(File file) {
            PersistenceRecipe result = null;
            for (FileFormat ff : recipes.keySet()) {
                if (ff.matches(file)) {
                    result = recipes.get(ff);
                }
            }
            if (null == result) {
                throw new IllegalArgumentException("Unrecognized file format for " + file);
            }
            return result;
        }
        
        @Override
        public void writeTo(List<Aas> aas, File thumbnail, List<FileResource> resources, File file) throws IOException {
            findMatching(file).writeTo(aas, thumbnail, resources, file);
        }

        @Override
        public List<Aas> readFrom(File file) throws IOException {
            return findMatching(file).readFrom(file);
        }

        @Override
        public Collection<FileFormat> getSupportedFormats() {
            return Collections.unmodifiableCollection(recipes.keySet()); // this shall be unmodifiable
        }

    }
    
    @Override
    protected boolean needsIdFix(String id) {
        // for now it's ok that it may mapply more global than just to submodel element
        return SubmodelElementIdShortBlacklist.isBlacklisted(id);
    }


}
