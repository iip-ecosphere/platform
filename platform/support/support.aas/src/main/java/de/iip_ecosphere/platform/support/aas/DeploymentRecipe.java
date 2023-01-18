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
public interface DeploymentRecipe {
    
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
         */
        public ImmediateDeploymentRecipe deploy(Aas aas);

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
         * Obtains access to the registry (via {@link AasFactory#obtainRegistry(Endpoint)}.
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
         * @throws IllegalArgumentException if {@code aas} was not created by the corresponding {@link AasFactory}
         */
        public RegistryDeploymentRecipe deploy(Aas aas);

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
     * @param regEndpoint the registry URL endpoint
     * @return an instance of the sub-recipe
     * @deprecated Use {@link #addInMemoryRegistry(Endpoint)} instead
     */
    @Deprecated
    public ImmediateDeploymentRecipe addInMemoryRegistry(String regEndpoint);

    /**
     * Adds an in-memory registry to the deployment.
     * 
     * @param regEndpoint the registry URL endpoint
     * @return an instance of the sub-recipe
     */
    public default ImmediateDeploymentRecipe addInMemoryRegistry(Endpoint regEndpoint) {
        return addInMemoryRegistry(regEndpoint.getEndpoint());
    }

    /**
     * Points to a standalone registry.
     * 
     * @param endpoint the registry endpoint
     * @return an instance of the sub-recipe
     */
    public RegistryDeploymentRecipe setRegistryUrl(Endpoint endpoint);
    
    /**
     * Sets the access control to allow cross origin.
     * 
     * @param accessControlAllowOrigin the information to be placed in the HTTP header field 
     * "Access-Control-Allow-Origin"; the specific server or {@link #ANY_CORS_ORIGIN}
     * @return an instance of the sub-recipe
     */
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
