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

package de.iip_ecosphere.platform.connectors.mqttv5;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.AbstractChannelConnector;
import de.iip_ecosphere.platform.connectors.ChannelAdapterSelector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.MachineConnector;
import de.iip_ecosphere.platform.connectors.types.ChannelProtocolAdapter;
import de.iip_ecosphere.platform.transport.connectors.SslUtils;
import de.iip_ecosphere.platform.transport.connectors.basics.MqttQoS;
import de.iip_ecosphere.platform.transport.connectors.impl.AbstractTransportConnector;

/**
 * Implements the generic MQTT v5 connector. Requires {@link ConnectorParameter#getApplicationId()} 
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
public class PahoMqttv5Connector<CO, CI> extends AbstractChannelConnector<byte[], byte[], CO, CI> {

    public static final String NAME = "MQTT v5";
    private static final Logger LOGGER = Logger.getLogger(PahoMqttv5Connector.class.getName());
    private MqttAsyncClient client;
    private boolean tlsEnabled = false;

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
            return PahoMqttv5Connector.class;
        }
        
    }
    
    /**
     * Creates a connector instance.
     * 
     * @param adapter the protocol adapter(s)
     */
    @SafeVarargs
    public PahoMqttv5Connector(ChannelProtocolAdapter<byte[], byte[], CO, CI>... adapter) {
        this(null, adapter);
    }

    /**
     * Creates a connector instance.
     * 
     * @param selector the adapter selector (<b>null</b> leads to a default selector for the first adapter)
     * @param adapter the protocol adapter(s)
     */
    @SafeVarargs
    public PahoMqttv5Connector(ChannelAdapterSelector<byte[], byte[], CO, CI> selector, 
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
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            received(message.getPayload());
        }

        @Override
        public void disconnected(MqttDisconnectResponse disconnectResponse) {
            // nothing
        }

        @Override
        public void mqttErrorOccurred(MqttException exception) {
            // nothing
        }

        @Override
        public void deliveryComplete(IMqttToken token) {
            // nothing
        }

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            // nothing
        }

        @Override
        public void authPacketArrived(int reasonCode, MqttProperties properties) {
            // nothing
        }

    }
    
    @Override
    protected void connectImpl(ConnectorParameter params) throws IOException {
        try {
            String broker;
            if (params.getKeystore() != null) {
                broker = "ssl://";
            } else {
                broker = "tcp://";
            }
            broker += params.getHost() + ":" + params.getPort();
            String appId = AbstractTransportConnector.getApplicationId(params.getApplicationId(), "conn", 
                    params.getAutoApplicationId());
            client = new MqttAsyncClient(broker, appId, new MemoryPersistence());
            client.setCallback(new Callback());
            MqttConnectionOptions connOpts = new MqttConnectionOptions();
            connOpts.setCleanStart(true);
            connOpts.setKeepAliveInterval(params.getKeepAlive());
            connOpts.setAutomaticReconnect(true);
            if (null != params.getKeystore()) {
                try {                
                    connOpts.setSocketFactory(SslUtils.createTlsContext(params.getKeystore(), 
                        params.getKeystorePassword(), params.getKeyAlias()).getSocketFactory());
                    connOpts.setHttpsHostnameVerificationEnabled(params.getHostnameVerification());
                    tlsEnabled = true;
                } catch (IOException e) {
                    LoggerFactory.getLogger(getClass()).error("MQTT: Loading keystore " + e.getMessage() 
                        + ". Trying with no TLS.");
                }
            }
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
            IMqttToken token = client.publish(channel, message);
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

    /**
     * Returns the supported encryption mechanisms.
     * 
     * @return the supported encryption mechanisms (comma-separated), may be <b>null</b> or empty
     */
    public String supportedEncryption() {
        return SslUtils.CONTEXT_ALG_TLS;
    }

    /**
     * Returns the actually enabled encryption mechanisms on this instance.
     * 
     * @return the enabled encryption mechanisms (comma-separated), may be <b>null</b> or empty
     */
    public String enabledEncryption() {
        return tlsEnabled ? SslUtils.CONTEXT_ALG_TLS : null;
    }

}
