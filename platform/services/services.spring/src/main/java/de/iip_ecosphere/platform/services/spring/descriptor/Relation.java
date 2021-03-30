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

import de.iip_ecosphere.platform.support.net.NetworkManager;

/**
 * Represents a relation/connection between services. [Name taken from usage view]
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Relation {

    public static final String LOCAL_CHANNEL = "";
    
    /**
     * Declares the direction types of an relation.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum Direction {
        
        /**
         * Input direction.
         */
        IN,

        /**
         * Output direction.
         */
        OUT,
        
        /**
         * If an explicit direction for other relations such as the empty channel for local connections shall be stated.
         */
        OTHER
    }
    
    /**
     * Returns the name of the communication channel this relation is realized by. Channel names may be used
     * to query host and port via {@link NetworkManager}.
     * 
     * @return the channel name, may be {@link #LOCAL_CHANNEL} referring to all channels used for local communication
     */
    public String getChannel();
    
    /**
     * Returns communication endpoint (port/host) the service shall communicate with. 
     * 
     * @return the communication endpoint (may be <b>null</b> for internal relations}
     */
    public Endpoint getEndpoint();
 
    /**
     * Returns the description of the relation.
     * 
     * @return the description, may be empty
     */
    public String getDescription();

    /**
     * Returns the type of the data.
     * 
     * @return the type as qualified Java name (may be <b>null</b> for the relation with empty {@link #getChannel()}
     */
    public String getType();

    /**
     * Returns the direction of the relation.
     * 
     * @return the direction (may be <b>null</b> for the relation with empty {@link #getChannel()}
     */
    public Direction getDirection();

}
