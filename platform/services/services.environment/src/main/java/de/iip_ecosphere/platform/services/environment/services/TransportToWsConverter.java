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
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.Service;
import de.iip_ecosphere.platform.services.environment.Starter;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;
import de.iip_ecosphere.platform.transport.serialization.GenericJsonToStringTranslator;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Transport converter for websockets. Use {@link #createServer(ServerAddress)} to create a server.
 * 
 * @param <T> the data type
 * @author Holger Eichelberger, SSE
 */
public class TransportToWsConverter<T> extends TransportConverter<T> {

    public static final Schema SCHEMA = Schema.WS;
    
    private Endpoint endpoint;
    private SenderClient sender;
    private boolean notConnectedError = false;
    private TypeTranslator<T, String> typeTranslator;

    /**
     * Creates a transport to web socket converter running the server in this instance.
     * The type translator will be the default one from 
     * {@link #TransportToWsConverter(String, Class, Endpoint, TypeTranslator))}.
     * 
     * @param transportStream the transport stream to listen on
     * @param dataType the data type to listen for
     * @param endpoint the server endpoint
     */
    public TransportToWsConverter(String transportStream, Class<T> dataType, Endpoint endpoint) {
        this(transportStream, dataType, endpoint, null);
    }

    /**
     * Creates a transport to web socket converter running the server in this instance.
     * 
     * @param transportStream the transport stream to listen on
     * @param dataType the data type to listen for
     * @param endpoint the server endpoint
     * @param translator the optional type translator; if not given, {@link GenericJsonToStringTranslator} will be used
     */
    public TransportToWsConverter(String transportStream, Class<T> dataType, Endpoint endpoint, 
        TypeTranslator<T, String> translator) {
        super(transportStream, dataType);
        this.endpoint = endpoint;
        if (null == translator) {
            this.typeTranslator = new GenericJsonToStringTranslator<>(dataType);
        } else {
            this.typeTranslator = translator;
        }
    }
    
    /**
     * Creates a combined set of server/converter instances.
     * 
     * @param <T> the data type
     * @param transportStream the transport stream to create the converter for
     * @param cls the data class
     * @param server the server, may be existing or <b>null</b>
     * @param setup the transport setup
     * @param service the service to create the instances for
     * @return the instances object for server/converter
     */
    public static <T> ConverterInstances<T> createInstances(String transportStream, Class<T> cls, Server server, 
        TransportSetup setup, Service service) {
        Endpoint endpoint;
        String path = "/app_" + Starter.getServiceId(service);
        if (setup.isLocalGatewayEndpoint()) {
            endpoint = new Endpoint(SCHEMA, NetUtils.getOwnIP(setup.getNetmask()), NetUtils.getEphemeralPort(), path);
            if (null == server) {
                server = createServer(endpoint);
            }
        } else {
            endpoint = setup.getGatewayServerEndpoint(SCHEMA, path);
            server = null;
        }
        return new ConverterInstances<T>(server, new TransportToWsConverter<T>(transportStream, cls, endpoint));
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

    @Override
    public void setExcludedFields(Set<String> excludedFields) {
        super.setExcludedFields(excludedFields);
        if (typeTranslator instanceof GenericJsonToStringTranslator) {
            JsonUtils.exceptFields(((GenericJsonToStringTranslator<?>) typeTranslator).getMapper(), 
                getExcludedFieldsArray());
        }
    }

    @Override
    public void initializeSubmodel(SubmodelBuilder smBuilder) {
        addEndpointToAas(smBuilder, endpoint);
    }
    
    @Override
    public void start(AasSetup aasSetup) {
        super.start(aasSetup);
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
            sender.send(typeTranslator.to(data));
            notConnectedError = false;
        } catch (IOException e) {
            getLogger().error("Cannot write data: {}", e.getMessage());
        } catch (WebsocketNotConnectedException e) {
            if (!notConnectedError) {
                getLogger().error("Cannot write data, not connected: {}", e.getMessage());
                notConnectedError = true;
            }
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
                T data = typeTranslator.from(message);
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
