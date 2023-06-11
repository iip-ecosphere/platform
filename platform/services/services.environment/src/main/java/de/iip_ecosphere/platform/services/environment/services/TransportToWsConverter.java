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
import java.util.Set;

import org.java_websocket.exceptions.WebsocketNotConnectedException;
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
    private Sender<T> sender;
    private boolean notConnectedError = false;
    private TypeTranslator<T, String> typeTranslator;

    /**
     * Creates a transport to web socket converter running the server in this instance.
     * The type translator will be the default one from 
     * {@link #TransportToWsConverter(String, Class, Endpoint, TypeTranslator)}.
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
     * @param translator the optional type translator
     * @see TransportConverterFactory#ensureTranslator(TypeTranslator, Class)
     */
    public TransportToWsConverter(String transportStream, Class<T> dataType, Endpoint endpoint, 
        TypeTranslator<T, String> translator) {
        super(transportStream, dataType);
        this.endpoint = endpoint;
        this.typeTranslator = TransportConverterFactory.ensureTranslator(translator, dataType);
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
        return WsTransportConverterFactory.INSTANCE.createServer(address);
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
        sender = WsTransportConverterFactory.INSTANCE.createSender(endpoint, typeTranslator, getType());
        if (null != sender) {
            try {
                sender.connectBlocking();
            } catch (InterruptedException e) {
                getLogger().error("Connection attempt interrupted: {}", e.getMessage());
            }
        }
    }

    /**
     * Stops the transport, deletes the AAS.
     */
    public void stop() {
        if (null != sender) {
            sender.close();
        }
    }

    @Override
    protected void handleNew(T data) {
        if (null != sender) {
            try {
                sender.send(data);
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
    }

    /**
     * Returns the logger.
     * 
     * @return the logger
     */
    protected static Logger getLogger() {
        return LoggerFactory.getLogger(TransportToWsConverter.class);
    }

    @Override
    public Endpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public Watcher<T> createWatcher(int period) {
        return WsTransportConverterFactory.INSTANCE.createWatcher(endpoint, typeTranslator, getType(), period);
    }
    
    /**
     * Returns the type translator.
     * 
     * @return the type translator
     */
    public TypeTranslator<T, String> getTypeTranslator() {
        return typeTranslator;
    }

}
