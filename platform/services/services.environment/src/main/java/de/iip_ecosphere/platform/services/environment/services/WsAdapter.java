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

package de.iip_ecosphere.platform.services.environment.services;

import java.io.IOException;
import java.net.URI;

import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.websocket.WebsocketFactory;
import de.iip_ecosphere.platform.support.websocket.WebsocketFactory.StatusListener;
import de.iip_ecosphere.platform.support.websocket.WebsocketFactory.WebSocket;

/**
 * Adapts the web sockets to the interface used before.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class WsAdapter {
    
    private WebSocket socket;
    private Logger logger;
    private String lastError;

    /**
     * Creates a web socket for the given server URI.
     * 
     * @param serverUri the server URI
     */
    public WsAdapter(URI serverUri, Logger logger) {
        this.logger = logger;
        try {
            socket = WebsocketFactory.getInstance().createSocket(serverUri);
            socket.setReceptionHander(m -> onMessage(m));
            socket.setStatusListener(new StatusListener() {

                @Override
                public void onConnect() {
                }

                @Override
                public void onClose(String reason, boolean remote) {
                    if (remote) {
                        logger.info("Connection closed by remote peer, reason: {}", reason);
                    }
                }

                @Override
                public void onError(String msg) {
                    if (null == lastError || !lastError.equals(msg)) {
                        lastError = msg;
                        logger.error("While running: {}", msg);
                    }
                }
                
            });
        } catch (IOException e) {
            logger.error("While creating socket: {}", e.getMessage());
        }
    }
    
    /**
     * Called when a message arrived.
     * 
     * @param text the message text
     */
    protected abstract void onMessage(String text);
    
    /**
     * Sends the given text through the websocket.
     * 
     * @param text the text to send
     * @throws IOException if sending fails
     */
    public void send(String text) throws IOException {
        if (null != socket) {
            socket.send(text);
        }
    }
    
    /**
     * Closes the socket.
     */
    public void close() {
        if (null != socket) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.error("While closing socket: {}", e.getMessage());
            }
        }
    }

    /**
     * Connects and blocks until connected or failed.
     * 
     * @return whether connecting was successful
     */
    public boolean connectBlocking() {
        if (null != socket) {
            try {
                return socket.connectBlocking();
            } catch (IOException e) {
                logger.error("While connecting socket: {}", e.getMessage());
                return false;
            }
        } else {
            return false;
        }
    }
    

    /**
     * Returns the socket URI.
     * 
     * @return the URI
     */
    public URI getURI() {
        return null == socket ? null : socket.getURI();
    }
    
    /**
     * Connects the socket.
     */
    public void connect() {
        if (null != socket) {
            try {
                socket.connect();
            } catch (IOException e) {
                logger.error("While connecting socket: {}", e.getMessage());
            }
        }
    }

    /**
     * Returns whether the socket is open.
     * 
     * @return {@code true} for open, {@code false} else
     */
    public boolean isOpen() {
        return null == socket ? false : socket.isOpen();
    }

    /**
     * Returns whether the socket is closed.
     * 
     * @return {@code true} for closed, {@code false} else
     */
    public boolean isClosed() {
        return null == socket ? true : socket.isClosed();
    }

    /**
     * Closes the socket while blocking.
     * 
     * @throws IOException if closing fails
     */
    public void closeBlocking() throws IOException {
        socket.closeBocking();
    }

}
