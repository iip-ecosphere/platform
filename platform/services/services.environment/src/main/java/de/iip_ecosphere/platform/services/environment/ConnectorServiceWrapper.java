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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslators;

/**
 * Wraps a connector into a service. Implicitly reacts on parameter "inPath" and "outPath" as string to override
 * dynamically the configured data path into the connector data. 
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
    private Map<String, ParameterConfigurer<?>> paramConfigurers = new HashMap<>();
    private String outPath; // the runtime-reconfigured data path
    private String inPath; // the runtime-reconfigured data path

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
        
        AbstractService.addConfigurer(paramConfigurers, "outPath", String.class, TypeTranslators.STRING, 
            v -> setOutPath(v), () -> outPath, "iip.connector." + getId() + ".outPath");
        AbstractService.addConfigurer(paramConfigurers, "inPath", String.class, TypeTranslators.STRING, 
            v -> setInPath(v), () -> inPath, "iip.connector." + getId() + ".inPath");
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
        doSetState(state);
        try {
            if (ServiceState.STARTING == state) {
                ConnectorParameter param = connParamSupplier.get();
                connector.connect(param);
                // not needed, but generation may statically switch off notifications and prevent testing with
                // different values
                connector.enableNotifications(param.getNotificationInterval() == 0);
                doSetState(ServiceState.RUNNING);
            } else if (ServiceState.STOPPING == state) {
                connector.disconnect();
                doSetState(ServiceState.STOPPED);
            } else if (ServiceState.UNDEPLOYING == state) {
                connector.dispose();
            }
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
    }
    
    /**
     * Changes the state by calling {@link AbstractService#setState(ServiceState)}. Introduced, so that super 
     * functionality is made available to super-classes as-is.
     * 
     * @param state the new state
     * @throws ExecutionException if changing the state fails for some reason
     */
    protected void doSetState(ServiceState state) throws ExecutionException {
        super.setState(state);
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

    @Override
    public ParameterConfigurer<?> getParameterConfigurer(String paramName) {
        return paramConfigurers.get(paramName);
    }
    
    @Override
    public Set<String> getParameterNames() {
        return paramConfigurers.keySet();
    }
    
    /**
     * Changes {@link #inPath}.
     * 
     * @param inPath the in path (ignored if <b>null</b> or empty)
     */
    private void setInPath(String inPath) {
        if (inPath != null && inPath.length() > 0) {
            this.inPath = inPath;
        }
    }
    
    /**
     * Changes {@link #outPath}.
     * 
     * @param outPath the out path (ignored if <b>null</b> or empty)
     */
    private void setOutPath(String outPath) {
        if (outPath != null && outPath.length() > 0) {
            this.outPath = outPath;
        }
    }
    
    /**
     * Returns the (eventually re-configured) data access path within the protocol.
     *  
     * @param cfgPath the configured path from the model
     * @return the path to use, may be {@code cfgPath}
     */
    public String getOutPath(String cfgPath) {
        String result = cfgPath;
        if (outPath != null) {
            result = outPath;
        }
        return result;
    }

    /**
     * Returns the (eventually re-configured) data access path within the protocol.
     *  
     * @param cfgPath the configured path from the model
     * @return the path to use, may be {@code cfgPath}
     */
    public String getInPath(String cfgPath) {
        String result = cfgPath;
        if (inPath != null) {
            result = inPath;
        }
        return result;
    }

    
}
