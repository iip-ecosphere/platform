/********************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.transport.spring.binder.generic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter.TransportParameterBuilder;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * An generic client for a single binder instance. Typically, different binders subscribe to different
 * topics. The implementation uses queuing/a consumer pattern to cope with threading problems.
 * 
 * Partially public for testing.
 * 
 * @author Holger Eichelberger, SSE
 */
@Component
public class GenericClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericMessageBinder.class); // map all to binder
    private static GenericClient lastInstance;
    private TransportConnector connector;
    private boolean globalInstance = false;
    private GenericConfiguration configuration;
    private Map<String, ReceptionCallback<?>> topics = Collections.synchronizedMap(new HashMap<>());
    
    /**
     * Creates and registers an instance.
     */
    public GenericClient() {
        lastInstance = this;
    }
    
    /**
     * Returns the last instance created for this class. [testing]
     * 
     * @return the last instance
     */
    public static GenericClient getLastInstance() {
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
     * Returns the actual configuration. [for testing]
     * 
     * @return the configuration, may be <b>null</b>
     */
    public GenericConfiguration getConfiguration() {
        return configuration;
    }
    
    /**
     * Creates the client based on a given AMQP client configuration.
     * 
     * @param config the AMQP configuration to take the connection information from
     */
    public synchronized void createClient(GenericConfiguration config) {
        if (null == connector) {
            try {
                configuration = config;
                if (config.getHost() == null || config.getHost().trim().length() == 0) {
                    connector = Transport.getGlobalTransport().createConnector();
                    globalInstance = true;
                } else {
                    connector = TransportFactory.createConnector();
                    LOGGER.info("Generic: Connecting to " + config.getHost() + " " + config.getPort());
                    TransportParameterBuilder pBuilder = TransportParameterBuilder
                        .newBuilder(config.getHost(), config.getPort())
                        .setHostnameVerification(config.getHostnameVerification())
                        .setAuthenticationKey(config.getAuthenticationKey());
                    if (config.useTls()) {
                        pBuilder.setKeystoreKey(config.getKeystoreKey());
                    }
                    connector.connect(pBuilder.build());
                    globalInstance = false;
                }
            } catch (IOException e) {
                LOGGER.error("Creating generic client: " + e.getMessage(), e);
            }                
        }
    }
    
    /**
     * Stops the client.
     */
    public void stopClient() {
        try {
            List<String> tpcs = new ArrayList<String>(topics.keySet());
            for (String t: tpcs) {
                unsubscribeFrom(t);
            }
            if (!globalInstance) {
                connector.disconnect();
            }
            topics.clear();
            connector = null;
        } catch (IOException e) {
            LOGGER.error("Stopping generic client: " + e.getMessage(), e);
        }
    }

    /**
     * Subscribes to {@code topic} if {@code topic} is not blacklisted by 
     * {@link GenericConfiguration#isFilteredTopic(String)}.
     * 
     * @param topic the topic to unsubscribe from
     * @param arrivedCallback the callback to be called when a message arrived
     * @return {@code true} if done/successful, {@code false} else
     */
    boolean subscribeTo(String topic, ArrivedCallback arrivedCallback) {
        boolean done = false;
        if (!configuration.isFilteredTopic(topic) && null != connector) {
            try {
                ReceptionCallback<byte[]> callback = new ReceptionCallback<byte[]>() {

                    @Override
                    public void received(byte[] data) {
                        arrivedCallback.messageArrived(topic, data);
                    }

                    @Override
                    public Class<byte[]> getType() {
                        return byte[].class;
                    }
                
                };
                connector.setReceptionCallback(topic, callback);
                topics.put(topic, callback);
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
     * {@link GenericConfiguration#isFilteredTopic(String)}.
     * 
     * @param topic the topic to unsubscribe from
     * @return {@code true} if done/successful, {@code false} else
     */
    boolean unsubscribeFrom(String topic) {
        boolean done = false;
        if (null != connector && !configuration.isFilteredTopic(topic)) {
            ReceptionCallback<?> callback = topics.remove(topic);
            if (callback != null) {
                try {
                    connector.detachReceptionCallback(topic, callback);
                    LOGGER.info("Unsubscribed from {}", topic);
                } catch (IOException e) {
                    LOGGER.error("Unsubscribing from {}: {}", topic, e.getMessage(), e);
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
        if (null != connector) {
            try {
                connector.asyncSend(topic, payload);
            } catch (IOException e) {
                LOGGER.error("Sending generic: " + e.getMessage());
            }
        }
    }
    
}
