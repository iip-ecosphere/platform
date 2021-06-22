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
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.Connector;

/**
 * Wraps a connector into a service.
 * 
 * @param <O> the output type from the underlying machine/platform
 * @param <I> the input type to the underlying machine/platform
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConnectorServiceWrapper<O, I, CO, CI> extends AbstractService {

    private Connector<O, I, CO, CI> connector;

    /**
     * Creates a service wrapper instance.
     * 
     * @param yaml the service information as read from YAML
     * @param connector the connector instance to wrap
     */
    public ConnectorServiceWrapper(YamlService yaml, Connector<O, I, CO, CI> connector) {
        super(yaml);
        this.connector = connector;
    }

    /**
     * Returns the connector instance.
     * 
     * @return the connector instance
     */
    public Connector<O, I, CO, CI> getConnector() {
        return connector;
    }
    
    /**
     * Calls {@link Connector#write(Object)} on {@code} data and handles the respective exception.
     * 
     * @param data the data to write
     */
    public void send(CI data) {
        try {
            connector.write(data);
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("Data loss, cannot send data: " + e.getMessage());
        }
    }
    
    @Override
    public void migrate(String resourceId) throws ExecutionException {
    }

    @Override
    public void update(URI location) throws ExecutionException {
    }

    @Override
    public void switchTo(String targetId) throws ExecutionException {
    }

    @Override
    public void reconfigure(Map<String, String> values) throws ExecutionException {
    }

}
