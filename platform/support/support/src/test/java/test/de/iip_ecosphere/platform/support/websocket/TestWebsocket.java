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

package test.de.iip_ecosphere.platform.support.websocket;

import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.websocket.WebsocketFactory;

/**
 * Implements an empty Rest interface for simple testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestWebsocket extends WebsocketFactory {

    @Override
    public WebSocket createSocket(final URI uri) throws IOException {
        return new WebSocket() {
            
            private StatusListener listener;
            private Consumer<String> receptionHandler;
            private boolean isOpen = false;
            private boolean isClosed = true;
            
            @Override
            public void setStatusListener(StatusListener listener) {
                this.listener = listener;
            }
            
            @Override
            public void setReceptionHander(Consumer<String> receptionHandler) {
                this.receptionHandler = receptionHandler;
            }
            
            @Override
            public void send(String text) throws IOException {
                if (null != receptionHandler) { // just pass back
                    receptionHandler.accept(text);
                }
            }
            
            @Override
            public boolean connectBlocking() throws IOException {
                if (null != listener) {
                    listener.onConnect();
                }
                isOpen = true;
                isClosed = false;
                return true;
            }
            
            @Override
            public void connect() throws IOException {
                if (null != listener) {
                    listener.onConnect();
                }
                isOpen = true;
                isClosed = false;
            }
            
            @Override
            public void close() throws IOException {
                if (null != listener) {
                    listener.onClose("close", false);
                }
                isOpen = false;
                isClosed = true;
            }

            @Override
            public void closeBocking() throws IOException {
                if (null != listener) {
                    listener.onClose("closeBlocking", false);
                }
                isOpen = false;
                isClosed = true;
            }

            @Override
            public URI getURI() {
                return uri;
            }

            @Override
            public boolean isOpen() {
                return isOpen;
            }

            @Override
            public boolean isClosed() {
                return isClosed;
            }
            
        };
    }

    @Override
    public Server createBroadcastingServer(ServerAddress address) throws IOException {
        return new Server() {
            
            @Override
            public void stop(boolean dispose) {
            }
            
            @Override
            public Server start() {
                return this;
            }
        };
    }

}
