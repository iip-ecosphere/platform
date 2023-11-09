/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.iip_aas.config;

import de.iip_ecosphere.platform.support.Schema;

/**
 * Validates data in an endpoint holder, may override returned data.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface EndpointValidator {
    
    /**
     * Validates the host.
     * 
     * @param host the original host
     * @param holder the querying endpoint holder
     * @return the validated host
     */
    public String validateHost(String host, EndpointHolder holder);

    /**
     * Validates the port.
     * 
     * @param port the original port
     * @param holder the querying endpoint holder
     * @return the validated port
     */
    public int validatePort(int port, EndpointHolder holder);
    
    /**
     * Validates the schema.
     * 
     * @param schema the original schema
     * @param holder the querying endpoint holder
     * @return the validated schema
     */
    public Schema validateSchema(Schema schema, EndpointHolder holder);
    
    /**
     * Validates the path.
     * 
     * @param path the original path
     * @param holder the querying endpoint holder
     * @return the validated path
     */
    public String validatePath(String path, EndpointHolder holder);

}
