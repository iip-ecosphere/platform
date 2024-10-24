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

import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.identities.IdentityStore;
import de.iip_ecosphere.platform.transport.connectors.basics.MqttQoS;

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
    private String keyAlias;
    private String keystoreKey;
    private boolean hostnameVerification = false;
    private String authenticationKey; // will replace user/password #22
    private MqttQoS qos = MqttQoS.AT_LEAST_ONCE;
    private CloseAction closeAction = CloseAction.UNSUBSCRIBE;
    
    /**
     * Automatic connector closing actions.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum CloseAction {
        NONE,
        UNSUBSCRIBE,
        DELETE;
        
        /**
         * Returns whether this close action indicates that streams/channels shall be auto-closed at all.
         * 
         * @return {@code true} for auto-close, {@code false} else
         */
        public boolean doClose() {
            return NONE != this;
        }

        /**
         * Returns whether this close action indicates that streams/channels shall be closed and deleted.
         * 
         * @return {@code true} for delete, {@code false} else
         */
        public boolean doDelete() {
            return DELETE == this;
        }

    }

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
         * Creates a transport parameter builder from (a copy of) the given transport parameters.
         * 
         * @param params the parameters to copy from
         * @return the transport parameter builder
         */
        public static TransportParameterBuilder newBuilder(TransportParameter params) {
            TransportParameterBuilder builder = new TransportParameterBuilder();
            builder.instance = new TransportParameter(params.host, params.port);
            builder.instance.actionTimeout = params.actionTimeout;
            builder.instance.applicationId = params.applicationId;
            builder.instance.autoApplicationId = params.autoApplicationId;
            builder.instance.authenticationKey = params.authenticationKey;
            builder.instance.keepAlive = params.keepAlive;
            builder.instance.keyAlias = params.keyAlias;
            builder.instance.keystoreKey = params.keystoreKey;
            builder.instance.hostnameVerification = params.hostnameVerification;
            builder.instance.authenticationKey = params.authenticationKey;
            builder.instance.qos = params.qos;
            builder.instance.closeAction = params.closeAction;
            return builder;
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
         * Sets up the optional TLS keystore key to be obtained from {@link IdentityStore}.
         * 
         * @param keystoreKey the (logical) key to access the keystore (<b>null</b> for none)
         * @return <b>this</b>
         */
        public TransportParameterBuilder setKeystoreKey(String keystoreKey) {
            instance.keystoreKey = keystoreKey;
            return this;
        }

        /**
         * Sets up optional TLS key alias.
         * 
         * @param alias key alias, may be <b>null</b> for none/first match
         * @return <b>this</b>
         */
        public TransportParameterBuilder setKeyAlias(String alias) {
            instance.keyAlias = alias;
            return this;
        }

        /**
         * Defines the {@link IdentityStore} key for the authentication, usually a password token.
         * 
         * @param authenticationKey the identity store key, may be empty or <b>null</b>
         * @return <b>this</b>
         */
        public TransportParameterBuilder setAuthenticationKey(String authenticationKey) {
            instance.authenticationKey = authenticationKey;
            return this;
        }
        
        /**
         * Defines whether TLS hostname verification shall be performed.
         * 
         * @param hostnameVerification {@code false} for no verification, {@code true} else
         * @return <b>this</b>
         */
        public TransportParameterBuilder setHostnameVerification(boolean hostnameVerification) {
            instance.hostnameVerification = hostnameVerification;
            return this;
        }
        
        /**
         * Defines the MQTT QoS level (may not apply to other protocols).
         *   
         * @param qos the QoS level (default is {@link MqttQoS#AT_LEAST_ONCE} 
         * @return <b>this</b>
         */
        public TransportParameterBuilder setMqttQoS(MqttQoS qos) {
            if (null != qos) {
                instance.qos = qos;
            }
            return this;
        }
        
        /**
         * Defines the close action.
         * 
         * @param action the action (default is {@link CloseAction#UNSUBSCRIBE})
         * @return <b>this</b>
         */
        public TransportParameterBuilder setCloseAction(CloseAction action) {
            if (null != action) {
                instance.closeAction = action;
            }
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
     * Returns the optional key to access the TLS keystore key to be obtained from {@link IdentityStore}.
     * 
     * @return the (logical) key to access the keystore, may be <b>null</b> for none
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
     * Returns the MQTT QoS level (may not apply to other protocols).
     *   
     * @return the QoS level 
     */
    public MqttQoS getMqttQoS() {
        return qos;
    }
    
    /**
     * Returns the close action.
     * 
     * @return the close action (default is {@link CloseAction#UNSUBSCRIBE})
     */
    public CloseAction getCloseAction() {
        return closeAction;
    }

}
