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

import de.iip_ecosphere.platform.support.Server;

/**
 * A specific server interface for AAS deployment.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface AasServer extends Server {

    /**
     * Deploys an AAS to this server.
     * 
     * @param aas the AAS to be deployed
     * @throws IOException if the deployment fails
     */
    public void deploy(Aas aas) throws IOException;
    
    /**
     * Deploys the given sub-model as sub-model of {@code aas}.
     * 
     * @param aas the AAS
     * @param submodel the sub-model
     * @throws IOException if the deployment fails
     */
    public void deploy(Aas aas, Submodel submodel) throws IOException;

    /**
     * Start the server without waiting time/blocking.
     * 
     * @return <b>this</b>
     */
    public AasServer start();

}
