/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.transport.connectors;

import java.io.Serializable;

import de.iip_ecosphere.platform.transport.connectors.TransportParameter.TransportParameterBuilder;

/**
 * Implements a reusable class to read transport setup information and to turn the information into transport 
 * parameters.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TransportSetup implements Serializable {
    
    private static final long serialVersionUID = 8110026253178816807L;
    private String host;
    private int port;
    private String password; // preliminary, AMQP
    private String user; // preliminary, AMQP
    
    /**
     * Returns the server/broker host name.
     * 
     * @return the server/broker host name.
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the server/broker port number.
     * 
     * @return the server/broker port
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the password.
     * 
     * @return the password (may be <b>null</b>, to be ignored then)
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the user name.
     * 
     * @return the user name (may be <b>null</b>, to be ignored then)
     */
    public String getUser() {
        return user;
    }

    /**
     * Defines the server/broker host name. [required by snakeyaml]
     * 
     * @param host the server/broker host name.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Defines the server/broker port number. [required by snakeyaml]
     * 
     * @param port the server/broker port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Defines the password. [required by snakeyaml]
     * 
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Defines the user name. [required by snakeyaml]
     * 
     * @param user the user name
     */
    public void setUser(String user) {
        this.user = user;
    }
    
    /**
     * Derives a transport parameter instance.
     * 
     * @return the transport parameter instance
     */
    public TransportParameter createParameter() {
        return TransportParameterBuilder.newBuilder(host, port)
            .setUser(user, password)
            .build();
    }

}
