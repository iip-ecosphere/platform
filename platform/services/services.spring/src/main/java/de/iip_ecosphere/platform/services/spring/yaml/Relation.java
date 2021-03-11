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
    
    private String channel = "";
    private Endpoint endpoint;
    
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
     * Returns communication endpoint (port/host) the service shall communicate with. 
     * 
     * @return the communication endpoint
     */
    public Endpoint getEndpoint() {
        return endpoint;
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
     * Defines communication endpoint (port/host) the service shall communicate with. 
     * 
     * @param endpoint the communication endpoint
     */
    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }
    
}
