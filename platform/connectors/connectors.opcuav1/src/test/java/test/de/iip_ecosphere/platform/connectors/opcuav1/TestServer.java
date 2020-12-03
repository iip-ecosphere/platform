package test.de.iip_ecosphere.platform.connectors.opcuav1;

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

import java.security.Security;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespaceWithLifecycle;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfigBuilder;
import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.transport.TransportProfile;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo;
import org.eclipse.milo.opcua.stack.server.EndpointConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Implements an OPC UA embedded test server.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestServer {

    protected static final boolean SECURE = false;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    static {
        // Required for SecurityPolicy.Aes256_Sha256_RsaPss
        Security.addProvider(new BouncyCastleProvider());
    }

    private final OpcUaServer server;
    private final ManagedNamespaceWithLifecycle exampleNamespace;
    private final ServerSetup setup;

    /**
     * Creates a test server.
     * 
     * @param namespaceCreator the namespace creator
     * @param setup the setup for the server
     * @throws ExecutionException if creating the server fails for some reason
     */
    public TestServer(NamespaceCreator namespaceCreator, ServerSetup setup) throws ExecutionException {
        this.setup = setup;
        String applicationUri = setup.initializeApplication();
        Set<EndpointConfiguration> endpointConfigurations = createEndpointConfigurations();
        
        OpcUaServerConfigBuilder builder = OpcUaServerConfig.builder()
            .setApplicationUri(applicationUri)
            .setApplicationName(LocalizedText.english("Eclipse Milo OPC UA Example Server"))
            .setEndpoints(endpointConfigurations)
            .setBuildInfo(
                new BuildInfo(
                    "urn:eclipse:milo:example-server",
                    "eclipse",
                    "eclipse milo example server",
                    OpcUaServer.SDK_VERSION,
                    "", DateTime.now()));
        setup.configureServerBuilder(builder);
        OpcUaServerConfig serverConfig = builder.setProductUri("urn:eclipse:milo:example-server")
            .build();

        server = new OpcUaServer(serverConfig);
        exampleNamespace = namespaceCreator.createNamespace(server);
        exampleNamespace.startup();
        logger.info("Namespace set up " + exampleNamespace.getNamespaceUri());
    }

    /**
     * Creates the endpoint configurations. {@link #setup} implicitly defines the endpoints to be created and
     * how to secure the endpoints.
     * 
     * @return the endpoint configurations
     */
    private Set<EndpointConfiguration> createEndpointConfigurations() {
        Set<EndpointConfiguration> endpointConfigurations = new LinkedHashSet<>();

        List<String> bindAddresses = newArrayList();
        bindAddresses.add("0.0.0.0");

        Set<String> hostnames = new LinkedHashSet<>();
        hostnames.add(HostnameUtil.getHostname());
        hostnames.addAll(HostnameUtil.getHostnames("0.0.0.0"));

        for (String bindAddress : bindAddresses) {
            for (String hostname : hostnames) {
                EndpointConfiguration.Builder builder = EndpointConfiguration.newBuilder()
                    .setBindAddress(bindAddress)
                    .setHostname(hostname)
                    .setPath("/" + setup.getPath());
                setup.configureCommonEndpointBuilder(builder);

                EndpointConfiguration.Builder tmp = setup.configureNoSecurityBuilder(builder.copy());
                if (null != tmp) {
                    endpointConfigurations.add(buildTcpEndpoint(tmp));
                    endpointConfigurations.add(buildHttpsEndpoint(tmp));
                }
                
                tmp = setup.configureTcpEndpointBuilder(builder.copy());
                if (null != tmp) {
                    endpointConfigurations.add(buildTcpEndpoint(tmp));
                }

                tmp = setup.configureHttpsEndpointBuilder(builder.copy());
                if (null != tmp) {
                    endpointConfigurations.add(buildHttpsEndpoint(tmp));
                }

                /*
                 * It's good practice to provide a discovery-specific endpoint with no security.
                 * It's required practice if all regular endpoints have security configured.
                 *
                 * Usage of the  "/discovery" suffix is defined by OPC UA Part 6:
                 *
                 * Each OPC UA Server Application implements the Discovery Service Set. If the OPC UA Server requires a
                 * different address for this Endpoint it shall create the address by appending the path "/discovery" to
                 * its base address.
                 */

                EndpointConfiguration.Builder discoveryBuilder = builder.copy()
                    .setPath(setup.getPath() + "/discovery")
                    .setSecurityPolicy(SecurityPolicy.None)
                    .setSecurityMode(MessageSecurityMode.None);

                endpointConfigurations.add(buildTcpEndpoint(discoveryBuilder));
                endpointConfigurations.add(buildHttpsEndpoint(discoveryBuilder));
            }
        }

        return endpointConfigurations;
    }

    /**
     * Builds a TCP endpoint for {@link ServerSetup#getTcpPort()}.
     * 
     * @param base the base configuration to be extended for HTTPS
     * @return the endpoint configuration
     */
    private EndpointConfiguration buildTcpEndpoint(EndpointConfiguration.Builder base) {
        return base.copy()
            .setTransportProfile(TransportProfile.TCP_UASC_UABINARY)
            .setBindPort(setup.getTcpPort())
            .build();
    }

    /**
     * Builds an HTTPS endpoint for {@link ServerSetup#getHttpsPort()}.
     * 
     * @param base the base configuration to be extended for HTTPS
     * @return the endpoint configuration
     */
    private EndpointConfiguration buildHttpsEndpoint(EndpointConfiguration.Builder base) {
        return base.copy()
            .setTransportProfile(TransportProfile.HTTPS_UABINARY)
            .setBindPort(setup.getHttpsPort())
            .build();
    }

    /**
     * Starts the server.
     * 
     * @return the future on the start operation, just call {@link CompletableFuture#get()}
     */
    public CompletableFuture<OpcUaServer> startup() {
        return server.startup();
    }

    /**
     * Shuts down the server.
     * 
     * @return the future on the start operation, just call {@link CompletableFuture#get()}
     */
    public CompletableFuture<OpcUaServer> shutdown() {
        exampleNamespace.shutdown();
        return server.shutdown();
    }

}
