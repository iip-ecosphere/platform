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
package de.iip_ecosphere.platform.transport.mqttv3;

import java.io.IOException;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.net.SslUtils;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.basics.AbstractMqttTransportConnector;
import de.iip_ecosphere.platform.transport.connectors.basics.MqttQoS;

/**
 * A MQTT v3 connector based on Eclipse Paho. Requires {@link TransportParameter#getApplicationId()}.
 * 
 * This implementation is potentially not thread-safe, i.e., it may require a sending queue.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PahoMqttV3TransportConnector extends AbstractMqttTransportConnector {

    public static final String NAME = "MQTT v3"; 
    
    private MqttAsyncClient client;
    private boolean tlsEnabled = false;
    private int qos = MqttQoS.AT_LEAST_ONCE.value();

    /**
     * Creates a connector instance.
     */
    public PahoMqttV3TransportConnector() {
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
            notifyCallback(topic, message.getPayload());
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            // nothing
        }

    }

    @Override
    public void connect(TransportParameter params) throws IOException {
        super.connect(params);
        this.qos = params.getMqttQoS().value();
        try {
            String broker;
            if (useTls(params)) {
                broker = "ssl://";
            } else {
                broker = "tcp://";
            }
            broker += params.getHost() + ":" + params.getPort();
            client = new MqttAsyncClient(broker, getApplicationId(), new MemoryPersistence());
            client.setCallback(new Callback());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(false);
            connOpts.setKeepAliveInterval(params.getKeepAlive());
            connOpts.setAutomaticReconnect(true);
            connOpts.setMaxInflight(1000); // preliminary, default 10
            applyAuthenticationKey(params.getAuthenticationKey(), (user, pwd, enc) -> {
                connOpts.setUserName(user);
                connOpts.setPassword(pwd.toCharArray());
                return true;
            }, () -> true);
            if (useTls(params)) {
                try {                
                    connOpts.setSocketFactory(createTlsContext(params).getSocketFactory());
                    connOpts.setHttpsHostnameVerificationEnabled(params.getHostnameVerification());
                    tlsEnabled = true;
                } catch (IOException e) {
                    LoggerFactory.getLogger(getClass()).error("MQTT: Loading keystore " + e.getMessage() 
                        + ". Trying with no TLS.");
                }
            }
            waitForCompletion(client.connect(connOpts));
            LoggerFactory.getLogger(getClass()).info("MQTT: connected");
        } catch (MqttException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void setReceptionCallback(String stream, ReceptionCallback<?> callback) throws IOException {
        super.setReceptionCallback(stream, callback);
        try {
            waitForCompletion(client.subscribe(stream, MqttQoS.AT_LEAST_ONCE.value()));
        } catch (MqttException e) {
            throw new IOException(e);
        }
    }
    
    @Override
    public void unsubscribe(String stream, boolean delete) throws IOException {
        super.unsubscribe(stream, delete);
        try {
            waitForCompletion(client.unsubscribe(stream));
        } catch (MqttException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void disconnect() throws IOException {
        super.disconnect();
        try {
            waitForCompletion(client.disconnect());
            client.close();
        } catch (MqttException e) {
            throw new IOException(e);
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
        message.setQos(qos);
        try {
            IMqttDeliveryToken token = client.publish(stream, message);
            if (block) {
                waitForCompletion(token);
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
        token.waitForCompletion(getActionTimeout());
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String supportedEncryption() {
        return SslUtils.CONTEXT_ALG_TLS;
    }

    @Override
    public String enabledEncryption() {
        return tlsEnabled ? SslUtils.CONTEXT_ALG_TLS : null;
    }
    
}
