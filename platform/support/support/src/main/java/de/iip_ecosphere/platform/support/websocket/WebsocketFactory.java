/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.websocket;

import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.plugins.PluginManager;

/**
 * Generic access to WebSockets. Requires an implementing plugin of type {@link WebsocketFactory} or an active 
 * {@link WebsocketFactoryProviderDescriptor}. Simplified interface akin to WebSocket.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class WebsocketFactory {
    
    private static WebsocketFactory instance; 

    static {
        instance = PluginManager.getPluginInstance(WebsocketFactory.class, WebsocketFactoryProviderDescriptor.class);
    }

    /**
     * Returns the WebServices instance.
     * 
     * @return the instance
     */
    public static WebsocketFactory getInstance() {
        return instance;
    }
    
    /**
     * Manually sets the instance. Shall not be needed, but may be required in some tests.
     * 
     * @param rest the Rest instance
     */
    public static void setInstance(WebsocketFactory rest) {
        if (null != rest) {
            instance = rest;
        }
    }
    
    /**
     * Is called on status changes.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface StatusListener {

        /**
         * When the connection is established.
         */
        public void onConnect();

        /**
         * When the connection is closed.
         * 
         * @param reason the closing reason
         * @param remote whether it was the local or the remote side
         */
        public void onClose(String reason, boolean remote);

        /**
         * When an error occurred.
         * 
         * @param message the error message
         */
        public void onError(String message);
        
    }
    
    /**
     * A web socket.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface WebSocket {
        
        /**
         * Sets the reception handler for incoming messages.
         * 
         * @param receptionHandler the response handler
         */
        public void setReceptionHander(Consumer<String> receptionHandler);

        /**
         * Sets the status listener.
         * 
         * @param listener the listener
         */
        public void setStatusListener(StatusListener listener);

        /**
         * Connects without blocking.
         * 
         * @throws IOException if connection fails
         */
        public void connect() throws IOException;
        
        /**
         * Connects and blocks until connected or failed.
         * 
         * @return whether connecting was successful
         * @throws IOException if connection fails
         */
        public boolean connectBlocking() throws IOException;
        
        /**
         * Sends the given text through the websocket.
         * 
         * @param text the text to send
         * @throws IOException if sending fails
         */
        public void send(String text) throws IOException;

        /**
         * Closes the socket.
         * 
         * @throws IOException if closing fails
         */
        public void close() throws IOException;

        /**
         * Closes the socket, waits for closing.
         * 
         * @throws IOException if closing fails
         */
        public void closeBocking() throws IOException;

        /**
         * Returns the URI this socket is connected to.
         * 
         * @return the URI
         */
        public URI getURI();        
        
        /**
         * Returns whether the socket is open.
         * 
         * @return {@code true} for open, {@code false} else
         */
        public boolean isOpen();

        /**
         * Returns whether the socket is closed.
         * 
         * @return {@code true} for closed, {@code false} else
         */
        public boolean isClosed();
        
    }
    
    /**
     * Creates the websocket.
     * 
     * @param uri the URI to connect to
     * @return the socket instance
     * @throws IOException if creating fails
     */
    public abstract WebSocket createSocket(URI uri) throws IOException;
    
    /**
     * Creates a broadcasting/relay server.
     * 
     * @param address the server address
     * @return the socket instance
     * @throws IOException if creating fails
     */
    public abstract Server createBroadcastingServer(ServerAddress address) throws IOException;

}
