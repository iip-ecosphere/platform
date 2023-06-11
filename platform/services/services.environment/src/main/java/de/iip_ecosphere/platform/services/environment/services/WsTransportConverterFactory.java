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

import de.iip_ecosphere.platform.services.environment.services.TransportConverter.Watcher;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Transport converter factory for websockets. Takes information from 
 * {@link TransportSetup#getGatewayServerEndpoint(Schema, String)}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class WsTransportConverterFactory extends TransportConverterFactory {

    public static final WsTransportConverterFactory INSTANCE = new WsTransportConverterFactory();
    
    /**
     * Prevents external creation.
     */
    private WsTransportConverterFactory() {
    }

    @Override
    protected <T> Sender<T> createSenderImpl(Endpoint endpoint, TypeTranslator<T, String> translator, 
        Class<T> cls) {
        return createWithUri(endpoint, u -> new WsSenderClient<T>(u, translator));
    }

    @Override
    protected <T> Watcher<T> createWatcherImpl(Endpoint endpoint, TypeTranslator<T, String> translator, Class<T> cls, 
        int period) {
        return createWithUri(endpoint, u -> new WsWatcher<T>(u, translator));
    }

    @Override
    public <T> TransportConverter<T> createConverterImpl(Endpoint endpoint, String transportStream,
        TypeTranslator<T, String> translator, Class<T> cls) {
        return new TransportToWsConverter<>(transportStream, cls, endpoint, translator);
    }
    
    @Override
    public Server createServer(ServerAddress address) {
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
    public Endpoint getGatewayEndpoint(AasSetup aas, TransportSetup transport, String path) {
        return transport.getGatewayServerEndpoint(TransportToWsConverter.SCHEMA, path);
    }

}
