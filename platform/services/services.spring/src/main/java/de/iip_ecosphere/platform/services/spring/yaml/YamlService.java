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

import de.iip_ecosphere.platform.services.ServiceKind;
import de.iip_ecosphere.platform.services.spring.descriptor.Service;

/**
 * Information about a single service.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlService implements Service {
    
    private String id;
    private String name;
    private String version;
    private String description = "";
    private List<String> cmdArg = new ArrayList<>();
    private String ensembleWith;
    private List<YamlServiceDependency> dependencies = new ArrayList<>();
    private List<YamlRelation> relations = new ArrayList<>();
    private List<YamlTypedData> parameters = new ArrayList<>();
    private YamlProcess process;
    private ServiceKind kind;
    private boolean deployable = false;
    private int instances = 1;
    private long memory = -1;
    private long disk = -1;
    private int cpus = 1;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<String> getCmdArg() {
        return cmdArg;
    }
    
    @Override
    public String getEnsembleWith() {
        return ensembleWith;
    }
    
    @Override
    public List<YamlServiceDependency> getDependencies() {
        return dependencies;
    }

    @Override
    public List<YamlRelation> getRelations() {
        return relations;
    }

    @Override
    public List<YamlTypedData> getParameters() {
        return parameters;
    }

    @Override
    public YamlProcess getProcess() {
        return process;
    }
    
    @Override
    public ServiceKind getKind() {
        return kind;
    }
    
    @Override
    public boolean isDeployable() {
        return deployable;
    }
    
    @Override
    public int getInstances() {
        return instances;
    }

    @Override
    public long getMemory() {
        return memory;
    }

    @Override
    public long getDisk() {
        return disk;
    }

    @Override
    public int getCpus() {
        return cpus;
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

    /**
     * Defines the command line arguments. [required by SnakeYaml]
     * 
     * @param cmdArg the command line arguments (may be empty for none)
     */
    public void setCmdArg(List<String> cmdArg) {
        this.cmdArg = cmdArg;
    }

    /**
     * Defines the service id of the ensemble leader starting this service. [required by SnakeYaml]
     * 
     * @param ensembleWith the service id of the ensemble leader (may be <b>null</b> for none)
     */
    public void setEnsembleWith(String ensembleWith) {
        this.ensembleWith = ensembleWith;
    }

    /**
     * Defines the command line arguments. [required by SnakeYaml]
     * 
     * @param dependencies the service dependences(may be empty for none)
     */
    public void setDependencies(List<YamlServiceDependency> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * Defines the service-specific relations and command line arguments. [required by SnakeYaml]
     * 
     * @param relations the relations, may be empty
     */
    public void setRelations(List<YamlRelation> relations) {
        this.relations = relations;
    }

    /**
     * Defines the service-specific configurable parameter. [required by SnakeYaml]
     * 
     * @param parameters the parameters, may be empty
     */
    public void setParameters(List<YamlTypedData> parameters) {
        this.parameters = parameters;
    }

    /**
     * Defines an optional attached process realizing the service. [required by SnakeYaml]
     * 
     * @param process the process information, may be <b>null</b>
     */
    public void setProcess(YamlProcess process) {
        this.process = process;
    }
    
    /**
     * Sets the service kind. [required by SnakeYaml]
     * 
     * @param kind the service kind
     */
    public void setKind(ServiceKind kind) {
        this.kind = kind;
    }
    
    /**
     * Sets whether this service is decentrally deployable.
     * 
     * @param deployable {@code true} for deployable, {@code false} for not deployable 
     */
    public void setDeployable(boolean deployable) {
        this.deployable = deployable;
    }

    /**
     * Defines the desired number of instances of this service to be started in the same process. This property is 
     * considered during deployment only if the deployer supports it.
     * 
     * @param instances the number of instances, ignored if not positive
     */
    public void setInstances(int instances) {
        this.instances = instances;
    }

    /**
     * Defines the desired memory for instances of this service. This property is considered during deployment
     * only if the deployer supports it.
     * 
     * @param memory the desired memory in <a href="https://en.wikipedia.org/wiki/Mebibyte">Mebibytes</a> (i.e., "m"), 
     *   ignored if not positive
     */
    public void setMemory(long memory) {
        this.memory = memory;
    }

    /**
     * Defines the desired disk space for instances of this service. This property is considered during deployment
     * only if the deployer supports it.
     * 
     * @param disk the desired disk space in <a href="https://en.wikipedia.org/wiki/Mebibyte">Mebibytes</a> (i.e., "m"),
     *   ignored if not positive
     */
    public void setDisk(long disk) {
        this.disk = disk;
    }

    /**
     * Defines the desired number of CPUs for instances of this service. This property is considered during deployment
     * only if the deployer supports it.
     * 
     * @param cpus the desired number of CPUs , ignored if not positive
     */
    public void setCpus(int cpus) {
        this.cpus = cpus;
    }

}
