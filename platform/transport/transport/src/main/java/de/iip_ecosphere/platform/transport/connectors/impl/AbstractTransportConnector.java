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
package de.iip_ecosphere.platform.transport.connectors.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.identities.IdentityStore;
import de.iip_ecosphere.platform.support.identities.IdentityToken;
import de.iip_ecosphere.platform.support.net.SslUtils;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter.CloseAction;
import de.iip_ecosphere.platform.transport.serialization.Serializer;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry.SerializerProvider;

/**
 * An abstract transport connector.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractTransportConnector implements TransportConnector {

    private Map<String, List<ReceptionCallback<?>>> callbacks = Collections.synchronizedMap(new HashMap<>());
    private TransportParameter params;
    private SerializerProvider serializerProvider = SerializerRegistry.DEFAULT_PROVIDER;

    /**
     * Returns whether the connector shall use TLS.
     * 
     * @param params the transport parameters
     * @return {@code true} for TLS enabled, {@code false} else
     */
    protected boolean useTls(TransportParameter params) {
        return null != params.getKeystoreKey();
    }

    /**
     * Helper method to determine a SSL/TLS context. Apply only if {@link #useTls(TransportParameter)}
     * returns {@code true}. Relies on {@code IdentityStore#createTlsContext(String, String, String...)} if
     * {@link TransportParameter#getKeystoreKey()} is given, else on 
     * {@link SslUtils#createTlsContext(java.io.File, String, String)}.
     * 
     * @param params the transport parameters
     * @return the TLS context
     * @throws IOException if creating the context or obtaining key information fails
     */
    protected SSLContext createTlsContext(TransportParameter params) throws IOException {
        return IdentityStore.getInstance().createTlsContext(params.getKeystoreKey(), params.getKeyAlias());
    }

    /**
     * Consumes token authentication data.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface AuthenticationConsumer {
        
        /**
         * Accepts a user name and a password.
         * 
         * @param user the user name
         * @param password the password
         * @param pwdEncAlg the name of the password encryption algorithm, e.g., UTF-8 for plaintext
         * @return {@code true} for applied, {@code false} for ignored (shall be logged)
         */
        public boolean accept(String user, String password, String pwdEncAlg);
        
    }

    /**
     * Tries to apply the given authentication key to the given consumer.
     * 
     * @param authenticationKey the authentication key
     * @param consumer the consumer
     * @return {@code true} for applied, {@code false} for ignored/failed
     */
    public static boolean applyAuthenticationKey(String authenticationKey, AuthenticationConsumer consumer) {
        boolean authDone = false;
        if (null != authenticationKey) {
            IdentityToken tok = IdentityStore.getInstance().getToken(authenticationKey);
            if (tok != null) {
                if (IdentityToken.TokenType.USERNAME == tok.getType()) {
                    authDone = consumer.accept(tok.getUserName(), tok.getTokenDataAsString(), 
                        tok.getTokenEncryptionAlgorithm());
                } else {
                    LoggerFactory.getLogger(AbstractTransportConnector.class).info(
                        "Cannot handle identity token type {}. Trying user/password.", tok.getType());
                }
            } else {
                LoggerFactory.getLogger(AbstractTransportConnector.class).info(
                    "Authentication key {} not found. Trying user/password.", authenticationKey);
            }
        }
        return authDone;
    }

    @Override
    public void setReceptionCallback(String stream, ReceptionCallback<?> callback) throws IOException {
        List<ReceptionCallback<?>> l = callbacks.get(stream);
        if (null == l) {
            l = Collections.synchronizedList(new ArrayList<ReceptionCallback<?>>());
            callbacks.put(stream, l);
        }
        l.add(callback);
    }
    
    @Override
    public void detachReceptionCallback(String stream, ReceptionCallback<?> callback) throws IOException {
        List<ReceptionCallback<?>> l = callbacks.get(stream);
        if (l != null) {
            boolean removed = l.remove(callback);
            if (removed && l.isEmpty()) {
                callbacks.remove(stream);
                unsubscribe(stream, true);
            }
        }
    }

    @Override
    public void connect(TransportParameter params) throws IOException {
        this.params = params;
    }

    @Override
    public void unsubscribe(String stream, boolean delete) throws IOException {
        callbacks.remove(stream);
    }

    @Override
    public void disconnect() throws IOException {
        if (getCloseAction().doClose()) {
            List<String> streams = new ArrayList<>(callbacks.keySet());
            for (String stream : streams) {
                try {
                    unsubscribe(stream, getCloseAction().doDelete());
                } catch (IOException e) {
                    LoggerFactory.getLogger(getClass()).warn("While disconnecting/unsubscribing '" 
                        + stream + "': " + e.getMessage());
                }
            }
        }
    }

    /**
     * Returns the transport parameters.
     * 
     * @return the parameters
     */
    protected TransportParameter getTransportParameter() {
        return params;
    }

    /**
     * Returns the timeout for individual send/receive actions.
     * 
     * @return the timeout in milliseconds
     */
    protected int getActionTimeout() {
        return params.getActionTimeout();
    }
    
    /**
     * Returns the close action.
     * 
     * @return the close action
     */
    protected CloseAction getCloseAction() {
        return params.getCloseAction();
    }

    /**
     * The unique application/client identifier. Considers {@link TransportParameter#getApplicationId()} and
     * {@link TransportParameter#getAutoApplicationId()}.
     * 
     * @return the client identifier
     */
    protected String getApplicationId() {
        return getApplicationId(params.getApplicationId(), "tp", params.getAutoApplicationId());
    }

    /**
     * Creates a unique application/client identifier.
     * 
     * @param applicationId the basic application id (may be <b>null</b>, turned to an empty string then)
     * @param infix an optional infix to be appended to {@code applicationId} (may be <b>null</b>, 
     *   turned to an empty string then)
     * @param makeUnique make unique or just compose given information
     * @return the client identifier
     */
    public static String getApplicationId(String applicationId, String infix, boolean makeUnique) {
        final String separator = "-";
        String appId = applicationId;
        if (null == appId) {
            appId = "";
        }
        if (null == infix) {
            infix = "";
        }
        if (infix.length() > 0) {
            if (appId.length() > 0) {
                appId += separator;
            }
            appId += infix;
            if (!appId.endsWith(separator) && makeUnique) {
                appId += separator;
            }
        }
        if (makeUnique) {
            appId += NetUtils.getOwnIP() + separator + System.currentTimeMillis(); 
        } 
        return appId;
    }

    /**
     * The callback for a certain stream.
     * 
     * @param stream the stream to return the callback for
     * @return the callback (may be <b>null</b> for none)
     */
    protected List<ReceptionCallback<?>> getCallback(String stream) {
        return callbacks.get(stream);
    }
    
    /**
     * Registers a {@code stream} name without callback. A callback may be registered later.
     * 
     * @param stream the name of the stream
     */
    protected void registerStream(String stream) {
        if (!isStreamKnown(stream)) {
            callbacks.put(stream, null);
        }
    }
    
    /**
     * Returns whether a {@code stream} name exists, irrespective of a registered callback.
     *  
     * @param stream the stream name to look for
     * @return {@code true} if the stream is known, {@code false} else
     */
    protected boolean isStreamKnown(String stream) {
        return callbacks.containsKey(stream);
    }

    /**
     * Notifies the callback in {@code stream} based on received serialized
     * {@code data}.
     * 
     * @param <T>    the type of data
     * @param stream the stream to notify the callback for
     * @param data   the received serialized data
     */
    @SuppressWarnings("unchecked")
    protected <T> void notifyCallback(String stream, byte[] data) {
        List<ReceptionCallback<?>> callbacks = getCallback(stream);
        if (null != callbacks) {
            for (int c = 0; c < callbacks.size(); c++) {
                ReceptionCallback<T> callback = (ReceptionCallback<T>) callbacks.get(c);
                Serializer<T> serializer = serializerProvider.getSerializer(callback.getType());
                if (null != serializer) {
                    try {
                        callback.received(serializer.from(data));
                    } catch (IOException e) {
                        LoggerFactory.getLogger(getClass()).error("Cannot deserialize: {}", e.getMessage());
                    }
                } else {
                    LoggerFactory.getLogger(getClass()).warn("No serializer registered for {}", 
                        callback.getType().getName());
                }
            }
        }
    }

    /**
     * Serializes {@code data} to {@code stream}. [helper]
     * 
     * @param <T>    the type of the data
     * @param stream the stream to serialize to
     * @param data   the data to serialize
     * @return the serialized bytes
     * @throws IOException in case that problems occur during serialization
     */
    protected <T> byte[] serialize(String stream, T data) throws IOException {
        byte[] result;
        @SuppressWarnings("unchecked")
        Class<T> cls = (Class<T>) data.getClass();
        Serializer<T> serializer = serializerProvider.getSerializer(cls);
        if (null != serializer) {
            result = serializer.to(data);
        } else {
            result = new byte[0];
        }
        return result;
    }
    
    @Override
    public void setSerializerProvider(SerializerProvider serializerProvider) {
        if (null != serializerProvider) {
            this.serializerProvider = serializerProvider;
        } else {
            LoggerFactory.getLogger(getClass()).warn("No serializer provider given. Ignoring change request.");
        }
    }


}
