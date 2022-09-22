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
import java.io.IOException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import de.iip_ecosphere.platform.support.identities.IdentityStore;
import de.iip_ecosphere.platform.support.net.SslUtils;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter.TransportParameterBuilder;
import de.iip_ecosphere.platform.transport.connectors.impl.AbstractTransportConnector;

/**
 * Defines a basic TLS-prepared configuration for binders.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BasicConfiguration {

    private String host;
    private int port; // in test, consider overriding initializer for ephemeral port
    private String keystoreKey;
    private File keystore;
    private String keyPassword;
    private String keyAlias;
    private boolean hostnameVerification = false;
    private String authenticationKey;

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
     * @deprecated use {@link #getKeystoreKey()} instead
     */
    @Deprecated
    public File getKeystore() {
        return keystore;
    }

    /**
     * Returns the optional identity key for the TLS keystore.
     * 
     * @return the identity key, <b>null</b> for none
     */
    public String getKeystoreKey() {
        return keystoreKey;
    }

    /**
     * Returns the password for the optional TLS keystore.
     * 
     * @return the TLS keystore password, may be <b>null</b> for none; the binder shall try a resolution
     *   via the {@link IdentityStore} to obtain a password token before using it as a plaintext password as 
     *   fallback
     * @deprecated use {@link #getKeystoreKey()} instead
     */
    @Deprecated
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
    
    /**
     * Returns whether TLS hostname verification shall be performed.
     * 
     * @return {@code false} for no verification (default), {@code true} else
     */
    public boolean getHostnameVerification() {
        return hostnameVerification;
    }

    /**
     * Returns the {@link IdentityStore} key for the authentication, usually a password token.
     * 
     * @return the identity store key, may be empty or <b>null</b>
     */
    public String getAuthenticationKey() {
        return authenticationKey;
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
     * Changes the optional identity key for the TLS keystore.
     * 
     * @param keystoreKey the identity key, <b>null</b> for none
     */
    public void setKeystoreKey(String keystoreKey) {
        this.keystoreKey = keystoreKey;
    }
    
    /**
     * Returns the optional TLS keystore.
     * 
     * @param keystore the TLS keystore (suffix ".jks" points to Java Key store, suffix ".p12" to PKCS12 keystore), may 
     *   be <b>null</b> for none
     * @deprecated use {@link #setKeystoreKey(String)} instead
     */
    @Deprecated
    public void setKeystore(File keystore) {
        this.keystore = keystore;
    }

    /**
     * Returns the password for the optional TLS keystore.
     * 
     * @param keyPassword the TLS keystore password, may be <b>null</b> for none; the binder shall try a resolution
     *   via the {@link IdentityStore} to obtain a password token before using it as a plaintext password as 
     *   fallback
     * @deprecated use {@link #setKeystoreKey(String)} instead
     */
    @Deprecated
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
    
    /**
     * Returns whether TLS hostname verification shall be performed.
     * 
     * @param hostnameVerification {@code false} for no verification, {@code true} else
     */
    public void setHostnameVerification(boolean hostnameVerification) {
        this.hostnameVerification = hostnameVerification;
    }

    /**
     * Returns the {@link IdentityStore} key for the authentication, usually a password token. [requred by SnakeYaml]
     * 
     * @param authenticationKey the identity store key, may be empty or <b>null</b>
     */
    public void setAuthenticationKey(String authenticationKey) {
        this.authenticationKey = authenticationKey;
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
        }
        builder.setKeystoreKey(getKeystoreKey());
        if (useTls()) {
            if (null != getKeyAlias()) {
                builder.setKeyAlias(getKeyAlias());
            }
            builder.setHostnameVerification(getHostnameVerification());
        }
        return builder;
    }
    
    /**
     * Returns whether the connector shall use TLS.
     * 
     * @return {@code true} for TLS enabled, {@code false} else
     */
    public boolean useTls() {
        return null != getKeystore() || null != getKeystoreKey();
    }

    /**
     * Helper method to determine a trust manager factory. Apply only if {@link #useTls(TransportParameter)}
     * returns {@code true}.
     * 
     * @return the trust manager factory
     * @throws IOException if creating the context or obtaining key information fails
     */
    public TrustManagerFactory createTrustManagerFactory() throws IOException {
        TrustManagerFactory result;
        if (null != getKeystoreKey()) {
            result = SslUtils.createTrustManagerFactory(IdentityStore.getInstance().getKeystoreFile(getKeystoreKey()));
        } else {
            result = SslUtils.createTrustManagerFactory(getKeystore(), 
                AbstractTransportConnector.getKeystorePassword(getKeyPassword()));
        }
        return result;
    }

    /**
     * Helper method to determine a SSL/TLS context. Apply only if {@link #useTls(TransportParameter)}
     * returns {@code true}. Relies on {@code IdentityStore#createTlsContext(String, String, String...)} if
     * {@link TransportParameter#getKeystoreKey()} is given, else on 
     * {@link SslUtils#createTlsContext(java.io.File, String, String)}.
     * 
     * @return the TLS context
     * @throws IOException if creating the context or obtaining key information fails
     */
    public SSLContext createTlsContext() throws IOException {
        SSLContext result;
        if (null != getKeystoreKey()) {
            result = IdentityStore.getInstance().createTlsContext(getKeystoreKey(), getKeyAlias());
        } else {
            result = SslUtils.createTlsContext(getKeystore(), 
                AbstractTransportConnector.getKeystorePassword(getKeyPassword()), getKeyAlias());
        }
        return result;
    }

}
