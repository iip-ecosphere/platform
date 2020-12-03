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
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import de.iip_ecosphere.platform.transport.Utils;

/**
 * A central AMQP client for all binders to reduce resource usage. Typically, different binders subscribe to different
 * topics. The implementation uses queuing/a consumer pattern to cope with threading problems.
 * 
 * Partially public for testing. Initial implementation, not optimized.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AmqpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpMessageBinder.class); // map all to binder
    private static Connection connection;
    private static Channel channel;
    private static AmqpConfiguration configuration;
    private static Deque<SendEntry> queue = new LinkedBlockingDeque<AmqpClient.SendEntry>();
    private static SendConsumer sendConsumer;
    private static Set<String> topics = Collections.synchronizedSet(new HashSet<>());
    
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
     * Represents a message to be send while queuing for sending.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class SendEntry {
        private String topic;
        private byte[] payload;
        
        /**
         * Creates a send entry.
         * 
         * @param topic the topic name
         * @param payload the payload
         */
        private SendEntry(String topic, byte[] payload) {
            this.topic = topic;
            this.payload = payload;
        }
    }
    
    /**
     * The send consumer running in parallel taking {@link SendEntry send entries} from {@link AmqpClient#queue} to
     * pass them on to the AMQP client for sending.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class SendConsumer implements Runnable {

        private boolean running = true;
        
        @Override
        public void run() {
            while (running) {
                SendEntry entry = queue.pollFirst();
                if (null != entry && null != channel) {
                    try {
                        ensureTopicQueue(entry.topic);
                        channel.basicPublish("", entry.topic, null, entry.payload);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                Utils.sleep(2);
            }
        }
        
    }
    
    /**
     * Ensures the existence of a queue for the given {@code topic}.
     * 
     * @param topic the topic
     * @throws IOException if registering the queue fails
     */
    private static void ensureTopicQueue(String topic) throws IOException {
        if (!topics.contains(topic)) {
            channel.queueDeclare(topic, false, false, true, null);
            topics.add(topic);
        }
    }
    
    /**
     * Creates the client based on a given AMQP client configuration.
     * 
     * @param config the AMQP configuration to take the connection information from
     */
    static synchronized void createClient(AmqpConfiguration config) {
        if (null == channel) {
            try {
                configuration = config;
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(config.getHost());
                factory.setPort(config.getPort());
                factory.setAutomaticRecoveryEnabled(true);
                factory.setUsername(config.getUser());
                factory.setPassword(config.getPassword());
                sendConsumer = new SendConsumer();
                new Thread(sendConsumer).start();
                connection = factory.newConnection();
                channel = connection.createChannel();
            } catch (IOException | TimeoutException e) {
                throw new RuntimeException(e);
            }                
        }
    }
    
    /**
     * Stops the client.
     */
    public static void stopClient() {
        try {
            sendConsumer.running = false;
            channel.close();
            topics.clear();
            channel = null;
            connection.close();
            connection = null;
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
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
    static boolean subscribeTo(String topic, ArrivedCallback arrivedCallback) {
        boolean done = false;
        if (!configuration.isFilteredTopic(topic) && null != channel) {
            try {
                ensureTopicQueue(topic);
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    arrivedCallback.messageArrived(delivery.getEnvelope().getRoutingKey(), delivery.getBody());
                };
                channel.basicConsume(topic, true, deliverCallback, consumerTag -> { });
                LOGGER.info("Subscribed to " + topic);
                done = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
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
    static boolean unsubscribeFrom(String topic) {
        boolean done = false;
        if (!configuration.isFilteredTopic(topic) && null != channel) {
            try {
                topics.remove(topic);
                channel.basicCancel(topic);
                LOGGER.info("Unsubscribed from " + topic);
                done = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
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
    static void send(String topic, byte[] payload) {
        queue.offer(new SendEntry(topic, payload));
    }
    
}
