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

import java.lang.reflect.Field;

import javax.servlet.http.HttpServlet;

import org.apache.catalina.startup.Tomcat;
import org.eclipse.basyx.aas.metamodel.map.descriptor.AASDescriptor;
import org.eclipse.basyx.aas.metamodel.map.descriptor.SubmodelDescriptor;
import org.eclipse.basyx.aas.registration.api.IAASRegistryService;
import org.eclipse.basyx.aas.registration.memory.InMemoryRegistry;
import org.eclipse.basyx.aas.registration.restapi.DirectoryModelProvider;
import org.eclipse.basyx.aas.restapi.AASModelProvider;
import org.eclipse.basyx.aas.restapi.VABMultiSubmodelProvider;
import org.eclipse.basyx.components.servlet.submodel.SubmodelServlet;
import org.eclipse.basyx.models.controlcomponent.ControlComponent;
import org.eclipse.basyx.submodel.restapi.SubModelProvider;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.modelprovider.map.VABMapProvider;
import org.eclipse.basyx.vab.protocol.basyx.server.BaSyxTCPServer;
import org.eclipse.basyx.vab.protocol.http.server.AASHTTPServer;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;
import org.eclipse.basyx.vab.protocol.http.server.VABHTTPInterface;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.DeploymentBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel;

/**
 * An initial BaSyx-specific deployment builder.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxDeploymentBuilder implements DeploymentBuilder {

    private String host;
    private int port;
    private BaSyxContext context;
    private IAASRegistryService registry;
    
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
        this.host = host;
        this.port = port;
        context = new BaSyxContext(contextPath, docBasePath, host, port);
    }

    @Override
    public DeploymentBuilder addInMemoryRegistry(String regPath) {
        registry = new InMemoryRegistry();
        IModelProvider registryProvider = new DirectoryModelProvider(registry);
        HttpServlet registryServlet = new VABHTTPInterface<IModelProvider>(registryProvider);
        context.addServletMapping("/" + regPath + "/*", registryServlet);
        return this;
    }

    @Override
    public DeploymentBuilder deploy(Aas aas) {
        if (null == registry) {
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

        AASDescriptor aasDescriptor = new AASDescriptor(bAas.getAas(), "http://" + host + ":" 
            + port + "/" + idToUrlPath(aas.getIdShort()) + "/aas");
        for (Submodel sm: bAas.submodels()) {
            if (sm instanceof BaSyxSubmodel) {
                BaSyxSubmodel submodel = (BaSyxSubmodel) sm;
                SubModelProvider subModelProvider = new SubModelProvider(submodel.getSubmodel());
                fullProvider.addSubmodel(submodel.getIdShort(), subModelProvider);
                aasDescriptor.addSubmodelDescriptor(new SubmodelDescriptor(submodel.getSubmodel(), "http://" + host 
                    + ":" + port + "/" + idToUrlPath(aas.getIdShort()) + "/aas/submodels/" 
                    + idToUrlPath(submodel.getIdShort()) + "/submodel"));
            } // connected sub-models are already deployed
        }
        
        HttpServlet aasServlet = new VABHTTPInterface<IModelProvider>(fullProvider);
        registry.register(aasDescriptor);
        
        context.addServletMapping("/" + idToUrlPath(aas.getIdShort()) + "/*", aasServlet);
        return this;
    }
    
    @Override
    public DeploymentBuilder deploy(Submodel subModel, String path) {
        if (!(subModel instanceof BaSyxSubmodel)) {
            throw new IllegalArgumentException("The subModel must be of instance BaSyxSubModel, i.e., created "
                + "through the AasFactory.");
        }
        SubmodelServlet smServlet = new SubmodelServlet(((BaSyxSubmodel) subModel).getSubmodel());
        context.addServletMapping(path + "/*", smServlet);
        return this;
    }
    
    @Override
    public Server createServer(int minWaitingTime) {
        AASHTTPServer server = new AASHTTPServer(context);
        Server result = new Server() {

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
            
        };
        return result;
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
     * This method creates a control component for the {@link TestMachine}.
     * 
     * @param cc the control component
     * @param port the port to run on
     * @return the server instance
     */
    public static Server createControlComponent(ControlComponent cc, int port) {
        // Server where the control component is reachable.
        VABMapProvider ccProvider = new VABMapProvider(cc);
        BaSyxTCPServer<VABMapProvider> server = new BaSyxTCPServer<>(ccProvider, port);
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
