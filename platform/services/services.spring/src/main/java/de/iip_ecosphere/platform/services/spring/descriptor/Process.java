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
 * from {@link #getPath()}. For the execution in a shell, the home directory will be set to the folder where the files 
 * in {@link #getPath()} are located. {@link #getPath()} must not be empty, {@link #getCmdArg()} may be empty, 
 * {@link #getStreamEndpoint()} and {@link #getAasEndpoint()} must be given.
 *  
 * @author Holger Eichelberger, SSE
 */
public interface Process {
    
    /**
     * Returns the path within the artifact to be extracted.
     * 
     * @return the relative path
     */
    public String getPath();
    
    /**
     * Returns the command line arguments to start the process. The shell will be executed within the folder where
     * the files from {@link #getPath()} are extracted.
     * 
     * @return the command line arguments (may be empty for none), {@link #getStreamEndpoint() streaming endpoint} and 
     *     {@link #getAASEndpoint() AAS endpoint} will be added anyway
     */
    public List<String> getCmdArg();

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
     * Returns whether the underlying process is already started when firing up the service.
     * 
     * @return {@code true} for started, {@code false} else (default)
     */
    public boolean isStarted();
    
}
