package test.de.oktoflow.platform.support.ws.websocket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.rest.Rest;
import de.iip_ecosphere.platform.support.websocket.WebsocketFactory.StatusListener;
import de.iip_ecosphere.platform.support.websocket.WebsocketFactory;
import de.iip_ecosphere.platform.support.websocket.WebsocketFactory.WebSocket;
import de.oktoflow.platform.support.ws.websocket.WebsocketWebsocketFactory;

/**
 * Tests {@link Rest}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class WebsocketFactoryTest {

    /**
     * A test status listener.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class WsStatusListener implements StatusListener {

        private boolean connected = false;
        
        @Override
        public void onConnect() {
            connected = true;
        }

        @Override
        public void onClose(String reason, boolean remote) {
            connected = false;
        }

        @Override
        public void onError(String message) {
            System.out.println("ERROR: " + message);
        }
        
    }
    
    /**
     * Tests basic WebSocket functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testWs() throws IOException, URISyntaxException {
        WebsocketFactory ws = WebsocketFactory.getInstance();
        Assert.assertTrue(ws instanceof WebsocketWebsocketFactory);
        
        ServerAddress address = new ServerAddress(Schema.WS);
        Server server = ws.createBroadcastingServer(address);
        server.start();

        List<String> received = new ArrayList<>();
        URI serverURI = new URI(address.toServerUri());
        WebSocket sender = ws.createSocket(serverURI);
        WsStatusListener senderListener = new WsStatusListener();
        sender.setStatusListener(senderListener);

        WebSocket receiver = ws.createSocket(serverURI);
        WsStatusListener receiverListener = new WsStatusListener();
        receiver.setStatusListener(receiverListener);
        receiver.setReceptionHander(s -> received.add(s));

        sender.connectBlocking();
        Assert.assertTrue(senderListener.connected);
        receiver.connect();
        TimeUtils.waitFor(() -> !receiverListener.connected, 3000, 200);
        Assert.assertTrue(receiverListener.connected);
        
        sender.send("ABC");
        TimeUtils.sleep(300);
        Assert.assertTrue(received.contains("ABC"));
        
        sender.close();
        TimeUtils.waitFor(() -> senderListener.connected, 3000, 200);
        Assert.assertTrue(!senderListener.connected);
        receiver.close();
        TimeUtils.waitFor(() -> receiverListener.connected, 3000, 200);
        Assert.assertTrue(!receiverListener.connected);

        server.stop(true);
    }

}
