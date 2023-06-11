package de.iip_ecosphere.platform.services.environment.services;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import de.iip_ecosphere.platform.support.ServerAddress;

/**
 * A simple web socket server.
 * 
 * @author Holger Eichelberger, SSE
 */
class BroadcastingWsServer extends WebSocketServer {

    private ServerAddress address;
    private Map<String, List<WebSocket>> connections = Collections.synchronizedMap(new HashMap<>());
    
    /**
     * Creates the server instance.
     * 
     * @param address the server address
     */
    BroadcastingWsServer(ServerAddress address) {
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
        TransportToWsConverter.getLogger().error("Errod on {}: {}", conn.getRemoteSocketAddress(), ex.getMessage());
    }

    @Override
    public void onStart() {
        TransportToWsConverter.getLogger().info("Started transport converter websocket server on {}", 
            address.getPort());
    }
    
}