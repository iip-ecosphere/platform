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

    private static DockerConfiguration config = DockerConfiguration.readFromYaml();
    
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
        String id = null;
        DockerContainerDescriptor container;
        try {
            FactoryDescriptor factory = new FactoryDescriptor();
            DockerConfiguration config = (DockerConfiguration) factory.getConfiguration();
            
            // Getting information about Docker image from image-info.yml
            String pathToYaml = location.toString() + config.getDocker().getDockerImageYamlFilename();
            URI yamlURI = new URI(pathToYaml);
            File imageInfo = UriResolver.resolveToFile(yamlURI, null);
            container = DockerContainerDescriptor.readFromYamlFile(imageInfo);
                        
            // Loading image
            String imageName = container.getDockerImageZipfile();
            String pathToImage = location.toString() + imageName;
            URI imageURI = new URI(pathToImage);
            String downloadDirectory = container.getDownloadDirectory();
            File downloadDir = new File(downloadDirectory);
            File image = UriResolver.resolveToFile(imageURI, downloadDir);
            
            String downloadedImageZipfile = image.getPath();
            container.setDownloadedImageZipfile(downloadedImageZipfile);
            
            DockerClient dockerClient = getDockerClient();
            if (dockerClient == null) {
                throw new ExecutionException(
                        "Could not connect with the Docker daemon. Adding a container failed.", null);
            }
            
            InputStream in = new FileInputStream(image);
            dockerClient.loadImageCmd(in).exec();
            
            // Creating Docker container
            String dockerImageName = container.getDockerImageName();
            String containerName = container.getName();
            dockerClient.createContainerCmd(dockerImageName).withName(containerName).exec(); 
            
            // Getting Docker id
            String dockerId = getDockerId(containerName);
            if (dockerId == null) {
                throw new ExecutionException("The Docker container id is null.", null);
            }
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
    
    // checkstyle: stop exception type check
    
    /**
     * Returns a Docker API Client.
     * If there is not running Docker daemon on the host it returns null.
     * 
     * @return DockerClient/NULL
     */
    public DockerClient getDockerClient() {
        DockerConfiguration config = DockerConfiguration.readFromYaml();
        String dockerhost = config.getDocker().getDockerHost();
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

    // checkstyle: resume exception type check

    @Override
    public void startContainer(String id) throws ExecutionException {
        DockerContainerDescriptor container = super.getContainer(id, "id", "start");
        String dockerId = container.getDockerId();
        
        DockerClient dockerClient = getDockerClient();
        if (dockerClient == null) {
            throw new ExecutionException("Could not connect with the Docker daemon. Starting container failed.", null);
        }
        setState(container, ContainerState.DEPLOYING);
        dockerClient.startContainerCmd(dockerId).exec();
        setState(container, ContainerState.DEPLOYED);
    }
    
    @Override
    public void stopContainer(String id) throws ExecutionException {
        DockerContainerDescriptor container = super.getContainer(id, "id", "stop");
        String dockerId = container.getDockerId();
        
        DockerClient dockerClient = getDockerClient();
        if (dockerClient == null) {
            throw new ExecutionException("Could not connect with the Docker daemon. Stoping container failed.", null);
        }
        setState(container, ContainerState.STOPPING);
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
        String dockerId = container.getDockerId();
        
        DockerClient dockerClient = getDockerClient();
        if (dockerClient == null) {
            throw new ExecutionException(
                    "Could not connect with the Docker daemon. Undeploying container failed.", null);
        }
        dockerClient.removeContainerCmd(dockerId).exec();
        
        // Removing image from download directory
        FactoryDescriptor factory = new FactoryDescriptor();
        DockerConfiguration config = (DockerConfiguration) factory.getConfiguration();
        if (config.getDocker().getDeleteWhenUndeployed()) {
            File downloadedImageZipfile = new File(container.getDownloadedImageZipfile());
            if (downloadedImageZipfile.exists()) {
                downloadedImageZipfile.delete();
            }
        }
        
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
    public Collection<DockerContainerDescriptor> getContainers() {
        return super.getContainers();
    }
    
    /** 
     * Returns an id of a Docker container with a given {@code name}.
     * @param name container's name
     * @return docker container id/NULL
     * @throws ExecutionException if connecting to Docker API Client failed 
     */
    public String getDockerId(String name) throws ExecutionException {
        DockerClient dockerClient = this.getDockerClient();
        if (dockerClient == null) {
            throw new ExecutionException(
                    "Could not connect with the Docker daemon. Getting container's id failed.", null);
        }
        // Getting list of all container that Docker "knows".
        ArrayList<Container> containers = (ArrayList<Container>) dockerClient.listContainersCmd()
                .withStatusFilter(Arrays.asList("created", "restarting", "running", "paused", "exited"))
                .withNameFilter(Arrays.asList(name))
                .exec();
        
        if (containers.size() == 0) {
            return null;
        } 
        // Looking for the container with a given name.
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
    public String getContainerSystemVersion() {
        DockerContainerManager cm = (DockerContainerManager) EcsFactory.getContainerManager();
        DockerClient dockerClient = cm.getDockerClient();
        if (dockerClient == null) {
            return "";
        }
        Info dockerInfo = dockerClient.infoCmd().exec();
        String dockerServerVersion = dockerInfo.getServerVersion();
        return dockerServerVersion;
    }
}
