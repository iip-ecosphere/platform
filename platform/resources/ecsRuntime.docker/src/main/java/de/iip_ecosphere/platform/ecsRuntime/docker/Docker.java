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

import org.apache.commons.lang.SystemUtils;

import de.iip_ecosphere.platform.ecsRuntime.EcsSetup.AbstractManagerSetup;

/**
 * Implements the docker specific configuration.
 * 
 * @author Monika Staciwa, SSE
 *
 */
public class Docker extends AbstractManagerSetup {

    // http://localhost:2375 does not seem to work although discussed as solution
    private String dockerHost = SystemUtils.IS_OS_WINDOWS 
        ? "unix:///var/run/docker.sock" : "unix:///var/run/docker.sock";
    private String dockerImageYamlFilename = "image-info.yml";
    private boolean deleteWhenUndeployed = false;
    private String downloadDirectory;
    private String registry;
    
    /**
     * Returns the docker host.
     * 
     * @return the docker host as Docker host string, e.g., unix:///var/run/docker.sock
     */
    public String getDockerHost() {
        return dockerHost;
    }
    
    /**
     * Defines the docker host. [required by SnakeYaml]
     * 
     * @param dockerHost the docker host as Docker host string, e.g., unix:///var/run/docker.sock
     */
    public void setDockerHost(String dockerHost) {
        this.dockerHost = dockerHost;
    }
    
    /**
     * Returns the name of the Yaml file with information about the Docker Image.
     * 
     * @return Name of the Yaml file
     */
    public String getDockerImageYamlFilename() {
        return this.dockerImageYamlFilename;
    }
    
    /**
     * Defines the standard name of the Yaml file with a information about the Docker Image.
     * 
     * @param filename the name of the Yaml file
     */
    public void setDockerImageYamlFilename(String filename) {
        this.dockerImageYamlFilename = filename;
    }
    
    /**
     * Returns True if Docker files should be removed when the corresponding container
     * gets undeployed. Otherwise it returns False.
     *  
     * @return True/False
     */
    public boolean getDeleteWhenUndeployed() {
        return this.deleteWhenUndeployed;
    }
    /**
     * Defines if Docker files should be deleted when the corresponding container 
     * gets undeployed. [required by SnakeYaml]
     * 
     * @param deleteWhenUndeployed True or False
     */
    public void setDeleteWhenUndeployed(boolean deleteWhenUndeployed) {
        this.deleteWhenUndeployed = deleteWhenUndeployed;
    }

    /**
     * Defines the download directory. [required by SnakeYaml]
     * @param directory
     */
    public void setDownloadDirectory(String directory) {
        this.downloadDirectory = directory;
    }
    
    /**
     * Returns the download directory. If the configured download directory is <b>null</b> or empty, it returns 
     * the system temporary directory.
     * @return directory, the system temporary directory if none is specified
     */
    public String getDownloadDirectory() {
        if (this.downloadDirectory == null || this.downloadDirectory.length() == 0) {
            return System.getProperty("java.io.tmpdir");
        }
        return this.downloadDirectory;
    }

    /**
     * Returns the host/port of the docker registry to use.
     * 
     * @return the registry, e.g., localhost:5050; may be <b>null</b> or empty
     */
    public String getRegistry() {
        return registry;
    }
    
    /**
     * Defines the host/port of the docker registry to use. [required by SnakeYaml]
     * 
     * @param registry the registry, e.g., localhost:5050
     */
    public void setRegistry(String registry) {
        this.registry = registry;
    }

}
