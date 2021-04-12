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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import de.iip_ecosphere.platform.ecsRuntime.AbstractContainerManager;
import de.iip_ecosphere.platform.ecsRuntime.ContainerManager;
import de.iip_ecosphere.platform.ecsRuntime.ContainerState;
import de.iip_ecosphere.platform.ecsRuntime.EcsFactoryDescriptor;
//import de.iip_ecosphere.platform.services.Version;
import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * Implements a docker-based container manager for IIP-Ecosphere.
 * 
 * @author Monika Staciwa, SSE
 */
public class DockerContainerManager extends AbstractContainerManager<DockerContainerDescriptor> {

    // Docker daemon listens for Docker Engine API on three different types of Socket: unix, tcp and fd.
    private static String dockerhost = "unix:///var/run/docker.sock";

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
        
    }
    
    @Override
    public String addContainer(URI location) throws ExecutionException {
        return null; // TODO implement, use super.addContainer(id, descriptor)
    }
    
    /**
     * Configures a Docker Client.
     * 
     * @return DockerClient
     */
    public DockerClient getDockerClient() {
        DockerClientConfig standardConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerhost).build(); 
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
        setState(getContainer(id, "id", "start"), ContainerState.DEPLOYED);
    }

    @Override
    public void stopContainer(String id) throws ExecutionException {
        DockerClient dockerClient = getDockerClient();     
        dockerClient.stopContainerCmd(id).exec();     
        setState(getContainer(id, "id", "stop"), ContainerState.STOPPED);
    }

    @Override
    public void migrateContainer(String id, String resourceId) throws ExecutionException {
        // TODO implement, use super.migrateContainer
        // TODO must change host value in AAS!
    }

    @Override
    public void undeployContainer(String id) throws ExecutionException {
        super.undeployContainer(id);
        DockerClient dockerClient = getDockerClient();
        dockerClient.removeContainerCmd(id).exec();          
        setState(getContainer(id, "id", "undeploy"), ContainerState.UNKNOWN);
    }

    @Override
    public void updateContainer(String id, URI location) throws ExecutionException {
        // TODO implement        
    }

    @Override
    public ContainerState getState(String id) {
        List<DockerContainerDescriptor> containers = (List<DockerContainerDescriptor>) this.getContainers();
        for (DockerContainerDescriptor container : containers) {
            String containerId = container.getId();
            if (containerId.equals(id)) {
                return container.getState();
            }
        }
        return null;
    }

    @Override
    public Set<String> getIds() {
        Set<String> ids = new HashSet<String>();
        List<DockerContainerDescriptor> containers = (List<DockerContainerDescriptor>) this.getContainers();
        for (DockerContainerDescriptor container : containers) {
            ids.add(container.getId());
        }
        return ids;
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
        case "Created":
            state = ContainerState.DEPLOYED;
            break;
        default :
            state = ContainerState.UNKNOWN;
            break;
        }
        return state;
    }
    
    @Override
    public Collection<DockerContainerDescriptor> getContainers() {
        List<DockerContainerDescriptor> containers = new ArrayList<DockerContainerDescriptor>();
        
        Runtime rt = Runtime.getRuntime();
        String command = "docker container ls -a";
        try {
            Process proc = rt.exec(command);
            BufferedReader stdInput = new BufferedReader(new 
                 InputStreamReader(proc.getInputStream()));

            BufferedReader stdError = new BufferedReader(new 
                 InputStreamReader(proc.getErrorStream()));
            
            // Read the output from the command
            String line = null;
            while (true) {
                line = stdInput.readLine();
                if (line == null) {
                    break;
                }
                
                // Output to parse:
                // CONTAINER ID        IMAGE                    COMMAND                  CREATED             STATUS    
                // 8f6983acd81a        arvindr226/alpine-ssh    "/usr/sbin/sshd -D"      3 weeks ago         Up 3 secon
                
                // Skipping the header
                if (line.substring(0, 12).equals("CONTAINER ID")) {
                    continue;
                }
                int lineLength = line.length();
                String id = line.substring(0, 12).trim();
                String dockerState = line.substring(90, 117).trim();
                ContainerState state = convertDockerContainerState(dockerState);
                String conName = line.substring(138, lineLength).trim();
                Version version = new Version("1.0"); // TODO using default version for now
                
                DockerContainerDescriptor containerDescriptor = new DockerContainerDescriptor(id, conName, version);
                containerDescriptor.setState(state);
                containers.add(containerDescriptor);
                    
            }
            // Read any errors from the attempted command
            while ((line = stdError.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        return containers;
    }

    @Override
    public DockerContainerDescriptor getContainer(String id) {
        List<DockerContainerDescriptor> containers = (List<DockerContainerDescriptor>) this.getContainers();
        int containerNumber = containers.size();
        for (int i = 0; i < containerNumber; i++) {
            DockerContainerDescriptor container = containers.get(i);
            String containerId = container.getId();
            if (containerId.equals(id)) {
                return container;
            }
        }
        return null;
    }

    @Override
    public String getContainerSystemName() {
        // TODO is es ok so?
        return "Docker";
    }

    @Override
    public String getContainerSystemVersion() {
        // TODO implement
        return null;
    }
}
