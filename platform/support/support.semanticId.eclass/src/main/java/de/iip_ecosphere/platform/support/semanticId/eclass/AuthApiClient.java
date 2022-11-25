/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.semanticId.eclass;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jsonp.JsonProcessingFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import de.iip_ecosphere.platform.support.identities.IdentityToken;
import de.iip_ecosphere.platform.support.semanticId.eclass.handler.ApiClient;

/**
 * Implements a REST client that can work with {@link IdentityToken}.
 * 
 * @author Holger Eichelberger, SSE
 *
 */
public class AuthApiClient extends ApiClient {

    private static SSLContext context;

    /**
     * Defines the SSL context. Call before instantiation.
     * 
     * @param ctx the context
     */
    static void setSslContext(SSLContext ctx) {
        context = ctx;
    }
    
    /*@Override
    protected Client buildHttpClient(boolean debugging) {
        if (null != context) {
            final ClientConfig clientConfig = new ClientConfig();
            clientConfig.register(MultiPartFeature.class);
            clientConfig.register(json);
            clientConfig.register(JacksonFeature.class);
            clientConfig.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
            if (debugging) {
                clientConfig.register(new LoggingFeature(java.util.logging.Logger
                            .getLogger(LoggingFeature.DEFAULT_LOGGER_NAME), 
                            java.util.logging.Level.INFO, 
                            LoggingFeature.Verbosity.PAYLOAD_ANY, 1024 * 50 /* Log payloads up to 50K *//*));
                clientConfig.property(LoggingFeature.LOGGING_FEATURE_VERBOSITY, LoggingFeature.Verbosity.PAYLOAD_ANY);
                // Set logger to ALL
                java.util.logging.Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME)
                    .setLevel(java.util.logging.Level.ALL);
            }
            performAdditionalClientConfiguration(clientConfig);
            return ClientBuilder.newBuilder().withConfig(clientConfig).sslContext(context).build();
        } else {
            return super.buildHttpClient(debugging);
        }
    }

    @Override
    protected void performAdditionalClientConfiguration(ClientConfig clientConfig) {
        clientConfig.register(JsonProcessingFeature.class);
    }*/

}    
