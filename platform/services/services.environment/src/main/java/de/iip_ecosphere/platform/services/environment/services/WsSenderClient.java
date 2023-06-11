package de.iip_ecosphere.platform.services.environment.services;

import java.io.IOException;
import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Simple web socket client for sending data.
 * 
 * @param <T> the type of data
 * @author Holger Eichelberger, SSE
 */
class WsSenderClient<T> extends WebSocketClient implements Sender<T> {

    private TypeTranslator<T, String> translator;
    
    /**
     * Creates the sender.
     * 
     * @param serverURI the sender
     * @param translator translates data instances to client transport format
     */
    public WsSenderClient(URI serverURI, TypeTranslator<T, String> translator) {
        super(serverURI);
        this.translator = translator;
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
        TransportToWsConverter.getLogger().error("Cannot write data: {}", ex.getMessage());
    }

    @Override
    public void send(T data) throws IOException {
        send(translator.to(data));
    }
    
}