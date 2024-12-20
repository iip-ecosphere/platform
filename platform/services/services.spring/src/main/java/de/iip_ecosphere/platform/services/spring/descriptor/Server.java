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

import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.support.Version;

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
     * Returns the version of the server.
     * 
     * @return the version
     */
    public Version getVersion();

    /**
     * Returns the description of the server.
     * 
     * @return the description
     */
    public String getDescription();
    
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
    
    /**
     * Returns the class to be started as server. Must implement {@link de.iip_ecosphere.platform.support.Server}.
     * 
     * @return the class name
     */
    public String getCls();
    
    /**
     * Execute the server as an own process.
     * 
     * @return {@code true} for process, {@code false} for in-service-manager execution
     */
    public boolean getAsProcess();

    /**
     * Turns this server into a temporary (partially default filled) service instance.
     * 
     * @return the service instance
     */
    public YamlService toService();

    /**
     * Returns the desired memory for instances of this server. 
     * 
     * @return the desired memory in <a href="https://en.wikipedia.org/wiki/Mebibyte">Mebibytes</a> (i.e., "m"), ignored
     *   if not positive
     */
    public long getMemory();

}
