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

import de.iip_ecosphere.platform.support.net.NetworkManager;

/**
 * Represents a relation/connection between services. Relations are [Name taken from usage view]
 * 
 * @author Holger Eichelberger, SSE
 */
public class Relation {

    public static final String LOCAL_CHANNEL = "";
    public static final String PORT_PLACEHOLDER = "${port}";
    public static final String HOST_PLACEHOLDER = "${host}";
    
    private String channel = "";
    private String portArg = "";
    private String hostArg = "";
    
    /**
     * Returns the name of the communication channel this relation is realized by. Channel names may be used
     * to query host and port via {@link NetworkManager}.
     * 
     * @return the channel name, may be {@link #LOCAL_CHANNEL} referring to all channels used for local communication
     */
    public String getChannel() {
        return channel;
    }
    
    /**
     * Returns the specified command line argument to set the communication port for this relation upon service 
     * deployment/execution. 
     * 
     * @return the generic port argument, {@value #PORT_PLACEHOLDER} is substituted by the port number
     */
    public String getPortArg() {
        return portArg;
    }

    /**
     * Returns the ready-to-use command line argument to set the communication port for this relation upon service 
     * deployment/execution. 
     * 
     * @param port the actual port number
     * @return the port argument
     */
    public String getPortArg(int port) {
        return portArg.replace(PORT_PLACEHOLDER, String.valueOf(port));
    }

    /**
     * Returns the specified command line argument to set the host to communicate with for this relation upon service 
     * deployment/execution. 
     * 
     * @return the generic port argument, {@value #HOST_PLACEHOLDER} is substituted by the host name, may be empty 
     *   for localhost
     */
    public String getHostArg() {
        return hostArg;
    }

    /**
     * Returns the ready-to.use command line argument to set the host to communicate with for this relation upon 
     * service deployment/execution. 
     * 
     * @param hostname the actual hostname
     * @return the port argument
     */
    public String getHostArg(String hostname) {
        return hostArg.replace(HOST_PLACEHOLDER, hostname);
    }
    
    /**
     * Defines the name of the communication channel this relation is realized by. [Required by Spring]
     * 
     * @param channel the channel name, may be {@link #LOCAL_CHANNEL} referring to all channels used for 
     *   local communication
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * Defines the command line argument to set the communication port for this relation upon service 
     * deployment/execution. [Required by Spring]
     * 
     * @param portArg the generic port argument, may contain {@value #PORT_PLACEHOLDER}
     */
    public void setPortArg(String portArg) {
        this.portArg = portArg;
    }

    /**
     * Defines the command line argument to set the host to communicate with for this relation upon service 
     * deployment/execution. [Required by Spring]
     * 
     * @param hostArg the host argument, may contain {@value #HOST_PLACEHOLDER}
     */
    public void setHostArg(String hostArg) {
        this.hostArg = hostArg;
    }
    
}
