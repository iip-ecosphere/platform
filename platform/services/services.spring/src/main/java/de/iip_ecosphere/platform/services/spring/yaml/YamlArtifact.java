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

import de.iip_ecosphere.platform.services.environment.AbstractYamlArtifact;
import de.iip_ecosphere.platform.services.spring.descriptor.Artifact;
import de.iip_ecosphere.platform.services.spring.descriptor.Validator;
import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;

/**
 * Information about an artifact containing services. The artifact is to be deployed. We assume that the underlying
 * yaml file is generated, i.e., repeated information such as relations can be consistently specified.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlArtifact extends AbstractYamlArtifact implements Artifact { 
    // inheritance from Service environment with <S> does not work with yaml

    private List<YamlService> services;
    private List<YamlType> types = new ArrayList<>();
    
    @Override
    public List<YamlService> getServices() {
        return services;
    }
    
    @Override
    public List<YamlType> getTypes() {
        return types;
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
     * Sets the service instances. [required by SnakeYaml]
     * 
     * @param services the services
     */
    public void setServices(List<YamlService> services) {
        this.services = services;
    }

    /**
     * Reads an {@link YamlArtifact} from a YAML input stream. The returned artifact may be invalid.
     * Use {@link Validator} to test the returned instance for validity.
     * 
     * @param in the input stream (may be <b>null</b>)
     * @return the artifact info
     */
    public static YamlArtifact readFromYaml(InputStream in) throws IOException {
        YamlArtifact result = AbstractSetup.readFromYaml(YamlArtifact.class, in);
        if (null == result.getServices()) {
            result.setServices(new ArrayList<>());
        }
        return result;
    }

}
