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

package de.iip_ecosphere.platform.transport.spring.binder.mqttv3;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter.TransportParameterBuilder;
import de.iip_ecosphere.platform.transport.connectors.basics.MqttQoS;

/**
 * Represents the configuration options of a MQTT v3 client.
 * 
 * @author Holger Eichelberger, SSE
 */
@ConfigurationProperties(prefix = "mqtt")
public class MqttConfiguration {
    
    private String host;
    private int port; // in test, consider overriding initializer for ephemeral port
    private String schema = "tcp";
    private String clientId;
    private boolean autoClientId = true;
    private int keepAlive = 60000;
    private int actionTimeout = 1000;
    private List<String> filteredTopics = new ArrayList<String>();
    private String qos = MqttQoS.AT_LEAST_ONCE.name();
    
    /**
     * Returns whether {@code topic} is a filtered topic, i.e., we shall not subscribe to this topic.
     * 
     * @param topic the topic name
     * @return {@code true} if the topic is filtered (no subscription), {@code false} else
     */
    public boolean isFilteredTopic(String topic) {
        return filteredTopics.contains(topic);
    }
    
    /**
     * Returns all filtered topics.
     * 
     * @return the filtered topics
     * @see #isFilteredTopic(String)
     */
    public List<String> getFilteredTopics() {
        return filteredTopics;
    }

    /**
     * Returns the broker host name.
     * 
     * @return the broker host name
     */
    public String getHost() {
        return host;
    }
    
    /**
     * Returns the broker connection string consisting of {@link #getSchema()}, {@link #getHost()} 
     * and {@link #getPort()}.
     * 
     * @return the broker connection string
     */
    public String getBrokerString() {
        return getSchema() + "://" + getHost() + ":" + getPort();
    }
    
    /**
     * Returns the broker port number.
     * 
     * @return the broker port number to connect to
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Returns the connection schema.
     * 
     * @return the connection schema ("tcp" by default)
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Returns the client identification.
     * 
     * @return the client identification
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Returns whether making the client ID unique is enabled.
     * 
     * @return {@code true} for enabled (default), {@code false} else
     */
    public boolean getAutoClientId() {
        return autoClientId;
    }

    /**
     * Returns the keep-alive time between heartbeats.
     * 
     * @return the keep-alive time in ms (60000 by default)
     */
    public int getKeepAlive() {
        return keepAlive;
    }

    /**
     * Returns the action timeout to wait for the broker to complete an action.
     *
     * @return the action timeout in ms (1000 by default)
     */
    public int getActionTimeout() {
        return actionTimeout;
    }
    
    /**
     * Returns the QoS level for sending.
     * 
     * @return the QoS level
     */
    public MqttQoS getQos() {
        return MqttQoS.valueOf(qos);
    }

    // setters required for @ConfigurationProperties

    /**
     * Changes the broker host name. [required by Spring]
     * 
     * @param host the broker host name
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Defines the broker port number. [required by Spring]
     * 
     * @param port the broker port number to connect to
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Changes the connection schema. [required by Spring]
     * 
     * @param schema the connection schema
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * Changes the client identification. [required by Spring]
     * 
     * @param clientId the client identification
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Changes whether the client identification is expected to be unique or shall be made unique upon first connect.
     * [required by Spring]
     * 
     * @param autoClientId {@code true} (default) for make unique, {@code false} else
     */
    public void setAutoClientId(boolean autoClientId) {
        this.autoClientId = autoClientId;
    }

    /**
     * Changes the keep-alive time between heartbeats. [required by Spring]
     * 
     * @param keepAlive the keep-alive time in ms
     */
    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    /**
     * Changes the action timeout to wait for the broker to complete an action. [required by Spring]
     *
     * @param actionTimeout the action timeout in ms (60000 by default)
     */
    public void setActionTimeout(int actionTimeout) {
        this.actionTimeout = actionTimeout;
    }

    /**
     * Changes all filtered topics. [required by Spring]
     * 
     * @param filteredTopics the new filtered topics
     * @see #isFilteredTopic(String)
     */
    public void setFilteredTopics(List<String> filteredTopics) {
        this.filteredTopics = filteredTopics;
    }

    /**
     * Defines the QoS level.
     * 
     * @param qos the QoS level, ignored if invalid, see {@link MqttQoS}
     */
    public void setQos(String qos) {
        try {
            MqttQoS.valueOf(qos.toUpperCase());
            this.qos = qos.toUpperCase();
        } catch (IllegalArgumentException e) {
            // ignore for now, leave as is
        }
    }

    // converter
    
    /**
     * Turns the actual configuration into a {@link TransportParameter} instance.
     * 
     * @return the transport parameter instance
     */
    public TransportParameter toTransportParameter() {
        return TransportParameterBuilder
           .newBuilder(getHost(), getPort())
           .setApplicationId(getClientId())
           .setAutoApplicationId(getAutoClientId())
           .setActionTimeout(getActionTimeout())
           .setKeepAlive(getKeepAlive()).build();
    }

}
