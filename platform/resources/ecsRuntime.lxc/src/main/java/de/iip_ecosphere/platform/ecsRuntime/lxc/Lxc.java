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

import org.apache.commons.lang.SystemUtils;

/**
 * Implements the docker specific configuration.
 * 
 * @author Monika Staciwa, SSE
 *
 */
public class Lxc {
    
    // TODO unify common parts with Docker -> ecsRuntime???

    // http://localhost:2375 does not seem to work although discussed as solution
    private String dockerHost = SystemUtils.IS_OS_WINDOWS 
        ? "unix:///var/run/docker.sock" : "unix:///var/run/docker.sock";
    private String lxcImageYamlFilename = "image-info.yml";
    private boolean deleteWhenUndeployed = false;
    private String downloadDirectory;
    
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
     * Returns the name of the Yaml file with information about the LXC Image.
     * 
     * @return Name of the Yaml file
     */
    public String getLxcImageYamlFilename() {
        return this.lxcImageYamlFilename;
    }
    
    /**
     * Defines the standard name of the Yaml file with a information about the LXC Image.
     * 
     * @param filename the name of the Yaml file
     */
    public void setLxcImageYamlFilename(String filename) {
        this.lxcImageYamlFilename = filename;
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
    
}
