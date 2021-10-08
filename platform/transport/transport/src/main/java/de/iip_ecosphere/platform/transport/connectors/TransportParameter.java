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
package de.iip_ecosphere.platform.transport.connectors;

import java.io.File;

import de.iip_ecosphere.platform.support.ServerAddress;

/**
 * Captures common transport parameter for all connector types. Connectors shall document which of the
 * optional settings are required.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TransportParameter {

    private String host;
    private int port;
    private int actionTimeout = 1000;
    private String applicationId = "";
    private boolean autoApplicationId = true;
    private int keepAlive = 2000; 
    private File keystore;
    private String keyPassword;
    private String user; // preliminary, AMQP
    private String password; // preliminary, AMQP

    /**
     * A builder for transport parameter. Connectors shall indicate the required settings.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class TransportParameterBuilder {

        private TransportParameter instance;

        /**
         * Prevents external creation.
         */
        private TransportParameterBuilder() {
        }
        
        /**
         * Creates a new builder.
         * 
         * @param host     the network name of the host
         * @param port     the TCP communication port of the host
         * @return the builder instance
         */
        public static TransportParameterBuilder newBuilder(String host, int port) {
            TransportParameterBuilder builder = new TransportParameterBuilder();
            builder.instance = new TransportParameter(host, port);
            return builder;
        }

        /**
         * Creates a new builder.
         * 
         * @param addr the server address (schema ignored)
         * @return the builder instance
         */
        public static TransportParameterBuilder newBuilder(ServerAddress addr) {
            return newBuilder(addr.getHost(), addr.getPort());
        }

        /**
         * Defines the optional application id. Optional, remains empty if unset.
         * 
         * @param applicationId the client/application id
         * @return <b>this</b>
         */
        public TransportParameterBuilder setApplicationId(String applicationId) {
            instance.applicationId = applicationId;
            return this;
        }
        
        
        /**
         * Defines whether the application identification is expected to be unique or shall be made unique upon first 
         * connect. May be ignored if not applicable.
         * 
         * @param autoApplicationId {@code true} (default) for make unique, {@code false} else
         * @return <b>this</b>
         */
        public TransportParameterBuilder setAutoApplicationId(boolean autoApplicationId) {
            instance.autoApplicationId = autoApplicationId;
            return this;
        }

        /**
         * Sets the keep alive time. Optional, remains 2000 if unset.
         * 
         * @param keepAlive the time to keep a connection alive (heartbeat) in milliseconds
         * @return <b>this</b>
         */
        public TransportParameterBuilder setKeepAlive(int keepAlive) {
            instance.keepAlive = keepAlive;
            return this;
        }

        /**
         * Sets the action timeout. Optional, remains 1000 if unset.
         * 
         * @param actionTimeout the timeout in milliseconds for send/receive actions
         * @return <b>this</b>
         */
        public TransportParameterBuilder setActionTimeout(int actionTimeout) {
            instance.actionTimeout = actionTimeout;
            return this;
        }

        /**
         * Sets plain user information. Preliminary!!!
         * 
         * @param user the user name
         * @param password the password
         * @return <b>this</b>
         */
        public TransportParameterBuilder setUser(String user, String password) {
            instance.user = user;
            instance.password = password;
            return this;
        }

        /**
         * Sets up optional TLS encryption details.
         * 
         * @param keystore the TLS keystore (suffix ".jks" points to Java Key store, suffix ".p12" to PKCS12 keystore),
         *   may be <b>null</b> for none; validity of the type of keystore may depend on the transport connector 
         *   implementation, e.g., PKCS12 may not work with all forms
         * @param password the TLS keystore, may be <b>null</b> for none
         * @return <b>this</b>
         */
        public TransportParameterBuilder setKeystore(File keystore, String password) {
            instance.keystore = keystore;
            instance.keyPassword = password;
            return this;
        }

        /**
         * Returns the created instance.
         * 
         * @return the created instance
         */
        public TransportParameter build() {
            return instance;
        }
        
    }
    
    /**
     * Creates a transport parameter instance.
     * 
     * @param host     the network name of the host
     * @param port     the TCP communication port of the host
     */
    private TransportParameter(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Returns the network name of the host.
     * 
     * @return the name
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the TCP communication port of the host.
     * 
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the timeout for individual send/receive actions.
     * 
     * @return the timeout in milliseconds
     */
    public int getActionTimeout() {
        return actionTimeout;
    }

    /**
     * Returns the time to keep a connection alive.
     * 
     * @return the time in milliseconds
     */
    public int getKeepAlive() {
        return keepAlive;
    }

    /**
     * Returns the unique application/client identifier.
     * 
     * @return the unique application/client identifier
     */
    public String getApplicationId() {
        return applicationId;
    }
    
    /**
     * Returns whether the application identification is expected to be unique or shall be made unique upon first 
     * connect. May be ignored if not applicable.
     * 
     * @return {@code true} (default) for make unique, {@code false} else
     */
    public boolean getAutoApplicationId() {
        return autoApplicationId;
    }
    
    /**
     * Returns the password. [preliminary]
     * 
     * @return the password (may be <b>null</b>, to be ignored then)
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the user name. [preliminary]
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
     * @return the TLS keystore, may be <b>null</b> for none
     */
    public String getKeystorePassword() {
        return keyPassword;
    }

}
