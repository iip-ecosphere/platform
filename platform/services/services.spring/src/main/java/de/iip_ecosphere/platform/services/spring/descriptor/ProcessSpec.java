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

import java.io.File;
import java.util.List;

import de.iip_ecosphere.platform.support.aas.AasFactory;

/**
 * If the service is not completely implemented rather than delegates functionality to an additional process that
 * must be started and managed along with the service. The process implementation (whatever it is) will be extracted 
 * from {@link #getPath()}. For the execution in a shell, the home directory will be set to the folder where the files 
 * in {@link #getPath()} are located. {@link #getPath()} must not be empty, {@link #getCmdArg()} may be empty, 
 * {@link #getStreamEndpoint()} and {@link #getAasEndpoint()} must be given.
 *  
 * @author Holger Eichelberger, SSE
 */
public interface ProcessSpec {

    /**
     * Returns the process implementing artifacts within the containing artifact to be extracted into the 
     * {@link #getHome() process home directory}.
     * 
     * @return the relative paths to the artifacts, shall start with "/" as part of ZIP/JAR
     */
    public List<String> getArtifacts();

    /**
     * Returns the system command or relative path within the artifact to be executed.
     * 
     * @return the command or relative path
     */
    public String getExecutable();
    
    /**
     * Returns an optional path to be prefixed before the executable. Relevance depends on the execution environment.
     * 
     * @return the optional executable path, may be <b>null</b> for none
     */
    public File getExecutablePath();
    
    /**
     * Returns the home directory of the process to be executed.
     * 
     * @return the home directory, may be <b>null</b> to rely on extracted paths, may be given to explicitly 
     *     define a home path
     */
    public File getHomePath();
    
    /**
     * Returns the command line arguments to start the process. The shell will be executed within the folder where
     * the files from {@link #getPath()} are extracted.
     * 
     * @return the command line arguments (may be empty for none), {@link #getStreamEndpoint() streaming endpoint} and 
     *     {@link #getAASEndpoint() AAS endpoint} will be added anyway
     */
    public List<String> getCmdArg();

    /**
     * Returns additional/optional command line arguments required to start the service. The port placeholder
     * {@link Endpoint#PORT_PLACEHOLDER} will be replaced with the command port the platform is using to send
     * administrative commands to the service (see {@link de.iip_ecosphere.platform.services.environment.Service}).
     * Similarly {@link #PROTOCOL_PLACEHOLDER} will be replaced with the AAS {@link AasFactory#getProtocols() protocol}.
     
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
     * Returns whether the underlying process is already started when firing up the service or it will be started 
     * through the service implementation. If specified, {@link #getArtifacts() artifacts} will be extracted anyway
     * into the {@link #getHome() process home directory}, assuming that a pre-installed executable will not specify
     * artifacts to be extracted.
     * 
     * @return {@code true} for started, {@code false} else (default)
     */
    public boolean isStarted();
    
    /**
     * Returns the time to wait for the process before going on with starting other services.
     * 
     * @return the wait time in ms, ignored if not positive
     */
    public int getWaitTime();
    
}
