/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
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
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;

/**
 * Mocks a {@link ConnectorServiceWrapper} by data in a JSON file through {@link DataMapper}.
 * 
 * @param <O> the output type from the underlying machine/platform
 * @param <I> the input type to the underlying machine/platform
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * 
 * @author Holger Eichelberger, SSE
 */
public class MockingConnectorServiceWrapper<O, I, CO, CI> extends ConnectorServiceWrapper<O, I, CO, CI> {

    private Class<? extends CO> connectorOutType;
    private ReceptionCallback<CO> callback;
    private ReceptionCallback<CI> inputCallback;
    private Supplier<ConnectorParameter> connParamSupplier;
    private boolean enableNotifications;
    private String fileName;
    
    /**
     * Creates a service wrapper instance.
     * 
     * @param yaml the service information as read from YAML
     * @param connector the connector instance to wrap
     * @param connParamSupplier the connector parameter supplier for connecting the underlying connector
     */
    public MockingConnectorServiceWrapper(YamlService yaml, Connector<O, I, CO, CI> connector, 
        Supplier<ConnectorParameter> connParamSupplier) {
        super(yaml, connector, connParamSupplier);
        this.connParamSupplier = connParamSupplier;
        connectorOutType = connector.getConnectorOutputType();
        fileName = "testData-" + connector.getClass().getSimpleName() + "_" + connectorOutType.getSimpleName() + ".yml";
    }

    /**
     * Calls {@link Connector#write(Object)} on {@code} data and handles the respective exception potentially thrown by 
     * the underlying connector.
     * 
     * @param data the data to write
     */
    public void send(CI data) {
        if (null != inputCallback) {
            inputCallback.received(data);
        }
        emitData(data);
    }
    
    /**
     * Attaches a reception {@code callback} to this connector. The {@code callback}
     * is called upon a reception. Handles the respective exception potentially thrown by the underlying connector.
     * 
     * @param callback the callback to attach
     */
    public void setReceptionCallback(ReceptionCallback<CO> callback) {
        this.callback = callback;
    }
    
    /**
     * The input data stream.
     * 
     * @param name the name of the file/resource to be used
     * @return the stream
     */
    protected InputStream getDataStream(String name) {
        return ResourceLoader.getResourceAsStream(name);
    }
    
    /**
     * Starts data ingestion.
     * 
     * @throws IOException if the data file cannot be found/opened
     */
    private void startData() throws IOException {
        LoggerFactory.getLogger(getClass()).info("Starting data with resource: {}", fileName);
        ConnectorParameter param = connParamSupplier.get();
        int notifInterval = enableNotifications ? 0 : param.getNotificationInterval();
        DataMapper.mapJsonData(getDataStream(fileName), connectorOutType, d -> {
            if (callback != null) {
                callback.received(d);
                if (notifInterval > 0) {
                    TimeUtils.sleep(notifInterval);
                }
            }
        });
    }
    
    @Override
    protected void handleState(ServiceState state) throws ExecutionException {
        doSetState(state);
        try {
            if (ServiceState.STARTING == state) {
                if (enableNotifications) {
                    startData();
                }
                doSetState(ServiceState.RUNNING);
            } else if (ServiceState.STOPPING == state) {
                callback = null;
                doSetState(ServiceState.STOPPED);
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

    @Override
    public void enablePolling(boolean enablePolling) {
        if (!enableNotifications && enablePolling) {
            try {
                startData();
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("Cannot start polling: {}", e.getMessage());
            }
        }
    }

    @Override
    public void enableNotifications(boolean enableNotifications) {
        this.enableNotifications = enableNotifications;
    }
    
    /**
     * Emits data received from the platform.
     * 
     * @param data the last data
     */
    public void emitData(CI data) {
        System.out.println(data);
    }
    
    /**
     * Sets a callback on data received from the platform.
     * 
     * @param inputCallback the input callback, may be <b>null</b> for none
     */
    public void setInputCallback(ReceptionCallback<CI> inputCallback) {
        this.inputCallback = inputCallback;
    }
        
}