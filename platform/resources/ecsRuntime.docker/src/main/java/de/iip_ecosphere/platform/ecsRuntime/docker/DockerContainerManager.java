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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import de.iip_ecosphere.platform.ecsRuntime.AbstractContainerManager;
import de.iip_ecosphere.platform.ecsRuntime.Configuration;
import de.iip_ecosphere.platform.ecsRuntime.ContainerManager;
import de.iip_ecosphere.platform.ecsRuntime.ContainerState;
import de.iip_ecosphere.platform.ecsRuntime.EcsFactory;
import de.iip_ecosphere.platform.ecsRuntime.EcsFactoryDescriptor;
import de.iip_ecosphere.platform.support.iip_aas.uri.UriResolver;
/**
 * Implements a docker-based container manager for IIP-Ecosphere.
 * 
 * @author Monika Staciwa, SSE
 */
public class DockerContainerManager extends AbstractContainerManager<DockerContainerDescriptor> {

    // Docker daemon listens for Docker Engine API on three different types of Socket: unix, tcp and fd.
    //private static String dockerhost = "unix:///var/run/docker.sock";
    private static DockerConfiguration config = DockerConfiguration.readFromYaml();
    //private static String standartDockerImageYamlFilename = "image-info.yml";
    
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
            return new DockerContainerManager();
        }
        
        @Override
        public Configuration getConfiguration() {
            return config;
        }
        
    }
    
    @Override
    public String addContainer(URI location) throws ExecutionException {
        // TODO version with zip (de.iip_ecosphere.platform.support.JarUtils)
        String id = null;
        
        FactoryDescriptor factory = new FactoryDescriptor();
        DockerConfiguration config = (DockerConfiguration) factory.getConfiguration();
        String pathToYamlFile = "file://" + location.getPath() + config.getDockerImageYamlFilename();
        DockerContainerDescriptor container;
        try {
            // Getting information about docker image from yaml file.
            URI yamlFileURI = new URI(pathToYamlFile);
            File yamlFile = UriResolver.resolveToFile(yamlFileURI, null);
            container = DockerContainerDescriptor.readFromYamlFile(yamlFile);
            String dockerImageZipfile = container.getDockerImageZipfile();
            
            // Loading a docker image
            String pathToDockerImageFile = "file://" + location.getPath() + dockerImageZipfile;
            URI dockerImageURI = new URI(pathToDockerImageFile);
            File dockerImageFile = UriResolver.resolveToFile(dockerImageURI, null);
            DockerClient dockerClient = getDockerClient();
            if (dockerClient == null) {
                throw new IOException("No running Docker daemon found. Adding a container failed.");
            }
            
            InputStream in = new FileInputStream(dockerImageFile);
            dockerClient.loadImageCmd(in).exec();
            
            // Creating a docker container
            String dockerImageName = container.getDockerImageName();
            String containerName = container.getName();
            // TODO throws exeception if container with given name already exists 
            // ( com.github.dockerjava.api.exception.ConflictException)
            dockerClient.createContainerCmd(dockerImageName).withName(containerName).exec(); 
            
            // Getting docker id
            String dockerId = getDockerId(containerName);
            container.setDockerId(dockerId);
            container.setState(ContainerState.AVAILABLE);
            
            id = super.addContainer(container.getId(), container);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return id; 
    }
    
    
    
    /**
     * Returns a Docker API Client.
     * If there is not running Docker daemon on the host it returns null.
     * 
     * @return DockerClient/NULL
     */
    public DockerClient getDockerClient() {
        DockerConfiguration config = DockerConfiguration.readFromYaml();
        String dockerhost = config.getDockerHost();
        DockerClientConfig standardConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerhost).build(); 
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(standardConfig.getDockerHost())
                .sslConfig(standardConfig.getSSLConfig())
                .build();
        DockerClient dockerClient = DockerClientImpl.getInstance(standardConfig, httpClient);
        try {
            dockerClient.infoCmd().exec();
        } catch (Exception e) {
            System.out.println("DockerContainerManager.getDockerClient() throws: " + e);
            return null;
        }
        return dockerClient;
    }
    
    @Override
    public void startContainer(String id) throws ExecutionException {
        DockerContainerDescriptor container = super.getContainer(id, "id", "start");
        String dockerId = container.getDockerId(); // TODO check if dockerId not null
        
        DockerClient dockerClient = getDockerClient();
        
        dockerClient.startContainerCmd(dockerId).exec();
        setState(container, ContainerState.DEPLOYED);
    }
    
    @Override
    public void stopContainer(String id) throws ExecutionException {
        DockerContainerDescriptor container = super.getContainer(id, "id", "stop");
        String dockerId = container.getDockerId();
        
        DockerClient dockerClient = getDockerClient();        
        dockerClient.stopContainerCmd(dockerId).exec();
        
        setState(container, ContainerState.STOPPED);
    }

    @Override
    public void migrateContainer(String id, String resourceId) throws ExecutionException {
        // TODO implement, use super.migrateContainer
        // TODO must change host value in AAS!
    }

    @Override
    public void undeployContainer(String id) throws ExecutionException {
        DockerContainerDescriptor container = getContainer(id);
        // Removing container from Docker
        String dockerId = container.getDockerId();
        DockerClient dockerClient = getDockerClient();
        dockerClient.removeContainerCmd(dockerId).exec();
        // Removing container from platform
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
    
    /**
     * Converts Docker's state of container (string) into a ContainerState.
     * 
     * @param dockerState Docker's status of container
     * @return state ContainerState
     */
    public static ContainerState convertDockerContainerState(String dockerState) {
        // Getting the first word in string - name of the state.
        String[] listOfWords = dockerState.split(" ");
        String dockerStateName = "";
        for (String word : listOfWords) {
            if (!word.equals("")) {
                dockerStateName = word;
                break;
            }
        }
        // Matching docker's state with IIP-Ecosphere platform's state.
        ContainerState state;
        switch(dockerStateName) {
        case "Up":
            state = ContainerState.AVAILABLE;
            break;
        case "Exited":
            state = ContainerState.STOPPED;
            break;
        case "Created": // TODO not sure about this
            state = ContainerState.DEPLOYED;
            break;
        default :
            state = ContainerState.UNKNOWN;
            break;
        }
        return state;
    }
    // TODO do I need it?
    @Override
    public Collection<DockerContainerDescriptor> getContainers() {
        return super.getContainers();
    }
    
    /** 
     * Returns an id of a Docker container with a given {@code name}.
     * @param name container's name
     * @return docker container id
     */
    public String getDockerId(String name) {
        DockerClient dockerClient = this.getDockerClient();
        ArrayList<Container> containers = (ArrayList<Container>) dockerClient.listContainersCmd()
                .withStatusFilter(Arrays.asList("created", "restarting", "running", "paused", "exited"))
                .withNameFilter(Arrays.asList(name))
                .exec();
        
        if (containers.size() == 0) {
            // TODO exception?
            return null;
        } 
        
        for (int i = 0; i < containers.size(); i++) {
            Container container = containers.get(i);
            String dockerName = container.getNames()[0];
            // removing the slash symbol before the name
            dockerName = dockerName.substring(1, dockerName.length());
            if (dockerName.equals(name)) {
                return container.getId();
            }
        }
        
        return null;
    }

    @Override
    public DockerContainerDescriptor getContainer(String id) {
        return super.getContainer(id);
    }

    @Override
    public String getContainerSystemName() {
        return "Docker";
    }

    @Override
    public String getContainerSystemVersion() throws IOException {
        DockerContainerManager cm = (DockerContainerManager) EcsFactory.getContainerManager();
        DockerClient dockerClient = cm.getDockerClient();
        if (dockerClient == null) {
            throw new IOException("No running Docker daemon found.");
        }
        Info dockerInfo = dockerClient.infoCmd().exec();
        String dockerServerVersion = dockerInfo.getServerVersion();
        return dockerServerVersion;
    }
}
