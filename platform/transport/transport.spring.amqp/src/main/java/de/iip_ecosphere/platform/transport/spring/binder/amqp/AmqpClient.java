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

package de.iip_ecosphere.platform.transport.spring.binder.amqp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import de.iip_ecosphere.platform.support.net.SslUtils;
import de.iip_ecosphere.platform.transport.connectors.impl.AbstractTransportConnector;

/**
 * An AMQP client for a single binder instance. Typically, different binders subscribe to different
 * topics. The implementation uses queuing/a consumer pattern to cope with threading problems.
 * 
 * Partially public for testing.
 * 
 * @author Holger Eichelberger, SSE
 */
@Component
public class AmqpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpMessageBinder.class); // map all to binder
    private static AmqpClient lastInstance;
    private Connection connection;
    private Channel channel;
    private AmqpConfiguration configuration;
    private Set<String> topics = Collections.synchronizedSet(new HashSet<>());
    private Map<String, String> tags = Collections.synchronizedMap(new HashMap<>());
    
    /**
     * Creates and registers an instance.
     */
    public AmqpClient() {
        lastInstance = this;
    }
    
    /**
     * Returns the last instance created for this class. [testing]
     * 
     * @return the last instance
     */
    public static AmqpClient getLastInstance() {
        return lastInstance;
    }
    
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
         * @param payload the payload of the message
         */
        public void messageArrived(String topic, byte[] payload);

    }
    
    /**
     * Ensures the existence of a queue for the given {@code topic}.
     * 
     * @param topic the topic
     * @throws IOException if registering the queue fails
     */
    private void ensureTopicQueue(String topic) throws IOException {
        if (!topics.contains(topic)) {
            channel.queueDeclare(topic, false, false, true, null);
            topics.add(topic);
        }
    }
    
    /**
     * Returns the actual configuration. [for testing]
     * 
     * @return the configuration, may be <b>null</b>
     */
    public AmqpConfiguration getConfiguration() {
        return configuration;
    }
    
    /**
     * Creates the client based on a given AMQP client configuration.
     * 
     * @param config the AMQP configuration to take the connection information from
     */
    @SuppressWarnings("deprecation")
    public synchronized void createClient(AmqpConfiguration config) {
        if (null == channel) {
            try {
                configuration = config;
                ConnectionFactory factory = new ConnectionFactory();
                LOGGER.info("AMQP: Connecting to " + config.getHost() + " " + config.getPort());
                factory.setHost(config.getHost());
                factory.setPort(config.getPort());
                factory.setAutomaticRecoveryEnabled(true);
                boolean authDone = AbstractTransportConnector.applyAuthenticationKey(config.getAuthenticationKey(), 
                    (user, pwd, enc) -> {
                        factory.setUsername(user);
                        factory.setPassword(pwd);
                        return true;
                    }
                );
                if (!authDone) {
                    factory.setUsername(config.getUser());
                    factory.setPassword(config.getPassword());
                }
                if (null != config.getKeystore()) {
                    try {                
                        factory.useSslProtocol(SslUtils.createTlsContext(config.getKeystore(), 
                            AbstractTransportConnector.getKeystorePassword(config.getKeyPassword()), 
                            config.getKeyAlias()));
                    } catch (IOException e) {
                        LOGGER.error("AMQP: Loading keystore " + e.getMessage() + ". Trying with no TLS.");
                    }
                }
                connection = factory.newConnection();
                channel = connection.createChannel();
                // now subscriptions shall happen directly
            } catch (IOException | TimeoutException e) {
                LOGGER.error("Creating AMQP client: " + e.getMessage(), e);
            }                
        }
    }
    
    /**
     * Stops the client.
     */
    public void stopClient() {
        try {
            List<String> tpcs = new ArrayList<String>(topics);
            for (String t: tpcs) {
                unsubscribeFrom(t);
            }
            channel.close();
            topics.clear();
            channel = null;
            connection.close();
            connection = null;
        } catch (IOException | TimeoutException e) {
            LOGGER.error("Stopping AMQP client: " + e.getMessage(), e);
        }
    }

    /**
     * Subscribes to {@code topic} if {@code topic} is not blacklisted by 
     * {@link AmqpConfiguration#isFilteredTopic(String)}.
     * 
     * @param topic the topic to unsubscribe from
     * @param arrivedCallback the callback to be called when a message arrived
     * @return {@code true} if done/successful, {@code false} else
     */
    boolean subscribeTo(String topic, ArrivedCallback arrivedCallback) {
        boolean done = false;
        if (!configuration.isFilteredTopic(topic) && null != channel) {
            try {
                ensureTopicQueue(topic);
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    arrivedCallback.messageArrived(delivery.getEnvelope().getRoutingKey(), delivery.getBody());
                    //channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false); autoack below
                };
                String tag = UUID.randomUUID().toString();
                channel.basicConsume(topic, true, tag, deliverCallback, consumerTag -> { });
                tags.put(topic, tag);
                LOGGER.info("Subscribed to " + topic);
                done = true;
            } catch (IOException e) {
                LOGGER.error("Subscribing to AMQP broker: " + e.getMessage(), e);
            }
        }
        return done;
    }
    
    /**
     * Unsubscribes from {@code topic} if {@code topic} is not blacklisted by 
     * {@link AmqpConfiguration#isFilteredTopic(String)}.
     * 
     * @param topic the topic to unsubscribe from
     * @return {@code true} if done/successful, {@code false} else
     */
    boolean unsubscribeFrom(String topic) {
        boolean done = false;
        if (null != channel && !configuration.isFilteredTopic(topic)) {
            if (!topics.contains(topic)) {
                try {
                    topics.remove(topic);
                    String tag = tags.remove(topic);
                    if (null != tag) {
                        channel.basicCancel(tag);
                    }
                    LOGGER.info("Unsubscribed from " + topic);
                    done = true;
                } catch (IOException e) {
                    LOGGER.error("Unsubscribing from AMQP broker: " + e.getMessage(), e);
                }
            }
        }
        return done;
    }
    
    /**
     * Sends {@code payload} to {@code topic}.
     * 
     * @param topic the topic to send to
     * @param payload the payload to send
     */
    void send(String topic, byte[] payload) {
        if (null != channel) {
            try {
                ensureTopicQueue(topic);
                channel.basicPublish("", topic, null, payload);
            } catch (AlreadyClosedException | IOException e) {
                LOGGER.error("Sending to AMQP broker: " + e.getMessage());
            }
        }
    }
    
}
