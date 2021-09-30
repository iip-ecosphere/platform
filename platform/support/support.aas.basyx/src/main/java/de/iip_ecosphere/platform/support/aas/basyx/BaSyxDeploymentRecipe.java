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

import javax.servlet.http.HttpServlet;

import org.eclipse.basyx.aas.metamodel.map.descriptor.AASDescriptor;
import org.eclipse.basyx.aas.metamodel.map.descriptor.SubmodelDescriptor;
import org.eclipse.basyx.aas.registration.memory.InMemoryRegistry;
import org.eclipse.basyx.aas.registration.restapi.AASRegistryModelProvider;
import org.eclipse.basyx.aas.restapi.AASModelProvider;
import org.eclipse.basyx.aas.restapi.MultiSubmodelProvider;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.submodel.restapi.SubmodelProvider;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.protocol.http.server.VABHTTPInterface;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
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

    private DeploymentSpec deploymentSpec;

    /**
     * Creates a deployment builder with root/empty document base path.
     * 
     * @param endpoint the endpoint to create the deployment context for
     */
    BaSyxDeploymentRecipe(Endpoint endpoint) {
        deploymentSpec = new DeploymentSpec(endpoint);
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
        deploymentSpec = new DeploymentSpec(new Endpoint(Schema.IGNORE, host, port, contextPath), docBasePath);
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
        deploymentSpec.setRegistry(new InMemoryRegistry());
        IModelProvider registryProvider = new AASRegistryModelProvider(deploymentSpec.getRegistry());
        HttpServlet registryServlet = new VABHTTPInterface<IModelProvider>(registryProvider);
        deploymentSpec.getContext().addServletMapping(Endpoint.checkEndpoint(regEndpoint) + "/*", registryServlet);
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
            return new BaSyxRegistryDeploymentAasServer(deploymentSpec, endpoint.toUri(), 
                AASServerBackend.INMEMORY, options);
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
        if (null == deploymentSpec.getRegistry()) {
            throw new IllegalArgumentException("No registry created before");
        }
        if (!(aas instanceof BaSyxAas)) {
            throw new IllegalArgumentException("The aas must be of instance BaSyxAas, i.e., created "
                + "through the AasFactory.");
        }
        BaSyxAas bAas = (BaSyxAas) aas;
        //Wrapping Submodels in IModelProvider
        AASModelProvider aasProvider = new AASModelProvider(bAas.getAas());
        MultiSubmodelProvider fullProvider = new MultiSubmodelProvider();
        fullProvider.setAssetAdministrationShell(aasProvider);

        AASDescriptor aasDescriptor = new AASDescriptor(bAas.getAas(), 
            AbstractAas.getAasEndpoint(deploymentSpec.getEndpoint(), aas));
        for (Submodel sm: bAas.submodels()) {
            if (sm instanceof BaSyxSubmodel) {
                BaSyxSubmodel submodel = (BaSyxSubmodel) sm;
                SubmodelProvider subModelProvider = new SubmodelProvider(submodel.getSubmodel());
                fullProvider.addSubmodel(subModelProvider);
                aasDescriptor.addSubmodelDescriptor(new SubmodelDescriptor(submodel.getSubmodel(), 
                    AbstractSubmodel.getSubmodelEndpoint(deploymentSpec.getEndpoint(), aas, submodel)));
            } // connected sub-models are already deployed
        }
        
        HttpServlet aasServlet = new VABHTTPInterface<IModelProvider>(fullProvider);
        deploymentSpec.getRegistry().register(aasDescriptor);
        
        String ep = deploymentSpec.getEndpoint().getEndpoint();
        while (ep.length() > 0 && ep.endsWith("/")) {
            ep = ep.substring(0, ep.length() - 1);
        }
        deploymentSpec.getContext().addServletMapping(ep + "/" + Tools.idToUrlPath(aas.getIdShort()) + "/*", 
            aasServlet);
        deploymentSpec.putDescriptor(aas.getIdShort(), new BaSyxAasDescriptor(fullProvider, aasDescriptor));
    }

}
