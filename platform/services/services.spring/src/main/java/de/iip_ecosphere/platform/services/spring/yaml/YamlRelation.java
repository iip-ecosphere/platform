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

import de.iip_ecosphere.platform.services.spring.descriptor.Relation;

/**
 * Represents a relation/connection between services. [Name taken from usage view]
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlRelation implements Relation {

    private String id;
    private String channel = "";
    private String service = "";
    private YamlEndpoint endpoint;
    private String description = "";
    private String type;
    private Direction direction = Direction.OTHER;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getChannel() {
        return channel;
    }
    
    @Override
    public String getService() {
        return service;
    }
    
    @Override
    public YamlEndpoint getEndpoint() {
        return endpoint;
    }
    
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getType() {
        return type;
    }
    
    @Override
    public String[] getTypes() {
        return type.split(",");
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    /**
     * Defines the id of this relation. [Required by SnakeYaml]
     * 
     * @param id the id of the channel, may be empty if {@link #getChannel() is empty} or in case of an outgoing data
     * port, must be given for an opposite side data port. 
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Defines the name of the communication channel this relation is realized by. [Required by SnakeYaml]
     * 
     * @param channel the channel name, may be {@link #LOCAL_CHANNEL} referring to all channels used for 
     *   local communication
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }
    
    /**
     * Defines the name of the communication channel this relation is realized by. [Required by SnakeYaml]
     * 
     * @param service the id of the service, may be empty if {@link #getChannel() is empty} or in case of an outgoing 
     * data port, must be given to denote the service holding an opposite side incoming data port. 
     */
    public void setService(String service) {
        this.service = service;
    }

    /**
     * Defines communication endpoint (port/host) the service shall communicate with. 
     * 
     * @param endpoint the communication endpoint
     */
    public void setEndpoint(YamlEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Defines the description of the relation. [required by SnakeYaml]
     * 
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Defines the type of the data. [required by SnakeYaml]
     * 
     * @param type the type as qualified Java name
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Defines the direction of the relation. [required by SnakeYaml]
     * 
     * @param direction the direction
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

}
