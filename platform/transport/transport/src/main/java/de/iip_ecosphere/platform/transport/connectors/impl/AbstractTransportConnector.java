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
import java.util.HashMap;
import java.util.Map;

import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.serialization.Serializer;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;

/**
 * An abstract transport connector.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractTransportConnector implements TransportConnector {

    private Map<String, ReceptionCallback<?>> callbacks = new HashMap<>();
    private TransportParameter params;

    @Override
    public void setReceptionCallback(String stream, ReceptionCallback<?> callback) throws IOException {
        callbacks.put(stream, callback);
    }

    @Override
    public void connect(TransportParameter params) throws IOException {
        this.params = params;
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
     * The unique application/client identifier.
     * 
     * @return the client identifier
     */
    protected String getApplicationId() {
        return params.getApplicationId();
    }

    /**
     * The callback for a certain stream.
     * 
     * @param stream the stream to return the callback for
     * @return the callback (may be <b>null</b> for none)
     */
    protected ReceptionCallback<?> getCallback(String stream) {
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
     * @throws IOException in case that deserialization fails
     */
    @SuppressWarnings("unchecked")
    protected <T> void notifyCallback(String stream, byte[] data) throws IOException {
        ReceptionCallback<T> callback = (ReceptionCallback<T>) getCallback(stream);
        if (null != callback) {
            Serializer<T> serializer = SerializerRegistry.getSerializer(callback.getType());
            if (null != serializer) {
                callback.received(serializer.from(data));
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
        Serializer<T> serializer = SerializerRegistry.getSerializer(cls);
        if (null != serializer) {
            result = serializer.to(data);
        } else {
            result = new byte[0];
        }
        return result;
    }

}
