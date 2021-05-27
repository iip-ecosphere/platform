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

import java.io.IOException;

import de.iip_ecosphere.platform.support.iip_aas.AasConfiguration;

/**
 * ECS runtime configuration (poor man's spring approach). Implementing components shall extend this class and add
 * their specific configuration settings. Subclasses must have a no-arg constructor and getters/setters for all
 * configuration values.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Configuration extends AasConfiguration {

    /**
     * Reads a {@link Configuration} instance from a default "ecsRuntime.yml" file in the root folder of the jar.
     * This method shall be used by subclasses akin to {@link #readFromYaml()}. 
     *
     * @param <C> the specific type of configuration to read (extended from {@code Configuration}}
     * @param cls the class of configuration to read
     * @return the configuration instance
     * @see #readFromYaml(Class, String)
     */
    public static <C extends Configuration> C readFromYaml(Class<C> cls) throws IOException {
        return readFromYaml(cls, "ecsRuntime.yml");
    }
    
    /**
     * Reads a {@link Configuration} instance from a default "ecsRuntime.yml" file in the root folder of the jar. 
     *
     * @return the configuration instance
     * @see #readFromYaml(Class)
     */
    public static Configuration readFromYaml() throws IOException {
        return readFromYaml(Configuration.class, "ecsRuntime.yml");
    }

}
