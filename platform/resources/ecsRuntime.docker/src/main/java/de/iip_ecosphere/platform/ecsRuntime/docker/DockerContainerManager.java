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

package de.iip_ecosphere.platform.ecsRuntime.docker;

import java.net.URI;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import de.iip_ecosphere.platform.ecsRuntime.ContainerDescriptor;
import de.iip_ecosphere.platform.ecsRuntime.ContainerManager;
import de.iip_ecosphere.platform.ecsRuntime.ContainerState;
import de.iip_ecosphere.platform.ecsRuntime.EcsFactoryDescriptor;

/**
 * Implements a docker-based container manager for IIP-Ecosphere.
 * 
 * @author Monika Staciwa, SSE
 */
public class DockerContainerManager implements ContainerManager {

    // don't change name of outer/inner class
    
    /**
     * Implements the factory descriptor for hooking the Docker container manager into the ECS factory.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class FactoryDescriptor implements EcsFactoryDescriptor {

        @Override
        public ContainerManager createContainerManagerInstance() {
            return new DockerContainerManager();
        }
        
    }
    
    @Override
    public String addContainer(URI location) throws ExecutionException {
        return null; // TODO implement
    }
    
    // Docker daemon listens for Docker Engine API on three different types of Socket: unix, tcp and fd.
    public static String DOCKER_HOST = "unix:///var/run/docker.sock";
    /**
     * This method configures a Docker Client.
     * 
     * @param dockerHost
     * @return DockerClient
     */
    public DockerClient getDockerClient() {
    	DockerClientConfig standardConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
        		.withDockerHost(DOCKER_HOST).build(); 
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
        	    .dockerHost(standardConfig.getDockerHost())
        	    .sslConfig(standardConfig.getSSLConfig())
        	    .build();
        DockerClient dockerClient = DockerClientImpl.getInstance(standardConfig, httpClient);
        return dockerClient;
    }
    
    @Override
    public void startContainer(String id) throws ExecutionException {
        DockerClient dockerClient = getDockerClient();     
        dockerClient.startContainerCmd(id).exec();
    }

    @Override
    public void stopContainer(String id) throws ExecutionException {
    	DockerClient dockerClient = getDockerClient();     
        dockerClient.stopContainerCmd(id).exec();     
    }

    @Override
    public void migrateContainer(String id, URI location) throws ExecutionException {
        // TODO implement        
    }

    @Override
    public void undeployContainer(String id) throws ExecutionException {
    	DockerClient dockerClient = getDockerClient();     
        dockerClient.removeContainerCmd(id).exec();          
    }

    @Override
    public void updateContainer(String id, URI location) throws ExecutionException {
        // TODO implement        
    }

    @Override
    public ContainerState getState(String id) {
        // TODO implement
        return null;
    }

    @Override
    public Set<String> getIds() {
        // TODO implement
        return null;
    }

    @Override
    public Collection<? extends ContainerDescriptor> getContainers() {
        // TODO implement
        return null;
    }

    @Override
    public ContainerDescriptor getContainer(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getContainerSystemName() {
        // TODO implement
        return null;
    }

    @Override
    public String getContainerSystemVersion() {
        // TODO implement
        return null;
    }
}
