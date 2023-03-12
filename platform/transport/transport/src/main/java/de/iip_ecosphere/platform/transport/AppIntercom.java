/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.transport;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;

/**
 * Simple app intercom facility. Declare the intercom as attribute and call {@code #start()} in 
 * service start and {@code #stop()} in service stop.
 * 
 * @param <T> the type of data to be exchanged
 * @author Holger Eichelberger, SSE
 */
public class AppIntercom<T> {

    private String channel;
    private TransportConnector conn;
    private ReceptionCallback<T> commandCallback;
    
    /**
     * Creates an intercom class.
     * 
     * @param channel the transport channel to use
     * @param consumer the consumer when data is received
     * @param cls the class of data
     */
    public AppIntercom(String channel, Consumer<T> consumer, Class<T> cls) {
        this.channel = channel;
        commandCallback = new ReceptionCallback<T>() {

            @Override
            public void received(T data) {
                consumer.accept(data);
            }

            @Override
            public Class<T> getType() {
                return cls;
            }
            
        };
    }

    /**
     * Creates an intercom class with default channel name.
     * 
     * @param consumer the consumer when data is received
     * @param cls the class of data
     */
    public AppIntercom(Consumer<T> consumer, Class<T> cls) {
        this("intercom", consumer, cls);
    }

    /**
     * Start the intercom.
     * 
     * @throws ExecutionException if starting fails
     */
    public void start() throws ExecutionException {
        try {
            conn = Transport.createConnector();
            if (null != conn) {
                conn.setReceptionCallback(channel, commandCallback);
            } else {
                LoggerFactory.getLogger(getClass()).error("Cannot set up transport intercom. No connector (null).");
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("Cannot set up transport intercom: " + e.getMessage());
        }
    }
    
    /**
     * Stops the intercom.
     */
    public void stop() {
        if (null != conn) {
            try {
                conn.detachReceptionCallback(channel, commandCallback);
                conn = null;
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("Cannot set up transport intercom: " + e.getMessage());
            }
        }
    }

    /**
     * Sends data asynchronously.
     * 
     * @param data the data
     * @throws IOException if data cannot be sent
     */
    public void asyncSend(T data) throws IOException {
        if (null != conn) {
            conn.asyncSend(channel, data);
        }
    }

    /**
     * Sends data synchronously.
     * 
     * @param data the data
     * @throws IOException if data cannot be sent
     */
    public void syncSend(T data) throws IOException {
        if (null != conn) {
            conn.syncSend(channel, data);
        }
    }

}
