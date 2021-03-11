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
 * Information about a single service.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Service {
    
    private String id;
    private String name;
    private String version;
    private String description = "";
    private List<String> cmdArg = new ArrayList<>();
    private List<ServiceDependency> dependencies = new ArrayList<>();
    private List<Relation> relations = new ArrayList<>();
    private Process process;

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
     * Returns the command line arguments.
     * 
     * @return the command line arguments (may be empty for none)
     */
    public List<String> getCmdArg() {
        return cmdArg;
    }
    
    /**
     * Defines the command line arguments. [required by Spring]
     * 
     * @return the service dependences(may be empty for none)
     */
    public List<ServiceDependency> getDependencies() {
        return dependencies;
    }

    /**
     * Returns the service-specific relations and command line arguments.
     * 
     * @return the relations, may be empty
     */
    public List<Relation> getRelations() {
        return relations;
    }
    
    /**
     * Returns an optional attached process realizing the service.
     * 
     * @return the process information, may be <b>null</b>
     */
    public Process getProcess() {
        return process;
    }

    /**
     * Defines the id of the service. [required by Spring]
     * 
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Defines the name of the service. [required by Spring]
     * 
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Defines the version of the service. [required by Spring]
     * 
     * @param version the version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Defines the description of the service. [required by Spring]
     * 
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
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
     * Defines the command line arguments. [required by Spring]
     * 
     * @param dependencies the service dependences(may be empty for none)
     */
    public void setDependencies(List<ServiceDependency> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * Defines the service-specific relations and command line arguments. [required by Spring]
     * 
     * @param relations the relations, may be empty
     */
    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }

    /**
     * Defines an optional attached process realizing the service. [required by Spring]
     * 
     * @param process the process information, may be <b>null</b>
     */
    public void setProcess(Process process) {
        this.process = process;
    }

}
