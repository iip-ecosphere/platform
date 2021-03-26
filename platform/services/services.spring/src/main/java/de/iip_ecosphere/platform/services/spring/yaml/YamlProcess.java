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

import de.iip_ecosphere.platform.services.spring.descriptor.Process;

/**
 * If the service is not completely implemented rather than delegates functionality to an additional process that
 * must be started and managed along with the service. The process implementation (whatever it is) will be extracted 
 * from {@link #getPath()}. For the execution in a shell, the home directory will be set to the folder where the files 
 * in {@link #getPath()} are located.
 *  
 * @author Holger Eichelberger, SSE
 */
public class YamlProcess implements Process {
    
    private String path;
    private List<String> cmdArg = new ArrayList<>();
    private YamlEndpoint serviceStreamEndpoint;
    private YamlEndpoint streamEndpoint;
    private YamlEndpoint aasEndpoint;
    private boolean started = false;

    @Override
    public String getPath() {
        return path;
    }
    
    @Override
    public List<String> getCmdArg() {
        return cmdArg;
    }

    @Override
    public YamlEndpoint getServiceStreamEndpoint() {
        return serviceStreamEndpoint;
    }

    @Override
    public YamlEndpoint getStreamEndpoint() {
        return streamEndpoint;
    }
    
    @Override
    public YamlEndpoint getAasEndpoint() {
        return aasEndpoint;
    }
    
    @Override
    public boolean isStarted() {
        return started;
    }
    
    /**
     * Returns the path within the artifact to be extracted. [required by SnakeYaml]
     * 
     * @param path the relative path
     */
    public void setPath(String path) {
        this.path = path;
    }
    
    /**
     * Defines the command line arguments. [required by SnakeYaml]
     * 
     * @param cmdArg the command line arguments (may be empty for none)
     */
    public void setCmdArg(List<String> cmdArg) {
        this.cmdArg = cmdArg;
    }

    /**
     * Defines communication endpoint (port/host) for streaming on the service side (to communicate with the 
     * process side). [required by SnakeYaml] 
     * 
     * @param serviceStreamEndpoint the communication endpoint
     */
    public void setServiceStreamEndpoint(YamlEndpoint serviceStreamEndpoint) {
        this.serviceStreamEndpoint = serviceStreamEndpoint;
    }

    /**
     * Defines communication endpoint (port/host) on the process side the service shall communicate with. 
     * [required by SnakeYaml] 
     * 
     * @param streamEndpoint the communication endpoint
     */
    public void setStreamEndpoint(YamlEndpoint streamEndpoint) {
        this.streamEndpoint = streamEndpoint;
    }
    
    /**
     * Defines communication endpoint (port/host) the service shall communicate with. [required by SnakeYaml] 
     * 
     * @param aasEndpoint the communication endpoint
     */
    public void setAasEndpoint(YamlEndpoint aasEndpoint) {
        this.aasEndpoint = aasEndpoint;
    }

    /**
     * Changes whether the underlying process is already started when firing up the service. [required by SnakeYaml] 
     * 
     * @param started {@code true} for started (default), {@code false} else
     */
    public void setStarted(boolean started) {
        this.started = started;
    }

}
