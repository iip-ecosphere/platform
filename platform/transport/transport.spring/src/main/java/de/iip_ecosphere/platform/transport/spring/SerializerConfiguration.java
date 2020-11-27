/********************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.transport.spring;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;

/**
 * Represents/reads the serialization setup from the application configuration. The list-based serializers
 * setting with prefix serialization defines the qualified class names of serializers to be registered with
 * {@link SerializerRegistry}.
 * 
 * @author Holger Eichelberger, SSE
 */
@ConfigurationProperties(prefix = "serialization")
public class SerializerConfiguration {
    
    private List<String> serializers = new ArrayList<String>();
    private String name = "";
    
    /**
     * Returns the qualified serializer class names to register.
     * 
     * @return the serializer class names (default empty)
     */
    public List<String> getSerializers() {
        return serializers;
    }
    
    /**
     * Returns the wire name of the registry.
     *  
     * @return the name
     */
    public String getName() {
        return name;
    }

    // setters required for @ConfigurationProperties

    /**
     * Defines the qualified serializer class names to register.
     * 
     * @param serializers the serializer class names
     */
    public void setSerializers(List<String> serializers) {
        this.serializers = serializers;
    }

    /**
     * Changes the wire name.
     * 
     * @param name the wire name
     */
    public void setName(String name) {
        this.name = name;
    }

}
