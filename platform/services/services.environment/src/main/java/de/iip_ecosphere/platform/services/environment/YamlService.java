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

package de.iip_ecosphere.platform.services.environment;

/**
 * Information about a single service.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlService {
    
    private String id;
    private String name;
    private String version;
    private String description = "";
    private ServiceKind kind;
    private boolean deployable = false;

    /**
     * Returns the name of the service.
     * 
     * @return the name
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the name of the service.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the version of the service.
     * 
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the description of the service.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets the service kind.
     * 
     * @return the service kind
     */
    public ServiceKind getKind() {
        return kind;
    }
    
    /**
     * Returns whether this service is decentrally deployable.
     * 
     * @return {@code true} for deployable, {@code false} for not deployable 
     */
    public boolean isDeployable() {
        return deployable;
    }
    
    /**
     * Defines the id of the service. [required by SnakeYaml]
     * 
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Defines the name of the service. [required by SnakeYaml]
     * 
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Defines the version of the service. [required by SnakeYaml]
     * 
     * @param version the version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Defines the description of the service. [required by SnakeYaml]
     * 
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

}
