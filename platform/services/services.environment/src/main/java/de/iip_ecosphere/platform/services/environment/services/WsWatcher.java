package de.iip_ecosphere.platform.services.environment.services;

import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import de.iip_ecosphere.platform.services.environment.services.TransportConverter.Watcher;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Watcher implementation.
 * 
 * @author Holger Eichelberger, SSE
 */
class WsWatcher<T> extends WebSocketClient implements Watcher<T> {

    private String lastError;
    private Consumer<T> consumer = d -> { };
    private TypeTranslator<T, String> typeTranslator;

    /**
     * Creates a watcher for the given URI.
     * 
     * @param serverUri the URI to watch
     * @param typeTranslator the type translator
     */
    public WsWatcher(URI serverUri, TypeTranslator<T, String> typeTranslator) {
        super(serverUri);
        this.typeTranslator = typeTranslator;
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
            TransportToWsConverter.getLogger().error("While ingesting result data: {}", e.getMessage());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (remote) {
            TransportToWsConverter.getLogger().info("Connection closed by remote peer, code: {} reason: {}", 
                code, reason);
        }
    }

    @Override
    public void onError(Exception ex) {
        String msg = ex.getMessage();
        if (null == lastError || !lastError.equals(msg)) {
            lastError = msg;
            TransportToWsConverter.getLogger().error("While watching: {}", ex.getMessage());
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