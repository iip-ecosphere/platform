/********************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/
package de.iip_ecosphere.platform.transport.mqttv5;

import java.io.IOException;

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

import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.SslUtils;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.basics.AbstractMqttTransportConnector;
import de.iip_ecosphere.platform.transport.connectors.basics.MqttQoS;

/**
 * A MQTT v5 connector based on Eclipse Paho. Requires {@link TransportParameter#getApplicationId()}.
 * 
 * This implementation is potentially not thread-safe, i.e., it may require a sending queue.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PahoMqttV5TransportConnector extends AbstractMqttTransportConnector {

    public static final String NAME = "MQTT v5";
    private MqttAsyncClient client;
    private boolean tlsEnabled = false;

    /**
     * Creates a connector instance.
     */
    public PahoMqttV5TransportConnector() {
    }

    /**
     * The internal reception callback.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class Callback implements MqttCallback {

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            notifyCallback(topic, message.getPayload());
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
    public void connect(TransportParameter params) throws IOException {
        super.connect(params);
        try {
            String broker;
            if (params.getKeystore() != null) {
                broker = "ssl://";
            } else {
                broker = "tcp://";
            }
            broker += params.getHost() + ":" + params.getPort();
            client = new MqttAsyncClient(broker, getApplicationId(), new MemoryPersistence());
            client.setCallback(new Callback());
            MqttConnectionOptions connOpts = new MqttConnectionOptions();
            connOpts.setCleanStart(false);
            connOpts.setKeepAliveInterval(params.getKeepAlive());
            connOpts.setAutomaticReconnect(true);
            if (null != params.getKeystore()) {
                try {
                    connOpts.setHttpsHostnameVerificationEnabled(params.getHostnameVerification());
                    connOpts.setSocketFactory(SslUtils.createTlsContext(params.getKeystore(), 
                        params.getKeystorePassword(), params.getKeyAlias()).getSocketFactory());
                    tlsEnabled = true;
                } catch (IOException e) {
                    LoggerFactory.getLogger(getClass()).error("MQTT: Loading keystore " + e.getMessage() 
                        + ". Trying with no TLS.");
                }
            }
            waitForCompletion(client.connect(connOpts));
        } catch (MqttException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public void setReceptionCallback(String stream, ReceptionCallback<?> callback) throws IOException {
        super.setReceptionCallback(stream, callback);
        try {
            waitForCompletion(client.subscribe(stream, MqttQoS.AT_LEAST_ONCE.value()));
        } catch (MqttException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() throws IOException {
        try {
            waitForCompletion(client.disconnect());
            client.close();
        } catch (MqttException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public void asyncSend(String stream, Object data) throws IOException {
        send(stream, data, false);
    }

    @Override
    public void syncSend(String stream, Object data) throws IOException {
        send(stream, data, true);
    }

    /**
     * Sends {@code data} to {@code stream}.
     * 
     * @param stream the stream to send to
     * @param data   the data to send
     * @param block  shall this be a blocking or a non-blocking send operation
     * @throws IOException in case that sending fails for some reason
     */
    private void send(String stream, Object data, boolean block) throws IOException {
        byte[] payload = serialize(stream, data);
        MqttMessage message = new MqttMessage(payload);
        message.setQos(MqttQoS.AT_LEAST_ONCE.value());
        try {
            IMqttToken token = client.publish(stream, message);
            if (block) {
                waitForCompletion(token);
            }
        } catch (MqttException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * Waits for completion until the {@code token} is processed.
     * 
     * @param token the token
     * @throws MqttException in case that processing of the token fails
     */
    private void waitForCompletion(IMqttToken token) throws MqttException {
        token.waitForCompletion(getActionTimeout());
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Returns the supported encryption mechanisms.
     * 
     * @return the supported encryption mechanisms, may be <b>null</b> or empty
     */
    public String supportedEncryption() {
        return SslUtils.CONTEXT_ALG_TLS;
    }

    /**
     * Returns the actually enabled encryption mechanisms on this instance.
     * 
     * @return the enabled encryption mechanisms, may be <b>null</b> or empty
     */
    public String enabledEncryption() {
        return tlsEnabled ? SslUtils.CONTEXT_ALG_TLS : null;
    }

}
