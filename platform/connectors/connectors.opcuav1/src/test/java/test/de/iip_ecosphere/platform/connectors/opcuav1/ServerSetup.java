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

import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfigBuilder;
import org.eclipse.milo.opcua.stack.server.EndpointConfiguration;

import de.iip_ecosphere.platform.connectors.ConnectorParameter;


/**
 * Details the server setup.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class ServerSetup {
    
    private int tcpPort;
    private int httpsPort;
    private String path;
    
    /**
     * Creates a server setup instance.
     * 
     * @param path the URL path on the endpoints (no trailing slash)
     * @param tcpPort the TCP port to serve
     * @param httpsPort the HTTPS port to serve (although not secured)
     */
    public ServerSetup(String path, int tcpPort, int httpsPort) {
        this.tcpPort = tcpPort;
        this.httpsPort = httpsPort;
        this.path = path;
    }

    /**
     * Returns the TCP port to serve.
     * 
     * @return the TCP port
     */
    public int getTcpPort() {
        return tcpPort;
    }

    /**
     * Returns the HTTPS port to serve.
     * 
     * @return the HTTPS port
     */
    public int getHttpsPort() {
        return httpsPort;
    }

    /**
     * Returns the URL path on the endpoints.
     * 
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Initializes the application.
     * 
     * @return the application URL
     * @throws ExecutionException if initializing the application fails
     */
    public abstract String initializeApplication() throws ExecutionException;

    /**
     * Shuts down the application to clean up resources if needed.
     * 
     * @throws ExecutionException if shutdown fails.
     */
    public abstract void shutdownApplication() throws ExecutionException;

    /**
     * Configures the common endpoint builder used as basis for the following endpoint builders below.
     * 
     * @param builder the builder
     */
    public abstract void configureCommonEndpointBuilder(EndpointConfiguration.Builder builder);
    
    /**
     * Configures the no-security builder on usual non-discovery endpoints.
     * 
     * @param builder the builder
     * @return {@code builder} or <b>null</b> for no such endpoint
     */
    public abstract EndpointConfiguration.Builder configureNoSecurityBuilder(EndpointConfiguration.Builder builder);

    /**
     * Configures the TCP builder on usual non-discovery endpoints.
     * 
     * @param builder the builder
     * @return {@code builder} or <b>null</b> for no such endpoint
     */
    public abstract EndpointConfiguration.Builder configureTcpEndpointBuilder(EndpointConfiguration.Builder builder);

    /**
     * Configures the HTTPS builder on usual non-discovery endpoints.
     * 
     * @param builder the builder
     * @return {@code builder} or <b>null</b> for no such endpoint
     */
    public abstract EndpointConfiguration.Builder configureHttpsEndpointBuilder(EndpointConfiguration.Builder builder);
    
    /**
     * Configures the server builder.
     * 
     * @param builder the server builder
     */
    public abstract void configureServerBuilder(OpcUaServerConfigBuilder builder);

    /**
     * Returns the connector parameter for setting up a corresponding connector. [convenience]
     * 
     * @return the connector parameter instance
     */
    public abstract ConnectorParameter getConnectorParameter();
    
}
