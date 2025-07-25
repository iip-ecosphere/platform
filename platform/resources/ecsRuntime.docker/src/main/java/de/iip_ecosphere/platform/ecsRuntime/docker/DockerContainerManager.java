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
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import de.iip_ecosphere.platform.ecsRuntime.AbstractContainerManager;
import de.iip_ecosphere.platform.ecsRuntime.EcsSetup;
import de.iip_ecosphere.platform.ecsRuntime.ContainerManager;
import de.iip_ecosphere.platform.ecsRuntime.ContainerState;
import de.iip_ecosphere.platform.ecsRuntime.EcsFactory;
import de.iip_ecosphere.platform.ecsRuntime.EcsFactoryDescriptor;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.net.UriResolver;
import de.iip_ecosphere.platform.support.identities.IdentityStore;
import de.iip_ecosphere.platform.support.identities.IdentityToken;
import de.iip_ecosphere.platform.support.identities.IdentityToken.TokenType;
import de.iip_ecosphere.platform.support.net.NetworkManager;
import de.iip_ecosphere.platform.support.net.NetworkManagerFactory;

/**
 * Implements a docker-based container manager for IIP-Ecosphere.
 * 
 * @author Monika Staciwa, SSE
 */
public class DockerContainerManager extends AbstractContainerManager<DockerContainerDescriptor> {

    private static DockerSetup config = DockerSetup.readFromYaml();
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerContainerManager.class);
    
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
        public EcsSetup getConfiguration() {
            return config;
        }
        
    }
    
    /**
     * Tries to obtain an authentication config from {@link IdentityStore}.
     * 
     * @param authKey the authentication key, may be empty or <b>null</b> and is ignored then
     * @param registry the registry to authenticate for
     * @return the authentication config or <b>null</b> for none
     */
    private static AuthConfig getAuthConfig(String authKey, String registry) {
        AuthConfig authConfig = null;
        if (DockerSetup.isNotEmpty(authKey)) {
            IdentityToken tok = IdentityStore.getInstance().getToken(authKey);
            if (null == tok) {
                LoggerFactory.getLogger(DockerContainerManager.class).warn("Cannot find identity token for {} "
                    + "and registry {}. Falling back to no authentication.", authKey, registry);
            } else if (tok.getType() == TokenType.USERNAME) {
                authConfig = new AuthConfig()
                    .withRegistryAddress(registry)
                    .withUsername(tok.getUserName())
                    .withPassword(tok.getTokenDataAsString());
            } else {
                LoggerFactory.getLogger(DockerContainerManager.class).warn("Token for {} is of type "
                    + "{}. Can only handle USERNAME. Falling back to no authentication.", authKey, tok.getType());
            }
        }
        return authConfig;
    }
    
    /**
     * Tries to pull the container if a registry is given.
     * 
     * @param setup the docker setup
     * @param dockerClient the docker client instance to use
     * @param container the actual container to load/add/pull
     * @return the container id if it was pulled/is there
     * @throws ExecutionException if pulling the container failed
     */
    private String tryPull(DockerSetup setup, DockerClient dockerClient, DockerContainerDescriptor container) 
        throws ExecutionException {
        String result = null;
        Docker dockerCfg = setup.getDocker();
        if (DockerSetup.isNotEmpty(dockerCfg.getRegistry())) {
            String imageName = container.getDockerImageName();
            if (imageName.length() > 0) {
                boolean internalRegistry = false;
                String registry = DockerContainerDescriptor.getRegistry(imageName); // R30.c
                if (DockerSetup.isNotEmpty(dockerCfg.getRegistry()) && !DockerSetup.isNotEmpty(registry)) {
                    registry = dockerCfg.getRegistry();
                    internalRegistry = true;
                }
                PullImageCmd cmd = dockerClient.pullImageCmd(imageName)
                    .withRegistry(registry);
                String tag = DockerContainerDescriptor.getTag(imageName);
                if (DockerSetup.isNotEmpty(tag)) {
                    cmd.withTag(tag);
                }
                AuthConfig authConfig = getAuthConfig(internalRegistry 
                    ? dockerCfg.getAuthenticationKey() : registry, registry);
                if (null != authConfig) {
                    cmd.withAuthConfig(authConfig);
                }
                try {
                    cmd.exec(new PullImageResultCallback()).awaitCompletion();
                } catch (InterruptedException e) {
                    throw new ExecutionException(e);
                }
                String dockerImageName = getImageName(container);
                String containerName = container.getName().trim().replaceAll("\\s", "_");

                ArrayList<String> exposedPorts = container.getExposedPorts();
                int port = 0;
                int port1 = 0;
                for (String portString : exposedPorts) {
                    if (portString.contains("TCP")) {
                        if (portString.contains("${port}")) {
                            continue;
                        }
                        port = Integer.parseInt(portString.substring(0, portString.indexOf("/")));
                    } else if (portString.contains("iip.port.svgMgr")) {
                        if (portString.contains("${port_1}")) {
                            continue;
                        }
                        port1 = Integer.parseInt(portString.substring(portString.indexOf("=") + 1));
                    }
                }
                if (port == 0) {
                    if (container.requiresPort(DockerContainerDescriptor.PORT_PLACEHOLDER)) {
                        // may be gone until used, limit then netMgr ports in setup
                        NetworkManager netMgr = NetworkManagerFactory.getInstance();
                        port = netMgr.obtainPort(container.getNetKey()).getPort();
                    }
                }
                if (port1 == 0) {
                    if (container.requiresPort(DockerContainerDescriptor.PORT_PLACEHOLDER_1)) {
                        // may be gone until used, limit then netMgr ports in setup
                        NetworkManager netMgr = NetworkManagerFactory.getInstance();
                        port1 = netMgr.obtainPort(container.getNetKey1()).getPort();
                    }
                }

                CreateContainerCmd cmdCreate = dockerClient.createContainerCmd(dockerImageName)
                        .withName(containerName);
                configure(cmdCreate, port, port1, container);
                cmdCreate.exec(); 
                
                result = imageName; // unsure, with/out version
            }
        }
        return result;
    }
    
    @Override
    public String addContainer(URI location) throws ExecutionException {
        String id = null;
        DockerContainerDescriptor container;
        LOGGER.info("Adding container at " + location + "...");
        try {
            DockerSetup setup = (DockerSetup) EcsFactory.getSetup();
            String downloadDirectory = setup.getDocker().getDownloadDirectory();
            File downloadDir = new File(downloadDirectory);
            
            // Getting information about Docker image from image-info.yml
            String pathToYaml = location.toString();
            if (pathToYaml.endsWith("/")) {
                pathToYaml += setup.getDocker().getDockerImageYamlFilename();
            }
            URI yamlURI = new URI(pathToYaml);
            File imageInfo = resolveUri(yamlURI, downloadDir);
            container = DockerContainerDescriptor.readFromYamlFile(imageInfo);

            String dockerId = null;
            DockerClient dockerClient = getDockerClient();
            if (dockerClient == null) {
                throwExecutionException("Adding container failed", "Could not connect to the Docker daemon");
            }

            dockerId = tryPull(setup, dockerClient, container);
            if (null == dockerId) { // fallback, try to load it directly via filename
                // Loading image
                String imageName = container.getDockerImageZipfile();
                int pos = pathToYaml.lastIndexOf('/');
                String pathToImage = pathToYaml.substring(0, pos + 1) + imageName;
                URI imageURI = new URI(pathToImage);
                File image = UriResolver.resolveToFile(imageURI, downloadDir);
                String downloadedImageZipfile = image.getPath();
                container.setDownloadedImageZipfile(downloadedImageZipfile);
                LOGGER.info("Loading image for " + location + " from " + image);
                InputStream in = new FileInputStream(image);
                dockerClient.loadImageCmd(in).exec();
                
                // Creating Docker container
                int port = 0;
                if (container.requiresPort(DockerContainerDescriptor.PORT_PLACEHOLDER)) {
                    // may be gone until used, limit then netMgr ports in setup
                    NetworkManager netMgr = NetworkManagerFactory.getInstance();
                    port = netMgr.obtainPort(container.getNetKey()).getPort();
                }
                int port1 = 0;
                if (container.requiresPort(DockerContainerDescriptor.PORT_PLACEHOLDER_1)) {
                    // may be gone until used, limit then netMgr ports in setup
                    NetworkManager netMgr = NetworkManagerFactory.getInstance();
                    port1 = netMgr.obtainPort(container.getNetKey1()).getPort();
                }
                String dockerImageName = getImageName(container);
                String containerName = container.getName().trim().replaceAll("\\s", "_");
                LOGGER.info("Creating container " + dockerImageName + " " + containerName);
                CreateContainerCmd cmd = dockerClient.createContainerCmd(dockerImageName)
                    .withName(containerName);
                configure(cmd, port, port1, container);
                cmd.exec(); 
                
                // Getting Docker id
                dockerId = getDockerId(containerName);
            }
            if (dockerId == null) {
                throwExecutionException("Adding container failed", "The Docker container id is null.");
            }
            LOGGER.info("Container " + dockerId + " is AVAILABLE");
            container.setDockerId(dockerId);
            container.setState(ContainerState.AVAILABLE);
            
            id = super.addContainer(container.getId(), container);
            LOGGER.info("Container " + container.getId() + " added");
        } catch (IOException e) {
            throwExecutionException("Adding container failed", e);
        } catch (URISyntaxException e) {
            throwExecutionException("Adding container failed", e);
        }
        LOGGER.info("Added container at " + location + "...");
        return id; 
    }
    
    /**
     * Validates and returns the docker image file name.
     * 
     * @param desc the descriptor to return the name from
     * @return the image file name, potentially turned to lower cases
     */
    private String getImageName(DockerContainerDescriptor desc) {
        return desc.getDockerImageName().toLowerCase(); // dont' add version, must comply with created image
    }

    /**
     * Configures the create container command.
     * 
     * @param cmd the command instance
     * @param port the port number for the AAS implementation server
     * @param port1 the second optional port number for the AAS implementation server
     * @param container the container descriptor
     */
    private void configure(CreateContainerCmd cmd, int port, int port1, DockerContainerDescriptor container) {
        List<String> env = container.instantiateEnv(port, port1);
        if (env.size() > 0) {
            cmd.withEnv(env);
        }
        List<ExposedPort> exPorts = container.instantiateExposedPorts(port, port1);
        if (exPorts.size() > 0) {
            cmd.withExposedPorts(exPorts);
        }
        // use default unless explicitly set
        if (container.getAttachStdIn()) {
            cmd.withAttachStdin(true);
        }
        if (container.getAttachStdOut()) {
            cmd.withAttachStdout(true);
        }
        if (container.getAttachStdErr()) {
            cmd.withAttachStderr(true);
        }
        if (container.getWithTty()) {
            cmd.withTty(true);
        }
        if (null != container.getNetworkMode()) {
            cmd.getHostConfig().withNetworkMode(container.getNetworkMode());
        }
        if (container.getDood()) {
            // DooD https://blog.nestybox.com/2019/09/14/dind.html#docker-out-of-docker-dood
            String host = config.getDocker().getDockerHost();
            cmd.withVolumes(new Volume(host + ":" + host));
        }
        if (container.getPrivileged()) {
            // https://www.docker.com/blog/docker-can-now-run-within-docker/
            cmd.getHostConfig().withPrivileged(true);
        }
    }

    // checkstyle: stop exception type check

    /**
     * Returns a Docker API Client.
     * If there is not running Docker daemon on the host it returns null.
     * 
     * @return DockerClient or <b>null</b> if no Docker daemon is running or if the docker {@code dockerHost} in the 
     *     configuration setup cannot be applied, e.g., a Linux socket path on Windows
     */
    public DockerClient getDockerClient() {
        DockerSetup config = DockerSetup.readFromYaml();
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
            LOGGER.warn("Obtaining Docker client(): " + e.getMessage());
            dockerClient = null;
        }
        return dockerClient;
    }

    // checkstyle: resume exception type check

    @Override
    public void startContainer(String id) throws ExecutionException {
        LOGGER.info("Starting container " + id);
        DockerContainerDescriptor container = getContainer(id, "id", "start");
        String containerName = container.getName().trim().replaceAll("\\s", "_");
        
        DockerClient dockerClient = getDockerClient();
        if (dockerClient == null) {
            throwExecutionException("Starting container failed", "Could not connect to the Docker daemon.");
        }
        
        setState(container, ContainerState.DEPLOYING);
        dockerClient.startContainerCmd(containerName).exec();
        setState(container, ContainerState.DEPLOYED);
        LOGGER.info("Container " + id + " started");
    }
    
    @Override
    public void stopContainer(String id) throws ExecutionException {
        LOGGER.info("Stopping container " + id);
        DockerContainerDescriptor container = getContainer(id, "id", "stop");
        String containerName = container.getName().trim().replaceAll("\\s", "_");
        
        DockerClient dockerClient = getDockerClient();
        if (dockerClient == null) {
            throwExecutionException("Stopping container failed", "Could not connect to the Docker daemon.");
        }
        setState(container, ContainerState.STOPPING);
        dockerClient.stopContainerCmd(containerName).exec();
        setState(container, ContainerState.STOPPED);
        LOGGER.info("Container " + id + " stopped");
    }

    @Override
    public void migrateContainer(String id, String resourceId) throws ExecutionException {
        // TODO implement, use super.migrateContainer
        // TODO must change host value in AAS!
    }

    @Override
    public void undeployContainer(String id) throws ExecutionException {
        LOGGER.info("Undeploying container " + id);
        DockerContainerDescriptor container = getContainer(id, "id", "undeploy");
        String containerName = container.getName().trim().replaceAll("\\s", "_");

        if (container.requiresPort(DockerContainerDescriptor.PORT_PLACEHOLDER)) {
            NetworkManager netMgr = NetworkManagerFactory.getInstance();
            netMgr.releasePort(container.getNetKey());
        }
        if (container.requiresPort(DockerContainerDescriptor.PORT_PLACEHOLDER_1)) {
            NetworkManager netMgr = NetworkManagerFactory.getInstance();
            netMgr.releasePort(container.getNetKey1());
        }
        
        DockerClient dockerClient = getDockerClient();
        if (dockerClient == null) {
            throwExecutionException("Undeploying container failed", "Could not connect to the Docker daemon.");
        }
        dockerClient.removeContainerCmd(containerName).exec();
        
        // Removing image from download directory
        DockerSetup config = (DockerSetup) EcsFactory.getSetup();
        if (config.getDocker().getDeleteWhenUndeployed()) {
            File downloadedImageZipfile = new File(container.getDownloadedImageZipfile());
            if (downloadedImageZipfile.exists()) {
                downloadedImageZipfile.delete();
            }
        }
        
        super.undeployContainer(id);
        LOGGER.info("Container " + id + " undeployed");
    }

    @Override
    public void updateContainer(String id, URI location) throws ExecutionException {
        // TODO implement (compare version)
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
            throwExecutionException("Getting container's id failed", "Could not connect to the Docker daemon.");
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
    public String getContainerSystemName() {
        return "Docker";
    }

    @Override
    public String getRuntimeName() {
        return "IIP-Ecosphere Docker ECS-Runtime"; // may override version to add docker/client version
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
