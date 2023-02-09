/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.spring.descriptor;

/**
 * Server process specification of servers to be started/stopped with an application.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Server extends ProcessSpec {

    /**
     * Returns the id of the server, also to be used as network management key.
     * 
     * @return the id of the server
     */
    public String getId();
    
    /**
     * Returns the network port of this server instance.
     * 
     * @return the network port
     */
    public int getPort();

    /**
     * Returns the host the server instance (may be superseded through a deployment plan).
     * 
     * @return the host name
     */
    public String getHost();

}
