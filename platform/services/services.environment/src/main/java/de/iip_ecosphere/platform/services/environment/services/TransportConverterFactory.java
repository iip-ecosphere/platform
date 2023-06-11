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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.services.TransportConverter.Watcher;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;
import de.iip_ecosphere.platform.transport.serialization.GenericJsonToStringTranslator;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Creates instances for the transport converter service support.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class TransportConverterFactory {

    private static TransportConverterFactory instance = WsTransportConverterFactory.INSTANCE;

    /**
     * Returns the actual factory instance.
     * 
     * @return the factor instance
     */
    public static TransportConverterFactory getInstance() {
        return instance;
    }
    
    /**
     * Creates a converter sender instance. The implementing factory is free to choose
     * which information to take and may fail if required information is not given.
     * 
     * @param <T> the type of data to be sent
     * @param aas the AAS setup for AAS-based senders
     * @param transport the transport setup for transport-based senders
     * @param path within the mechanism to identify the data, may be an id short, an URI sub-path etc.
     * @param translator the type-to-sender format translator, may be <b>null</b>
     * @param cls the data type class
     * @return the sender instance, may be <b>null</b>
     * @see #createSender(Endpoint, TypeTranslator, Class)
     * @see #getGatewayEndpoint(AasSetup, TransportSetup, String)
     */
    public final <T> Sender<T> createSender(AasSetup aas, TransportSetup transport, String path, 
        TypeTranslator<T, String> translator, Class<T> cls) {
        return createSender(getGatewayEndpoint(aas, transport, path), translator, cls);
    }

    /**
     * Creates a converter sender instance.
     * 
     * @param <T> the type of data to be sent
     * @param endpoint the server endpoint
     * @param translator the type-to-sender format translator, may be <b>null</b>
     * @param cls the data type class
     * @return the sender instance, may be <b>null</b>
     * @see #createSenderImpl(Endpoint, TypeTranslator, Class)
     * @see #ensureTranslator(TypeTranslator, Class)
     */
    public final <T> Sender<T> createSender(Endpoint endpoint, TypeTranslator<T, String> translator, Class<T> cls) {
        return createSenderImpl(endpoint, ensureTranslator(translator, cls), cls);
    }

    /**
     * Creates a converter sender instance.
     * 
     * @param <T> the type of data to be sent
     * @param endpoint the server endpoint
     * @param translator the type-to-sender format translator
     * @param cls the data type class
     * @return the sender instance, may be <b>null</b>
     */
    protected abstract <T> Sender<T> createSenderImpl(Endpoint endpoint, TypeTranslator<T, String> translator, 
        Class<T> cls);
    
    // checkstyle: stop parameter number check
    
    /**
     * Creates a watcher instance. The implementing factory is free to choose
     * which information to take and may fail if required information is not given.
     * 
     * @param <T> the type of data to be watched
     * @param aas the AAS setup for AAS-based watchers
     * @param transport the transport setup for transport-based watchers
     * @param path within the mechanism to identify the data, may be an id short, an URI sub-path etc.
     * @param translator the receiver-to-type format translator (may be <b>null</b>)
     * @param cls the data type class
     * @param period the watching period in ms
     * @return the watcher instance, may be <b>null</b>
     * @see #createWatcher(Endpoint, TypeTranslator, Class, int)
     * @see #getGatewayEndpoint(AasSetup, TransportSetup, String)
     */
    public final <T> Watcher<T> createWatcher(AasSetup aas, TransportSetup transport, String path, 
        TypeTranslator<T, String> translator, Class<T> cls, int period) {
        return createWatcher(getGatewayEndpoint(aas, transport, path), translator, cls, period);
    }

    // checkstyle: resume parameter number check

    /**
     * Creates a watcher instance.
     * 
     * @param <T> the type of data to be watched
     * @param endpoint the server endpoint
     * @param translator the receiver-to-type format translator
     * @param cls the data type class
     * @param period the watching period in ms
     * @return the watcher instance, may be <b>null</b>
     * @see #createWatcherImpl(Endpoint, TypeTranslator, Class, int)
     * @see #ensureTranslator(TypeTranslator, Class)
     */
    public final <T> Watcher<T> createWatcher(Endpoint endpoint, TypeTranslator<T, String> translator, Class<T> cls, 
        int period) {
        return createWatcherImpl(endpoint, ensureTranslator(translator, cls), cls, period);
    }

    /**
     * Creates a watcher instance.
     * 
     * @param <T> the type of data to be watched
     * @param endpoint the server endpoint
     * @param translator the receiver-to-type format translator
     * @param cls the data type class
     * @param period the watching period in ms
     * @return the watcher instance, may be <b>null</b>
     */
    protected abstract <T> Watcher<T> createWatcherImpl(Endpoint endpoint, TypeTranslator<T, String> translator, 
        Class<T> cls, int period);
    
    // checkstyle: stop parameter number check

    /**
     * Creates a converter instance. The implementing factory is free to choose
     * which information to take and may fail if required information is not given.
     * 
     * @param <T> the type of data to be watched
     * @param aas the AAS setup for AAS-based watchers
     * @param transport the transport setup for transport-based watchers
     * @param transportStream the stream to listen to
     * @param path within the mechanism to stream the data to, may be an id short, an URI sub-path etc.
     * @param translator the receiver-to-type format translator (may be <b>null</b>)
     * @param cls the data type class
     * @return the converter instance, may be <b>null</b>
     * @see #createConverter(Endpoint, String, TypeTranslator, Class)
     * @see #getGatewayEndpoint(AasSetup, TransportSetup, String)
     */
    public final <T> TransportConverter<T> createConverter(AasSetup aas, TransportSetup transport, 
        String transportStream, String path, TypeTranslator<T, String> translator, Class<T> cls) {
        return createConverter(getGatewayEndpoint(aas, transport, path), transportStream, translator, cls);
    }

    // checkstyle: resume parameter number check

    /**
     * Creates a converter instance. The implementing factory is free to choose
     * which information to take and may fail if required information is not given.
     * 
     * @param <T> the type of data to be watched
     * @param endpoint the server endpoint
     * @param transportStream the stream to listen to
     * @param translator the receiver-to-type format translator (may be <b>null</b>)
     * @param cls the data type class
     * @return the converter instance, may be <b>null</b>
     * @see #createConverterImpl(Endpoint, String, TypeTranslator, Class)
     * @see #ensureTranslator(TypeTranslator, Class)
     */
    public final <T> TransportConverter<T> createConverter(Endpoint endpoint,
        String transportStream, TypeTranslator<T, String> translator, Class<T> cls) {
        return createConverterImpl(endpoint, transportStream, ensureTranslator(translator, cls), cls);
    }

    /**
     * Creates a converter instance. The implementing factory is free to choose
     * which information to take and may fail if required information is not given.
     * 
     * @param <T> the type of data to be watched
     * @param endpoint the server endpoint
     * @param transportStream the stream to listen to
     * @param translator the receiver-to-type format translator
     * @param cls the data type class
     * @return the converter instance, may be <b>null</b>
     */
    public abstract <T> TransportConverter<T> createConverterImpl(Endpoint endpoint,
        String transportStream, TypeTranslator<T, String> translator, Class<T> cls);

    /**
     * Ensures the existence of a translator instance.
     * 
     * @param <T> the data type
     * @param translator the type translator; if <b>null</b>, {@link GenericJsonToStringTranslator} is used
     * @param cls the data type class
     * @return the type translator, either {@code tanslator} or a default instance
     */
    protected static <T> TypeTranslator<T, String> ensureTranslator(TypeTranslator<T, String> translator, 
        Class<T> cls) {
        if (null == translator) {
            translator = new GenericJsonToStringTranslator<>(cls);
        } 
        return translator;
    }

    /**
     * Creates some instance with a URI.
     * 
     * @param <R> the resulting instance type
     * @param aas the AAS setup
     * @param transport the transport setup to take {@link TransportSetup#getGatewayServerEndpoint(Schema, String)} from
     * @param path the path for determining the gateway server endpoint
     * @param creator the creator function
     * @return created instance, may be <b>null</b>
     */
    protected <R> R createWithUri(AasSetup aas, TransportSetup transport, String path, Function<URI, R> creator) {
        return createWithUri(getGatewayEndpoint(aas, transport, path), creator);
    }

    /**
     * Creates some instance with a URI.
     * 
     * @param <R> the resulting instance type
     * @param endpoint the endpoint to take the URI from
     * @param creator the creator function
     * @return created instance, may be <b>null</b>
     */
    protected static <R> R createWithUri(Endpoint endpoint, Function<URI, R> creator) {
        String uri = endpoint.toUri();
        try {
            return creator.apply(new URI(uri));
        } catch (URISyntaxException e) {
            // static URI, unlikely to fail
            LoggerFactory.getLogger(TransportConverterFactory.class).error("URI syntax error: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Creates a converter server instance. The implementing factory is free to choose
     * which information to take and may fail if required information is not given.
     * 
     * @param aas the AAS setup for AAS-based senders
     * @param transport the transport setup for transport-based senders
     * @return the server instance, may be <b>null</b> if none is required for the actual factory
     * @see #getGatewayEndpoint(AasSetup, TransportSetup, String)
     * @see #createServer(ServerAddress)
     */
    public final Server createServer(AasSetup aas, TransportSetup transport) {
        return createServer(getGatewayEndpoint(aas, transport, ""));
    }

    /**
     * Creates a converter server instance.
     * 
     * @param address the server address
     * @return the server instance, may be <b>null</b> if none is required for the actual factory
     */
    public abstract Server createServer(ServerAddress address);

    /**
     * Returns an endpoint for the given path. The implementing factory is free to choose
     * which information to take and may fail if required information is not given.
     * 
     * @param aas the AAS setup for AAS-based senders
     * @param transport the transport setup for transport-based senders
     * @param path the path within the resulting endpoint
     * @return the endpoint
     */
    public abstract Endpoint getGatewayEndpoint(AasSetup aas, TransportSetup transport, String path);

}
