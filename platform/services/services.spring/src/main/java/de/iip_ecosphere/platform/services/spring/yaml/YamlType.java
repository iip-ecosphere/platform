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

import java.util.ArrayList;
import java.util.List;

import de.iip_ecosphere.platform.services.spring.descriptor.Type;

/**
 * Implements {@link Type} for Yaml.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlType implements Type {
    
    private String name;
    private List<YamlField> fields = new ArrayList<>();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<YamlField> getFields() {
        return fields;
    }
    
    /**
     * Defines the name of the type. [required by SnakeYaml]
     * 
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Defines the fields of the type.
     * 
     * @param fields the fields
     */
    public void setFields(List<YamlField> fields) {
        this.fields = fields;
    }

}
