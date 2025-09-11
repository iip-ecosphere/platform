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

package de.iip_ecosphere.platform.support.aas.basyx2.apps.asset;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.basyx.http.Aas4JHTTPSerializationExtension;
import org.eclipse.digitaltwin.basyx.http.BaSyxHTTPConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.IdentityTokenWithRole;
import de.iip_ecosphere.platform.support.aas.SetupSpec.ComponentSetup;
import de.iip_ecosphere.platform.support.aas.SetupSpec.State;
import de.iip_ecosphere.platform.support.aas.basyx2.AasOperationsProvider;
import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;
import de.iip_ecosphere.platform.support.rest.Rest;
import de.iip_ecosphere.platform.support.rest.Rest.Request;
import de.iip_ecosphere.platform.support.rest.Rest.Response;
import de.iip_ecosphere.platform.support.rest.Rest.RestServer;

/**
 * Implements a simple Asset REST server as alternative to the (usually Tomcat-based) BaSyX-like implementation. The
 * implementation is based on the REST plugin abstraction of oktoflow (assuming a non-Tomcat-based implementation to 
 * avoid conflicts with the singleton call of 
 * {@link java.net.URL#setURLStreamHandlerFactory(java.net.URLStreamHandlerFactory)}) but re-using BaSyx components 
 * where adequate.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AssetRestServer implements Server {

    private static final String PREFIX_AUTH_BASIC = "Basic";
    private static final String PREFIX_AUTH_BEARER = "Bearer";
    private AasOperationsProvider opProvider;
    private ComponentSetup setup;
    private RestServer server;
    private ObjectMapper mapper;
    private Map<String, IdentityTokenWithRole> users;

    /**
     * Creates an Asset REST server instance.
     * 
     * @param opProvider the operations provider
     * @param setup the component setup
     */
    public AssetRestServer(AasOperationsProvider opProvider, ComponentSetup setup) {
        this.opProvider = opProvider;
        this.setup = setup;
    }
    
    @Override
    public Server start() {
        mapper = new BaSyxHTTPConfiguration().jackson2ObjectMapperBuilder(
            Arrays.asList(new Aas4JHTTPSerializationExtension())).build();

        if (setup.getState() == State.STOPPED) {
            int port = setup.getServerAddress().getPort();
            System.out.println("Starting AAS-REST server (rest-plugin) on " + port);
            server = Rest.getInstance().createServer(port);
            KeyStoreDescriptor kDesc = setup.getKeyStore();
            if (kDesc != null) {
                server.secure(kDesc.getAbsolutePath(), kDesc.getPassword(), kDesc.getAlias());
            }
            final String paramOpName = server.toPathVariable("opName");
            final String paramCategory = server.toPathVariable("category");
            server.definePost("/" + AasOperationsProvider.PREFIX_SERVICE + paramOpName, (req, res) -> {
                String opName = req.getParam(paramOpName);
                return handle(req, res, opName, opProvider.getServiceFunction(opName));
            });
            server.definePost("/" + AasOperationsProvider.PREFIX_OPERATIONS + paramOpName 
                + "/" + paramCategory, (req, res) -> {
                    String opName = req.getParam(paramOpName);
                    String category = req.getParam(paramCategory);
                    return handle(req, res, opName, opProvider.getOperation(category, opName));
                });
            AuthenticationDescriptor auth = setup.getAuthentication();
            if (AuthenticationDescriptor.isEnabledOnServer(auth)) {
                setupAuthentication(auth);
            }
            server.start();
            setup.notifyStateChange(State.RUNNING);
        }
        return this;
    }
    
    /**
     * Sets up the authentication.
     * 
     * @param auth the authentication descriptor
     */
    private void setupAuthentication(AuthenticationDescriptor auth) {
        if (auth.getServerUsers() != null) {
            users = new HashMap<>();
            for (IdentityTokenWithRole token : auth.getServerUsers()) {
                if (token.getUserName() != null) {
                    users.put(token.getUserName(), token);
                }
            }
        }
        server.defineBefore((req, res) -> {
            String info = req.getParam("Authorization");
            if (null == info) {
                if (!auth.requiresAnonymousAccess()) {
                    server.halt(HttpStatus.FORBIDDEN.value(), "Anonymous access not permitted.");
                }
            } else {
                if (info.startsWith(PREFIX_AUTH_BASIC)) {
                    String cred = new String(Base64.getDecoder().decode(info.substring(PREFIX_AUTH_BASIC.length())));
                    int pos = cred.indexOf(":");
                    if (pos > 0) {
                        String userName = cred.substring(0, pos);
                        String pwd = cred.substring(pos + 1);
                        IdentityTokenWithRole token = users.get(userName);
                        if (null == token || !token.getTokenDataAsString().equals(pwd)) {
                            server.halt(HttpStatus.FORBIDDEN.value(), "Unknown user/password.");
                        }
                    }
                } else if (info.startsWith(PREFIX_AUTH_BEARER)) {
                    info.substring(PREFIX_AUTH_BEARER.length()); // TODO oauth
                }
            }
        });
    }

    /**
     * Handles an operation call request.
     *
     * @param req the HTTP request
     * @param res the HTTP response
     * @param opName the operation name
     * @param op the actual function to execute
     * @return the result body
     */
    private String handle(Request req, Response res, String opName, Function<Object[], Object> op) {
        res.setApplicationJsonType();
        try {
            OperationVariable[] args = mapper.readValue(req.getBody(), OperationVariable[].class);
            ResponseEntity<OperationVariable[]> result = AssetSpringApp.invokeOperation(opName, args, op);
            res.setStatus(result.getStatusCode().value());
            // handle error implicit?
            return mapper.writeValueAsString(result.getBody());
        } catch (JsonProcessingException e) {
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ""; // value?
        }
    }

    @Override
    public void stop(boolean dispose) {
        if (null != server) {
            server.stop(dispose);
            setup.notifyStateChange(State.STOPPED);
            server = null;
            mapper = null;
        }
    }

}
