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

package de.iip_ecosphere.platform.services.spring.yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * Information about an artifact containing services. The artifact is to be deployed. We assume that the underlying
 * yaml file is generated, i.e., repeated information such as relations can be consistently specified.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Artifact {

    private static final Logger LOGGER = LoggerFactory.getLogger(Artifact.class);
    private String id;
    private String name;
    private List<Service> services;

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
    public List<Service> getServices() {
        return services;
    }

    /**
     * Defines the id of the service. [required by Spring]
     * 
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Defines the name of the service. [required by Spring]
     * 
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Sets the service instances. [required by Spring]
     * 
     * @param services the services
     */
    public void setServices(List<Service> services) {
        this.services = services;
    }


    /**
     * Tries reading {@link Artifact} from a yaml input stream.
     * 
     * @param in the input stream (may be <b>null</b>)
     * @return the artifact info
     */
    public static Artifact readFromYaml(InputStream in) throws IOException {
        Artifact result;
        if (null == in) {
            result = new Artifact();
        } else {
            try {        
                Yaml yaml = new Yaml(new Constructor(Artifact.class));
                result = yaml.load(in);
            } catch (YAMLException e) {
                throw new IOException(e);
            }
        }
        if (null == result.services) {
            result.services = new ArrayList<>();
        }
        for (int s = result.services.size() - 1; s >= 0; s--) {
            Service info = result.services.get(s);
            boolean isValid = (null != info.getId() && info.getId().length() > 0);
            if (!isValid) {
                result.services.remove(s);
                LOGGER.error("Service #" + (s + 1) + " is invalid and not considered further.");
            }
        }
        return result;
    }

}
