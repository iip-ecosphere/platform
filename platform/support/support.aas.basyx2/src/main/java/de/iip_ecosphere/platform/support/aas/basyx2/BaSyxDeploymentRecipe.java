/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasServer;
import de.iip_ecosphere.platform.support.aas.BasicSetupSpec;
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.LocalPersistenceType;
import de.iip_ecosphere.platform.support.aas.SetupSpec;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.basyx2.BaSyxAbstractAasServer.ServerType;
import de.iip_ecosphere.platform.support.function.IORunnable;

/**
 * An initial BaSyx-specific deployment builder.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxDeploymentRecipe implements DeploymentRecipe {

    private SetupSpec setupSpec;

    /**
     * Creates a deployment builder with root/empty document base path.
     * 
     * @param spec the setup specification
     */
    BaSyxDeploymentRecipe(SetupSpec spec) {
        this.setupSpec = spec;
    }
    
    @Override
    public DeploymentRecipe setAccessControlAllowOrigin(String accessControlAllowOrigin) {
        // TODO needed? SetupSpec?
        return this;
    }

    @Override
    public DeploymentRecipe setBearerTokenAuthenticationConfiguration(String issuerUri, String jwkSetUri, 
        String requiredAud) throws IllegalArgumentException {
        // TODO needed? SetupSpec?
        return this;
    }

    /**
     * Implements the immediate deployment recipe.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class BaSyxImmediateDeploymentRecipe implements ImmediateDeploymentRecipe {

        private List<IORunnable> actions = new ArrayList<>();
        
        @Override
        public ImmediateDeploymentRecipe deploy(Aas aas) throws IOException {
            actions.add(() -> {
                BaSyxDeploymentRecipe.deploy(setupSpec, aas);
            });
            return this;
        }

        @Override
        public AasServer createServer(String... options) {
            return new BaSyxLocalServer(setupSpec, ServerType.COMBINED, LocalPersistenceType.INMEMORY, options)
                .addActionsAfterStart(actions);
        }
        
    }

    @Override
    public ImmediateDeploymentRecipe forRegistry() {
        return new BaSyxImmediateDeploymentRecipe();
    }

    /**
     * Implements the registry deployment recipe.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class BaSyxRegistryDeploymentRecipe implements RegistryDeploymentRecipe {

        private SetupSpec spec;
        
        /**
         * Creates an instance.
         * 
         * @param spec the setup specification
         */
        private BaSyxRegistryDeploymentRecipe(SetupSpec spec) {
            this.spec = spec;
        }
        
        @Override
        public Registry obtainRegistry() throws IOException {
            return AasFactory.getInstance().obtainRegistry(spec, spec.getAasRegistryEndpoint().getSchema());
        }

        @Override
        public RegistryDeploymentRecipe deploy(Aas aas) throws IOException {
            BaSyxDeploymentRecipe.deploy(spec, aas);
            return this;
        }

        @Override
        public AasServer createServer(String... options) {
            return new BaSyxLocalServer(spec, ServerType.COMBINED, LocalPersistenceType.INMEMORY, options);
        }
        
    }
 
    @Override
    public RegistryDeploymentRecipe forRegistry(Endpoint aasRegistry, Endpoint smRegistry) {
        BasicSetupSpec spec = new BasicSetupSpec(setupSpec);
        spec.setRegistryEndpoints(aasRegistry, smRegistry);
        return new BaSyxRegistryDeploymentRecipe(setupSpec);
    }
    
    /**
     * Creates a new BaSyx AAS server.
     * 
     * @param spec the setup specification
     * @param aas the AAS
     * @throws IOException if the deployment cannot be executed, e.g. due to permission issues
     */
    static void deploy(SetupSpec spec, Aas aas) throws IOException {
        BaSyxRegistry registry = new BaSyxRegistry(spec);
        registry.createAas(aas, "");
        String ep = registry.getEndpoint(aas);
        registry.register(aas, null, "");
        for (Submodel sm: aas.submodels()) {
            registry.createSubmodel(aas, sm);
            ep = registry.getEndpoint(aas, sm);
            registry.register(aas, sm, ep);
        }
    }

}
