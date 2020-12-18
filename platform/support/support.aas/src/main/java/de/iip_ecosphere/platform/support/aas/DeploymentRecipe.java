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

/**
 * Defines the interface of a recipe that is able to deploy AAS at least to local servers for now.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface DeploymentRecipe {
    
    /**
     * Adds an in-memory registry to the deployment.
     * 
     * @param regPath the registry URL path
     * @return <b>this</b>
     */
    public DeploymentRecipe addInMemoryRegistry(String regPath);

    /**
     * Deploys the ASS and its sub-models. Intended for initial deployment. Requires a valid registry to be created 
     * before. For incremental deployment of sub-models for already deployed AAS please consult 
     * {@link AasServer#deploy(Aas, Submodel)}.
     * 
     * @param aas the AAS to deploy
     * @return <b>this</b>
     * @throws IllegalArgumentException if no registry was created before or {@code aas} was not created 
     *   by the corresponding {@link AasFactory}
     */
    public DeploymentRecipe deploy(Aas aas);

    /**
     * Creates the server instance.
     * 
     * @return the server
     */
    public AasServer createServer();

}
