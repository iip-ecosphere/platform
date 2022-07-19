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

package de.iip_ecosphere.platform.ecsRuntime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.support.iip_aas.Version;
import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;

/**
 * Basic {@link ContainerDescriptor} implementation, e.g., including a representation of the {@link ServiceState} 
 * statemachine.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BasicContainerDescriptor implements ContainerDescriptor {
    
    private String id;
    private String name;
    private Version version;
    private ContainerState state;
    private URI uri;
    private String imageFile;
    
    /**
     * Creates a container descriptor instance.
     */
    public BasicContainerDescriptor() {
    }
    
    /**
     * Creates a container descriptor instance.
     * 
     * @param id the container id
     * @param name the (file) name of the container
     * @param version the version of the container
     * @param uri the URI where the descriptor was loaded from
     * @throws IllegalArgumentException if {@code id}, {@code name}, {@code version} or {@code uri} is invalid, e.g., 
     *     <b>null</b> or empty
     */
    protected BasicContainerDescriptor(String id, String name, Version version, URI uri) {
        if (null == id || id.length() == 0) {
            throw new IllegalArgumentException("id must not be null or empty");
        }
        if (null == name || name.length() == 0) {
            throw new IllegalArgumentException("name must not be null or empty");
        }
        if (null == version) {
            throw new IllegalArgumentException("version must not be null");
        }
        this.id = id;
        this.name = name;
        this.version = version;
        this.state = ContainerState.AVAILABLE;
        setUri(uri);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Version getVersion() {
        return version;
    }
    
    /**
     * Defines the container's id. Typically, the id of a container shall not be modified at all. If this is needed
     * for some reason, implementing classes may use this with care. Use the constructor instead. 
     * [required by SnakeYaml]
     * 
     * @param id the container id
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Defines the container's name. Typically, the name of a container shall not be modified at all. If this is needed
     * for some reason, implementing classes may use this with care. Use the constructor instead.
     * [required by SnakeYaml]
     * 
     * @param name the container name
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * Defines the container's version. [required by SnakeYaml]
     * 
     * @param version the container version
     */
    public void setVersion(Version version) {
        this.version = version;
    }
    
    /**
     * Changes the container state. [required by SnakeYaml]
     * 
     * @param state the new container state
     */
    public void setState(ContainerState state) {
        if (null != state) {
            this.state = state;
        }
    }

    @Override
    public ContainerState getState() {
        return state;
    }
    
    @Override
    public URI getUri() {
        return uri;
    }

    /**
     * Sets the URI where the container was loaded from. [required by SnakeYaml]
     * 
     * @param uri the URI where the descriptor was loaded from
     * @throws IllegalArgumentException if {@code uri} is invalid, e.g., <b>null</b>
     */
    public void setUri(URI uri) {
        if (null == uri) {
            throw new IllegalArgumentException("uri must not be null");
        }
        this.uri = uri.normalize();
    }

    /**
     * Defines the name of the (compressed) image file of the container. [required by SnakeYaml]
     * 
     * @param imageFile the name of the file
     */
    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }
    
    /**
     * Returns the name of the (compressed) image file of the container.
     * 
     * @return name the name of the file
     */
    public String getImageFile() {
        return imageFile;
    }

    /**
     * Defines the name of the compressed file with the Docker image. [required by SnakeYaml, legacy]
     * 
     * @param dockerImageZipfile the name of the file
     * @see #setImageFile(String)
     */
    public void setDockerImageZipfile(String dockerImageZipfile) {
        this.imageFile = dockerImageZipfile;
    }
    
    /**
     * Returns the name of the compressed file with the Docker image. [legacy]
     * 
     * @return name the name
     * @see #getImageFile()
     */
    public String getDockerImageZipfile() {
        return imageFile;
    }
    
    /**
     * Returns a DockerContainerDescriptor with a information from a yaml file.
     * @param file yaml file
     * @return DockerContainerDescriptor (may be <b>null</b>)
     */
    public static BasicContainerDescriptor readFromYamlFile(File file) {
        BasicContainerDescriptor result = null;
        InputStream in;
        try {
            in = new FileInputStream(file);
            result = readFromYaml(in, file.toURI());
        } catch (FileNotFoundException e) {
            LoggerFactory.getLogger(BasicContainerDescriptor.class).error(
                "Reading container descriptor: " + e.getMessage());
        }
        return result;
    }

    /**
     * Returns a DockerContainerDescriptor with a information from a yaml file.
     * @param in an inout stream with Yaml contents (may be <b>null</b>)
     * @param uri the URI the descriptor was read from
     * @return DockerContainerDescriptor (may be <b>null</b>)
     */
    public static BasicContainerDescriptor readFromYaml(InputStream in, URI uri) {
        BasicContainerDescriptor result = null;
        if (in != null) {
            try {
                result = AbstractSetup.readFromYaml(BasicContainerDescriptor.class, in);
                result.setUri(uri);
            } catch (IOException e) {
                LoggerFactory.getLogger(BasicContainerDescriptor.class).error(
                    "Reading container descriptor: " + e.getMessage());
            }
        }
        return result;
    }
    
}
