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

import de.iip_ecosphere.platform.services.TypedDataConnectorDescriptor;

/**
 * Implements {@link TypedDataConnectorDescriptor} for Spring Cloud.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SpringCloudServiceTypedConnectorData extends SpringCloudServiceTypedData 
    implements TypedDataConnectorDescriptor {

    private String id;
    private String service;
    private String function;
    
    // checkstyle: stop parameter number check
    
    /**
     * Creates an instance.
     * 
     * @param id the id of the connection.
     * @param name the name of the data, e.g., the channel 
     * @param description an optional description of the data (may be empty)
     * @param type the type, either a standard java class or a dynamic proxy for types declared by the services that are
     *   not available in this (execution/platform) environment
     * @param service the id of the service this connector is pointing to
     * @param function the function associated to this connector
     */
    SpringCloudServiceTypedConnectorData(String id, String name, String description, Class<?> type, String service, 
        String function) {
        super(name, description, type);
        this.id = id;
        this.service = service;
        this.function = function;
    }

    // checkstyle: resume parameter number check

    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getService() {
        return service;
    }
    
    @Override
    public String getFunction() {
        return function;
    }
    
}
