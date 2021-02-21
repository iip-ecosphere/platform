/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.connectors.mqttv3;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import de.iip_ecosphere.platform.connectors.AbstractChannelConnector;
import de.iip_ecosphere.platform.connectors.ChannelAdapterSelector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.MachineConnector;
import de.iip_ecosphere.platform.connectors.types.ChannelProtocolAdapter;
import de.iip_ecosphere.platform.transport.connectors.basics.MqttQoS;
import de.iip_ecosphere.platform.transport.connectors.impl.AbstractTransportConnector;

/**
 * Implements the generic MQTT v3 connector. Requires {@link ConnectorParameter#getApplicationId()} 
 * and {@link ConnectorParameter#getKeepAlive()}. Do not rename, this class is referenced in {@code META-INF/services}.
 * 
 * This implementation is potentially not thread-safe, i.e., it may require a sending queue.
 * 
 * @param <CO> the output type to the IIP-Ecosphere platform
 * @param <CI> the input type from the IIP-Ecosphere platform
 * @author Holger Eichelberger, SSE
 */
@MachineConnector(hasModel = false, supportsEvents = true, supportsHierarchicalQNames = false, 
    supportsModelCalls = false, supportsModelProperties = false, supportsModelStructs = false)
public class PahoMqttv3Connector<CO, CI> extends AbstractChannelConnector<byte[], byte[], CO, CI> {

    public static final String NAME = "MQTT v3";
    private static final Logger LOGGER = Logger.getLogger(PahoMqttv3Connector.class.getName());
    private MqttAsyncClient client;

    /**
     * The descriptor of this connector (see META-INF/services).
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Descriptor implements ConnectorDescriptor {

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public Class<?> getType() {
            return PahoMqttv3Connector.class;
        }
        
    }
    
    /**
     * Creates a connector instance.
     * 
     * @param adapter the protocol adapter(s)
     * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty or adapters are <b>null</b>
     */
    @SafeVarargs
    public PahoMqttv3Connector(ChannelProtocolAdapter<byte[], byte[], CO, CI>... adapter) {
        this(null, adapter);
    }

    /**
     * Creates a connector instance.
     * 
     * @param selector the adapter selector (<b>null</b> leads to a default selector for the first adapter)
     * @param adapter the protocol adapter(s)
     * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty or adapters are <b>null</b>
     */
    @SafeVarargs
    public PahoMqttv3Connector(ChannelAdapterSelector<byte[], byte[], CO, CI> selector, 
        ChannelProtocolAdapter<byte[], byte[], CO, CI>... adapter) {
        super(selector, adapter);
    }

    /**
     * The internal reception callback.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class Callback implements MqttCallback {

        @Override
        public void connectionLost(Throwable cause) {
            // if reconnect allowed, do nothing else close client
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            received(message.getPayload());
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            // nothing
        }

    }
    
    @Override
    protected void connectImpl(ConnectorParameter params) throws IOException {
        try {
            String broker = "tcp://" + params.getHost() + ":" + params.getPort();
            String appId = AbstractTransportConnector.getApplicationId(params.getApplicationId(), "conn", 
                params.getAutoApplicationId());
            client = new MqttAsyncClient(broker, appId, new MemoryPersistence());
            client.setCallback(new Callback());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setKeepAliveInterval(params.getKeepAlive());
            connOpts.setAutomaticReconnect(true);
            waitForCompletion(client.connect(connOpts));
            for (String out : getOutputChannels()) {
                try {
                    waitForCompletion(client.subscribe(out, MqttQoS.AT_LEAST_ONCE.value()));
                } catch (MqttException e) {
                    throw new IOException(e);
                }
            }
        } catch (MqttException e) {
            throw new IOException(e);
        }
    }
    
    /**
     * Waits for completion until the {@code token} is processed.
     * 
     * @param token the token
     * @throws MqttException in case that processing of the token fails
     */
    private void waitForCompletion(IMqttToken token) throws MqttException {
        token.waitForCompletion(getConnectorParameter().getRequestTimeout());
    }

    @Override
    protected void disconnectImpl() throws IOException {
        try {
            waitForCompletion(client.disconnect());
            client.close();
        } catch (MqttException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void writeImpl(byte[] data, String channel) throws IOException {
        MqttMessage message = new MqttMessage(data);
        message.setQos(MqttQoS.AT_LEAST_ONCE.value());
        try {
            IMqttDeliveryToken token = client.publish(channel, message);
            waitForCompletion(token); // for now
        } catch (MqttException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected byte[] read() throws IOException {
        return null; // no polling at all needed
    }

    @Override
    protected void error(String message, Throwable th) {
        LOGGER.log(Level.SEVERE, message, th);
    }

}
