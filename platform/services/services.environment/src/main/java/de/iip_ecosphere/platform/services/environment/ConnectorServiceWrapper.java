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
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;

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
    private Supplier<ConnectorParameter> connParamSupplier;

    /**
     * Creates a service wrapper instance.
     * 
     * @param yaml the service information as read from YAML
     * @param connector the connector instance to wrap
     */
    public ConnectorServiceWrapper(YamlService yaml, Connector<O, I, CO, CI> connector) { // TODO remove after build
        super(yaml);
        this.connector = connector;
    }

    /**
     * Creates a service wrapper instance.
     * 
     * @param yaml the service information as read from YAML
     * @param connector the connector instance to wrap
     * @param connParamSupplier the connector parameter supplier for connecting the underlying connector
     */
    public ConnectorServiceWrapper(YamlService yaml, Connector<O, I, CO, CI> connector, 
        Supplier<ConnectorParameter> connParamSupplier) {
        super(yaml);
        this.connector = connector;
        this.connParamSupplier = connParamSupplier; 
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
     * Calls {@link Connector#write(Object)} on {@code} data and handles the respective exception potentially thrown by 
     * the underlying connector.
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
    
    /**
     * Attaches a reception {@code callback} to this connector. The {@code callback}
     * is called upon a reception. Handles the respective exception potentially thrown by the underlying connector.
     * 
     * @param callback the callback to attach
     */
    public void setReceptionCallback(ReceptionCallback<CO> callback) {
        try {
            connector.setReceptionCallback(callback);
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("Data loss, cannot set reception callback: " + e.getMessage());
        }
    }
    
    @Override
    public void setState(ServiceState state) throws ExecutionException {
        super.setState(state);
        try {
            if (ServiceState.STARTING == state) {
                ConnectorParameter param = connParamSupplier.get();
                connector.connect(param);
                // not needed, but generation may statically switch off notifications and prevent testing with
                // different values
                connector.enableNotifications(param.getNotificationInterval() == 0);
                super.setState(ServiceState.RUNNING);
            } else if (ServiceState.STOPPING == state) {
                connector.disconnect();
                super.setState(ServiceState.STOPPED);
            } else if (ServiceState.UNDEPLOYING == state) {
                connector.dispose();
            }
        } catch (IOException e) {
            throw new ExecutionException(e);
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

    /**
     * Enable/disable polling (does not influence the polling timer).
     * 
     * @param enablePolling whether polling shall enabled
     * @see #enableNotifications(boolean)
     */
    public void enablePolling(boolean enablePolling) {
        connector.enablePolling(enablePolling);
    }

    /**
     * Enables/disables notifications/polling at all.
     * 
     * @param enableNotifications enable or disable notifications
     */
    public void enableNotifications(boolean enableNotifications) {
        connector.enableNotifications(enableNotifications);
    }

}
