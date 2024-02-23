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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.identities.IdentityStore;
import de.iip_ecosphere.platform.support.identities.IdentityToken;
import de.iip_ecosphere.platform.support.iip_aas.NameplateSetup;

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
    
    /**
     * Modes for caching data avoiding repeated ingestion.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum CacheMode {
        
        /**
         * No caching, the default.
         */
        NONE,
        
        /**
         * Based on the hash value.
         */
        HASH,
        
        /**
         * Based on a deep value comparison.
         */
        EQUALS
    }
    
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
    private String keystoreKey;
    private String keyAlias;
    private boolean hostnameVerification = false;
    private CacheMode cacheMode = CacheMode.NONE;
    private NameplateSetup.Service service;
    private Map<String, Object> specificSettings = new HashMap<>();
    
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
         * Creates a new connector parameter builder based on the given {@code params}.
         * 
         * @param params the connector params to take information from
         * @param host the host name to override the value in {@code params}, may be <b>null</b> for the value 
         *     from {@code params}.
         * @param port the port number to override the value in {@code params}, may be <b>null</b> for the value 
         *     from {@code params}.
         * @param schema the schema to override the value in {@code params}, may be <b>null</b> for the value 
         *     from {@code params}.
         * @return the connector parameter builder
         */
        public static ConnectorParameterBuilder newBuilder(ConnectorParameter params, String host, Integer port, 
            Schema schema) {
            ConnectorParameterBuilder builder = newBuilder(null == host ? params.getHost() : host, 
                null == port ? params.getPort() : port, null == schema ? params.getSchema() : schema);
            builder.instance.identityToken = params.identityToken;
            builder.instance.requestTimeout = params.requestTimeout;
            builder.instance.endpointPath = params.endpointPath;
            builder.instance.applicationId = params.applicationId;
            builder.instance.autoApplicationId = params.autoApplicationId;
            builder.instance.applicationDescription = params.applicationDescription;
            builder.instance.notificationInterval = params.notificationInterval;
            builder.instance.keepAlive = params.keepAlive;
            builder.instance.keystoreKey = params.keystoreKey;
            builder.instance.keyAlias = params.keyAlias;
            builder.instance.hostnameVerification = params.hostnameVerification;
            builder.instance.cacheMode = params.cacheMode;
            builder.instance.service = params.service;
            builder.instance.specificSettings.clear();
            builder.instance.specificSettings.putAll(params.specificSettings);
            return builder;
        }

        /**
         * Creates a new connector parameter builder based on the given {@code params}.
         * 
         * @param params the connector params to take information from
         * @return the connector parameter builder
         */
        public static ConnectorParameterBuilder newBuilder(ConnectorParameter params) {
            return newBuilder(params, null, null, null);
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
         * Sets the optional service information to select upon.
         * 
         * @param service the device service information the connector shall connect to
         * @return <b>this</b>
         */
        public ConnectorParameterBuilder setService(NameplateSetup.Service service) {
            instance.service = service;
            return this;
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
         * Sets up the optional TLS keystore key to be obtained from {@link IdentityStore}.
         * 
         * @param keystoreKey the (logical) key to access the keystore (<b>null</b> for none)
         * @return <b>this</b>
         */
        public ConnectorParameterBuilder setKeystoreKey(String keystoreKey) {
            instance.keystoreKey = keystoreKey;
            return this;
        }

        /**
         * Sets up optional TLS key alias.
         * 
         * @param alias key alias, may be <b>null</b> for none/first match
         * @return <b>this</b>
         */
        public ConnectorParameterBuilder setKeyAlias(String alias) {
            instance.keyAlias = alias;
            return this;
        }
        
        /**
         * Defines whether TLS hostname verification shall be performed.
         * 
         * @param hostnameVerification {@code false} for no verification, {@code true} else
         * @return <b>this</b>
         */
        public ConnectorParameterBuilder setHostnameVerification(boolean hostnameVerification) {
            instance.hostnameVerification = hostnameVerification;
            return this;
        }

        /**
         * Defines the cache mode.
         * 
         * @param cacheMode the cache mode
         * @return <b>this</b>
         */
        public ConnectorParameterBuilder setCacheMode(CacheMode cacheMode) {
            if (null != cacheMode) {
                instance.cacheMode = cacheMode;
            }
            return this;
        }
        
        /**
         * Adds connector specific settings.
         * 
         * @param key the key of the setting as defined by the connect
         * @param value the value of the setting
         * @return <b>this</b> (builder style)
         */
        public ConnectorParameterBuilder setSpecificSetting(String key, Object value) {
            instance.specificSettings.put(key, value);
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
                token = identityToken.get(ANY_ENDPOINT);
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
     * Returns whether TLS hostname verification shall be performed.
     * 
     * @return {@code false} for no verification (default), {@code true} else
     */
    public boolean getHostnameVerification() {
        return hostnameVerification;
    }
    
    /**
     * Returns the cache mode.
     * 
     * @return the cache mode
     */
    public CacheMode getCacheMode() {
        return cacheMode;
    }

    /**
     * Returns the device service information this connector shall connect to.
     * 
     * @return the device service information, may be <b>null</b>
     */
    public NameplateSetup.Service getService() {
        return service;
    }
    
    /**
     * Returns a connector specific setting.
     * 
     * @param key the key of the setting as defined by the connect
     * @return the value, may be <b>null</b>
     */
    public Object getSpecificSetting(String key) {
        return specificSettings.get(key);
    }
    
    /**
     * Returns a connector specific setting as String.
     * 
     * @param key the key of the setting as defined by the connect
     * @return the value, may be <b>null</b>
     */
    public String getSpecificStringSetting(String key) {
        String result = null;
        Object setting = specificSettings.get(key);
        if (null != setting) {
            result = setting.toString();
        }
        return result;
    }

    /**
     * Returns a connector specific setting as Integer.
     * 
     * @param key the key of the setting as defined by the connect
     * @return the value, may be <b>null</b>
     */
    public Integer getSpecificIntSetting(String key) {
        Integer result = null;
        Object setting = specificSettings.get(key);
        if (setting instanceof Integer) {
            result = (Integer) setting;
        } else if (setting != null) {
            try {
                result = Integer.valueOf(setting.toString());
            } catch (NumberFormatException e) {
                LoggerFactory.getLogger(ConnectorParameter.class).warn(
                    "Value {} of specific setting {} is not an integer.", setting, key);
            }
        }
        return result;
    }
    
    /**
     * Applies the connector specific setting in {@code key} if specified to {@code setter}.
     *  
     * @param key the key of the setting as defined by the connect
     * @param setter the value setter
     */
    public void setSpecificIntSetting(String key, Consumer<Integer> setter) {
        Integer value = getSpecificIntSetting(key);
        if (null != value) {
            setter.accept(value);
        }
    }
    

}
