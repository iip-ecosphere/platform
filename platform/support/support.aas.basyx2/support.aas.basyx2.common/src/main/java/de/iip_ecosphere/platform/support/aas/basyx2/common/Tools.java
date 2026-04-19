/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx2.common;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.net.HttpClientHelper;
import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;
import de.iip_ecosphere.platform.support.aas.SetupSpec.ComponentSetup;

/**
 * Some common utilities, e.g., for client/server.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Tools {

    private static Map<String, Object> cache = new HashMap<>();

    /**
     * Creates a client instance.
     * @param <C> the client type
     * @author Holger Eichelberger, SSE
     */
    public interface ClientSupplier<C> {
        
        /**
         * Creates a client instance.
         * 
         * @param builder the HTTP builder
         * @return the client instance
         */
        public C create(HttpClient.Builder builder);
        
    }

    /**
     * Consumes an HTTPClient builder and applies it to client.
     * @param <C> the client type
     * @author Holger Eichelberger, SSE
     */
    public interface HttpClientBuilderConsumer<C> {

        /**
         * Applies {@code builder} to {@code client}.
         * 
         * @param client the client
         * @param interceptor the interceptor
         */
        public void accept(C client, Consumer<HttpRequest.Builder> interceptor);
        
    }

    /**
     * Consumes an URI and applies it to client.
     * @param <C> the client type
     * @author Holger Eichelberger, SSE
     */
    public interface UriConsumer<C> {
        
        /**
         * Applies {@code uri} to {@code client}.
         * 
         * @param uri the uri
         * @param client the client
         */
        public void accept(String uri, C client);
        
    }

    /**
     * Consumes a client and creates for it an API instance.
     * @param <A> the API type
     * @param <C> the client type
     * @author Holger Eichelberger, SSE
     */
    public interface ApiProvider<A, C> {

        /**
         * Creates the API instance.
         * 
         * @param uri the client URI
         * @param client the client
         */
        public A create(String uri, C client);
        
    }

    // checkstyle: stop parameter number check
    
    /**
     * Creates an API instance.
     * 
     * @param <A> the API type
     * @param <C> the client type
     * @param setup the component setup carrying endpoint, keystore, authentication
     * @param uri specific URI, may be <b>null</b> for {@code endpoint}
     * @param builderConsumer applies the configured HTTPClient builder to the client
     * @param uriConsumer applies the uri (either {@code uri} or {@code endpoint} to the client
     * @param apiProvider creates the API instance
     * @return the API instance
     */
    public static <A, C> A createApi(ComponentSetup setup, String uri, ClientSupplier<C> clientSupplier, 
        HttpClientBuilderConsumer<C> builderConsumer, UriConsumer<C> uriConsumer, ApiProvider<A, C> apiProvider) {
        de.iip_ecosphere.platform.support.Endpoint endpoint = setup.getEndpoint();
        KeyStoreDescriptor keystore = setup.getKeyStore();
        KeyStoreDescriptor ksd = null;
        if (null != keystore && keystore.appliesToClient()) {
            if (null == uri || uri.startsWith(endpoint.toServerUri())) {
                ksd = keystore;
            }
        }
        Consumer<HttpRequest.Builder> interceptor = null;
        if (setup.getAuthentication() != null) {
            interceptor = b -> { 
                AuthenticationDescriptor.authenticate((n, v) -> b.header(n, v), setup.getAuthentication());
            };
        }
        C client = clientSupplier.create(HttpClientHelper.createHttpClient(ksd));
        builderConsumer.accept(client, interceptor);
        String u = null == uri ? endpoint.toServerUri() : uri;
        uriConsumer.accept(u, client);
        
        // TokenManager may go via interceptor
        return apiProvider.create(u, client);
    }
    
    /**
     * Fetches an API instance from the cache.
     * 
     * @param <A> the API type
     * @param setup the component setup carrying endpoint, keystore, authentication
     * @param uri specific URI, may be <b>null</b> for {@code endpoint}
     * @param supplier the API instance supplier
     * @return the API instance
     */
    public static <A> A fromCache(ComponentSetup setup, String uri, Class<A> cls, Supplier<A> supplier) {
        de.iip_ecosphere.platform.support.Endpoint endpoint = setup.getEndpoint();
        String u = null == uri ? endpoint.toServerUri() : uri;
        String key = cls.getName() + "-" + u;
        return cls.cast(cache.computeIfAbsent(key, k -> supplier.get()));
    }

    /**
     * Fetches an API instance from cache or creates it instance.
     * 
     * @param <A> the API type
     * @param <C> the client type
     * @param setup the component setup carrying endpoint, keystore, authentication
     * @param uri specific URI, may be <b>null</b> for {@code endpoint}
     * @param builderConsumer applies the configured HTTPClient builder to the client
     * @param uriConsumer applies the uri (either {@code uri} or {@code endpoint} to the client
     * @param apiProvider creates the API instance
     * @param cls the API class
     * @return the API instance
     */
    public static <A, C> A getApi(ComponentSetup setup, String uri, ClientSupplier<C> client, 
        HttpClientBuilderConsumer<C> builderConsumer, UriConsumer<C> uriConsumer, ApiProvider<A, C> apiProvider, 
        Class<A> cls) {
        return fromCache(setup, uri, cls, 
            () -> createApi(setup, uri, client, builderConsumer, uriConsumer, apiProvider));
    }

    // checkstyle: resume parameter number check

    /**
     * Clears the cache. [testing]
     */
    public static void clearCache() {
        cache.clear();
    }

}
