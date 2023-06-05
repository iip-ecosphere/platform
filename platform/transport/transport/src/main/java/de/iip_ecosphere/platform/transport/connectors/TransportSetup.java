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

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.identities.IdentityStore;
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
    private String keystoreKey;
    private String keyAlias;
    private boolean hostnameVerification = false;
    private String authenticationKey;
    private int gatewayPort = 10000;
    private String netmask = "";
    
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
     * Returns the optional TLS keystore key pointing into the {@link IdentityStore}.
     * 
     * @return the TLS keystore key, may be <b>null</b> for none
     */
    public String getKeystoreKey() {
        return keystoreKey;
    }
    
    /**
     * Returns the alias of the key in {@link #getKeystoreKey()} to use.
     * 
     * @return the alias or <b>null</b> for none/first match
     */
    public String getKeyAlias() {
        return keyAlias;
    }
    
    /**
     * Returns the {@link IdentityStore} key for the authentication, usually a password token.
     * 
     * @return the identity store key, may be empty or <b>null</b>
     */
    public String getAuthenticationKey() {
        return authenticationKey;
    }
    
    /**
     * Returns whether TLS hostname verification shall be performed.
     * 
     * @return {@code false} for no verification (default), {@code true} else
     */
    public boolean getHostnameVerification() {
        return hostnameVerification;
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
     * Changes the optional TLS keystore key. [required by SnakeYaml]
     * 
     * @param keystoreKey the TLS keystore key, may be <b>null</b> for none
     */
    public void setKeystoreKey(String keystoreKey) {
        this.keystoreKey = keystoreKey;
    }
    
    /**
     * Returns the alias of the key in {@link #getKeystoreKey()} to use. [required by SnakeYaml]
     * 
     * @param keyAlias the alias or <b>null</b> for none/first match
     */
    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }
    
    /**
     * Returns the {@link IdentityStore} key for the authentication, usually a password token. [required by SnakeYaml]
     * 
     * @param authenticationKey the identity store key, may be empty or <b>null</b>
     */
    public void setAuthenticationKey(String authenticationKey) {
        this.authenticationKey = authenticationKey;
    }
    
    /**
     * Returns whether TLS hostname verification shall be performed. [required by SnakeYaml]
     * 
     * @param hostnameVerification {@code false} for no verification (default), {@code true} else
     */
    public void setHostnameVerification(boolean hostnameVerification) {
        this.hostnameVerification = hostnameVerification;
    }

    /**
     * Returns the transport gateway port (e.g., websocket).
     * 
     * @return the port, negative indicates that dependent on the context none or an ephemeral port shall be used
     */
    public int getGatewayPort() {
        return gatewayPort;
    }
    
    /**
     * Changes the transport gateway/websocket port. [snakeyaml]
     * 
     * @param gatewayPort the port, negative indicates that dependent on the context none or an ephemeral port
     *  shall be used
     */
    public void setGatewayPort(int gatewayPort) {
        this.gatewayPort = gatewayPort;
    }
    
    /**
     * Returns the netmask/network Java regex.
     * 
     * @return the netmask/network Java regex
     */
    public String getNetmask() {
        return netmask;
    }

    /**
     * Defines the netmask/network Java regex. [snakeyaml]
     * 
     * @param netmask the netmask
     */
    public void setNetmask(String netmask) {
        this.netmask = netmask;
    }
    
    /**
     * Returns the web socket server endpoint for a given {@code path}. The gateways server
     * enables simplified pub-sub communication with the UI.
     * 
     * @param schema the schema to use
     * @param path the path, may be empty, e.g., to obtain just an address
     * @return the endpoint
     * @see #getGatewayPort()
     */
    public Endpoint getGatewayServerEndpoint(Schema schema, String path) {
        return new Endpoint(schema, getHost(), getGatewayPort(), path);
    }
    
    /**
     * Returns whether this setup leads to a local gateway endpoint.
     * 
     * @return {@code true} for a local endpoint, {@code false} for a global
     */
    public boolean isLocalGatewayEndpoint() {
        return getGatewayPort() < 0;
    }

    /**
     * Derives a transport parameter instance.
     * 
     * @return the transport parameter instance
     */
    public TransportParameter createParameter() {
        return TransportParameterBuilder.newBuilder(host, port)
            .setKeystoreKey(keystoreKey)
            .setKeyAlias(keyAlias)
            .setHostnameVerification(hostnameVerification)
            .setAuthenticationKey(authenticationKey)
            .build();
    }
    
    /**
     * Copies this setup into a new instance.
     * 
     * @return the copied instance
     */
    public TransportSetup copy() {
        TransportSetup setup = new TransportSetup();
        setup.setAuthenticationKey(authenticationKey);
        setup.setHost(host);
        setup.setPort(port);
        setup.setHostnameVerification(hostnameVerification);
        setup.setKeyAlias(keyAlias);
        setup.setKeystoreKey(keystoreKey);
        setup.setGatewayPort(gatewayPort);
        return setup;
    }

}
