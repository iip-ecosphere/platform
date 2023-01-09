/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.ecsRuntime.lxc;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.jcloud.lxd.bean.LxdServerCredential;
import au.com.jcloud.lxd.enums.ContainerStateAction;
import au.com.jcloud.lxd.enums.RemoteServer;
import au.com.jcloud.lxd.model.Container;
import au.com.jcloud.lxd.service.ILinuxCliService;
import au.com.jcloud.lxd.service.ILxdApiService;
import au.com.jcloud.lxd.service.ILxdService;
import au.com.jcloud.lxd.service.impl.LinuxCliServiceImpl;
import au.com.jcloud.lxd.service.impl.LxdApiServiceImpl;
import au.com.jcloud.lxd.service.impl.LxdServiceImpl;
import de.iip_ecosphere.platform.ecsRuntime.AbstractContainerManager;
import de.iip_ecosphere.platform.ecsRuntime.EcsSetup;
import de.iip_ecosphere.platform.ecsRuntime.ContainerManager;
import de.iip_ecosphere.platform.ecsRuntime.EcsFactory;
import de.iip_ecosphere.platform.ecsRuntime.EcsFactoryDescriptor;
import de.iip_ecosphere.platform.support.net.UriResolver;

/**
 * Implements a lxc-based container manager for IIP-Ecosphere.
 * 
 * @author Luca Schulz, SSE
 */
public class LxcContainerManager extends AbstractContainerManager<LxcContainerDescriptor> {

    private static LxcSetup config = LxcSetup.readFromYaml();
    private static final Logger LOGGER = LoggerFactory.getLogger(LxcContainerManager.class);

    /**
     * Implements the factory descriptor for hooking the LXC container manager into
     * the ECS factory.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class FactoryDescriptor implements EcsFactoryDescriptor {

        @Override
        public ContainerManager createContainerManagerInstance() {
            return new LxcContainerManager();
        }

        @Override
        public EcsSetup getConfiguration() {
            return config;
        }

    }

    /**
     * Adds a container from a selected repository that was defined in a YAML-File
     * to the local repository.
     * 
     * @param location, the location where the Yaml with container info is
     * @return containerName, name of the created container
     **/
    @Override
    public String addContainer(URI location) throws ExecutionException {

        String containerName = null;

        LxcContainerDescriptor container;
        LOGGER.info("Adding container at " + location + "...");
        try {
            FactoryDescriptor factory = new FactoryDescriptor();
            LxcSetup setup = (LxcSetup) factory.getConfiguration();
            String downloadDirectory = setup.getLxc().getDownloadDirectory();
            File downloadDir = new File(downloadDirectory);

            // Getting information about LXC image from image-info.yml
            String pathToYaml = location.toString();
            if (pathToYaml.endsWith("/")) {
                pathToYaml += setup.getLxc().getLxcImageYamlFilename();
            }
            URI yamlURI = new URI(pathToYaml);
            File imageInfo = UriResolver.resolveToFile(yamlURI, downloadDir);
            container = LxcContainerDescriptor.readFromYamlFile(imageInfo);

            ILinuxCliService lxcCliService = new LinuxCliServiceImpl();
            ILxdService lxcClient = getLxcClient();
            if (lxcClient == null) {
                throwExecutionException("Adding container failed", "Could not connect to the LXC daemon");
            }

            // Creating LXC container
            String lxcImageName = getImageAlias(container);
            String lxcImageFingerprint = getLxcId(container);
            containerName = container.getName().trim().replaceAll("\\s", "_");

            if (getLxcId(container) == null && !lxcImageName.contains(":")) {
                lxcImageFingerprint = lxcClient.loadImageMap().get(lxcImageName).getFingerprint();
            }

            /**
             * 
             * First IF for container creation with local image if fingerprint was found
             * Second IF for container creation with remote image, should be removed if CURL
             * can be corrected.
             * 
             **/
            if (lxcClient.loadContainer(containerName) == null && lxcImageFingerprint != null) {
                LOGGER.info("Creating container from " + lxcImageName + " as " + containerName);
                lxcClient.createContainer(containerName, lxcImageFingerprint);
            } else if (lxcClient.loadContainer(containerName) == null && lxcImageFingerprint == null) {
                LOGGER.info("Creating container from " + lxcImageName + " as " + containerName);
                lxcCliService.executeLinuxCmd("lxc init " + lxcImageName + " " + containerName);
            } else {
                LOGGER.info("Container with the same name already exists " + "or Remote Server cant be reached");
            }
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throwExecutionException("Adding container failed", e);
        }
        return containerName;
    }

    /**
     * 
     * Adds a container from a tarball that can be created with distrobuilder and a
     * template or via exporting an image.
     * 
     * @see maybe add function to create tarball with distrobuilder in this method
     * @param location, the location where the Yaml with container info is
     * 
     * @return containerName, the name of the created container
     * 
     **/
    public String addContainerFromTarball(URI location) throws ExecutionException {

        String workingDir = System.getProperty("user.dir");
        String homeDir = workingDir + "/src/test/resources/";

        String containerName = null;

        LxcContainerDescriptor container;
        LOGGER.info("Adding container at " + location + "...");
        try {
            FactoryDescriptor factory = new FactoryDescriptor();
            LxcSetup setup = (LxcSetup) factory.getConfiguration();
            String downloadDirectory = setup.getLxc().getDownloadDirectory();
            File downloadDir = new File(downloadDirectory);

            // Getting information about LXC image from image-info-template.yml
            String pathToYaml = location.toString();
            if (pathToYaml.endsWith("/")) {
                pathToYaml += "image-info-template.yml";
            }
            System.out.println("Path to YAML: " + pathToYaml);
            URI yamlURI = new URI(pathToYaml);
            File imageInfo = UriResolver.resolveToFile(yamlURI, downloadDir);
            container = LxcContainerDescriptor.readFromYamlFile(imageInfo);

            ILxdService lxcClient = getLxcClient();
            if (lxcClient == null) {
                throwExecutionException("Adding container failed", "Could not connect to the LXC daemon");
            }

            // Creating LXC container
            ILinuxCliService lxcCliService = new LinuxCliServiceImpl();
            String lxcImageName = getImageAlias(container);
            String lxcImageTar = homeDir + getZip(container);
            containerName = container.getName().trim().replaceAll("\\s", "_");
            String fingerprint = null;
            String executed = null;

            /**
             * First check if image with same name already exists than try importing image
             * if image with same fingerprint but different name exists executed is empty.
             * 
             **/

            if (!lxcClient.loadImageAliasMap().containsKey(lxcImageName)) {
                LOGGER.info("Trying to import image " + lxcImageName);
                executed = lxcCliService
                        .executeLinuxCmd("lxc image import " + lxcImageTar + " --alias " + lxcImageName);
                if (!executed.isEmpty()) {
                    fingerprint = lxcClient.loadImageMap().get(lxcImageName).getFingerprint();
                } else {
                    LOGGER.info("Image with different name but same fingerprint already exists.");
                }
            } else {
                LOGGER.info("Image with same name already exists");
            }

            /**
             * If image name and fingerprint didn't exist container creation is started.
             **/
            if (fingerprint != null) {

                if (lxcClient.loadContainer(containerName) == null) {
                    LOGGER.info("Creating container: " + containerName);
                    lxcClient.createContainer(containerName, fingerprint);
                } else {
                    LOGGER.info("Container with the same name already exists");
                }
            }
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throwExecutionException("Adding container failed", e);
        }
        return containerName;
    }

    /**
     * Validates and returns the LXC image file name.
     * 
     * @param desc the descriptor to return the name from
     * @return the image name, potentially turned to lower cases
     */
    private String getImageAlias(LxcContainerDescriptor desc) {
        return desc.getLxcImageAlias(); // dont' add version, must comply with created image
    }

    /**
     * Validates and returns the LXC image file name.
     * 
     * @param desc the descriptor to return the name from
     * @return the image zip file name, potentially turned to lower cases
     */
    private String getZip(LxcContainerDescriptor desc) {
        return desc.getLxcZip();
    }

    /**
     * Validates and returns the LXC image file name.
     * 
     * @param desc the descriptor to return the name from
     * @return the image zip file name, potentially turned to lower cases
     */
    private String getLxcId(LxcContainerDescriptor desc) {
        return desc.getId();
    }

    /**
     * Returns a LXC API Client. If there is not running LXC daemon on the host it
     * returns null.
     * 
     * @return LxcClient or <b>null</b> if no LXC daemon is running or if the lxc
     *         {@code lxcHost} in the configuration setup cannot be applied, e.g., a
     *         Linux socket path
     */
    public ILxdService getLxcClient() {
        LxcSetup config = LxcSetup.readFromYaml();
        String lxchost = config.getLxc().getLxcHost();
        String lxcport = config.getLxc().getLxcPort();

        ILxdService service = new LxdServiceImpl();
        ILxdApiService lxdApiService = new LxdApiServiceImpl();
        ILinuxCliService linuxCliService = new LinuxCliServiceImpl();
        lxdApiService.setLinuxCliService(linuxCliService);
        service.setLxdApiService(lxdApiService);

        LxdServerCredential credential = new LxdServerCredential(lxchost + ":" + lxcport,
                System.getProperty("snap_cert"), System.getProperty("snap_key"));
        service.setLxdServerCredential(credential);

        try {
            service.loadServerInfo();
        } catch (Exception e) {
            LOGGER.warn("Obtaining LXC client(): " + e.getMessage());
            service = null;
        }
        return service;
    }

    /**
     * Starts an existing container with given name.
     * 
     * @param name, the name of the container
     * 
     **/
    @Override
    public void startContainer(String name) throws ExecutionException {
        LOGGER.info("Starting container " + name);
        // Um die States des Containers zu aktualisieren muessen passende LXC States
        // hinzugefuegt werden
        // LxcContainerDescriptor container = getContainer(name, "name", "start");

        ILxdService lxcClient = getLxcClient();
        if (lxcClient == null) {
            throwExecutionException("Starting container failed", "Could not connect to the LXC daemon.");
        }

        try {
            lxcClient.startContainer(name);
        } catch (IOException | InterruptedException e) {
            throwExecutionException("Starting container failed", e);
        }
        LOGGER.info("Container " + name + " started");
    }

    /**
     * Stops an existing container with given name.
     * 
     * @param name, the name of the container
     * 
     **/
    @Override
    public void stopContainer(String name) throws ExecutionException {
        LOGGER.info("Stopping container " + name);
        // LxcContainerDescriptor container = getContainer(name, "name", "start");

        ILxdService lxcClient = getLxcClient();
        if (lxcClient == null) {
            throwExecutionException("Stopping container failed", "Could not connect to the LXC daemon.");
        }

        try {
            lxcClient.stopContainer(name);
        } catch (IOException | InterruptedException e) {
            throwExecutionException("Stopping container failed", e);
        }
        LOGGER.info("Container " + name + " stopped");
    }

    /**
     * Deletes an existing container with given name.
     * 
     * @param name, the name of the container
     * 
     **/
    public void deleteContainer(String name) throws ExecutionException {
        LOGGER.info("Deleting container " + name);
        // Um die States des Containers zu aktualisieren muessen passende LXC States
        // hinzugefuegt werden
        // LxcContainerDescriptor container = getContainer(name, "name", "start");

        ILxdService lxcClient = getLxcClient();
        if (lxcClient == null) {
            throwExecutionException("Deleting container failed", "Could not connect to the LXC daemon.");
        }

        try {
            lxcClient.deleteContainer(name);
        } catch (IOException | InterruptedException e) {
            throwExecutionException("Deleting container failed", e);
        }
        LOGGER.info("Container " + name + " deleted");
    }

    /**
     * Freezes an existing container with given name.
     * 
     * @param name, the name of the container
     * 
     **/
    public void freezeContainer(String name) throws ExecutionException {
        LOGGER.info("Freezing container " + name);
        // Um die States des Containers zu aktualisieren muessen passende LXC States
        // hinzugefuegt werden
        // LxcContainerDescriptor container = getContainer(name, "name", "start");

        ILxdService lxcClient = getLxcClient();
        if (lxcClient == null) {
            throwExecutionException("Freezing container failed", "Could not connect to the LXC daemon.");
        }

        try {
            lxcClient.changeContainerState(name, ContainerStateAction.FREEZE, false, false, "");
        } catch (IOException | InterruptedException e) {
            throwExecutionException("Freezing container failed", e);
        }
        LOGGER.info("Container " + name + " freezed");
    }

    /**
     * Unfreezes an existing container with given name.
     * 
     * @param name, the name of the container
     * 
     **/
    public void unfreezeContainer(String name) throws ExecutionException {
        LOGGER.info("Unfreezing container " + name);
        // Um die States des Containers zu aktualisieren muessen passende LXC States
        // hinzugefuegt werden
        // LxcContainerDescriptor container = getContainer(name, "name", "start");

        ILxdService lxcClient = getLxcClient();
        if (lxcClient == null) {
            throwExecutionException("Unfreezing container failed", "Could not connect to the LXC daemon.");
        }

        try {
            lxcClient.changeContainerState(name, ContainerStateAction.UNFREEZE, false, false, "");
        } catch (IOException | InterruptedException e) {
            throwExecutionException("Unfreezing container failed", e);
        }
        LOGGER.info("Container " + name + " unfreezed");
    }

    /**
     * Creates a snapshot of a container with given name.
     * 
     * @param containerName, the name of the container
     * @param snapshot,      the name that the snapshot is going to get
     * 
     **/
    public void createSnapshot(String containerName, String snapshot) throws ExecutionException {
        LOGGER.info("Creating snapshot of: " + containerName);
        ILxdService lxcClient = getLxcClient();

        if (lxcClient == null) {
            throwExecutionException("Creating snapshot failed", "Could not connect to the LXC daemon.");
        }

        try {
            lxcClient.createSnapshot(containerName, snapshot);
        } catch (IOException | InterruptedException e) {
            throwExecutionException("Creating snapshot failed", e);
        }
        LOGGER.info("Snapshot of: " + containerName + " created");
    }

    /**
     * Publishes an container and the corresponding snapshot as an image to the
     * local server.
     * 
     * @param containerName, the name of the container
     * @param snapshot,      the name of the corresponding snapshot
     * @param imageAlias,    the name that the image is going to get
     * @param isPublic,      defines if the image should be public or not
     * 
     **/
    public void publishImage(String containerName, String snapshot, String imageAlias, boolean isPublic)
            throws ExecutionException {
        LOGGER.info("Publishing image of: " + containerName);
        ILxdService lxcClient = getLxcClient();

        if (lxcClient == null) {
            throwExecutionException("Publishing image failed", "Could not connect to the LXC daemon.");
        }

        ILinuxCliService lxcCliService = new LinuxCliServiceImpl();
        try {
            if (isPublic) {
                lxcCliService.executeLinuxCmd(
                        "lxc publish " + containerName + "/" + snapshot + " --alias " + imageAlias + " --public");
            } else {
                lxcCliService
                        .executeLinuxCmd("lxc publish " + containerName + "/" + snapshot + " --alias " + imageAlias);
            }
        } catch (IOException | InterruptedException e) {
            throwExecutionException("Publishing image failed", e);
        }
        LOGGER.info("Image of: " + containerName + " created");
    }

    /**
     * Exports an existing image as a tarball to the users home directory.
     * 
     * @param imageAlias, the name the image that is going to be exported
     * 
     **/
    public void exportImage(String imageAlias) throws ExecutionException {

        String homeDir = System.getProperty("user.home");

        LOGGER.info("Exporting image : " + imageAlias);
        ILxdService lxcClient = getLxcClient();

        if (lxcClient == null) {
            throwExecutionException("Exporting image failed", "Could not connect to the LXC daemon.");
        }

        ILinuxCliService lxcCliService = new LinuxCliServiceImpl();
        try {

            lxcCliService.executeLinuxCmd("lxc image export " + imageAlias + " " + homeDir + "/" + imageAlias);

        } catch (IOException | InterruptedException e) {
            throwExecutionException("Exporting image failed", e);
        }
        LOGGER.info("Image : " + imageAlias + " exported to " + homeDir);
    }

    /**
     * Imports an image from an existing tarball to the local server.
     * 
     * @param tarball,    the path to the tarball/gzip file
     * @param imageAlias, the name that the image is going to get
     * 
     **/
    public void importImage(String tarball, String imageAlias) throws ExecutionException {

        LOGGER.info("Importing image as : " + imageAlias);
        ILxdService lxcClient = getLxcClient();

        if (lxcClient == null) {
            throwExecutionException("Importing image failed", "Could not connect to the LXC daemon.");
        }

        ILinuxCliService lxcCliService = new LinuxCliServiceImpl();
        try {

            lxcCliService.executeLinuxCmd("lxc image import " + tarball + " --alias " + imageAlias);

        } catch (IOException | InterruptedException e) {
            throwExecutionException("Importing image failed", e);
        }
        LOGGER.info("Image from: " + tarball + " imported as " + imageAlias);
    }

    /**
     * Copy/import an image from a remote server that is defined in
     * {@coder RemoteServer.java} and set up on localhost.
     * 
     * @param remote,      the RemoteServer where the wanted image is stored
     * @param remoteImage, the name of the image that is going to be copied
     * @param imageAlias,  the name that the image is going to get
     * 
     **/
    public void copyImageFromRemote(RemoteServer remote, String remoteImage, String imageAlias)
            throws ExecutionException {

        LOGGER.info("Copying Image from : " + remote.getName());
        ILxdService lxcClient = getLxcClient();

        if (lxcClient == null) {
            throwExecutionException("Copying image failed", "Could not connect to the LXC daemon.");
        }

        ILinuxCliService lxcCliService = new LinuxCliServiceImpl();
        try {

            lxcCliService.executeLinuxCmd(
                    "lxc image copy " + remote.getName() + ":" + remoteImage + " local:" + " --alias " + imageAlias);

        } catch (IOException | InterruptedException e) {
            throwExecutionException("Copying image failed", e);
        }
        LOGGER.info("Image from: " + remote.getName() + " imported as " + imageAlias);
    }

    /**
     * Returns the ContainerSystemName.
     * 
     * @return ContainerSystemName e.g. "LXC"
     **/
    @Override
    public String getContainerSystemName() {
        return "LXC";
    }

    /**
     * Returns the ContainerSystemVersion.
     * 
     * @return ContainerSystemVersion
     **/
    @Override
    public String getContainerSystemVersion() {
        LxcContainerManager cm = (LxcContainerManager) EcsFactory.getContainerManager();
        ILxdService lxcClient = cm.getLxcClient();
        if (lxcClient == null) {
            return "";
        }

        return "";
    }

    /**
     * This method is not relevant for LXC.
     **/
    @Override
    public void updateContainer(String id, URI location) throws ExecutionException {
        // TODO Auto-generated method stub

    }

    /**
     * Returns an name of a LXC container with a given {@code name}.
     * 
     * @param name container's name
     * @return LXC container id/NULL
     * @throws ExecutionException if connecting to LXC API Client failed
     */
    public String getLxcName(String name) throws ExecutionException {
        ILxdService lxcClient = this.getLxcClient();

        if (lxcClient == null) {
            throwExecutionException("Getting container's id failed", "Could not connect to the LXC daemon.");
        }
        // Getting list of all container that LXC "knows".
        Collection<Container> containers = null;
        try {
            containers = lxcClient.loadContainerMap().values();
            if (containers.size() == 0) {
                return null;
            }
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Looking for the container with a given name.
        for (Container container : containers) {
            if (container.getName().equals(name)) {
                return container.getName();
            }
        }
        return null;
    }

}
