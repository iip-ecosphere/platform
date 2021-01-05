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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;

import org.eclipse.basyx.aas.metamodel.map.descriptor.AASDescriptor;
import org.eclipse.basyx.aas.metamodel.map.descriptor.SubmodelDescriptor;
import org.eclipse.basyx.aas.registration.api.IAASRegistryService;
import org.eclipse.basyx.aas.registration.memory.InMemoryRegistry;
import org.eclipse.basyx.aas.registration.restapi.DirectoryModelProvider;
import org.eclipse.basyx.aas.restapi.AASModelProvider;
import org.eclipse.basyx.aas.restapi.VABMultiSubmodelProvider;
import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.basyx.submodel.restapi.SubModelProvider;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.modelprovider.generic.VABModelProvider;
import org.eclipse.basyx.vab.modelprovider.map.VABMapProvider;
import org.eclipse.basyx.vab.protocol.basyx.server.BaSyxTCPServer;
import org.eclipse.basyx.vab.protocol.http.server.AASHTTPServer;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;
import org.eclipse.basyx.vab.protocol.http.server.VABHTTPInterface;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasServer;
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.Submodel;

/**
 * An initial BaSyx-specific deployment builder.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxDeploymentRecipe implements DeploymentRecipe {

    private DeploymentSpec deploymentSpec = new DeploymentSpec();

    /**
     * Creates a deployment builder with root/empty document base path.
     * 
     * @param endpoint the endpoint to create the deployment context for
     */
    BaSyxDeploymentRecipe(Endpoint endpoint) {
        deploymentSpec.endpoint = endpoint;
        deploymentSpec.context = new BaSyxContext(endpoint.getEndpoint(), "", endpoint.getHost(), endpoint.getPort());
        deploymentSpec.contextConfig = new BaSyxContextConfiguration(
            endpoint.getEndpoint(), "", endpoint.getHost(), endpoint.getPort()) {
            
            @Override
            public BaSyxContext createBaSyxContext() {
                return deploymentSpec.context;
            }
            
        };
    }

    /**
     * Creates a deployment builder.
     * 
     * @param host the target host
     * @param port the target IP port
     * @param contextPath the context base path (may be empty, otherwise shall start with a "/")
     * @param docBasePath the documents base path (may be empty, otherwise shall start with a "/") 
     */
    BaSyxDeploymentRecipe(String host, int port, String contextPath, String docBasePath) {
    }
    
    /**
     * Stores basic common deployment information.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class DeploymentSpec {
        private Endpoint endpoint;
        private BaSyxContextConfiguration contextConfig;
        private BaSyxContext context;
        private IAASRegistryService registry;
        private Map<String, BaSyxAasDescriptor> descriptors = new HashMap<>();
        
    }

    /**
     * Implements the immediate deployment recipe.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class BaSyxImmediateDeploymentRecipe implements ImmediateDeploymentRecipe {

        @Override
        public ImmediateDeploymentRecipe deploy(Aas aas) {
            BaSyxDeploymentRecipe.deploy(deploymentSpec, aas);
            return this;
        }

        @Override
        public AasServer createServer(String... options) {
            return new BaSyxImmediateDeploymentAasServer(deploymentSpec);
        }
        
    }

    @Override
    public ImmediateDeploymentRecipe addInMemoryRegistry(String regEndpoint) {
        deploymentSpec.registry = new InMemoryRegistry();
        IModelProvider registryProvider = new DirectoryModelProvider(deploymentSpec.registry);
        HttpServlet registryServlet = new VABHTTPInterface<IModelProvider>(registryProvider);
        deploymentSpec.context.addServletMapping(Endpoint.checkEndpoint(regEndpoint) + "/*", registryServlet);
        return new BaSyxImmediateDeploymentRecipe();
    }

    /**
     * Implements the registry deployment recipe.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class BaSyxRegistryDeploymentRecipe implements RegistryDeploymentRecipe {

        private Endpoint endpoint;
        
        /**
         * Creates an instance.
         * 
         * @param endpoint the registry endpoint
         */
        private BaSyxRegistryDeploymentRecipe(Endpoint endpoint) {
            this.endpoint = endpoint;
        }
        
        @Override
        public Registry obtainRegistry() throws IOException {
            return AasFactory.getInstance().obtainRegistry(endpoint);
        }

        @Override
        public AasServer createServer(String... options) {
            return new BaSyxRegistryDeploymentAasServer(deploymentSpec, 
                endpoint.toUri(), options);
        }
        
    }
 
    @Override
    public RegistryDeploymentRecipe setRegistryUrl(Endpoint endpoint) {
        return new BaSyxRegistryDeploymentRecipe(endpoint);
    }
    
    /**
     * Creates a new BaSyx AAS server.
     * 
     * @param deploymentSpec the deployment set instance
     * @param aas the AAS
     */
    static void deploy(DeploymentSpec deploymentSpec, Aas aas) {
        if (null == deploymentSpec.registry) {
            throw new IllegalArgumentException("No registry created before");
        }
        if (!(aas instanceof BaSyxAas)) {
            throw new IllegalArgumentException("The aas must be of instance BaSyxAas, i.e., created "
                + "through the AasFactory.");
        }
        BaSyxAas bAas = (BaSyxAas) aas;
        //Wrapping Submodels in IModelProvider
        AASModelProvider aasProvider = new AASModelProvider(bAas.getAas());
        VABMultiSubmodelProvider fullProvider = new VABMultiSubmodelProvider();
        fullProvider.setAssetAdministrationShell(aasProvider);

        AASDescriptor aasDescriptor = new AASDescriptor(bAas.getAas(), 
            AbstractAas.getAasEndpoint(deploymentSpec.endpoint, aas));
        for (Submodel sm: bAas.submodels()) {
            if (sm instanceof BaSyxSubmodel) {
                BaSyxSubmodel submodel = (BaSyxSubmodel) sm;
                SubModelProvider subModelProvider = new SubModelProvider(submodel.getSubmodel());
                fullProvider.addSubmodel(subModelProvider);
                aasDescriptor.addSubmodelDescriptor(new SubmodelDescriptor(submodel.getSubmodel(), 
                    AbstractSubmodel.getSubmodelEndpoint(deploymentSpec.endpoint, aas, submodel)));
            } // connected sub-models are already deployed
        }
        
        HttpServlet aasServlet = new VABHTTPInterface<IModelProvider>(fullProvider);
        deploymentSpec.registry.register(aasDescriptor);
        
        deploymentSpec.context.addServletMapping("/" + Tools.idToUrlPath(aas.getIdShort()) + "/*", aasServlet);
        deploymentSpec.descriptors.put(aas.getIdShort(), new BaSyxAasDescriptor(fullProvider, aasDescriptor));
    }
    
    /**
     * An internal AAS deployment descriptor.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class BaSyxAasDescriptor {
        private AASDescriptor aasDescriptor;
        private VABMultiSubmodelProvider fullProvider;
        
        /**
         * Creates an instance.
         * 
         * @param fullProvider the sub-model provider
         * @param aasDescriptor the AAS descriptor
         */
        private BaSyxAasDescriptor(VABMultiSubmodelProvider fullProvider, AASDescriptor aasDescriptor) {
            this.fullProvider = fullProvider;
            this.aasDescriptor = aasDescriptor;
        }
    }
    
    /**
     * Implements the {@link AasServer} instance.
     * 
     * @author Holger Eichelberger, SSE
     */
    private abstract static class BaSyxAbstractAasServer implements AasServer {

        private DeploymentSpec deploymentSpec;
        
        /**
         * Creates a new BaSyx AAS server.
         * 
         * @param deploymentSet the deployment set instance for runtime deployments
         */
        BaSyxAbstractAasServer(DeploymentSpec deploymentSet) {
            this.deploymentSpec = deploymentSet;
        }
        
        @Override
        public void deploy(Aas aas) throws IOException {
            BaSyxDeploymentRecipe.deploy(deploymentSpec, aas);
        }
        
        @Override
        public void deploy(Aas aas, Submodel submodel) {
            if (!(submodel instanceof BaSyxSubmodel)) {
                throw new IllegalArgumentException("The subModel must be of instance BaSyxSubModel, i.e., created "
                    + "through the AasFactory.");
            }
            BaSyxAasDescriptor desc = deploymentSpec.descriptors.get(aas.getIdShort());
            if (null == desc) {
                throw new IllegalArgumentException("The AAS " + aas.getIdShort() + " is unknown on this server "
                    + "instance.");
            }
            
            BaSyxSubmodel sm = (BaSyxSubmodel) submodel;
            SubModelProvider subModelProvider = new SubModelProvider(sm.getSubmodel());
            desc.fullProvider.addSubmodel(subModelProvider);
            desc.aasDescriptor.addSubmodelDescriptor(new SubmodelDescriptor(sm.getSubmodel(), 
                AbstractSubmodel.getSubmodelEndpoint(deploymentSpec.endpoint, aas, submodel)));
        }
        
        @Override
        public void stop(boolean dispose) {
            if (dispose) {
                Tools.disposeTomcatWorkingDir(null, deploymentSpec.endpoint.getPort());
            }
        }

    }

    /**
     * Implements the {@link AasServer} instance.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class BaSyxImmediateDeploymentAasServer extends BaSyxAbstractAasServer {

        private AASHTTPServer server;
        
        /**
         * Creates a new BaSyx AAS server.
         * 
         * @param deploymentSet the deployment set instance for runtime deployments
         */
        BaSyxImmediateDeploymentAasServer(DeploymentSpec deploymentSet) {
            super(deploymentSet);
            server = new AASHTTPServer(deploymentSet.context);
        }
        
        @Override
        public AasServer start() {
            server.start();
            return this;
        }

        @Override
        public void stop(boolean dispose) {
            server.shutdown();
            super.stop(dispose); // if not disposable, schedule for deletion at JVM end
        }

    }

    /**
     * Implements the {@link AasServer} instance.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class BaSyxRegistryDeploymentAasServer extends BaSyxAbstractAasServer {

        private AASServerComponent server; 
        
        /**
         * Creates a new BaSyx AAS server.
         * 
         * @param deploymentSet the deployment set instance for runtime deployments
         * @param regUrl the registryUR
         * @param options for server creation
         */
        BaSyxRegistryDeploymentAasServer(DeploymentSpec deploymentSet, String regUrl, String... options) {
            super(deploymentSet);
            AASServerBackend backend = Tools.getOption(options, AASServerBackend.INMEMORY, AASServerBackend.class);
            server = new AASServerComponent(deploymentSet.contextConfig, new BaSyxAASServerConfiguration(
                backend, "", regUrl)); // may require source via options
        }
        
        @Override
        public AasServer start() {
            server.startComponent();
            return this;
        }

        @Override
        public void stop(boolean dispose) {
            server.stopComponent();
            super.stop(dispose); // if not disposable, schedule for deletion at JVM end
        }

    }
    
    /** 
     * This method creates a control component.
     * 
     * @param cc the control component (usually hash-based model provider)
     * @param port the port to run on
     * @return the server instance
     * @see #createControlComponent(VABModelProvider, int)
     */
    public static Server createControlComponent(HashMap<String, Object> cc, int port) {
        // Server where the control component is reachable.
        return createControlComponent(new VABMapProvider(cc), port);
    }

    /** 
     * This method creates a control component for a model provider.
     * 
     * @param provider the model provider
     * @param port the port to run on
     * @return the server instance
     */
    public static Server createControlComponent(VABModelProvider provider, int port) {
        // Server where the control component is reachable.
        BaSyxTCPServer<VABModelProvider> server = new BaSyxTCPServer<>(provider, port);
        Server result = new Server() {

            @Override
            public Server start() {
                server.start();
                return this;
            }

            @Override
            public void stop(boolean dispose) {
                server.stop();
            }

        };
        return result;
    }

}
