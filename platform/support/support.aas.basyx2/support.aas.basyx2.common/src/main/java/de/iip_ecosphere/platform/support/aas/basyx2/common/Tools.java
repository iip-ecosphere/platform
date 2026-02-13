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

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;


import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;
import de.iip_ecosphere.platform.support.net.SslUtils;
import de.iip_ecosphere.platform.support.aas.SetupSpec.ComponentSetup;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Some common utilities, e.g., for client/server.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Tools {

    /**
     * Consumes an HTTPClient builder and applies it to client.
     * @param <C> the client type
     * @author Holger Eichelberger, SSE
     */
    public interface HttpClientBuilderConsumer<C> {

        /**
         * Applies {@code builder} to {@code client}.
         * 
         * @param builder the builder
         * @param client the client
         */
        public void accept(HttpClient.Builder builder, C client, Consumer<HttpRequest.Builder> interceptor);
        
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
    public static <A, C> A createApi(ComponentSetup setup, String uri, C client, 
        HttpClientBuilderConsumer<C> builderConsumer, UriConsumer<C> uriConsumer, ApiProvider<A, C> apiProvider, 
        Class<A> cls) {
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
        try {
            builderConsumer.accept(createHttpClient(ksd), client, interceptor);
        } catch (IOException e) {
            LoggerFactory.getLogger(Tools.class).error(
                "While creating {}, creating http client failed: {}", cls.getName(), e.getMessage());
        }
        String u = null == uri ? endpoint.toServerUri() : uri;
        uriConsumer.accept(u, client);
        
        // TokenManager may go via interceptor
        return apiProvider.create(u, client);
    }

    // checkstyle: resume parameter number check
    
    /**
     * Creates a HTTP client builder from a keystore descriptor.
     * 
     * @param desc the descriptor
     * @return the client builder
     * @throws IOException if creating the SSL/TSL context from {@code desc} fails
     */
    public static HttpClient.Builder createHttpClient(KeyStoreDescriptor desc) throws IOException {
        SSLContext context = null;
        Boolean oldHNV = null;
        if (null != desc) {
            context = SslUtils.createTlsContext(desc.getPath(), desc.getPassword(), desc.getAlias());
            oldHNV = setJdkHostnameVerification(desc);
        }
        HttpClient.Builder result = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1);
        if (null != context) {
            result.sslContext(context);
        } 
        /* if (AUTHENTICATED) {
            // authenticator sets header empty, requires challenge-response-auth 
            result.authenticator(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                }
            });
        }*/
        if (null != oldHNV) {
            setJdkHostnameVerification(oldHNV);
        }
        return result;
    }

    /**
     * Sets JDK HTTP/SSL hostname verification.
     * 
     * @param desc the keystore descriptor indicating whether verification is enabled or disabled
     * @return the value of the flag before, by default {@code false}
     */
    public static boolean setJdkHostnameVerification(KeyStoreDescriptor desc) {
        return setJdkHostnameVerification(!desc.applyHostnameVerification());
    }

    /**
     * Sets JDK HTTP/SSL hostname verification.
     * 
     * @param disable {@code true} the verification, {@code false} enables it
     * @return the value of the flag before, by default {@code false}
     */
    public static boolean setJdkHostnameVerification(boolean disable) {
        final String prop = "jdk.internal.httpclient.disableHostnameVerification";
        boolean old = Boolean.valueOf(System.getProperty(prop, "false"));
        System.setProperty(prop, String.valueOf(disable));
        return old;
    }

}
