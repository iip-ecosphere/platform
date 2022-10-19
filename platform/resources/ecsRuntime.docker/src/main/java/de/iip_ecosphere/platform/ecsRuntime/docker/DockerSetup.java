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

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.ecsRuntime.EcsSetup;
import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;

/**
 * Implements the docker specific configuration. For configuration prerequisites, see {@link EcsSetup}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DockerSetup extends EcsSetup {

    private Docker docker = new Docker();

    /**
     * Returns Docker configuration.
     * @return docker
     */
    public Docker getDocker() {
        return docker;
    }
    
    /**
     * Defines Docker configuration. [required by SnakeYaml]
     * @param docker 
     */
    public void setDocker(Docker docker) {
        this.docker = docker;
    }
    
    /**
    * Reads a {@link DockerSetup} instance from a {@link AbstractSetup#DEFAULT_FNAME} in the 
    * root folder of the jar/classpath. 
    *
    * @return configuration instance
    */
    public static DockerSetup readFromYaml() {
        DockerSetup result;
        try {
            return EcsSetup.readConfiguration(DockerSetup.class);
        } catch (IOException e) {
            LoggerFactory.getLogger(DockerSetup.class).error("Reading configuration: " + e.getMessage());
            result = new DockerSetup();
        }
        return result;
    }
    
}
