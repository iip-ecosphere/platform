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

import de.iip_ecosphere.platform.services.spring.descriptor.Field;

/**
 * Implements {@link Field} for YAML.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlField implements Field {
    
    private String name;
    private String type;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }
    
    /**
     * Defines the name of the attribute. [required by SnakeYaml]
     * 
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Defines the type of the attribute. [required by SnakeYaml]
     * 
     * @param type the type as qualified Java name
     */
    public void setType(String type) {
        this.type = type;
    }

}
