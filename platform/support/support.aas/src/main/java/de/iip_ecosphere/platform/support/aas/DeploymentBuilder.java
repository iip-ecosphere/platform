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

import de.iip_ecosphere.platform.support.Server;

/**
 * Defines the interface of a builder that is able to deploy AAS at least to local servers for now.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface DeploymentBuilder {
    
    /**
     * Adds an in-memory registry to the deployment.
     * 
     * @param regPath the registry URL path
     * @return <b>this</b>
     */
    public DeploymentBuilder addInMemoryRegistry(String regPath);

    /**
     * Deploys the ASS. Requires a valid registry to be created before.
     * 
     * @param aas the AAS to deploy
     * @return <b>this</b>
     * @throws IllegalArgumentException if no registry was created before or {@code aas} was not created 
     *   by the corresponding {@link AasFactory}
     */
    public DeploymentBuilder deploy(Aas aas);

    /**
     * Deploys the given {@code subModel}.
     * 
     * @param subModel the subModel
     * @param path the path at which to make the sub-model available (shall be a qualified URL prefix path)
     * @return <b>this</b>
     * @throws IllegalArgumentException if no registry was created before or {@code aas} was not created 
     *   by the corresponding {@link AasFactory}
     */
    public DeploymentBuilder deploy(Submodel subModel, String path);

    /**
     * Starts the server.
     * 
     * @param minWaitingTime the minimum waiting time
     * @return the server
     */
    public Server createServer(int minWaitingTime);

}
