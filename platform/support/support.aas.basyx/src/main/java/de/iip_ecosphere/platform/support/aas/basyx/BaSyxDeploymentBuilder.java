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
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;

import org.apache.catalina.startup.Tomcat;
import org.eclipse.basyx.aas.metamodel.map.descriptor.AASDescriptor;
import org.eclipse.basyx.aas.metamodel.map.descriptor.SubmodelDescriptor;
import org.eclipse.basyx.aas.registration.api.IAASRegistryService;
import org.eclipse.basyx.aas.registration.memory.InMemoryRegistry;
import org.eclipse.basyx.aas.registration.restapi.DirectoryModelProvider;
import org.eclipse.basyx.aas.restapi.AASModelProvider;
import org.eclipse.basyx.aas.restapi.VABMultiSubmodelProvider;
import org.eclipse.basyx.submodel.restapi.SubModelProvider;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.modelprovider.generic.VABModelProvider;
import org.eclipse.basyx.vab.modelprovider.map.VABMapProvider;
import org.eclipse.basyx.vab.protocol.basyx.server.BaSyxTCPServer;
import org.eclipse.basyx.vab.protocol.http.server.AASHTTPServer;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;
import org.eclipse.basyx.vab.protocol.http.server.VABHTTPInterface;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasServer;
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe;
import de.iip_ecosphere.platform.support.aas.Submodel;

/**
 * An initial BaSyx-specific deployment builder.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxDeploymentBuilder implements DeploymentRecipe {

    private DeploymentSet deploymentSet = new DeploymentSet();
    
    /**
     * Creates a deployment builder with root context path and root/empty document base path.
     * 
     * @param host the target host
     * @param port the target IP port
     */
    BaSyxDeploymentBuilder(String host, int port) {
        this("", "", host, port);
    }

    /**
     * Creates a deployment builder with root/empty document base path.
     * 
     * @param contextPath the context base path (may be empty, otherwise shall start with a "/")
     * @param host the target host
     * @param port the target IP port
     */
    BaSyxDeploymentBuilder(String contextPath, String host, int port) {
        this(contextPath, "", host, port);
    }

    /**
     * Creates a deployment builder.
     * 
     * @param contextPath the context base path (may be empty, otherwise shall start with a "/")
     * @param docBasePath the documents base path (may be empty, otherwise shall start with a "/") 
     * @param host the target host
     * @param port the target IP port
     */
    BaSyxDeploymentBuilder(String contextPath, String docBasePath, String host, int port) {
        deploymentSet.host = host;
        deploymentSet.port = port;
        deploymentSet.context = new BaSyxContext(contextPath, docBasePath, host, port);
    }
    
    private static class DeploymentSet {
        private String host;
        private int port;
        private BaSyxContext context;
        private IAASRegistryService registry;
        private Map<String, BaSyxAasDescriptor> descriptors = new HashMap<>();
        
    }

    @Override
    public DeploymentRecipe addInMemoryRegistry(String regPath) {
        deploymentSet.registry = new InMemoryRegistry();
        IModelProvider registryProvider = new DirectoryModelProvider(deploymentSet.registry);
        HttpServlet registryServlet = new VABHTTPInterface<IModelProvider>(registryProvider);
        deploymentSet.context.addServletMapping("/" + regPath + "/*", registryServlet);
        return this;
    }

    @Override
    public DeploymentRecipe deploy(Aas aas) {
        deploy(deploymentSet, aas);
        return this;
    }
    
    /**
     * Creates a new BaSyx AAS server.
     * 
     * @param deploymentSet the deployment set instance
     * @param aas the AAS
     */
    static void deploy(DeploymentSet deploymentSet, Aas aas) {
        if (null == deploymentSet.registry) {
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

        AASDescriptor aasDescriptor = new AASDescriptor(bAas.getAas(), "http://" + deploymentSet.host + ":" 
            + deploymentSet.port + "/" + idToUrlPath(aas.getIdShort()) + "/aas");
        for (Submodel sm: bAas.submodels()) {
            if (sm instanceof BaSyxSubmodel) {
                BaSyxSubmodel submodel = (BaSyxSubmodel) sm;
                SubModelProvider subModelProvider = new SubModelProvider(submodel.getSubmodel());
                fullProvider.addSubmodel(submodel.getIdShort(), subModelProvider);
                aasDescriptor.addSubmodelDescriptor(new SubmodelDescriptor(submodel.getSubmodel(), "http://" 
                    + deploymentSet.host + ":" + deploymentSet.port + "/" + idToUrlPath(aas.getIdShort()) 
                    + "/aas/submodels/" + idToUrlPath(submodel.getIdShort()) + "/submodel"));
            } // connected sub-models are already deployed
        }
        
        HttpServlet aasServlet = new VABHTTPInterface<IModelProvider>(fullProvider);
        deploymentSet.registry.register(aasDescriptor);
        
        deploymentSet.context.addServletMapping("/" + idToUrlPath(aas.getIdShort()) + "/*", aasServlet);
        deploymentSet.descriptors.put(aas.getIdShort(), new BaSyxAasDescriptor(fullProvider, aasDescriptor));
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
    private static class BaSyxAasServer implements AasServer {

        private AASHTTPServer server;
        private DeploymentSet deploymentSet;
        
        /**
         * Creates a new BaSyx AAS server.
         * 
         * @param deploymentSet the deployment set instance for runtime deployments
         */
        BaSyxAasServer(DeploymentSet deploymentSet) {
            this.deploymentSet = deploymentSet;
            server = new AASHTTPServer(deploymentSet.context);
        }
        
        @Override
        public void start() {
            server.start();
        }

        @Override
        public void start(int minWaitingTime) {
            startServer(server, minWaitingTime);
        }

        @Override
        public void stop() {
            server.shutdown();
        }

        @Override
        public void deploy(Aas aas) throws IOException {
            BaSyxDeploymentBuilder.deploy(deploymentSet, aas);
        }
        
        @Override
        public void deploy(Aas aas, Submodel submodel) {
            if (!(submodel instanceof BaSyxSubmodel)) {
                throw new IllegalArgumentException("The subModel must be of instance BaSyxSubModel, i.e., created "
                    + "through the AasFactory.");
            }
            BaSyxAasDescriptor desc = deploymentSet.descriptors.get(aas.getIdShort());
            if (null == desc) {
                throw new IllegalArgumentException("The AAS " + aas.getIdShort() + " is unknown on this server "
                    + "instance.");
            }
            
            BaSyxSubmodel sm = (BaSyxSubmodel) submodel;
            
            SubModelProvider subModelProvider = new SubModelProvider(sm.getSubmodel());
            desc.fullProvider.addSubmodel(submodel.getIdShort(), subModelProvider);
            desc.aasDescriptor.addSubmodelDescriptor(new SubmodelDescriptor(sm.getSubmodel(), "http://" 
                + deploymentSet.host + ":" + deploymentSet.port + "/" + idToUrlPath(aas.getIdShort()) 
                + "/aas/submodels/" + idToUrlPath(submodel.getIdShort()) + "/submodel"));
        }

    }
    
    @Override
    public AasServer createServer() {
        return new BaSyxAasServer(deploymentSet);
    }
    
    /**
     * Starts and tries to wait for the server to come up. Unfortunately, no support for this here, just an
     * unblocking call.
     * 
     * @param httpServer the server instance
     * @param minWaitingTime the minimum waiting time
     * @return {@code server}
     */
    public static AASHTTPServer startServer(AASHTTPServer httpServer, int minWaitingTime) {
        httpServer.start();
        boolean fallbackWaiting = true;
        try {
            Field tomcatField = AASHTTPServer.class.getField("tomcat");
            Tomcat tomcat = (Tomcat) tomcatField.get(httpServer);
            tomcat.wait();
            fallbackWaiting = false;
        } catch (NoSuchFieldException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InterruptedException e) {
        }
        if (fallbackWaiting) {
            TimeUtils.sleep(3000); // the server does not tell us when it is ready
        }
        return httpServer;
    }
    
    /**
     * Turns an id into a URL path.
     * 
     * @param id the id
     * @return the URL path
     */
    private static String idToUrlPath(String id) {
        return id; // to allow for translations, whitespaces, whatever
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
            public void start() {
                server.start();
            }

            @Override
            public void start(int minWaitingTime) {
                server.start();
            }

            @Override
            public void stop() {
                server.stop();
            }
            
        };
        return result;
    }

}
