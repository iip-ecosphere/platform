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

package de.iip_ecosphere.platform.platform.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.iip_ecosphere.platform.services.environment.switching.ServiceBase;
import de.iip_ecosphere.platform.support.iip_aas.Version;
import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;

/**
 * Describes a simple YAML deployment plan assigning services from an artifact to resources.
 * If {@link #isParallelize()} is {@code false} (the default), the order of the assignments determines the 
 * sequence how they shall be executed by the CLI/platform.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServiceDeploymentPlan extends AbstractSetup {

    /**
     * Base class for something that shall be assigned to a resource.
     * 
     * @author Holger Eichelberger, SSE
     */
    public abstract static class ResourceAssignment {

        private String resource;

        /**
         * Returns the name/id of the resource.
         * 
         * @return the name/id
         */
        public String getResource() {
            return resource;
        }
        
        /**
         * Sets the name/id of the resource. [required by SnakeYaml]
         * 
         * @param resource the name/id of the resource
         */
        public void setResource(String resource) {
            this.resource = resource;
        }

    }
    
    /**
     * Assigns a container to a resource as prerequisite for starting services on the resource.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class ContainerResourceAssignment extends ResourceAssignment {
        
        private String containerDesc;

        /**
         * Returns the path/URI of the container (information) descriptor. The referenced container image must be 
         * located in the same folder.
         * 
         * @return the path/URI
         */
        public String getContainerDesc() {
            return containerDesc;
        }
        
        /**
         * Sets the path/URI of the container (information) descriptor. The referenced container image must be 
         * located in the same folder. [required by SnakeYaml]
         * 
         * @param containerDesc the path/URI to the descriptor file
         */
        public void setContainerDesc(String containerDesc) {
            this.containerDesc = containerDesc;
        }

    }
    
    /**
     * Assigns one or multiple services to a resource.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class ServiceResourceAssignment extends ResourceAssignment {
        
        private List<String> services;
        private String artifact;

        /**
         * Returns the assigned service names.
         * 
         * @return the names
         */
        public List<String> getServices() {
            return services;
        }
        
        /**
         * Returns the path/URI of the artifact.
         * 
         * @return the path/URI
         */
        public String getArtifact() {
            return artifact;
        }
        
        /**
         * Returns the {@link #getServices()} in terms of an array.
         * 
         * @param appId the application id, may be null or empty for legacy start
         * @param appInstanceId the application instance id, may be null or empty for legacy start
         * @return the full service ids if {@code appId} and {@code appInstanecId] are given, the plain service ids else
         */
        public String[] getServicesAsArray(String appId, String appInstanceId) {
            String[] result = new String[services.size()];
            if (null == appId || appId.length() == 0 || null == appInstanceId || appInstanceId.length() == 0) {
                result = services.toArray(result);
            } else {
                for (int s = 0; s < services.size(); s++) {
                    result[s] = ServiceBase.composeId(services.get(s), appId, appInstanceId);
                }
            }
            return result;
        }

        /**
         * Defines the assigned service names. [required by SnakeYaml]
         * 
         * @param services the service names names
         */
        public void setServices(List<String> services) {
            this.services = services;
        }
        
        /**
         * Defines the path/URI of the artifact. [required by SnakeYaml]
         * 
         * @param artifact the path/URI
         */
        public void setArtifact(String artifact) {
            this.artifact = artifact;
        }

    }
    
    private String application = "";
    private String id = "";
    private String appId = "";
    private Version version = new Version();
    private String description = "";
    private String artifact;
    private List<ContainerResourceAssignment> container = new ArrayList<>();
    // naming: initially only for services without container!
    private List<ServiceResourceAssignment> assignments = new ArrayList<>();
    private boolean parallelize = false;
    private boolean onUndeployRemoveArtifact = true;
    private Map<String, String> ensembles = new HashMap<>();
    private boolean disabled = false;
    private boolean allowMultiExecution = true;
    private List<String> arguments;
    private Map<String, String> servers = new HashMap<>();
    private Map<String, Map<String, String>> serviceParams = new HashMap<>();

    /**
     * Returns the name of the application.
     * 
     * @return the name
     */
    public String getApplication() {
        return application;
    }

    /**
     * Returns the id of the plan.
     * 
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the id of the application (can be used to find its AAS).
     * 
     * @return the id
     */
    public String getAppId() {
        return appId;
    }

    /**
     * Returns the version of the plan.
     * 
     * @return the version
     */
    public Version getVersion() {
        return version;
    }

    /**
     * Returns the description of the application for the CLI/UI to display.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the path/URI of the artifact.
     * 
     * @return the path/URI
     */
    public String getArtifact() {
        return artifact;
    }
    
    /**
     * Returns whether the plan shall be executed in parallel.
     * 
     * @return {@code true} for parallel execution, {@code false} else
     */
    public boolean isParallelize() {
        return parallelize;
    }

    /**
     * Returns whether the plan is disabled in the sense that it shall not be shown on an UI.
     * 
     * @return {@code true} for disabled, {@code false} else
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Returns whether the plan can be executed multiple times or only once, i.e., it checks for .
     * 
     * @return {@code true} for allowed multi-execution, {@code false} for single-execution else
     */
    public boolean isMultiExecution() {
        return allowMultiExecution;
    }

    /**
     * Returns whether artifacts shall be removed from the platform on undeployment.
     * 
     * @return {@code true} for removal, {@code false} else (default {@code true})
     */
    public boolean isOnUndeployRemoveArtifact() {
        return onUndeployRemoveArtifact;
    }

    /**
     * Returns the service-resource-assignments.
     * 
     * @return the assignments
     */
    public List<ServiceResourceAssignment> getAssignments() {
        return assignments;
    }
    
    /**
     * Returns the container-resource-assignments.
     * 
     * @return the container
     */
    public List<ContainerResourceAssignment> getContainer() {
        return container;
    }
    
    /**
     * Returns the optional ensembles.
     * 
     * @return the ensembles (member-leader mapping)
     */
    public Map<String, String> getEnsembles() {
        return ensembles;
    }

    /**
     * Returns the optional servers mapping.
     * 
     * @return the servers (id-IP address mapping)
     */
    public Map<String, String> getServers() {
        return servers;
    }

    /**
     * Service parameters to be re-configured upon service start.
     * 
     * @return the service-id/name/value mapping for parameters
     */
    public Map<String, Map<String, String>> getServiceParams() {
        return serviceParams;
    }
    
    /**
     * Returns the optional arguments.
     * 
     * @return the arguments, may be empty or <b>null</b> for none
     */
    public List<String> getArguments() {
        return arguments;
    }

    /**
     * Returns the optional arguments. [SnakeYaml]
     * 
     * @param arguments the arguments, may be empty or <b>null</b> for none
     */
    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    /**
     * Changes the name of the application. [snakeyaml]
     * 
     * @param application the name
     */
    public void setApplication(String application) {
        this.application = application;
    }

    /**
     * Changes the id of the plan. [snakeyaml]
     * 
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Changes the id of the application, may be used to retrieve its AAS. [snakeyaml]
     * 
     * @param appId the id
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * Changes the version of the plan. [snakeyaml]
     * 
     * @param version the version
     */
    public void setVersion(Version version) {
        this.version = version;
    }

    /**
     * Changes the description of the application for the CLI/UI to display. [snakeyaml]
     * 
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Defines the path/URI of the artifact. [required by SnakeYaml]
     * 
     * @param artifact the path/URI
     */
    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }

    /**
     * Changes the service-resource-assignments. [required by SnakeYaml]
     * 
     * @param assignments the assignments
     */
    public void setAssignments(List<ServiceResourceAssignment> assignments) {
        this.assignments = assignments;
    }
    
    /**
     * Changes the container-resource-assignments. [required by SnakeYaml]
     * 
     * @param container the container
     */
    public void setContainer(List<ContainerResourceAssignment> container) {
        this.container = container;
    }

    /**
     * Changes whether the plan shall be executed in parallel. [required by SnakeYaml]
     * 
     * @param parallelize {@code true} for parallel execution, {@code false} else
     */
    public void setParallelize(boolean parallelize) {
        this.parallelize = parallelize;
    }

    /**
     * Changes whether the plan shall be considered disabled (on UI). [required by SnakeYaml]
     * 
     * @param disabled {@code true} for disabled, {@code false} else
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * Returns whether the plan can be executed multiple times or only once, i.e., it checks for .
     * 
     * @param allowMultiExecution {@code true} for allowed multi-execution, {@code false} for single-execution else
     */
    public void setMultiExecution(boolean allowMultiExecution) {
        this.allowMultiExecution = allowMultiExecution;
    }

    /**
     * Changes whether artifacts shall be removed from the platform on undeployment. [required by SnakeYaml]
     * 
     * @param onUndeployRemoveArtifact {@code true} for removal, {@code false} else
     */
    public void setOnUndeployRemoveArtifact(boolean onUndeployRemoveArtifact) {
        this.onUndeployRemoveArtifact = onUndeployRemoveArtifact;
    }
    
    /**
     * Defines the optional ensembles. [required by SnakeYaml]
     * 
     * @param ensembles the ensembles (member-leader mapping)
     */
    public void setEnsembles(Map<String, String> ensembles) {
        this.ensembles = ensembles;
    }

    /**
     * Defines the optional servers mapping.
     * 
     * @param servers the servers (id-IP address mapping)
     */
    public void setServers(Map<String, String> servers) {
        this.servers = servers;
    }
    
    /**
     * Service parameters to be re-configured upon service start. [SnakeYaml]
     * 
     * @param serviceParams the service-id/name/value mapping for parameters
     */
    public void setServiceParams(Map<String, Map<String, String>> serviceParams) {
        this.serviceParams = serviceParams;
    }
    
    /**
     * Reads the service deployment plan from a given YAML file.
     * 
     * @param filename the file name
     * @return the instance
     * @throws IOException if reading fails
     */
    public static ServiceDeploymentPlan readFromYaml(String filename) throws IOException {
        return readFromYaml(ServiceDeploymentPlan.class, filename);
    }

}
