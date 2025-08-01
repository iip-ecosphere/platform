package de.iip_ecosphere.platform.services.environment.services;

import java.io.IOException;
import java.net.URI;

import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Simple web socket client for sending data.
 * 
 * @param <T> the type of data
 * @author Holger Eichelberger, SSE
 */
class WsSenderClient<T> extends WsAdapter implements Sender<T> {

    private TypeTranslator<T, String> translator;
    
    /**
     * Creates the sender.
     * 
     * @param serverURI the sender
     * @param translator translates data instances to client transport format
     */
    public WsSenderClient(URI serverURI, TypeTranslator<T, String> translator) {
        super(serverURI, LoggerFactory.getLogger(WsSenderClient.class));
        this.translator = translator;
    }

    @Override
    public void send(T data) throws IOException {
        send(translator.to(data));
    }

    @Override
    protected void onMessage(String text) {
        // nothing
    }

}