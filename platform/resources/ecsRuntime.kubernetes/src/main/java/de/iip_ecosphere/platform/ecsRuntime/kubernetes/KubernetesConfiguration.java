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

package de.iip_ecosphere.platform.ecsRuntime.kubernetes;

import java.io.IOException;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.ecsRuntime.Configuration;
import de.iip_ecosphere.platform.support.iip_aas.config.AbstractConfiguration;

/**
 * Implements the Kubernetes specific configuration. For configuration prerequisites, see {@link Configuration}.
 * 
 * @author Ahmad Alomosh, SSE
 */
public class KubernetesConfiguration extends Configuration {

     /**
     * Reads a {@link KubernetesConfiguration} instance from a {@link AbstractConfiguration#DEFAULT_FNAME} in the 
     * root folder of the jar/classpath. 
     *
     * @return configuration instance
     */
    public static KubernetesConfiguration readFromYaml() {
        KubernetesConfiguration result;
        try {
            return Configuration.readConfiguration(KubernetesConfiguration.class);
        } catch (IOException e) {
            LoggerFactory.getLogger(KubernetesConfiguration.class).error("Reading configuration: " + e.getMessage());
            result = new KubernetesConfiguration();
        }
        return result;
    }
    
}
