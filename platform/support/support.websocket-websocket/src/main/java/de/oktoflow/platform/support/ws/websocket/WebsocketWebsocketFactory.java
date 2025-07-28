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

package de.oktoflow.platform.support.ws.websocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.websocket.WebsocketFactory;

/**
 * Implements the WS interface by WebSocket.
 * 
 * @author Holger Eichelberger, SSE
 */
public class WebsocketWebsocketFactory extends WebsocketFactory {

    /**
     * Implements the actual web socket. Indirection required as the WebSocketClient conflicts in exception types.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class WebSocketImpl 
        implements de.iip_ecosphere.platform.support.websocket.WebsocketFactory.WebSocket {

        private WebSocketClientImpl client;
        
        /**
         * Creates a web socket for the given server URI.
         * 
         * @param serverUri the server URI
         */
        public WebSocketImpl(URI serverUri) {
            this.client = new WebSocketClientImpl(serverUri);
        }
        
        @Override
        public void connect() throws IOException {
            this.client.connect();
        }        
        
        @Override
        public boolean connectBlocking() throws IOException {
            try {
                return this.client.connectBlocking();
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }

        @Override
        public void close() throws IOException {
            this.client.close();
        }

        @Override
        public void setReceptionHander(Consumer<String> receptionHandler) {
            this.client.receptionHandler = receptionHandler;
        }

        @Override
        public void setStatusListener(StatusListener listener) {
            this.client.statusListener = listener;
        }

        @Override
        public void send(String text) throws IOException {
            try {
                this.client.send(text);
            } catch (WebsocketNotConnectedException e) {
                throw new IOException(e);
            }
        }

        @Override
        public void closeBocking() throws IOException {
            try {
                this.client.closeBlocking();
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }

        @Override
        public URI getURI() {
            return this.client.getURI();
        }

        @Override
        public boolean isOpen() {
            return this.client.isOpen();
        }

        @Override
        public boolean isClosed() {
            return this.client.isClosed();
        }

    }
    
    /**
     * Client implementation.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class WebSocketClientImpl extends WebSocketClient {

        private Consumer<String> receptionHandler = s -> getLogger().info("No response handler set, ignoring: {}", s);
        private StatusListener statusListener;
        private String lastError; // new instance of WebSocket per try
        
        /**
         * Creates a web socket client for the given server URI.
         * 
         * @param serverUri the server URI
         */
        public WebSocketClientImpl(URI serverUri) {
            super(serverUri);
        }

        /**
         * Creates a web socket for the given server URI.
         * 
         * @param serverUri the server URI
         * @param httpHeaders the headers to use
         */
        public WebSocketClientImpl(URI serverUri, Map<String, String> httpHeaders) {
            super(serverUri, httpHeaders);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            if (null != statusListener) {
                statusListener.onConnect();
            }
        }

        @Override
        public void onMessage(String message) {
            if (null != receptionHandler) {
                receptionHandler.accept(message);
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            if (remote) {
                getLogger().info("Connection closed by remote peer, code: {} reason: {}", code, reason);
            }
            if (null != statusListener) {
                statusListener.onClose(reason, remote);
            }
        }

        @Override
        public void onError(Exception ex) {
            String msg = ex.getMessage();
            if (null == lastError || !lastError.equals(msg)) {
                lastError = msg;
                getLogger().error("While running WebSocket client: {}", ex.getMessage());
                if (null != statusListener) {
                    statusListener.onError(msg);
                }
            }
        }

    }

    /**
     * A simple web socket server.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class BroadcastingWsServer extends WebSocketServer {

        private ServerAddress address;
        private Map<String, List<org.java_websocket.WebSocket>> connections 
            = Collections.synchronizedMap(new HashMap<>());
        
        /**
         * Creates the server instance.
         * 
         * @param address the server address
         */
        BroadcastingWsServer(ServerAddress address) {
            super(new InetSocketAddress(address.getHost(), address.getPort()));
            setReuseAddr(false); // ToTallNate #220 -> allow rebind
            this.address = address;
        }
        
        @Override
        public void onOpen(org.java_websocket.WebSocket conn, ClientHandshake handshake) {
            List<org.java_websocket.WebSocket> cList = connections.get(conn.getResourceDescriptor());
            if (null == cList) {
                cList = Collections.synchronizedList(new ArrayList<>());
                connections.put(conn.getResourceDescriptor(), cList);
            }
            cList.add(conn);
        }

        @Override
        public void onClose(org.java_websocket.WebSocket conn, int code, String reason, boolean remote) {
            List<org.java_websocket.WebSocket> cList = connections.get(conn.getResourceDescriptor());
            if (null != cList) {
                cList.remove(conn);
                if (cList.isEmpty()) {
                    connections.remove(conn.getResourceDescriptor());
                }
            }
        }

        @Override
        public void onMessage(org.java_websocket.WebSocket conn, String message) {
            List<org.java_websocket.WebSocket> cList = connections.get(conn.getResourceDescriptor());
            if (null != cList) {
                for (int c = 0; c < cList.size(); c++) {
                    cList.get(c).send(message);
                }
            }            
            //"topic" specific broadcast(message);
        }

        @Override
        public void onError(org.java_websocket.WebSocket conn, Exception ex) {
            getLogger().error("Error on {}: {}", conn.getRemoteSocketAddress(), ex.getMessage());
        }

        @Override
        public void onStart() {
            getLogger().info("Started websocket broadcasting server on {}:{}", address.getHost(), address.getPort());
        }
        
    }    
    
    /**
     * Returns the logger.
     * 
     * @return the logger
     */
    protected static Logger getLogger() {
        return LoggerFactory.getLogger(WebsocketWebsocketFactory.class);
    }    
    
    @Override
    public WebSocketImpl createSocket(URI uri) throws IOException {
        return new WebSocketImpl(uri);
    }
    
    @Override
    public Server createBroadcastingServer(ServerAddress address) throws IOException {
        return new Server() {
            
            private BroadcastingWsServer server;

            @Override
            public Server start() {
                LoggerFactory.getLogger(WebsocketWebsocketFactory.class).info("Starting Websocket broadcasting "
                    + "server on {}:{}", address.getHost(), address.getPort());
                server = new BroadcastingWsServer(address);
                new Thread(server).start();
                return this;
            }

            @Override
            public void stop(boolean dispose) {
                try {
                    server.stop(1000);
                } catch (InterruptedException e) {
                }
            }
        };
    }

}
