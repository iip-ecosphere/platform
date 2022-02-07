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
import java.util.List;

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
         * @return the services
         */
        public String[] getServicesAsArray() {
            return services.toArray(new String[services.size()]);
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
    
    private String artifact;
    private List<ContainerResourceAssignment> container = new ArrayList<>();
    // naming: initially only for services without container!
    private List<ServiceResourceAssignment> assignments  = new ArrayList<>();
    private boolean parallelize = false;
    private boolean onUndeployRemoveArtifact = true;
    
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
     * Changes whether artifacts shall be removed from the platform on undeployment.
     * 
     * @param onUndeployRemoveArtifact {@code true} for removal, {@code false} else
     */
    public void setOnUndeployRemoveArtifact(boolean onUndeployRemoveArtifact) {
        this.onUndeployRemoveArtifact = onUndeployRemoveArtifact;
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
