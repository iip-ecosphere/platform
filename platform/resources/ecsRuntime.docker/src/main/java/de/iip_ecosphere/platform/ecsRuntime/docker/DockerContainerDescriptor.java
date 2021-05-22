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
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.yaml.snakeyaml.Yaml;

import de.iip_ecosphere.platform.ecsRuntime.AbstractContainerDescriptor;
import de.iip_ecosphere.platform.ecsRuntime.ContainerState;
import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * Implements a container descriptor for docker-based container managment.
 * 
 * @author Monika Staciwa, SSE
 */
public class DockerContainerDescriptor extends AbstractContainerDescriptor {
    
    private String dockerId;
    private String dockerImageName;
    private String dockerImageZipfile;
    private String downloadDirectory;
        
    /**
     * Creates a container descriptor instance.
     */
    public DockerContainerDescriptor() {
        
    }
    /**
     * Creates a container descriptor instance.
     * 
     * @param id the container id
     * @param name the (file) name of the container
     * @param version the version of the container
     * @throws IllegalArgumentException if id, name or version is invalid, i.e., null or empty
     */
    protected DockerContainerDescriptor(String id, String name, Version version) {
        super(id, name, version);
    }
    
    @Override
    public void setId(String id) {
        super.setId(id);
    }
    
    @Override
    public void setName(String name) {
        super.setName(name);
    }
    
    @Override
    public void setVersion(Version version) {
        super.setVersion(version);
    }
    
    @Override
    public void setState(ContainerState state) {
        super.setState(state);
    }
    
    /**
     * Defines the Docker container's id.
     * @param dockerId
     */
    public void setDockerId(String dockerId) {
        this.dockerId = dockerId;
    }
    
    /**
     * Returns the Docker container's id.
     * @return Docker id
     */
    public String getDockerId() {
        return this.dockerId;
    }
    
    /**
     * Defines the name of the compressed file with the Docker image.
     * @param dockerImageZipfile
     */
    public void setDockerImageZipfile(String dockerImageZipfile) {
        this.dockerImageZipfile = dockerImageZipfile;
    }
    
    /**
     * Returns the name of the compressed file with the Docker image.
     * @return name
     */
    public String getDockerImageZipfile() {
        return this.dockerImageZipfile;
    }
    
    /**
     * Defines the name of the Docker image.
     * @param dockerImageName
     */
    public void setDockerImageName(String dockerImageName) {
        this.dockerImageName = dockerImageName;
    }
    
    /**
     * Returns the name of the Docker image.
     * @return name
     */
    public String getDockerImageName() {
        return this.dockerImageName;
    }
    
    /**
     * Defines the download directory.
     * @param directory
     */
    public void setDownloadDirectory(String directory) {
        this.downloadDirectory = directory;
    }
    
    /**
     * Returns the download directory. If the download directory is null, it returns an empty string.
     * @return directory
     */
    public String getDownloadDirectory() {
        if (this.downloadDirectory == null) {
            return "";
        }
        return this.downloadDirectory;
    }
    
    /**
     * Returns a DockerContainerDescriptor with a information from a yaml file.
     * @param file yaml file
     * @return DockerContainerDescriptor
     */
    public static DockerContainerDescriptor readFromYamlFile(File file) {
        DockerContainerDescriptor result = null;
        InputStream in;
        try {
            in = new FileInputStream(file);
            if (in != null) {
                Yaml yaml = new Yaml();
                result = yaml.load(in);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }
}
