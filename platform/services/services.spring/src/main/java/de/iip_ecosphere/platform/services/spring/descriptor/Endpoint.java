/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
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
 * Represents a communication endpoint. The port must be given, the host may be empty.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Endpoint {

    public static final String PORT_PLACEHOLDER = "${port}";
    public static final String HOST_PLACEHOLDER = "${host}";
    
    /**
     * Returns the specified command line argument to set the communication port for this relation upon service 
     * deployment/execution. 
     * 
     * @return the generic port argument
     */
    public String getPortArg();

    /**
     * Returns the whether {@link #getPortArg()} is generic through {@link #PORT_PLACEHOLDER}.
     * 
     * @return {@code true} for generic, {@code false} else
     */
    public boolean isPortArgGeneric();
    
    /**
     * Returns the ready-to-use command line argument to set the communication port for this relation upon service 
     * deployment/execution. 
     * 
     * @param port the actual port number
     * @return the port argument, {@value #PORT_PLACEHOLDER} is substituted by {@code port}
     */
    public String getPortArg(int port);

    /**
     * Returns the specified command line argument to set the host to communicate with for this relation upon service 
     * deployment/execution. 
     * 
     * @return the generic host argument
     */
    public String getHostArg();

    /**
     * Returns the whether {@link #getHostArg()} is generic through {@link #HOST_PLACEHOLDER}.
     * 
     * @return {@code true} for generic, {@code false} else
     */
    public boolean isHostArgGeneric();
    
    /**
     * Returns the ready-to.use command line argument to set the host to communicate with for this relation upon 
     * service deployment/execution. 
     * 
     * @param hostname the actual hostname, {@value #HOST_PLACEHOLDER} is substituted by {@code hostname}, may be empty 
     *   for localhost
     * @return the host argument
     */
    public String getHostArg(String hostname);
    
}
