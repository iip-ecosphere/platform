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

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import de.iip_ecosphere.platform.services.spring.descriptor.Artifact;
import de.iip_ecosphere.platform.services.spring.descriptor.Validator;

/**
 * Information about an artifact containing services. The artifact is to be deployed. We assume that the underlying
 * yaml file is generated, i.e., repeated information such as relations can be consistently specified.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlArtifact implements Artifact {

    private String id;
    private String name;
    private List<YamlService> services;
    private List<YamlType> types = new ArrayList<>();

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public List<YamlService> getServices() {
        return services;
    }

    @Override
    public List<YamlType> getTypes() {
        return types;
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
    public void setServices(List<YamlService> services) {
        this.services = services;
    }

    /**
     * Sets the declared types. [required by SnakeYaml]
     * 
     * @param types the types
     */
    public void setTypes(List<YamlType> types) {
        this.types = types;
    }

    /**
     * Reads an {@link YamlArtifact} from a YAML input stream. The returned artifact may be invalid.
     * Use {@link Validator} to test the returned instance for validity.
     * 
     * @param in the input stream (may be <b>null</b>)
     * @return the artifact info
     */
    public static YamlArtifact readFromYaml(InputStream in) throws IOException {
        YamlArtifact result;
        if (null == in) {
            result = new YamlArtifact();
        } else {
            try {        
                Yaml yaml = new Yaml(new Constructor(YamlArtifact.class));
                result = yaml.load(in);
            } catch (YAMLException e) {
                throw new IOException(e);
            }
        }
        if (null == result.services) {
            result.services = new ArrayList<>();
        }
        return result;
    }

}
