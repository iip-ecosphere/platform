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

import java.util.List;

/**
 * If the service is not completely implemented rather than delegates functionality to an additional process that
 * must be started and managed along with the service. The process implementation (whatever it is) will be extracted 
 * from {@link #getHomePath()}. For the execution in a shell, the home directory will be set to the folder where the 
 * files in {@link #getHomePath()} are located. {@link #getHomePath()} must not be empty, {@link #getCmdArg()} may be 
 * empty, {@link #getStreamEndpoint()} and {@link #getAasEndpoint()} must be given.
 *  
 * @author Holger Eichelberger, SSE
 */
public interface ProcessSpec extends de.iip_ecosphere.platform.services.environment.ProcessSpec {

    /**
     * Returns additional/optional command line arguments required to start the service. The port placeholder
     * {@link Endpoint#PORT_PLACEHOLDER} will be replaced with the command port the platform is using to send
     * administrative commands to the service (see {@link de.iip_ecosphere.platform.services.environment.Service}).
     *
     * @param port the port used for the command communication
     * @param protocol the protocol used for the command communication
     * @return the resolved command line arguments (may be empty for none)
     */
    public List<String> getCmdArg(int port, String protocol);

    /**
     * Returns streaming endpoint (port/host) on the service side the process shall communicate with. Counterpart of
     * {@link #getStreamEndpoint()}.
     * 
     * @return the streaming endpoint
     */
    public Endpoint getServiceStreamEndpoint();
    
    /**
     * Returns streaming endpoint (port/host) on the process side the service shall communicate with. Counterpart of 
     * {@link #getServiceStreamEndpoint()}.
     * 
     * @return the streaming endpoint
     */
    public Endpoint getStreamEndpoint();
    
    /**
     * Returns AAS endpoint (port/host) the service shall communicate with for commands. 
     * 
     * @return the AAS endpoint
     */
    public Endpoint getAasEndpoint();
    
    /**
     * Returns the time to wait for the process before going on with starting other services.
     * 
     * @return the wait time in ms, ignored if not positive
     */
    public int getWaitTime();
    
}
