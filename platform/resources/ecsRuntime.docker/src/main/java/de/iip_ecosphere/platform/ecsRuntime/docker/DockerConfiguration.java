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

import java.io.IOException;

import de.iip_ecosphere.platform.ecsRuntime.Configuration;

/**
 * Implements the docker specific configuration. For configuration prerequisites, see {@link Configuration}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DockerConfiguration extends Configuration {

    private String dockerHost;

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
     * Reads a {@link DockerConfiguration} instance from a default "ecsRuntime.yml" file in the root folder of the jar. 
     *
     * @return configuration instance
     */
    public static DockerConfiguration readFromYaml() throws IOException {
        return Configuration.readFromYaml(DockerConfiguration.class);
    }
    
}
