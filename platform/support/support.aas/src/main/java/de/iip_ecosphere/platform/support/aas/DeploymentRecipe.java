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

package de.iip_ecosphere.platform.support.aas;

import java.io.IOException;

import de.iip_ecosphere.platform.support.Endpoint;

/**
 * Defines the interface of a recipe that is able to deploy AAS. For each potential deployment path, we add
 * a sub-recipe that limits the defines the specific deployment operations.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface DeploymentRecipe extends CorsEnabledRecipe {
    
    /**
     * Allow any origin.
     */
    public static final String ANY_CORS_ORIGIN = "*";
    
    /**
     * Defines a sub-recive allowing for immediate deployment of AAS.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface ImmediateDeploymentRecipe {

        /**
         * Deploys the ASS and its sub-models. Intended for initial deployment. Requires a valid registry to be created 
         * before. For incremental deployment of sub-models for already deployed AAS please consult 
         * {@link AasServer#deploy(Aas, Submodel)}.
         * 
         * @param aas the AAS to deploy
         * @return <b>this</b>
         * @throws IllegalArgumentException if {@code aas} was not created by the corresponding {@link AasFactory}
         * @throws IOException if the deployment cannot be executed, e.g. due to permission issues
         */
        public ImmediateDeploymentRecipe deploy(Aas aas) throws IOException;

        /**
         * Creates the server instance.
         * 
         * @param options implementation-specific options for server creation
         * @return the server
         */
        public AasServer createServer(String... options);

    }
    
    /**
     * Defines a sub-recipe where no immediate deployment is possible rather than interaction with the
     * specified registry via {@link #obtainRegistry()}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface RegistryDeploymentRecipe {

        /**
         * Obtains access to the registry (via {@link AasFactory#obtainRegistry(SetupSpec)}.
         * 
         * @return the registry instance
         * @throws IOException in case that the recipe/connection cannot be created
         */
        public Registry obtainRegistry() throws IOException;

        /**
         * Deploys the ASS and its sub-models. Intended for initial deployment. Requires a valid registry to be created 
         * before. For incremental deployment of sub-models for already deployed AAS please consult 
         * {@link AasServer#deploy(Aas, Submodel)}.
         * 
         * @param aas the AAS to deploy
         * @return <b>this</b>
         * @throws IOException if the deployment cannot be executed, e.g. due to permission issues
         */
        public RegistryDeploymentRecipe deploy(Aas aas) throws IOException;

        /**
         * Creates the server instance.
         * 
         * @param options implementation-specific options for server creation
         * @return the server
         */
        public AasServer createServer(String... options);

    }
    
    /**
     * Adds an in-memory registry to the deployment.
     * 
     * @return an instance of the sub-recipe
     */
    public ImmediateDeploymentRecipe forRegistry();

    /**
     * Points to a standalone registry for older metamodels, Does not work with metamodel v3.
     * 
     * @param endpoint the registry endpoint
     * @return an instance of the sub-recipe
     */
    public default RegistryDeploymentRecipe forRegistry(Endpoint endpoint) {
        return forRegistry(endpoint, endpoint);
    }

    /**
     * Points to a standalone registry.
     * 
     * @param aasRegistry the AAS registry endpoint
     * @param submodelRegistry the submodel registry endpoint
     * @return an instance of the sub-recipe
     */
    public RegistryDeploymentRecipe forRegistry(Endpoint aasRegistry, Endpoint submodelRegistry);

    @Override
    public DeploymentRecipe setAccessControlAllowOrigin(String accessControlAllowOrigin);

    /**
     * Sets the bearer authentication configuration.
     * 
     * @param issuerUri the URI of the issuer
     * @param jwkSetUri unclear
     * @param requiredAud unclear (may be <b>null</b>)
     * @return an instance of the sub-recipe
     * @throws IllegalArgumentException if the passed in information is invalid
     */
    public DeploymentRecipe setBearerTokenAuthenticationConfiguration(String issuerUri, String jwkSetUri, 
        String requiredAud) throws IllegalArgumentException;
    
}
