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

package de.iip_ecosphere.platform.transport.spring.binder.hivemqv3;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import de.iip_ecosphere.platform.transport.connectors.impl.AbstractTransportConnector;

/**
 * A central HiveMq client for all binders to reduce resource usage. Typically, different binders subscribe to different
 * topics. 
 * 
 * Partially public for testing. Initial implementation, not optimized.
 * 
 * @author Holger Eichelberger, SSE
 */
public class HivemqV3Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(HivemqV3MessageBinder.class);
    private static Mqtt3AsyncClient client;
    private static HivemqV3Configuration configuration;
    private static MqttQos qos = MqttQos.AT_LEAST_ONCE;
    
    /**
     * Called when a message for a topic arrives.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface ArrivedCallback {

        /**
         * This method is called when a message arrives from the server.
         *
         * @param topic name of the topic on the message was published to
         * @param payload the actual payload.
         */
        public void messageArrived(String topic, byte[] payload);

    }
    
    /**
     * Creates the client based on a given MQTT client configuration.
     * 
     * @param config the MQTT configuration to take the connection information from
     */
    static synchronized void createClient(HivemqV3Configuration config) {
        if (null == client) {
            configuration = config;
            qos = config.getQos();
            String clientId = AbstractTransportConnector.getApplicationId(config.getClientId(), "stream", 
                config.getAutoClientId());
            LOGGER.info("Connecting to " + config.getPort() + "@" + config.getHost() 
                + " with client id " + clientId);
            
            Mqtt3AsyncClient cl = MqttClient.builder()
                .useMqttVersion3()
                .identifier(clientId)
                .serverHost(config.getHost())
                .serverPort(config.getPort())
                .automaticReconnect().applyAutomaticReconnect()
                //.useSslWithDefaultConfig()
                .buildAsync();

            cl.connectWith()
                //.simpleAuth()
                //    .username("my-user")
                //    .password("my-password".getBytes())
                //    .applySimpleAuth()
                .cleanSession(false)
                .keepAlive(config.getKeepAlive())
                .send()
                .whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        LOGGER.error("Connecting MQTT client: " + throwable.getMessage(), throwable);
                    } else {
                        client = cl;
                        // setup subscribes or start publishing
                    }
                }).join();
        }
    }
    
    /**
     * Stops the client.
     */
    public static void stopClient() {
        if (null != client) {
            client.disconnect()
                .whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        LOGGER.error("Connecting MQTT client: " + throwable.getMessage(), throwable);
                    } else {
                        client = null;
                    }
                });
        }
    }

    /**
     * Subscribes to {@code topic} if {@code topic} is not blacklisted by 
     * {@link HivemqV3Configuration#isFilteredTopic(String)}.
     * 
     * @param topic the topic to unsubscribe from
     * @param arrivedCallback the callback to be called when a message arrived
     * @return {@code true} if done/successful, {@code false} else
     */
    static boolean subscribeTo(final String topic, final ArrivedCallback arrivedCallback) {
        AtomicBoolean done = new AtomicBoolean(false);
        if (!configuration.isFilteredTopic(topic) && null != client) {
            client.subscribeWith()
                .topicFilter(topic)
                .callback(publish -> {
                    arrivedCallback.messageArrived(topic, publish.getPayloadAsBytes());
                })
                .send()
                .whenComplete((subAck, th) -> {
                    if (th != null) {
                        LOGGER.error("Subscribing to MQTT topic '" + topic + "': " + th.getMessage(), th);
                    } else {
                        LOGGER.info("Subscribed to " + topic);
                        done.set(true);
                    }
                }).join();
        }
        return done.get();
    }
    
    /**
     * Unsubscribes from {@code topic} if {@code topic} is not blacklisted by 
     * {@link HivemqV3Configuration#isFilteredTopic(String)}.
     * 
     * @param topic the topic to unsubscribe from
     * @return {@code true} if done/successful, {@code false} else
     */
    static boolean unsubscribeFrom(String topic) {
        AtomicBoolean done = new AtomicBoolean(false);
        if (!configuration.isFilteredTopic(topic) && null != client) {
            client.unsubscribeWith()
                .topicFilter(topic)
                .send()
                .whenComplete((subAck, th) -> {
                    if (th != null) {
                        LOGGER.error("Unsubscribing from MQTT topic '" + topic + "': " + th.getMessage(), th);
                    } else {
                        LOGGER.info("Unsubscribed from " + topic);
                        done.set(true);
                    }
                }).join();
        }
        return done.get();
    }
    
    /**
     * Sends {@code payload} to {@code topic}.
     * 
     * @param topic the topic to send to
     * @param payload the payload to send
     */
    static void send(String topic, byte[] payload) {
        if (null != client) {
            client.publishWith()
                .topic(topic)
                .payload(payload)
                .qos(qos)
                .send()
                .whenComplete((publish, th) -> {
                    if (th != null) {
                        LOGGER.error("Acquiring send lock: " + th.getMessage(), th);
                    }
                });
        }
    }
    
}
