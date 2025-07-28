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

package test.de.iip_ecosphere.platform.support;

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
import de.iip_ecosphere.platform.support.websocket.WebsocketFactory;
import de.iip_ecosphere.platform.support.websocket.WebsocketFactory.StatusListener;
import de.iip_ecosphere.platform.support.websocket.WebsocketFactory.WebSocket;

/**
 * Simple {@link WebsocketFactory} test.
 * 
 * @author Holger Eichelberger, SSE
 */
public class WebsocketTest {

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
        Assert.assertNotNull(ws);
        WebsocketFactory.setInstance(ws);
        
        ServerAddress address = new ServerAddress(Schema.WS);
        Server server = ws.createBroadcastingServer(address);
        server.start();

        List<String> received = new ArrayList<>();
        URI serverURI = new URI(address.toServerUri());
        WebSocket senderReceiver = ws.createSocket(serverURI);
        WsStatusListener senderReceiverListener = new WsStatusListener();
        senderReceiver.setStatusListener(senderReceiverListener);
        senderReceiver.setReceptionHander(s -> received.add(s));

        senderReceiver.connectBlocking();
        Assert.assertTrue(senderReceiverListener.connected);
        senderReceiver.connect();
        
        senderReceiver.send("ABC");
        TimeUtils.sleep(300);
        Assert.assertTrue(received.contains("ABC"));
        
        senderReceiver.close();
        Assert.assertTrue(!senderReceiverListener.connected);

        server.stop(true);
    }

}
