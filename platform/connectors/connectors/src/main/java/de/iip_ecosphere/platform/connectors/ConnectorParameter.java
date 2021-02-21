/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.connectors;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Map;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;

/**
 * Defines the connection parameters for a {@link Connector}. Specific connectors shall document required parameter.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConnectorParameter {

    public static final String ANY_ENDPOINT = "";
    public static final Schema DEFAULT_SCHEMA = Schema.TCP;
    public static final int DEFAULT_REQUEST_TIMEOUT = 5000;
    public static final int DEFAULT_NOTIFICATION_INTERVAL = 1000;
    public static final int DEFAULT_KEEP_ALIVE = 2000;
    
    // taken from OPC UA, preliminary
    private X509Certificate certificate;
    private KeyPair keyPair;
    private Map<String, IdentityToken> identityToken;
    private Schema schema = DEFAULT_SCHEMA;
    private int port;
    private String host;
    private int requestTimeout = DEFAULT_REQUEST_TIMEOUT;
    private String endpointPath = "";
    private String applicationId = "";
    private boolean autoApplicationId = true;
    private String applicationDescription = "";
    private int notificationInterval = DEFAULT_NOTIFICATION_INTERVAL;
    private int keepAlive = DEFAULT_KEEP_ALIVE;
    
    /**
     * Builds a connector parameter object.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class ConnectorParameterBuilder {
        
        private ConnectorParameter instance;

        /**
         * Prevents external creation.
         */
        private ConnectorParameterBuilder() {
        }
        
        /**
         * Creates a new connector parameter builder with required basic information. Schema is set to 
         * {@link ConnectorParameter#DEFAULT_SCHEMA}.
         * 
         * @param host the host to connect to
         * @param port the port to connect to
         * @return the connector parameter builder
         */
        public static ConnectorParameterBuilder newBuilder(String host, int port) {
            return newBuilder(host, port, null);
        }

        /**
         * Creates a new connector parameter builder with required basic information.
         * 
         * @param host the host to connect to
         * @param port the port to connect to
         * @param schema protocol schema, usually ({@link ConnectorParameter#DEFAULT_SCHEMA} 
         *     if value is <b>null</b>)
         * @return the connector parameter builder
         */
        public static ConnectorParameterBuilder newBuilder(String host, int port, Schema schema) {
            ConnectorParameterBuilder builder = new ConnectorParameterBuilder();
            builder.instance = new ConnectorParameter(host, port);
            if (null != schema) {
                builder.instance.schema = schema;
            }
            return builder;
        }        

        /**
         * Creates a new connector parameter builder with required basic information.
         * 
         * @param addr the server address
         * @return the connector parameter builder
         */
        public static ConnectorParameterBuilder newBuilder(ServerAddress addr) {
            return newBuilder(addr.getHost(), addr.getPort(), addr.getSchema());
        }
        
        /**
         * Sets a connector-dependent endpoint path, a URL path. Optional, remains an empty string if not called.
         * 
         * @param endpointPath the endpoint path
         * @return <b>this</b>
         */
        public ConnectorParameterBuilder setEndpointPath(String endpointPath) {
            instance.endpointPath = endpointPath;
            return this;
        }
        
        /**
         * Sets the request timeout.
         * 
         * @param requestTimeout the request timeout. Optional, if not called uses the default value of 
         * {@value ConnectorParameter#DEFAULT_REQUEST_TIMEOUT} ms.
         * @return <b>this</b>
         */
        public ConnectorParameterBuilder setRequestTimeout(int requestTimeout) {
            instance.requestTimeout = requestTimeout;
            return this;
        }

        /**
         * Sets the keep alive time for connection heartbeats/reconnects (if supported).
         * 
         * @param keepAlive the keep alive time. Optional, if not called uses the default value of 
         * {@value ConnectorParameter#DEFAULT_KEEP_ALIVE} ms.
         * @return <b>this</b>
         */
        public ConnectorParameterBuilder setKeepAlive(int keepAlive) {
            instance.keepAlive = keepAlive;
            return this;
        }
        
        /**
         * Defines the notification interval, i.e., how frequently the connector shall look for new
         * values. This may happen via events, notifications or polling depending on the connector
         * implementation.
         * 
         * @param notificationInterval the notification interval in ms, disabled if less than 1, default is 
         * {@value ConnectorParameter#DEFAULT_NOTIFICATION_INTERVAL}
         * @return the polling period
         */
        public ConnectorParameterBuilder setNotificationInterval(int notificationInterval) {
            instance.notificationInterval = notificationInterval;
            return this;
        }

        /**
         * Sets connector-dependent application information. Optional, if not called both settings will remain empty 
         * strings. 
         * 
         * @param applicationId String/URL to identify the application. 
         * @param applicationDescription application description. 
         * @return <b>this</b>
         */
        public ConnectorParameterBuilder setApplicationInformation(String applicationId, 
            String applicationDescription) {
            instance.applicationId = applicationId;
            instance.applicationDescription = applicationDescription;
            return this;
        }
        
        /**
         * Defines whether the application identification is expected to be unique or shall be made unique upon first 
         * connect. May be ignored if not applicable.
         * 
         * @param autoApplicationId {@code true} (default) for make unique, {@code false} else
         * @return <b>this</b>
         */
        public ConnectorParameterBuilder setAutoApplicationId(boolean autoApplicationId) {
            instance.autoApplicationId = autoApplicationId;
            return this;
        }
        
        /**
         * Sets security information for encryption. Optional, if not called no encryption will be used.
         * 
         * @param certificate the client certificate (may be <b>null</b> for no certificate/no TLS)
         * @param keyPair the client certificate (may be <b>null</b> for no encryption/no TLS)
         * @return <b>this</b>
         */
        public ConnectorParameterBuilder setSecurityInformation(X509Certificate certificate, KeyPair keyPair) {
            instance.certificate = certificate;
            instance.keyPair = keyPair;
            return this;
        }

        /**
         * Sets the endpoint identities. Optional, if not called, anonymous identity is assumed.
         * 
         * @param identityToken the client identity token per endpoint URL (may be <b>null</b> for anonymous), an 
         *   {@link #ANY_ENDPOINT} denotes just all endpoints
         * @return <b>this</b>
         */
        public ConnectorParameterBuilder setIdentities(Map<String, IdentityToken> identityToken) {
            instance.identityToken = identityToken;
            return this;
        }
        
        /**
         * Creates the instance.
         * 
         * @return the created instance
         */
        public ConnectorParameter build() {
            return instance;
        }
        
    }
    
    /**
     * Creates a connector parameter instance.
     * 
     * @param port the port to connect to
     * @param host the host to connect to
     */
    private ConnectorParameter(String host, int port) {
        this.port = port;
        this.host = host;
    }
    
    /**
     * Returns the TSL certificate.
     * 
     * @return the certificate (may be <b>null</b> for no encryption)
     */
    public X509Certificate getClientCertificate() {
        return certificate;
    }
    
    /**
     * Returns the TLS encryption key pair.
     * 
     * @return the key pair (may be <b>null</b> for no encryption)
     */
    public KeyPair getClientKeyPair() {
        return keyPair;
    }

    /**
     * Returns the identity token.
     * 
     * @param endpointUrl the endpoint URL to return the token for 
     * @return the identity token (may be <b>null</b> for anonymous identity)
     */
    public IdentityToken getIdentityToken(String endpointUrl) {
        IdentityToken token = null;
        if (null != identityToken) {
            token = identityToken.get(endpointUrl);
            if (null == token) {
                token = identityToken.get("");
            }
        }
        return token;
    }
    
    /**
     * Returns whether there is any identity or the client just runs in anonymous mode.
     * 
     * @return {@code true} for totally anonymous, {@code false} for at least some identities
     */
    public boolean isAnonymousIdentity() {
        return null == identityToken;
    }
    
    /**
     * Helps determining in case of multiple endpoint URLs which ones are more feasible.
     * Might not be used by all connector implementations.
     * 
     * @param endpointUrl the endpoint URL in question
     * @param securityLevel connector specific information about the security level
     * @return {@code true} for feasible (default), {@code false} else
     */
    public boolean isFeasibleEndpoint(String endpointUrl, byte securityLevel) {
        return true;
    }
    
    /**
     * The connection schema.
     * 
     * @return the schema (default {@link #DEFAULT_SCHEMA})
     */
    public Schema getSchema() {
        return schema;
    }
    
    /**
     * The connection port.
     * 
     * @return the connection port
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Returns the server host.
     * 
     * @return the server host name
     */
    public String getHost() {
        return host;
    }
    
    /**
     * Returns the base URL prefix path/endpoint URL.
     * 
     * @return the base URL prefix/endpoint URL
     */
    public String getEndpointPath() {
        return endpointPath;
    }

    
    /**
     * String/URL to identify the application. Connector-dependent.
     * 
     * @return the identifier
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Application description. Connector-dependent.
     * 
     * @return the description
     */
    public String getApplicationDescription() {
        return applicationDescription;
    }
    
    /**
     * Returns the request timeout.
     * 
     * @return the request timeout in ms
     */
    public int getRequestTimeout() {
        return requestTimeout;
    }
    
    /**
     * Returns the notification interval, i.e., how frequently the connector shall look for new
     * values. This may happen via events, notifications or polling depending on the connector
     * implementation.
     * 
     * @return the interval in ms, deactivated if less than 1 
     */
    public int getNotificationInterval() {
        return notificationInterval;
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
     * Returns whether the application identification is expected to be unique or shall be made unique upon first 
     * connect. May be ignored if not applicable.
     * 
     * @return {@code true} (default) for make unique, {@code false} else
     */
    public boolean getAutoApplicationId() {
        return autoApplicationId;
    }

}
