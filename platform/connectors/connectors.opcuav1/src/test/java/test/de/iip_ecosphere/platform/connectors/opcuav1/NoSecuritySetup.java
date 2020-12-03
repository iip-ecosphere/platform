/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.connectors.opcuav1;

import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_ANONYMOUS;
import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_USERNAME;

import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfigBuilder;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.server.EndpointConfiguration;
import org.eclipse.milo.opcua.stack.server.EndpointConfiguration.Builder;

import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;

/**
 * Describes a non-secure setup for testing purposes.
 * 
 * @author Holger Eichelberger, SSE
 */
public class NoSecuritySetup extends ServerSetup {

    /**
     * Creates a server setup instance.
     * 
     * @param path the URL path on the endpoints (no trailing slash)
     * @param tcpPort the TCP port to serve
     * @param httpsPort the HTTPS port to serve (although not secured)
     */
    public NoSecuritySetup(String path, int tcpPort, int httpsPort) {
        super(path, tcpPort, httpsPort);
    }

    @Override
    public String initializeApplication() throws ExecutionException {
        return "urn:eclipse:milo:examples:server";
    }
    
    @Override
    public void shutdownApplication() throws ExecutionException {
    }    

    @Override
    public void configureCommonEndpointBuilder(EndpointConfiguration.Builder builder) {
        builder.addTokenPolicies(
            USER_TOKEN_POLICY_ANONYMOUS,
            USER_TOKEN_POLICY_USERNAME);
    }
    
    @Override
    public EndpointConfiguration.Builder configureNoSecurityBuilder(EndpointConfiguration.Builder builder) {
        builder.setSecurityPolicy(SecurityPolicy.None)
            .setSecurityMode(MessageSecurityMode.None);
        return builder;
    }

    @Override
    public Builder configureTcpEndpointBuilder(Builder builder) {
        return null;
    }

    @Override
    public Builder configureHttpsEndpointBuilder(Builder builder) {
        return null;
    }

    @Override
    public void configureServerBuilder(OpcUaServerConfigBuilder builder) {
        //builder.setIdentityValidator(identityValidator);
    }

    @Override
    public ConnectorParameter getConnectorParameter() {
        // no identity -> anonymous
        // no encryption 
        return ConnectorParameterBuilder.newBuilder("localhost", getTcpPort())
            .setEndpointPath(getPath())
            .setApplicationInformation("urn:eclipse:milo:examples:client", "eclipse milo opc-ua client")
            .setNotificationInterval(1000) // test waits for that
            .build();
    }

}
