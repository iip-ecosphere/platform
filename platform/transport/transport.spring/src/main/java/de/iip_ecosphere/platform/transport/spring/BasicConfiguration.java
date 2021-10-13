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

package de.iip_ecosphere.platform.transport.spring;

import java.io.File;

import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter.TransportParameterBuilder;

/**
 * Defines a basic TLS-prepared configuration for binders.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BasicConfiguration {

    private String host;
    private int port; // in test, consider overriding initializer for ephemeral port
    private File keystore;
    private String keyPassword;
    private String keyAlias;

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
     * @return the TLS keystore, may be <b>null</b> for none
     */
    public String getKeyPassword() {
        return keyPassword;
    }

    /**
     * Returns the alias denoting the key to use.
     * 
     * @return the alias, may be <b>null</b> for none/first match
     */
    public String getKeyAlias() {
        return keyAlias;
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
     * Returns the optional TLS keystore.
     * 
     * @param keystore the TLS keystore (suffix ".jks" points to Java Key store, suffix ".p12" to PKCS12 keystore), may 
     *   be <b>null</b> for none
     */
    public void setKeystore(File keystore) {
        this.keystore = keystore;
    }

    /**
     * Returns the password for the optional TLS keystore.
     * 
     * @param keyPassword the TLS keystore, may be <b>null</b> for none
     */
    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    /**
     * Returns the alias denoting the key to use.
     * 
     * @param alias the alias, may be <b>null</b> for none/first match
     */
    public void setKeyAlias(String alias) {
        this.keyAlias = alias;
    }

    // converter

    /**
     * Turns the actual configuration into a {@link TransportParameter} instance.
     * When overriding this method, please consider {@link #createTransportParameterBuilder()} as
     * default implementation for transferring the settings in this class.
     * 
     * @return the transport parameter instance
     */
    public TransportParameter toTransportParameter() {
        return createTransportParameterBuilder().build();
    }
    
    /**
     * Turns the information in this class into a default transport parameter builder.
     * 
     * @return <code>builder</code>
     */
    protected TransportParameterBuilder createTransportParameterBuilder() {
        TransportParameterBuilder builder = TransportParameterBuilder.newBuilder(getHost(), getPort());
        if (null != getKeystore()) {
            builder.setKeystore(getKeystore(), getKeyPassword());
            if (null != getKeyAlias()) {
                builder.setKeyAlias(getKeyAlias());
            }
        }
        return builder;
    }

}
