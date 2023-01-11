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
        return setup;
    }

}
