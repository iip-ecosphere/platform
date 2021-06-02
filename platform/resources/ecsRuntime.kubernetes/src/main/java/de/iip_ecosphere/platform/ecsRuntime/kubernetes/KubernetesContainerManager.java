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

package de.iip_ecosphere.platform.ecsRuntime.kubernetes;

import java.net.URI;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.ecsRuntime.AbstractContainerManager;
import de.iip_ecosphere.platform.ecsRuntime.Configuration;
import de.iip_ecosphere.platform.ecsRuntime.ContainerManager;
import de.iip_ecosphere.platform.ecsRuntime.ContainerState;
import de.iip_ecosphere.platform.ecsRuntime.EcsFactoryDescriptor;

/**
 * Implements a docker-based container manager for IIP-Ecosphere.
 * 
 * @author Ahmad Alomosh, SSE
 */
public class KubernetesContainerManager extends AbstractContainerManager<KubernetesContainerDescriptor> {

    private static KubernetesConfiguration config = KubernetesConfiguration.readFromYaml();
    
    // don't change name of outer/inner class
    // TODO upon start, scan file-system for containers and add them automatically if applicable
    
    /**
     * Implements the factory descriptor for hooking the Docker container manager 
     * into the ECS factory.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class FactoryDescriptor implements EcsFactoryDescriptor {

        @Override
        public ContainerManager createContainerManagerInstance() {
            return new KubernetesContainerManager();
        }
        
        @Override
        public Configuration getConfiguration() {
            return config;
        }
        
    }
    
    @Override
    public String addContainer(URI location) throws ExecutionException {
        // TODO use UriResolver.resolveToFile(yamlURI, null);
        // TODO call super.addContainer
        // TODO return container id
        return ""; 
    }
    
    @Override
    public void startContainer(String id) throws ExecutionException {
        // TODO implement
    }
    
    @Override
    public void stopContainer(String id) throws ExecutionException {
        // TODO implement
    }

    @Override
    public void migrateContainer(String id, String resourceId) throws ExecutionException {
        // TODO implement, use super.migrateContainer
        // TODO must change host value in AAS!
    }

    @Override
    public void undeployContainer(String id) throws ExecutionException {
        // TODO implement
        super.undeployContainer(id);
    }

    @Override
    public void updateContainer(String id, URI location) throws ExecutionException {
        // TODO implement (compare version)
    }

    @Override
    public ContainerState getState(String id) {
        return super.getState(id);
    }

    @Override
    public Set<String> getIds() {
        return super.getIds();
    }
    
    @Override
    public Collection<KubernetesContainerDescriptor> getContainers() {
        return super.getContainers();
    }

    @Override
    public KubernetesContainerDescriptor getContainer(String id) {
        return super.getContainer(id);
    }

    @Override
    public String getContainerSystemName() {
        return "Kubernetes";
    }

    @Override
    public String getContainerSystemVersion() {
        // TODO implement
        return "";
    }
}
