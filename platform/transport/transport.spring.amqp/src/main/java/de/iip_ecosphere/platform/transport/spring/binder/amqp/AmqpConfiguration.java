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

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter.TransportParameterBuilder;

/**
 * Represents the configuration options of an AMQP client.
 * 
 * Currently, we support simple user name / plain password authentication for testing. [UKL security]
 * 
 * @author Holger Eichelberger, SSE
 */
@ConfigurationProperties(prefix = "amqp")
public class AmqpConfiguration {
    
    private String host;
    private int port; // in test, consider overriding initializer for ephemeral port
    private List<String> filteredTopics = new ArrayList<String>();
    private String user = "";
    private String password = "";
    
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
     * Returns the broker port number.
     * 
     * @return the broker port number to connect to
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Returns the user name.
     * 
     * @return the user name (empty by default)
     */
    public String getUser() {
        return user;
    }

    /**
     * Returns the password.
     * 
     * @return the password (empty by default)
     */
    public String getPassword() {
        return password;
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
     * Changes the user name. [required by Spring]
     * 
     * @param user the user name
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Changes the password. [required by Spring]
     * 
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
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

    // converter
    
    /**
     * Turns the actual configuration into a {@link TransportParameter} instance.
     * 
     * @return the transport parameter instance
     */
    public TransportParameter toTransportParameter() {
        return TransportParameterBuilder.newBuilder(getHost(), getPort()).build();
    }

}
