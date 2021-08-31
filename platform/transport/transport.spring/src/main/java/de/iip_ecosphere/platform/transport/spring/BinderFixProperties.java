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

package de.iip_ecosphere.platform.transport.spring;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.config.BinderProperties;
import org.springframework.stereotype.Component;

/**
 * Replication of binder properties to have all binder information available. Somehow, the Spring structure does 
 * not work in its context.
 * 
 * Example:
 * <pre>
 * binders:
 *   properties:
 *      internal:
 *          type: hivemqv3Binder
 *          environment:
 *              mqtt:
 *                  host: localhost
 *                  clientId: external
 *                  port: 8883
 *      external:
 *          type: hivemqv3Binder
 *          environment:
 *              mqtt:
 *                  host: localhost
 *                  clientId: external
 *                  port: 8883
 * </pre>
 * 
 * @author Holger Eichelberger, SSE
 */
@Component
@ConfigurationProperties(prefix = "binders")
public class BinderFixProperties {

    private Map<String, BinderProperties> properties;

    /**
     * Defines the binder properties. [required by Spring]
     * 
     * @param properties the new binder properties
     */
    public void setProperties(Map<String, BinderProperties> properties) {
        this.properties = properties;
    }

    /**
     * Returns the binder properties.
     * 
     * @return the binder properties
     */
    public Map<String, BinderProperties> getProperties() {
        return properties;
    }
    
}
