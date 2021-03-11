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

package de.iip_ecosphere.platform.services.spring.yaml;

import java.util.ArrayList;
import java.util.List;

/**
 * If the service is not completely implemented rather than delegates functionality to an additional process that
 * must be started and managed along with the service. The process implementation (whatever it is) will be extracted 
 * from {@link #getPath()}. For the execution in a shell, the home directory will be set to the folder where the files 
 * in {@link #getPath()} are located.
 *  
 * @author Holger Eichelberger, SSE
 */
public class Process {
    
    private String path;
    private List<String> cmdArg = new ArrayList<>();
    private Endpoint streamEndpoint;
    private Endpoint aasEndpoint;

    /**
     * Returns the path within the artifact to be extracted.
     * 
     * @return the relative path
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Returns the command line arguments to start the process. The shell will be executed within the folder where
     * the files from {@link #getPath()} are extracted.
     * 
     * @return the command line arguments (may be empty for none), {@link #getStreamEndpoint() streaming endpoint} and 
     *     {@link #getAASEndpoint() AAS endpoint} will be added anyway
     */
    public List<String> getCmdArg() {
        return cmdArg;
    }
    
    /**
     * Returns streaming endpoint (port/host) the service shall communicate with. 
     * 
     * @return the streaming endpoint
     */
    public Endpoint getStreamEndpoint() {
        return streamEndpoint;
    }
    
    /**
     * Returns AAS endpoint (port/host) the service shall communicate with for commands. 
     * 
     * @return the AAS endpoint
     */
    public Endpoint getAasEndpoint() {
        return aasEndpoint;
    }
    
    
    /**
     * Returns the path within the artifact to be extracted. [required by Spring]
     * 
     * @param path the relative path
     */
    public void setPath(String path) {
        this.path = path;
    }
    
    /**
     * Defines the command line arguments. [required by Spring]
     * 
     * @param cmdArg the command line arguments (may be empty for none)
     */
    public void setCmdArg(List<String> cmdArg) {
        this.cmdArg = cmdArg;
    }
    
    /**
     * Defines communication endpoint (port/host) the service shall communicate with. [required by Spring] 
     * 
     * @param streamEndpoint the communication endpoint
     */
    public void setStreamEndpoint(Endpoint streamEndpoint) {
        this.streamEndpoint = streamEndpoint;
    }
    
    /**
     * Defines communication endpoint (port/host) the service shall communicate with. [required by Spring] 
     * 
     * @param aasEndpoint the communication endpoint
     */
    public void setAasEndpoint(Endpoint aasEndpoint) {
        this.aasEndpoint = aasEndpoint;
    }
    
}
