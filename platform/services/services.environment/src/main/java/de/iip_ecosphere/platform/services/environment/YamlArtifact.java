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

package de.iip_ecosphere.platform.services.environment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.iip_ecosphere.platform.support.iip_aas.config.AbstractConfiguration;

/**
 * Information about an artifact containing services. The artifact is to be deployed. We assume that the underlying
 * yaml file is generated, i.e., repeated information such as relations can be consistently specified.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlArtifact <S extends YamlService> {

    private String id;
    private String name;
    private List<S> services;

    /**
     * Returns the name of the service.
     * 
     * @return the name
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the name of the service.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the services.
     * 
     * @return the services
     */
    public List<S> getServices() {
        return services;
    }

    /**
     * Defines the id of the service. [required by SnakeYaml]
     * 
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Defines the name of the service. [required by SnakeYaml]
     * 
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Sets the service instances. [required by SnakeYaml]
     * 
     * @param services the services
     */
    public void setServices(List<S> services) {
        this.services = services;
    }

    /**
     * Reads an {@link YamlArtifact} from a YAML input stream. The returned artifact may be invalid.
     * 
     * @param <S> the service type
     * @param <Y> the artifact type
     * @param in the input stream (may be <b>null</b>)
     * @param cls the class to read (for refinements)
     * @return the artifact info
     */
    public static <S extends YamlService, Y extends YamlArtifact<S>> Y readFromYaml(InputStream in, 
        Class<Y> cls) throws IOException {
        Y result = AbstractConfiguration.readFromYaml(cls, in);
        if (null == result.getServices()) {
            result.setServices(new ArrayList<>());
        }
        return result;
    }

}
