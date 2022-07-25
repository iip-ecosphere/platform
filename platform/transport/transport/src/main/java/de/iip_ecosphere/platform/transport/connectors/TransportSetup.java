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

import java.io.File;
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
    private File keystore;
    private String keyPassword;
    private String keyAlias;
    private boolean hostnameVerification = false;
    private String authenticationKey; // will replace user/password #22
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
     * Returns the optional TLS keystore.
     * 
     * @return the TLS keystore (suffix ".jks" points to Java Key store, suffix ".p12" to PKCS12 keystore), may 
     *   be <b>null</b> for none
     */
    public File getKeystore() {
        return keystore;
    }

    /**
     * Returns the password for the optional TLS keystore.
     * 
     * @return the TLS keystore password, may be <b>null</b> for none; the transport connector shall try a resolution
     *   via the {@link IdentityStore} to obtain a password token before using it as a plaintext password as 
     *   fallback
     */
    public String getKeystorePassword() {
        return keyPassword;
    }
    
    /**
     * Returns the alias of the key in {@link #getKeystore()} to use.
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
     * Defines the password. [required by snakeyaml]
     * 
     * @param password the password
     * @deprecated #22, use {@link #setAuthenticationKey(String)} instead
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Defines the user name. [required by snakeyaml]
     * 
     * @param user the user name
     * @deprecated #22, use {@link #setAuthenticationKey(String)} instead
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Returns the optional TLS keystore. [requred by SnakeYaml]
     * 
     * @param keystore the TLS keystore (suffix ".jks" points to Java Key store, suffix ".p12" to PKCS12 keystore), may 
     *   be <b>null</b> for none
     */
    public void setKeystore(File keystore) {
        this.keystore = keystore;
    }

    /**
     * Returns the password for the optional TLS keystore. [requred by SnakeYaml]
     * 
     * @param keyPassword the TLS keystore password, may be <b>null</b> for none; the transport connector shall try 
     *   a resolution via the {@link IdentityStore} to obtain a password token before using it as a plaintext password 
     *   as fallback
     */
    public void setKeystorePassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }
    
    /**
     * Returns the alias of the key in {@link #getKeystore()} to use. [requred by SnakeYaml]
     * 
     * @param keyAlias the alias or <b>null</b> for none/first match
     */
    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }
    
    /**
     * Returns the {@link IdentityStore} key for the authentication, usually a password token. [requred by SnakeYaml]
     * 
     * @param authenticationKey the identity store key, may be empty or <b>null</b>
     */
    public void setAuthenticationKey(String authenticationKey) {
        this.authenticationKey = authenticationKey;
    }
    
    /**
     * Returns whether TLS hostname verification shall be performed. [requred by SnakeYaml]
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
    @SuppressWarnings("deprecation")
    public TransportParameter createParameter() {
        return TransportParameterBuilder.newBuilder(host, port)
            .setUser(user, password)
            .setKeystore(keystore, keyPassword)
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
        setup.setKeystore(keystore);
        setup.setKeystorePassword(keyPassword);
        setup.setPassword(keyPassword);
        setup.setUser(user);
        return setup;
    }

}
