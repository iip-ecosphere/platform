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

import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.basics.AbstractMqttTransportConnector;
import de.iip_ecosphere.platform.transport.serialization.Serializer;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;

/**
 * Does a direct memory transfer.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DirectMemoryTransferTransportConnector extends AbstractTransportConnector {

    public static final String NAME = "IIP-Ecosphere direct memory transfer";
    
    private static Map<String, List<DirectMemoryTransferTransportConnector>> subscriptions 
        = new HashMap<String, List<DirectMemoryTransferTransportConnector>>();

    @Override
    public void setReceptionCallback(String stream, ReceptionCallback<?> callback) throws IOException {
        super.setReceptionCallback(stream, callback);
        synchronized (subscriptions) {
            List<DirectMemoryTransferTransportConnector> list = subscriptions.get(stream);
            if (null == list) {
                list = Collections.synchronizedList(new ArrayList<DirectMemoryTransferTransportConnector>());
                subscriptions.put(stream, list);
            }
            list.add(this);
        }
    }
    
    @Override
    public void unsubscribe(String stream, boolean delete) throws IOException {
        super.unsubscribe(stream, delete);
        synchronized (subscriptions) {
            List<DirectMemoryTransferTransportConnector> list = subscriptions.remove(stream);
            if (null != list) {
                list.remove(this);
            }
        }
    }
    
    @Override
    public void syncSend(String stream, Object data) throws IOException {
        transfer(stream, data);
    }
    
    /**
     * Transfers {@code data} to {@code stream}.
     * 
     * @param <T> the type of data
     * @param stream the stream
     * @param data the data
     * @throws IOException if the transfer fails
     */
    @SuppressWarnings("unchecked")
    private final <T> void transfer(String stream, Object data) throws IOException {
        List<DirectMemoryTransferTransportConnector> list = subscriptions.get(stream);
        if (null != list) {
            for (DirectMemoryTransferTransportConnector c : list) {
                List<ReceptionCallback<?>> callbacks = c.getCallback(stream);
                if (null != callbacks) {
                    for (int i = 0; i < callbacks.size(); i++) {
                        ReceptionCallback<T> callback = (ReceptionCallback<T>) callbacks.get(i);
                        if (null != callback) {
                            Class<T> type = callback.getType();
                            Serializer<T> serializer = SerializerRegistry.getSerializer(type);
                            T received;
                            if (null != serializer) {
                                received = serializer.clone(type.cast(data));
                            } else {
                                // Potentially dangerous... if there is no serializer, we use the same instance
                                received = type.cast(data);
                            }
                            callback.received(received);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void asyncSend(String stream, Object data) throws IOException {
        transfer(stream, data);
    }

    @Override
    public String composeStreamName(String parent, String name) {
        return AbstractMqttTransportConnector.composeNames(parent, name); // syntax irrelevant
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Clears everything.
     */
    public void clear() {
        subscriptions.clear();
    }

    @Override
    public String supportedEncryption() {
        return null;
    }

    @Override
    public String enabledEncryption() {
        return null;
    }

}
