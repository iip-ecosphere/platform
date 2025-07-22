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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.connectors.AbstractConnector;
import de.iip_ecosphere.platform.connectors.CachingStrategy;
import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.events.ConnectorTriggerQuery;
import de.iip_ecosphere.platform.connectors.events.EventHandlingConnector;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.identities.IdentityToken;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.services.environment.DataMapper.BaseDataUnitFunctions;
import de.iip_ecosphere.platform.services.environment.DataMapper.IOIterator;
import de.iip_ecosphere.platform.services.environment.testing.DataRecorder;

/**
 * Mocks a {@link ConnectorServiceWrapper} by data in a JSON file through {@link DataMapper}.
 * JSON data may contain meta values $period or $repeats as for {@link BaseDataUnitFunctions}.
 * 
 * @param <O> the output type from the underlying machine/platform
 * @param <I> the input type to the underlying machine/platform
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * 
 * @author Holger Eichelberger, SSE
 */
public class MockingConnectorServiceWrapper<O, I, CO, CI> extends ConnectorServiceWrapper<O, I, CO, CI> 
    implements EventHandlingConnector {

    private Class<? extends CO> connectorOutType;
    private ReceptionCallback<CO> callback;
    private ReceptionCallback<CI> inputCallback;
    private Supplier<ConnectorParameter> connParamSupplier;
    private boolean enableNotifications;
    private String fileName;
    private DataRunnable dataRunnable;
    private CachingStrategy cachingStrategy;
    private IOIterator<? extends CO> triggerIterator;
    private DataRecorder recorder;    
    private Map<String, Object> storage;
    private int notificationInterval = 0;
    
    /**
     * Creates a service wrapper instance.
     * 
     * @param yaml the service information as read from YAML
     * @param connector the connector instance to wrap
     * @param connParamSupplier the connector parameter supplier for connecting the underlying connector
     */
    @SuppressWarnings("unchecked")
    public MockingConnectorServiceWrapper(YamlService yaml, Connector<O, I, CO, CI> connector, 
        Supplier<ConnectorParameter> connParamSupplier) {
        super(yaml, connector, connParamSupplier);
        this.connParamSupplier = connParamSupplier;
        cachingStrategy = CachingStrategy.createInstance(connector.getCachingStrategyCls());
        cachingStrategy.setCacheMode(connParamSupplier.get().getCacheMode());
        connectorOutType = connector.getConnectorOutputType();
        fileName = "testData-" + connector.getClass().getSimpleName() + "_" + connectorOutType.getSimpleName() + ".yml";
        // adjust to IIP-Ecosphere separated interface conventions
        if (connectorOutType.isInterface()) {
            try {
                connectorOutType = (Class<? extends CO>) Class.forName(connectorOutType.getName() + "Impl");
            } catch (ClassNotFoundException e) {
            }
        }
        recorder = createDataRecorder();
    }
    
    /**
     * Creates an optional data recorder instance. 
     * 
     * @return the data recorder instance, may be <b>null</b> for none
     * @see #createDataRecorderOrig()
     */
    protected DataRecorder createDataRecorder() {
        return createDataRecorderOrig();
    }

    /**
     * Creates a default data recorder instance (writes to target in JSON format). Cannot be overriden to be accessible 
     * to subclasses although {@link #createDataRecorder()} is overridden.  
     * 
     * @return the data recorder instance, may be <b>null</b> for none
     * @see #createDataRecorderOrig()
     */
    protected final DataRecorder createDataRecorderOrig() {
        return new DataRecorder(new File("target/recordings/connector-" + getId() + "-recorded.txt"), 
            DataRecorder.JSON_FORMATTER);
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
     * Returns the input data stream for mocking. Considers ".yml" (legacy, typo) and ".json" files.
     * 
     * @param name the name of the file/resource to be used
     * @return the stream, may be <b>null</b>
     */
    protected InputStream getDataStream(String name) {
        InputStream result = openDataStream(name);
        if (null == result && name.endsWith(".yml")) {
            String altName = name.substring(0, name.length() - 3) + "json";
            LoggerFactory.getLogger(getClass()).info("File {} not found, falling back to : {}", name, altName);
            result = openDataStream(altName);
        }
        return result;
    }
    
    /**
     * Tries to open an input data stream for mocking.
     * 
     * @param name the name of the file/resource to be used
     * @return the stream, may be <b>null</b>
     */
    protected InputStream openDataStream(String name) {
        InputStream result = ResourceLoader.getResourceAsStream(name);
        if (null == result) {
            result = ResourceLoader.getResourceAsStream("resources/" + name); // app packaging
        }
        return result;
    }
    
    /**
     * Starts data ingestion.
     * 
     * @param continueFunction supplier indicating whether further data shall be ingested although still potential
     *    input data may be available
     * 
     * @throws IOException if the data file cannot be found/opened
     */
    private void startData(Supplier<Boolean> continueFunction) throws IOException {
        LoggerFactory.getLogger(getClass()).info("Starting data with resource: {}", fileName);
        ConnectorParameter param = connParamSupplier.get();
        // output as debugging support
        IdentityToken tok = param.getIdentityToken(ConnectorParameter.ANY_ENDPOINT);
        if (null != tok) {
            LoggerFactory.getLogger(getClass()).info("Hint: AnyEndpoint has id token: {} with token data {}", 
                tok.getType(), tok.getTokenData() != null && tok.getTokenData().length > 0);
        }
        if (AbstractConnector.useTls(param)) {
            LoggerFactory.getLogger(getClass()).info("Hint: Aiming for TLS via identity store key {}", 
                param.getKeystoreKey());
        }
        // setup mock output
        trigger();
    }
    
    /**
     * Handles received data.
     * 
     * @param data the data
     * @param notifInterval the notification interval causing a sleep if data was sent, ignored if not positive
     */
    private void handleReceived(CO data, int notifInterval) {
        if (callback != null) {
            boolean send = cachingStrategy.checkCache(data);
            LoggerFactory.getLogger(MockingConnectorServiceWrapper.class)
                .info("Received {} passing on {}", data, send);
            if (send && callback != null) {
                callback.received(data);
                if (notifInterval > 0) {
                    TimeUtils.sleep(notifInterval);
                }
            }
        } else {
            LoggerFactory.getLogger(getClass()).info("No callback for data");
        }
    }
    
    /**
     * Runnable for parallel data ingestion.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class DataRunnable implements Runnable {

        private boolean cont = true;
        
        @Override
        public void run() {
            try {
                startData(() -> cont);
            } catch (IOException e) {
                LoggerFactory.getLogger(MockingConnectorServiceWrapper.class).info(
                    "Starting data: {}", e.getMessage());
            }
        }

        /**
         * Stops ingestion.
         */
        public void stop() {
            cont = false;
        }
        
    }
    
    /**
     * Starts a parallel thread for data ingestion. Non-blocking execution is typically required by a streaming engine.
     */
    private void startDataThread() {
        if (null == dataRunnable) {
            dataRunnable = new DataRunnable();
            new Thread(dataRunnable).start();
        }
    }
    
    @Override
    public void setState(ServiceState state) throws ExecutionException {
        doSetState(state);
        if (ServiceState.STARTING == state) {
            if (enableNotifications) {
                startDataThread();
            }
            doSetState(ServiceState.RUNNING);
        } else if (ServiceState.STOPPING == state) {
            if (null != dataRunnable) {
                dataRunnable.stop();
            }
            dataRunnable = null;
            callback = null;
            doSetState(ServiceState.STOPPED);
            if (null != recorder) {
                recorder.close();
            }
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
    public void enablePolling(boolean enablePolling) {
        if (!enableNotifications && enablePolling) {
            startDataThread();
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
        System.out.println("Connector " + getId() + ": " + data);
        if (null != recorder) {
            recorder.record("data", data);
        }
    }
    
    /**
     * Sets a callback on data received from the platform.
     * 
     * @param inputCallback the input callback, may be <b>null</b> for none
     */
    public void setInputCallback(ReceptionCallback<CI> inputCallback) {
        this.inputCallback = inputCallback;
    }

    @Override
    public void trigger() {
        if (null == triggerIterator) {
            LoggerFactory.getLogger(getClass()).info("Opening trigger resource: {}", fileName);
            try {
                triggerIterator = DataMapper.mapJsonDataToIterator(getDataStream(fileName), 
                    DataMapper.createBaseDataUnitClass(connectorOutType));
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("While opening trigger resource {}: {}", 
                    fileName, e.getMessage());
            }
        }
        if (null != triggerIterator) { // trigger would overlap with 
            int period = 0;            
            try {
                while (triggerIterator.hasNext()) {
                    CO next = triggerIterator.next();
                    BaseDataUnitFunctions bduf = (BaseDataUnitFunctions) next; // see createBaseDataUnitClass
                    boolean endless = bduf.get$repeats() < 0;
                    boolean once = bduf.get$repeats() == 0;
                    int count = 0;
                    while (endless || once || count < bduf.get$repeats()) {
                        LoggerFactory.getLogger(getClass()).info("Ingesting data for {} "
                            + "[endless {}, once {}, count {}]: {}", getId(), endless, once, count, next);
                        handleReceived(next, notificationInterval);
                        period = bduf.get$period();
                        if (period > 0) {
                            TimeUtils.sleep(period);
                        }
                        count++;
                        if (once) {
                            break;
                        }
                    }
                }
                LoggerFactory.getLogger(getClass()).info("Mocking data processed for {}, stopping trigger.", getId());
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("While processing trigger on {}: {}", 
                    fileName, e.getMessage());
            }
        } else {
            LoggerFactory.getLogger(getClass()).info("Trigger received but no data. Ignoring");
        }
    }

    @Override
    public void trigger(ConnectorTriggerQuery query) {
        trigger(); // preliminary, ignore query, take data from file
    }

    @Override
    public void setStorageValue(String key, Object value) {
        if (null != key) {
            if (null == storage) {
                storage = new HashMap<>();
            }
            storage.put(key, value);
        }
    }

    @Override
    public Object getStorageValue(String key) {
        return null == storage || null == key ? null : storage.get(key);
    }

    @Override
    public void setDataTimeDifference(int difference) {
        if (difference >= 0) {
            this.notificationInterval = difference;
        }
    }
        
}
