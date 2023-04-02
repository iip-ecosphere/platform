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

package de.iip_ecosphere.platform.services.spring;

import de.iip_ecosphere.platform.services.TypedDataDescriptor;

/**
 * Implements {@link TypedDataDescriptor} for Spring Cloud.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SpringCloudServiceTypedData implements TypedDataDescriptor {

    private String name;
    private String description;
    private Class<?> type;
    
    /**
     * Creates an instance.
     * 
     * @param name the name of the data
     * @param description an optional description of the data (may be empty)
     * @param type the type, either a standard java class or a dynamic proxy for types declared by the services that are
     *   not available in this (execution/platform) environment
     */
    SpringCloudServiceTypedData(String name, String description, Class<?> type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }
    
    /**
     * Copies a data instance.
     * 
     * @param origin the origin descriptor to copy from
     */
    SpringCloudServiceTypedData(TypedDataDescriptor origin) {
        this.name = origin.getName();
        this.description = origin.getDescription();
        this.type = origin.getType();
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public String getDescription() {
        return description;
    }

}
