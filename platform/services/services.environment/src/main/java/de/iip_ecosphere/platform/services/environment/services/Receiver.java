/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.environment.services;

import java.io.IOException;
import java.util.function.Consumer;

import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.connectors.AbstractReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;

/**
 * Basic abilities for a generic receiver, which may also operate outside the streaming environment.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Receiver {
    
    public static final boolean DEBUG = false;
    private TransportConnector connector;
    private boolean running = true;
    
    /**
     * A generic logging reception callback.
     * 
     * @param <T> the type of data
     * @author Holger Eichelberger, SSE
     */
    public static class LoggingReceptionCallback<T> extends AbstractReceptionCallback<T> {

        private String stream;
        private Consumer<T> consumer;
       
        /**
         * Creates the callback.
         * 
         * @param cls the class of data to receive
         * @param stream the stream to listen to
         * @param consumer the consumer to feed with the data
         */
        public LoggingReceptionCallback(Class<T> cls, String stream, Consumer<T> consumer) {
            super(cls);
            this.stream = stream;
            this.consumer = consumer;
        }
        
        /**
         * Returns the name of the stream/channel.
         * 
         * @return the name of the stream/channel
         */
        public String getStream() {
            return stream;
        }
        
        @Override
        public void received(T data) {
            if (DEBUG) {
                System.out.println(stream + ": " + data);
            }
            consumer.accept(data);
        }
        
    }
    
    /**
     * Defines/changes the transport connector.
     * 
     * @param connector the new connector
     */
    protected void setConnector(TransportConnector connector) {
        this.connector = connector;
    }

    /**
     * Returns the transport connector.
     * 
     * @return the connector
     */
    protected TransportConnector getConnector() {
        return connector;
    }

    /**
     * Subscribes to the given logger callbacks/streams.
     * 
     * @param callbacks the callbacks/streams
     */
    protected void subscribeTo(LoggingReceptionCallback<?>... callbacks) {
        for (LoggingReceptionCallback<?> c : callbacks) {
            try {
                connector.setReceptionCallback(c.getStream(), c);
            } catch (IOException e) {
                System.err.println("Subscribing to " + c.getStream() + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Starts the receiver.
     */
    public void start() {
        while (running) {
            TimeUtils.sleep(100);
        }
    }
    
    /**
     * Stops the receiver.
     */
    public void stop() {
        running = false;
    }

}
