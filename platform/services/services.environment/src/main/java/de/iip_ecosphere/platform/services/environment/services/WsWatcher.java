package de.iip_ecosphere.platform.services.environment.services;

import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

import de.iip_ecosphere.platform.services.environment.services.TransportConverter.Watcher;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Watcher implementation.
 * 
 * @author Holger Eichelberger, SSE
 */
class WsWatcher<T> extends WsAdapter implements Watcher<T> {

    private Consumer<T> consumer = d -> { };
    private TypeTranslator<T, String> typeTranslator;

    /**
     * Creates a watcher for the given URI.
     * 
     * @param serverUri the URI to watch
     * @param typeTranslator the type translator
     */
    public WsWatcher(URI serverUri, TypeTranslator<T, String> typeTranslator) {
        super(serverUri, TransportToWsConverter.getLogger());
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
    protected void onMessage(String message) {
        try {
            T data = typeTranslator.from(message);
            consumer.accept(data);
        } catch (IOException e) {
            TransportToWsConverter.getLogger().error("While ingesting result data: {}", e.getMessage());
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