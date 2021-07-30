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
import java.util.ArrayList;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.InternetProtocol;

import de.iip_ecosphere.platform.ecsRuntime.AbstractContainerDescriptor;
import de.iip_ecosphere.platform.ecsRuntime.ContainerState;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * Implements a container descriptor for docker-based container management.
 * 
 * @author Monika Staciwa, SSE
 */
public class DockerContainerDescriptor extends AbstractContainerDescriptor {
    
    /**
     * Describes an exposed port.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class ExpPort {
        
        private String port = "";
        private InternetProtocol protocol = InternetProtocol.DEFAULT;
        
        /**
         * Returns the port number. May contain {@link #PORT_PLACEHOLDER} to be replaced by the dynamic port 
         *    of the AAS implementation server of the service manager
         * 
         * @return the port number
         */
        public String getPort() {
            return port;
        }
        
        /**
         * Defines the port number. [required by SnakeYaml]
         * 
         * @param port the port number
         */
        public void setPort(String port) {
            if (null != port) {
                this.port = port;
            }
        }
        
        /**
         * Returns the internet protocol.
         * 
         * @return the internet protocol
         */
        public InternetProtocol getProtocol() {
            return protocol;
        }
        
        /**
         * Defines the internet protocol. [required by SnakeYaml]
         * 
         * @param protocol the internet protocol
         */
        public void setProtocol(InternetProtocol protocol) {
            this.protocol = protocol;
        }
    }
    
    public static final String PORT_PLACEHOLDER = "${port}";
    private static int instanceCount = 0;

    // internal
    private int instance = instanceCount++;
    private String dockerId;
    private String downloadedImageZipfile;

    // configurable
    private String dockerImageName;
    private String dockerImageZipfile;
    private ArrayList<String> exposedPorts = new ArrayList<String>();
    private ArrayList<String> env = new ArrayList<String>();
        
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
    
    // [required by SnakeYaml]
    @Override
    public void setId(String id) {
        super.setId(id);
    }
    
    // [required by SnakeYaml]
    @Override
    public void setName(String name) {
        super.setName(name);
    }
    
    // [required by SnakeYaml]
    @Override
    public void setVersion(Version version) {
        super.setVersion(version);
    }
    
    // [required by SnakeYaml]
    @Override
    public void setState(ContainerState state) {
        super.setState(state);
    }
    
    /**
     * Defines the Docker container's id.
     * @param dockerId
     */
    void setDockerId(String dockerId) {
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
     * Defines the name of the compressed file with the Docker image. [required by SnakeYaml]
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
     * Defines the name of the Docker image. [required by SnakeYaml]
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
     * Defines the name of the downloaded file with the Docker image.
     * @param name
     */
    void setDownloadedImageZipfile(String name) {
        this.downloadedImageZipfile = name;
    }
    
    /**
     * Returns a name of downloaded file with the Docker image.
     * @return image's name
     */
    public String getDownloadedImageZipfile() {
        return this.downloadedImageZipfile;
    }

    /**
     * Defines the exposed ports. [required by SnakeYaml]
     * @param exposedPorts the exposed ports
     */
    public void setExposedPorts(ArrayList<String> exposedPorts) {
        if (null != exposedPorts) {
            this.exposedPorts = exposedPorts;
        }
    }
    
    /**
     * Returns the ports exposed by the container.
     * @return the exposed ports
     */
    public ArrayList<String> getExposedPorts() {
        return this.exposedPorts;
    }

    /**
     * Instantiates the exposed by the container.
     * @param port to replace {@link #PORT_PLACEHOLDER}
     * @return the exposed ports
     */
    public List<ExposedPort> instantiateExposedPorts(int port) {
        ArrayList<ExposedPort> result = new ArrayList<ExposedPort>();
        String tmpPort = String.valueOf(port);
        for (String e: exposedPorts) {
            String tmp = e.replace(PORT_PLACEHOLDER, tmpPort);
            int pos = tmp.indexOf('/');
            String iPort;
            String iProtocol;
            if (pos > 0) {
                iPort = tmp.substring(0, pos);
                iProtocol = tmp.substring(pos + 1);
                if ("DEFAULT".equals(iProtocol)) {
                    iProtocol = InternetProtocol.DEFAULT.name();
                }
            } else {
                iPort = tmp;
                iProtocol = InternetProtocol.TCP.name();
            }
            try {
                result.add(new ExposedPort(Integer.parseInt(iPort), InternetProtocol.valueOf(iProtocol)));
            } catch (IllegalArgumentException ex) {
            }
        }
        return result;
    }

    /**
     * Defines the environment settings to start the container. [required by SnakeYaml]
     * @param env the environment settings, may contain {@link #PORT_PLACEHOLDER} to be replaced by the dynamic port 
     *    of the AAS implementation server of the service manager
     */
    public void setEnv(ArrayList<String> env) {
        if (null != env) {
            this.env = env;
        }
    }
    
    /**
     * Returns the plain environment settings to start the container.
     * @return the environment settings, may contain {@link #PORT_PLACEHOLDER}}
     */
    public ArrayList<String> getEnv() {
        return this.env;
    }

    /**
     * Returns the substituted environment variable settings to start the container.
     * @param port the port to substitute {@link #PORT_PLACEHOLDER} 
     * @return the instantiated environment variable settings 
     */
    public List<String> instantiateEnv(int port) {
        List<String> result = new ArrayList<String>();
        for (String s : env) {
            result.add(s.replace(PORT_PLACEHOLDER, String.valueOf(port)));
        }
        return result;
    }

    /**
     * Returns whether a dynamic port for {@link #PORT_PLACEHOLDER} is required.
     * 
     * @return {@code true} for dynamic port, {@code false} else
     */
    public boolean requiresPort() {
        boolean result = false;
        for (String s : env) {
            if (s.contains(PORT_PLACEHOLDER)) {
                result = true;
                break;
            }
        }
        if (!result) {
            for (String e : exposedPorts) {
                if (e.contains(PORT_PLACEHOLDER)) {
                    result = true;
                    break;
                }
            }
            
        }
        return result;
    }
    
    /**
     * Returns the key for the network manager.
     * 
     * @return the key
     */
    public String getNetKey() {
        return Id.getDeviceId() + "_" + dockerImageName + "_" + instance;
    }

    /**
     * Returns a DockerContainerDescriptor with a information from a yaml file.
     * @param file yaml file
     * @return DockerContainerDescriptor (may be <b>null</b>)
     */
    public static DockerContainerDescriptor readFromYamlFile(File file) {
        DockerContainerDescriptor result = null;
        InputStream in;
        try {
            in = new FileInputStream(file);
            result = readFromYaml(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * Returns a DockerContainerDescriptor with a information from a yaml file.
     * @param in an inout stream with yaml contents (may be <b>null</b>)
     * @return DockerContainerDescriptor (may be <b>null</b>)
     */
    public static DockerContainerDescriptor readFromYaml(InputStream in) {
        DockerContainerDescriptor result = null;
        if (in != null) {
            Yaml yaml = new Yaml();
            result = yaml.load(in);
        }
        return result;
    }

}
