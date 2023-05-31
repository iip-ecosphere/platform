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

package de.iip_ecosphere.platform.services.environment.services;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;

/**
 * Transport converter for websockets. Use {@link #createServer(Endpoint)} to create a server.
 * 
 * @param <T> the data type
 * @author Holger Eichelberger, SSE
 */
public class TransportToWsConverter<T> extends TransportConverter<T> {

    private Endpoint endpoint;
    private SenderClient sender;
    private ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Creates a transport to web socket converter running the server in this instance.
     * 
     * @param transportStream the transport stream to listen on
     * @param dataType the data type to listen for
     * @param endpoint the server endpoint
     */
    public TransportToWsConverter(String transportStream, Class<T> dataType, Endpoint endpoint) {
        super(transportStream, dataType);
        this.endpoint = endpoint;
    }

    /**
     * Creates a server instance for the given address.
     * 
     * @param address the address
     * @return the server instance
     */
    public static Server createServer(ServerAddress address) {
        return new Server() {
            
            private BroadcastingWsServer server;

            @Override
            public Server start() {
                server = new BroadcastingWsServer(address);
                new Thread(server).start();
                return this;
            }

            @Override
            public void stop(boolean dispose) {
                try {
                    server.stop();
                } catch (InterruptedException e) {
                }
            }
            
        };
        
    }
    
    /**
     * Starts the transport tracer.
     * 
     * @param aasSetup the AAS setup to use
     * @param deploy whether the AAS represented by this converter shall be deployed
     */
    public void start(AasSetup aasSetup, boolean deploy) {
        super.start(aasSetup, deploy);
        sender = createWithUri(u -> new SenderClient(u));
        try {
            sender.connectBlocking();
        } catch (InterruptedException e) {
            
        }
    }

    /**
     * Stops the transport, deletes the AAS.
     */
    public void stop() {
        sender.close();
    }

    @Override
    protected void handleNew(T data) {
        try {
            sender.send(objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            getLogger().error("Cannot write data: {}", e.getMessage());
        }
    }

    /**
     * Simple web socket client for sending data.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class SenderClient extends WebSocketClient {

        /**
         * Creates the sender.
         * 
         * @param serverURI the sender
         */
        public SenderClient(URI serverURI) {
            super(serverURI);
        }
        
        @Override
        public void onOpen(ServerHandshake handshakedata) {
        }

        @Override
        public void onMessage(String message) {
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
        }

        @Override
        public void onError(Exception ex) {
            getLogger().error("Cannot write data: {}", ex.getMessage());
        }
        
    }

    /**
     * A simple web socket server.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class BroadcastingWsServer extends WebSocketServer {

        private ServerAddress address;
        private Map<String, List<WebSocket>> connections = Collections.synchronizedMap(new HashMap<>());
        
        /**
         * Creates the server instance.
         * 
         * @param address the server address
         */
        private BroadcastingWsServer(ServerAddress address) {
            super(new InetSocketAddress(address.getHost(), address.getPort()));
            this.address = address;
        }
        
        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            List<WebSocket> cList = connections.get(conn.getResourceDescriptor());
            if (null == cList) {
                cList = Collections.synchronizedList(new ArrayList<>());
                connections.put(conn.getResourceDescriptor(), cList);
            }
            cList.add(conn);
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            List<WebSocket> cList = connections.get(conn.getResourceDescriptor());
            if (null != cList) {
                cList.remove(conn);
                if (cList.isEmpty()) {
                    connections.remove(conn.getResourceDescriptor());
                }
            }
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            List<WebSocket> cList = connections.get(conn.getResourceDescriptor());
            if (null != cList) {
                for (int c = 0; c < cList.size(); c++) {
                    cList.get(c).send(message);
                }
            }            
            //"topic" specific broadcast(message);
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            getLogger().error("Errod on {}: {}", conn.getRemoteSocketAddress(), ex.getMessage());
        }

        @Override
        public void onStart() {
            getLogger().info("Started transport converter websocket server on {}", address.getPort());
        }
        
    }
    
    /**
     * Watcher implementation.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class WsWatcher extends WebSocketClient implements Watcher<T> {

        private String lastError;
        private Consumer<T> consumer = d -> { };

        /**
         * Creates a watcher for the given URI.
         * 
         * @param serverUri the URI to watch
         */
        public WsWatcher(URI serverUri) {
            super(serverUri);
        }

        @Override
        public Watcher<T> start() {
            connect();
            return this;
        }

        @Override
        public Watcher<T> stop() {
            close();
            return this;
        }
        
        @Override
        public void onOpen(ServerHandshake handshakedata) {
        }

        @Override
        public void onMessage(String message) {
            try {
                T data = objectMapper.readValue(message, getType());
                consumer.accept(data);
            } catch (IOException e) {
                getLogger().error("While ingesting result data: {}", e.getMessage());
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            if (remote) {
                getLogger().info("Connection closed by remote peer, code: {} reason: {}", code, reason);
            }
        }

        @Override
        public void onError(Exception ex) {
            String msg = ex.getMessage();
            if (null == lastError || !lastError.equals(msg)) {
                lastError = msg;
                getLogger().error("While watching: {}", ex.getMessage());
            }
        }

        @Override
        public void setConsumer(Consumer<T> consumer) {
            if (null == consumer) {
                this.consumer = d -> { };
            } else {
                this.consumer = consumer;
            }
        }

    }

    /**
     * Returns the logger.
     * 
     * @return the logger
     */
    protected static Logger getLogger() {
        return LoggerFactory.getLogger(TransportToWsConverter.class);
    }
    
    /**
     * Creates some instance with a URI.
     * 
     * @param <R> the resulting instance type
     * @param creator the creator function
     * @return created instance
     */
    private <R> R createWithUri(Function<URI, R> creator) {
        String uri = endpoint.toUri();
        try {
            return creator.apply(new URI(uri));
        } catch (URISyntaxException e) {
            // static URI, unlikely to fail
            getLogger().error("URI syntax error: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Endpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public Watcher<T> createWatcher(int period) {
        return createWithUri(u -> new WsWatcher(u));
    }

}
