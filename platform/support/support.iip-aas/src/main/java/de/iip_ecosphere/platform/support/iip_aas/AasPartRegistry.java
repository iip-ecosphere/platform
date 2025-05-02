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

package de.iip_ecosphere.platform.support.iip_aas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasUtils;
import de.iip_ecosphere.platform.support.aas.AssetKind;
import de.iip_ecosphere.platform.support.aas.CorsEnabledRecipe;
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe;
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe.ImmediateDeploymentRecipe;
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe.RegistryDeploymentRecipe;
import de.iip_ecosphere.platform.support.aas.SetupSpec.State;
import de.iip_ecosphere.platform.support.aas.SetupSpec;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.iip_aas.config.EndpointHolder;
import de.iip_ecosphere.platform.support.iip_aas.config.ProtocolAddressHolder;
import de.iip_ecosphere.platform.support.iip_aas.config.RuntimeSetupEndpointValidator;
import de.iip_ecosphere.platform.support.iip_aas.config.ServerAddressHolder;
import de.iip_ecosphere.platform.support.jsl.ExcludeFirst;
import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;

/**
 * A registry for {@link AasContributor} instances to be loaded via the Java Service loader.
 * 
 * @author Holger Eichelberger, SSE
 * @see ExcludeFirst
 */
public class AasPartRegistry {

    /**
     * The name of the top-level AAS created by this registry in {@link #build()}.
     */
    public static final String NAME_AAS = "IIP_Ecosphere";
    
    // common submodel names are declared already here to relax dependencies
    public static final String NAME_SUBMODEL_SERVICES = "services";
    public static final String NAME_COLLECTION_SERVICES = "services";
    public static final String NAME_SUBMODEL_RESOURCES = "resources"; 
    public static final String NAME_PROP_DEVICE_AAS = "deviceAas";
    
    /**
     * The URN of the top-level AAS created by this registry in {@link #build()}.
     */
    public static final String URN_AAS = "urn:::AAS:::iipEcosphere#";
    
    /**
     * The URN of the asset of the top-level AAS created by this registry in {@link #build()}.
     */
    public static final String URN_AAS_ASSET = "urn:::AAS:::iipEcosphere#asset";
    
    public static final Schema DEFAULT_SCHEMA = Schema.HTTP;
    public static final String DEFAULT_HOST = ServerAddress.LOCALHOST;
    public static final String NO_SPECIFIC_SERVER_HOST = "-";
    public static final int DEFAULT_PORT = 8080;
    public static final int DEFAULT_SM_PORT = 8082;
    public static final int DEFAULT_REGISTRY_PORT = 8081; 
    public static final int DEFAULT_SM_REGISTRY_PORT = 8083;
    public static final int DEFAULT_PROTOCOL_PORT = 9000;
    public static final String DEFAULT_AAS_ENDPOINT = "";
    public static final String DEFAULT_REGISTRY_ENDPOINT = "registry";
    public static final String DEFAULT_PROTOCOL = AasFactory.DEFAULT_PROTOCOL;
    
    private static AasSetup setup = new AasSetup();
    private static Supplier<List<Aas>> aasSupplier;
    private static int aasImplPort = -1;

    /**
     * Aas installation/setup modes.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static enum AasMode {
        
        /**
         * Deploy to the given registry/server.
         */
        REMOTE_DEPLOY,
        
        /**
         * Run a local server and register the AAS with the given registry.
         */
        REGISTER
    }
    
    /**
     * The technical setup of the AAS/VAB endpoints as data class to be used with a usual configuration format/YAML 
     * parser. For local server setup/testing, {@link #server} and {@link #registry} shall point to the same server 
     * instance but with different endpoint paths (in memory registry). For a real installation, this information may 
     * differ. Typically, the {@link #implementation} runs on localhost.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class AasSetup implements SetupSpec {

        private EndpointHolder server = new EndpointHolder(AasPartRegistry.DEFAULT_SCHEMA, 
            DEFAULT_HOST, DEFAULT_PORT, DEFAULT_AAS_ENDPOINT);

        private EndpointHolder smServer = new EndpointHolder(AasPartRegistry.DEFAULT_SCHEMA, 
                DEFAULT_HOST, DEFAULT_SM_PORT, DEFAULT_AAS_ENDPOINT);
        
        private EndpointHolder registry = new EndpointHolder(DEFAULT_SCHEMA, DEFAULT_HOST, 
            DEFAULT_REGISTRY_PORT, DEFAULT_REGISTRY_ENDPOINT);

        private EndpointHolder smRegistry = new EndpointHolder(DEFAULT_SCHEMA, DEFAULT_HOST, 
                DEFAULT_SM_REGISTRY_PORT, DEFAULT_REGISTRY_ENDPOINT);

        private ProtocolAddressHolder implementation = new ProtocolAddressHolder(Schema.IGNORE, 
            DEFAULT_HOST, DEFAULT_PROTOCOL_PORT, DEFAULT_PROTOCOL);
        
        private String serverHost = NO_SPECIFIC_SERVER_HOST; // -> use what is stated in server/registry
        
        private AasMode mode = AasMode.REMOTE_DEPLOY;
        private String accessControlAllowOrigin = DeploymentRecipe.ANY_CORS_ORIGIN;
        private int aasStartupTimeout = 120000;
        private String pluginId; // default of AasFactory

        private State aasRegistryState = State.STOPPED;
        private State smRegistryState = State.STOPPED;
        private State aasRepoState = State.STOPPED;
        private State smRepoState = State.STOPPED;
        private State assetServerState = State.STOPPED;

        /**
         * Default constructor.
         */
        public AasSetup() {
        }

        /**
         * Copy constructor.
         * 
         * @param setup the setup to copy from
         */
        public AasSetup(AasSetup setup) {
            setServer(setup.server);
            setSubmodelServer(setup.smServer);
            setRegistry(setup.registry);
            setSubmodelRegistry(setup.smRegistry);
            setImplementation(setup.implementation);
            setMode(setup.mode);
        }
        
        /**
         * Returns the AAS server information as endpoint.
         * 
         * @return the AAS server information as endpoint
         */
        @JsonIgnore
        public Endpoint getServerEndpoint() {
            return server.getEndpoint();
        }

        /**
         * Returns the registry server information as endpoint.
         * 
         * @return the registry server information as endpoint
         */
        @JsonIgnore
        public Endpoint getRegistryEndpoint() {
            return registry.getEndpoint();
        }

        /**
         * Returns the implementation server information as server address.
         * 
         * @return the implementation server information as server address
         */
        @JsonIgnore
        public ServerAddress getImplementationServer() {
            return implementation.getServerAddress();
        }
        
        /**
         * Returns the implementation protocol.
         * 
         * @return the implementation protocol (see {@link AasFactory})
         */
        @JsonIgnore
        public String getImplementationProtocol() {
            return implementation.getProtocol();
        }

        /**
         * Returns the AAS server information.
         * 
         * @return the AAS server information
         */
        public EndpointHolder getServer() {
            return server;
        }

        /**
         * Returns the submodel server information.
         * 
         * @return the submodel server information
         */
        public EndpointHolder getSubmodelServer() {
            return smServer;
        }

        /**
         * Defines the AAS server information. [required by data mapper]
         * 
         * @param aas the AAS server information
         */
        public void setServer(EndpointHolder aas) {
            this.server = new EndpointHolder(aas);
            this.server.setValidator(RuntimeSetupEndpointValidator.create(r -> r.getAasServer()));
        }

        /**
         * Defines the submodel server information. [required by data mapper]
         * 
         * @param aas the submodel server information
         */
        public void setSubmodelServer(EndpointHolder aas) {
            this.smServer = new EndpointHolder(aas);
            this.smServer.setValidator(
                RuntimeSetupEndpointValidator.create(r -> r.getSubmodelServer(), false)); // false -> v1 optional
        }

        /**
         * Returns the AAS information. [required by data mapper]
         * 
         * @return the AAS information
         */
        public EndpointHolder getRegistry() {
            return registry;
        }

        /**
         * Returns the submodel information. [required by data mapper]
         * 
         * @return the submodel information
         */
        public EndpointHolder getSubmodelRegistry() {
            return smRegistry;
        }

        /**
         * Returns the AAS mode.
         * 
         * @return the AAS mode
         */
        public AasMode getMode() {
            return mode;
        }

        /**
         * Returns the AAS startup timeout, i.e., the time we may wait for an AAS server to come up.
         * 
         * @return the timeout in ms (default 120000)
         */
        public int getAasStartupTimeout() {
            return aasStartupTimeout;
        }

        /**
         * Changes the AAS startup timeout, i.e., the time we may wait for an AAS server to come up.
         * 
         * @param aasStartupTimeout the timeout in ms
         */
        public void setAasStartupTimeout(int aasStartupTimeout) {
            this.aasStartupTimeout = aasStartupTimeout;
        }

        /**
         * Defines the AAS mode. [required by data mapper, snakeyaml]
         * 
         * @param mode the AAS mode
         */
        public void setMode(AasMode mode) {
            this.mode = mode;
        }
        
        /**
         * Returns the server host. Often, the address stated in {@link #server} or {@link #registry} is sufficient.
         * However, if the devices shall use a specific address, while the server shall listen to multiple or all 
         * available IP addresses, the address to be used for server instance creation may have to be different, e.g., 
         * "localhost" rather than a specific IO.
         * 
         * @return the server host, may be {@link #NO_SPECIFIC_SERVER_HOST} to indicate that the addresses in 
         *     {@link #server} or {@link #registry} shall be used
         */
        public String getServerHost() {
            return serverHost;
        }
        
        /**
         * Potentially adapts the endpoint with respect to {@link #getServerEndpoint()}.
         * 
         * @param endpoint the endpoint to be adapted
         * @return the adapted endpoint or {@code endpoint}
         */
        public Endpoint adaptEndpoint(Endpoint endpoint) {
            Endpoint result;
            if (NO_SPECIFIC_SERVER_HOST.equals(serverHost)) {
                result = endpoint;
            } else {
                result = new Endpoint(endpoint.getSchema(), serverHost, endpoint.getPort(), endpoint.getEndpoint());
            }
            return result;
        }

        /**
         * Changes the server host. 
         * 
         * @param serverHost the server host, may be {@link #NO_SPECIFIC_SERVER_HOST} to indicate that the addresses in 
         *     {@link #server} or {@link #registry} shall be used
         * @see #getServerHost()
         */
        public void setServerHost(String serverHost) {
            this.serverHost = serverHost;
        }
        
        /**
         * Defines the registry information. [required by data mapper, snakeyaml]
         * 
         * @param registry the registry information
         */
        public void setRegistry(EndpointHolder registry) {
            this.registry = new EndpointHolder(registry);
            this.registry.setValidator(RuntimeSetupEndpointValidator.create(r -> r.getAasRegistry()));
        }
        
        /**
         * Defines the registry information. [required by data mapper, snakeyaml]
         * 
         * @param registry the registry information
         */
        public void setSubmodelRegistry(EndpointHolder registry) {
            this.smRegistry = new EndpointHolder(registry);
            this.smRegistry.setValidator(
                RuntimeSetupEndpointValidator.create(r -> r.getSubmodelRegistry(), false)); // false -> v1 optional
        }        

        /**
         * Returns the implementation (server) information. [required by data mapper, snakeyaml]
         * For convenience, the port number may be invalid and is turned then into an ephemeral port.
         * 
         * @return the implementation (server) information
         */
        public ProtocolAddressHolder getImplementation() {
            return implementation;
        }

        /**
         * Defines the implementation (server) information. [required by data mapper, snakeyaml]
         * 
         * @param implementation the implementation (server) information
         */
        public void setImplementation(ProtocolAddressHolder implementation) {
            this.implementation = implementation;
        }
        
        /**
         * Sets the access control to allow cross origin. [Snakeyaml]
         * 
         * @param accessControlAllowOrigin the information to be placed in the HTTP header field 
         * "Access-Control-Allow-Origin"; the specific server or {@link DeploymentRecipe#ANY_CORS_ORIGIN}
         */
        public void setAccessControlAllowOrigin(String accessControlAllowOrigin) {
            this.accessControlAllowOrigin = accessControlAllowOrigin;
        }

        /**
         * Returns the access control to allow cross origin.
         * 
         * @return the information to be placed in the HTTP header field 
         * "Access-Control-Allow-Origin"; the specific server or {@link DeploymentRecipe#ANY_CORS_ORIGIN}, 
         * may be <b>null</b> or empty
         */
        public String getAccessControlAllowOrigin() {
            return accessControlAllowOrigin;
        }

        /**
         * Sets the plugin id of the AAS implementation. [Snakeyaml]
         * 
         * @param pluginId the plugin id of the AAS implementation
         */
        public void setPluginId(String pluginId) {
            this.pluginId = pluginId;
        }

        /**
         * Returns the plugin id of the AAS implementation.
         * 
         * @return the plugin id, by default <b>null</b> leading to the default id in {@link AasFactory}
         */
        public String getPluginId() {
            return pluginId;
        }

        /**
         * Returns a default setup with all hosts to {@link ServerAddress#LOCALHOST} and all ports to ephemeral
         * while the registry port is the same as the aas port.
         * 
         * @return the local ephemeral setup
         */
        @JsonIgnore
        public static AasSetup createLocalEphemeralSetup() {
            return createLocalEphemeralSetup(null, true);
        }
        
        /**
         * Returns a default setup with all hosts to {@link ServerAddress#LOCALHOST} and all ports to ephemeral.
         * 
         * @param setup the instance to set up (if <b>null</b> a new one is created)
         * @param regPortSame shall the registry port be the same as the AAS port
         * @return the local ephemeral setup
         */
        @JsonIgnore
        public static AasSetup createLocalEphemeralSetup(AasSetup setup, boolean regPortSame) {
            return createLocalEphemeralSetup(setup, regPortSame, () -> new AasSetup());
        }

        /**
         * Returns a default setup with all hosts to {@link ServerAddress#LOCALHOST} and all ports to ephemeral.
         * 
         * @param <A> the AAS setup type
         * @param setup the instance to set up (if <b>null</b> a new one is created via {@code supplier})
         * @param regPortSame shall the registry port be the same as the AAS port
         * @param supplier a supplier for a new instance
         * @return the local ephemeral setup
         */
        @JsonIgnore
        public static <A extends AasSetup> A createLocalEphemeralSetup(A setup, boolean regPortSame, 
            Supplier<A> supplier) {
            A result = setup;
            if (null == result) {
                result = supplier.get();
            }
            result.getServer().setHost(ServerAddress.LOCALHOST);
            result.getServer().setPort(NetUtils.getEphemeralPort());
            result.getSubmodelServer().setHost(ServerAddress.LOCALHOST);
            result.getSubmodelServer().setPort(NetUtils.getEphemeralPort());
            result.getRegistry().setHost(ServerAddress.LOCALHOST);
            result.getRegistry().setPort(regPortSame ? result.getServer().getPort() 
                : NetUtils.getEphemeralPort());
            result.getSubmodelRegistry().setHost(ServerAddress.LOCALHOST);
            result.getSubmodelRegistry().setPort(regPortSame ? result.getServer().getPort() 
                : NetUtils.getEphemeralPort());
            result.getImplementation().setHost(ServerAddress.LOCALHOST);
            result.getImplementation().setPort(NetUtils.getEphemeralPort()); // could both be the same?
            return result;
        }
        
        @Override
        @JsonIgnore
        public Endpoint getAasRegistryEndpoint() {
            return registry.getEndpoint();
        }

        @Override
        public KeyStoreDescriptor getAasRegistryKeyStore() {
            return registry.getKeystoreDescriptor();
        }

        @Override
        public State getAasRegistryState() {
            return isRunning(registry, aasRegistryState);
        }

        @Override
        public void notifyAasRegistryStateChange(State state) {
            this.aasRegistryState = state;
        }

        @Override
        @JsonIgnore
        public Endpoint getSubmodelRegistryEndpoint() {
            return smRegistry.getEndpoint();
        }

        @Override
        public KeyStoreDescriptor getSubmodelRegistryKeyStore() {
            return smRegistry.getKeystoreDescriptor();
        }

        @Override
        public State getSubmodelRegistryState() {
            return isRunning(smRegistry, smRegistryState);
        }

        @Override
        public void notifySubmodelRegistryStateChange(State state) {
            this.smRegistryState = state;
        }

        @Override
        @JsonIgnore
        public Endpoint getAasRepositoryEndpoint() {
            return server.getEndpoint();
        }

        @Override
        public State getAasRepositoryState() {
            return isRunning(server, aasRepoState);
        }

        @Override
        public void notifyAasRepositoryStateChange(State state) {
            this.aasRepoState = state;
        }
        
        @Override
        @JsonIgnore
        public Endpoint getSubmodelRepositoryEndpoint() {
            return smServer.getEndpoint();
        }
        
        @Override
        public State getSubmodelRepositoryState() {
            return isRunning(smServer, smRepoState);
        }

        @Override
        public void notifySubmodelRepositoryStateChange(State state) {
            this.smRepoState = state;
        }

        @Override
        public KeyStoreDescriptor getAasRepositoryKeyStore() {
            return server.getKeystoreDescriptor();
        }

        @Override
        public KeyStoreDescriptor getSubmodelRepositoryKeyStore() {
            return smServer.getKeystoreDescriptor();
        }

        @Override
        public ServerAddress getAssetServerAddress() {
            return implementation.getServerAddress();
        }
        
        @Override
        public String getAssetServerProtocol() {
            return implementation.getProtocol();
        }

        @Override
        public State getAssetServerState() {
            return isRunning(implementation, assetServerState);
        }

        /**
         * Returns whether a certain server is running, based on the {@link ServerAddressHolder#isRunning()} in 
         * {@link State#STOPPED} or based on the notified state else.
         * 
         * @param holder the holder
         * @param state the notified state
         * @return the actual state
         */
        private State isRunning(ServerAddressHolder holder, State state) {
            State result = state;
            if (state == State.STOPPED && holder.isRunning()) {
                result = State.EXTERNAL;
            }
            return result;
        }

        @Override
        public void notifyAssetServerStateChange(State state) {
            this.assetServerState = state;
        }

        @Override
        public KeyStoreDescriptor getAssetServerKeyStore() {
            return implementation.getKeystoreDescriptor();
        }

    }
    
    /**
     * Sets a supplier to provide instance to the real AAS server instance (rather than remote/connected instances via 
     * {@link #retrieveIipAas()}. The real instance is e.g., needed to persist/store an AAS. Handle with care. 
     * 
     * @param supplier the supplier, may be <b>null</b> for none 
     */
    public static void setAasSupplier(Supplier<List<Aas>> supplier) {
        aasSupplier = supplier;
    }

    /**
     * Returns the AAS setup.
     * 
     * @return the setup
     */
    public static AasSetup getSetup() {
        return setup;
    }

    /**
     * Defines the AAS setup without resetting the AAS device/component implementation port.
     * 
     * @param aasSetup the setup information
     * @return the setup information before this call
     * @see #setAasSetup(AasSetup, boolean)
     */
    public static AasSetup setAasSetup(AasSetup aasSetup) {
        return setAasSetup(aasSetup, false);
    }

    /**
     * Defines the AAS setup.
     * 
     * @param aasSetup the setup information
     * @param resetImplPort reset or keep the AAS device/component implementation port
     * @return the setup information before this call
     */
    public static AasSetup setAasSetup(AasSetup aasSetup, boolean resetImplPort) {
        AasSetup old = aasSetup;
        setup = aasSetup;
        AasFactory.setPluginId(setup.getPluginId());
        if (resetImplPort) {
            aasImplPort = -1;
        }
        return old;
    }
    
    /**
     * Returns the contributor loader.
     * 
     * @return the loader instance
     */
    private static ServiceLoader<AasContributor> getContributorLoader() {
        return ServiceLoader.load(AasContributor.class);        
    }
    
    /**
     * Returns the contributors.
     * 
     * @return the contributors
     */
    public static Iterator<AasContributor> contributors() {
        return getContributorLoader().iterator();
    }
    
    /**
     * Returns the contributor classes.
     * 
     * @return the contributor classes
     */
    public static Set<Class<? extends AasContributor>> contributorClasses() {
        Set<Class<? extends AasContributor>> result = new HashSet<Class<? extends AasContributor>>();
        Iterator<AasContributor> iter = contributors();
        while (iter.hasNext()) {
            result.add(iter.next().getClass());
        }
        return result;
    }

    /**
     * Represents the result of building the platform AAS.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class AasBuildResult {
        
        private List<Aas> aas;
        private ProtocolServerBuilder sBuilder;
        private Server protocolServer;
        
        /**
         * Creates an instance.
         * 
         * @param aas the created AAS
         * @param sBuilder the server builder
         * @param protocolServer the protocol server instance (may be <b>null</b> if not started)
         */
        private AasBuildResult(List<Aas> aas, ProtocolServerBuilder sBuilder, Server protocolServer) {
            this.aas = aas;
            this.sBuilder = sBuilder;
            this.protocolServer = protocolServer;
        }
        
        /**
         * Returns the list of created AAS.
         * 
         * @return the created AAS
         */
        public List<Aas> getAas() {
            return aas;
        }
        
        /**
         * Returns the protocol server builder instance.
         * 
         * @return the server builder instance
         */
        public ProtocolServerBuilder getProtocolServerBuilder() {
            return sBuilder;
        }
        
        /**
         * Returns the protocol server instance.
         * 
         * @return the protocol server instance (may be <b>null</b> if not started)
         */
        public Server getProtocolServer() {
            return protocolServer;
        }
        
    }

    /**
     * Build up all AAS of the currently running platform part including all contributors. No implementation server 
     * is started.  [public for testing]
     * 
     * @return the list of AAS
     */
    public static AasBuildResult build() {
        return build(c -> true, false);
    }

    /**
     * Build up all AAS of the currently running platform part including all contributors. [public for testing]
     * 
     * @param startImplServer whether the implementation server shall be started before creating the AAS, this may be 
     *   required for incremental deployment
     * @return the list of AAS
     */
    public static AasBuildResult build(boolean startImplServer) {
        return build(c -> true, startImplServer);
    }
    
    /**
     * Build up all AAS of the currently running platform part. No implementation server is started. 
     * [public for testing]
     * 
     * @param filter filter out contributors, in particular for testing, e.g., active AAS that require an 
     *   implementation server
     * 
     * @return the list of AAS
     */
    public static AasBuildResult build(Predicate<AasContributor> filter) {
        return build(filter, false);
    }

    /**
     * Build up all AAS of the currently running platform part. [public for testing]
     * 
     * @param filter filter out contributors, in particular for testing, e.g., active AAS that require an 
     *   implementation server
     * @param startImplServer whether the implementation server shall be started before creating the AAS, this may be 
     *   required for incremental deployment
     * 
     * @return the list of AAS
     */
    public static AasBuildResult build(Predicate<AasContributor> filter, boolean startImplServer) {
        return build(filter, startImplServer, null);
    }

    /**
     * Build up all AAS of the currently running platform part. [public for testing]
     * 
     * @param filter filter out contributors, in particular for testing, e.g., active AAS that require an 
     *   implementation server
     * @param startImplServer whether the implementation server shall be started before creating the AAS, this may be 
     *   required for incremental deployment
     * @param sBuilder the protocol server builder. May be <b>null</b> then the method creates a new one, may 
     *   be an instance that must then match the settings in {@link AasSetup#getImplementation()}.
     * 
     * @return the list of AAS
     */
    public static AasBuildResult build(Predicate<AasContributor> filter, boolean startImplServer, 
        ProtocolServerBuilder sBuilder) {
        List<Aas> aas = new ArrayList<>();
        AasFactory factory = AasFactory.getInstance();
        
        AasBuilder aasBuilder;
        try {
            aasBuilder = retrieveIipAas().createAasBuilder();
        } catch (IOException e) {
            // fallback, AAS does not yet exist, top-level
            aasBuilder = factory.createAasBuilder(NAME_AAS, URN_AAS);
            // initial asset
            aasBuilder.createAssetBuilder(NAME_AAS, URN_AAS_ASSET, AssetKind.INSTANCE).build();
        }
        
        ProtocolAddressHolder impl = setup.getImplementation();
        if (aasImplPort < 0) { // do this only once, if e.g., for ecsSvcMgr
            aasImplPort = ServerAddress.validatePort(impl.getPort());
        }
        String implHost = impl.getHost();
        if (implHost.equals("127.0.0.1")) {
            // make AAS implementation server externally available
            implHost = NetUtils.getOwnIP(NetUtils.getNetMask(impl.getNetmask(), impl.getHost()));
        }
        impl.setHost(implHost);
        LoggerFactory.getLogger(AasPartRegistry.class).info("Using {}:{} for AAS implementation server", 
            implHost, aasImplPort);
        setup.getImplementation().setPort(aasImplPort);
        InvocablesCreator iCreator = factory.createInvocablesCreator(setup);
        if (null == sBuilder) {
            sBuilder = factory.createProtocolServerBuilder(setup);
        }
        Iterator<AasContributor> iter = contributors();
        while (iter.hasNext()) {
            AasContributor contributor = iter.next();
            if (filter.test(contributor) && contributor.isValid()) {
                Aas partAas = contributor.contributeTo(aasBuilder, iCreator);
                contributor.contributeTo(sBuilder);
                if (null != partAas) {
                    aas.add(partAas);
                }
            }
        }
        Server protocolServer = null;
        if (startImplServer) {
            LoggerFactory.getLogger(AasPartRegistry.class).info("Starting AAS implementation server on {}:{}", 
                implHost, aasImplPort);
            protocolServer = sBuilder.build().start();
        }
        aas.add(0, aasBuilder.build());
        return new AasBuildResult(aas, sBuilder, protocolServer);
    }
    
    // checkstyle: stop exception type check
    
    /**
     * Obtains the IIP-Ecosphere platform AAS. Be careful with the returned instance, as if
     * the AAS is modified in the mean time, you may hold an outdated instance.
     * 
     * @return the platform AAS (may be <b>null</b> for none)
     * @throws IOException if the AAS cannot be read due to connection errors
     */
    public static Aas retrieveIipAas() throws IOException {
        return retrieveAas(URN_AAS);
    }

    /**
     * Obtains an AAS instance via the setup in this class. Be careful with the returned instance, as if
     * the AAS is modified in the mean time, you may hold an outdated instance.
     * 
     * @param identifier the identifier of the AAS (may be <b>null</b> or empty for an identification based on 
     *    {@code idShort}, interpreted as an URN if this starts with {@code urn})
     * @return the platform AAS (may be <b>null</b> for none)
     * @throws IOException if the AAS cannot be read due to connection errors
     */
    public static Aas retrieveAas(String identifier) throws IOException {
        return retrieveAas(setup, identifier);
    }
    
    /**
     * Obtains an AAS instance. Be careful with the returned instance, as if the AAS is modified in the mean time, you 
     * may hold an outdated instance. Populates the submodels initially with elements.
     * 
     * @param setup the AAS setup
     * @param identifier the identifier of the AAS (may be <b>null</b> or empty for an identification based on 
     *    {@code idShort}, interpreted as an URN if this starts with {@code urn})
     * @return the platform AAS (may be <b>null</b> for none)
     * @throws IOException if the AAS cannot be read due to connection errors
     */
    public static Aas retrieveAas(AasSetup setup, String identifier) throws IOException {
        return retrieveAas(setup, identifier, true);
    }

    /**
     * Obtains an AAS instance. Be careful with the returned instance, as if the AAS is modified in the mean time, you 
     * may hold an outdated instance.
     * 
     * @param setup the AAS setup
     * @param identifier the identifier of the AAS (may be <b>null</b> or empty for an identification based on 
     *    {@code idShort}, interpreted as an URN if this starts with {@code urn})
     * @param populate populates the submodels initially with elements (performance!)    
     * @return the platform AAS (may be <b>null</b> for none)
     * @throws IOException if the AAS cannot be read due to connection errors
     */
    public static Aas retrieveAas(AasSetup setup, String identifier, boolean populate) throws IOException {
        Registry reg = AasFactory.getInstance().obtainRegistry(setup, setup.getServer().getSchema());
        if (null == reg) {
            throw new IOException("No AAS registry at " + setup.getRegistryEndpoint().toUri());
        }
        try {
            return AasFactory.getInstance().obtainRegistry(setup).retrieveAas(identifier, populate);
        } catch (Throwable t) {
            throw new IOException(t);
        }
    }

    /**
     * Returns the real AAS server instance based on the instances it was initially built. This instance
     * is only available on server side and shall be used only in cases where a potentially remote/connected
     * AAS (as it is typically returned by {@link #retrieveIipAas()}) is not sufficient, e.g., for storing/persisting
     * the AAS. It shall <b>not</b> be used for parallel accesses or for modifying the AAS.  
     *  
     * @return the AAS instance, may be <b>null</b>
     */
    public static List<Aas> getIipAasInstance() {
        return null != aasSupplier ? aasSupplier.get() : null;
    }

    // checkstyle: resume exception type check
    
    /**
     * Deploy the given AAS to a local server. However, server and registry are created within the same tomcat instance 
     * and cannot be executed with different TLS settings.[testing]
     * 
     * @param aas the list of aas, e.g., from {@link #build()}
     * @param options optional server creation options
     * @return the server instance
     */
    public static Server deploy(List<Aas> aas, String... options) {
        ImmediateDeploymentRecipe dBuilder = applyCorsOrigin(AasFactory.getInstance()
            .createDeploymentRecipe(setup), setup)
            .forRegistry();
        for (Aas a: aas) {
            dBuilder.deploy(a);
        }
        return dBuilder.createServer(options);
    }
    
    /**
     * Helper to apply the {@link AasSetup#getAccessControlAllowOrigin()} from {@code setup} to {@code rcp}.
     * 
     * @param <T> the receipt type
     * @param rcp the recipe
     * @param setup the setup to take the information from
     * @return the rcp
     */
    public static <T extends CorsEnabledRecipe> T applyCorsOrigin(T rcp, AasSetup setup) {
        String acao = setup.getAccessControlAllowOrigin();
        if (null != acao && acao.length() > 0) {
            rcp.setAccessControlAllowOrigin(acao);
        }
        return rcp;
    }

    /**
     * Registers the given AAS to a remote registry and creates a local server for the AAS.
     * 
     * @param aas the list of aas, e.g., from {@link #build()}
     * @param registry optional registry endpoint for remote registration, assuming a local in-memory registry 
     *     if <b>null</b>
     * @param options optional server creation options
     * @return the server instance
     * @throws IOException if access to the AAS registry fails
     */
    public static Server register(List<Aas> aas, Endpoint registry, String... options) throws IOException {
        RegistryDeploymentRecipe dBuilder = applyCorsOrigin(AasFactory.getInstance()
            .createDeploymentRecipe(setup), setup)
            .forRegistry(registry);
        Registry reg = dBuilder.obtainRegistry();
        for (Aas a: aas) {
            for (Submodel s : a.submodels()) {
                reg.register(a, s, null);
            }
        }
        return dBuilder.createServer(options);
    }

    /**
     * Performs a remote deployment of the given {@code aas}. Assumes that server and registry are up and running.
     * 
     * @param aas the list of AAS, e.g., from {@link #build()}
     * @throws IOException if the deployment of an AAS fails or access to the AAS registry fails
     */
    public static void remoteDeploy(List<Aas> aas) throws IOException {
        remoteDeploy(setup, aas);
    }

    /**
     * Performs a remote deployment of the given {@code aas}. Assumes that server and registry are up and running.
     * 
     * @param setup the AAS setup to use
     * @param aas the list of AAS, e.g., from {@link #build()}
     * @throws IOException if the deployment of an AAS fails or access to the AAS registry fails
     */
    public static void remoteDeploy(AasSetup setup, List<Aas> aas) throws IOException {
        Endpoint aasEndpoint = setup.getServerEndpoint();
        RegistryDeploymentRecipe regD = applyCorsOrigin(AasFactory.getInstance()
            .createDeploymentRecipe(setup), setup)
            .forRegistry(setup.getRegistryEndpoint());
        
        Registry reg = regD.obtainRegistry();
        for (Aas a: aas) {
            try {
                reg.createAas(a, aasEndpoint.toServerUri()); // not the registry URI!
            } catch (IllegalArgumentException e) {
                // well, already there, ignore
            }
            for (Submodel s : a.submodels()) {
                try {
                    reg.createSubmodel(a, s);
                } catch (IllegalArgumentException e) {
                    // well, already there, ignore
                }   
            }
        }
    }
    
    /**
     * Returns the first AAS in {@code list} matching the given name. [utility]
     * 
     * @param list the list to consider
     * @param idShort the short name to filter for
     * @return the first AAS or <b>null</b> for none
     */
    public static Aas getAas(List<Aas> list, String idShort) {
        return list.stream()
            .filter(a -> a.getIdShort().equals(idShort))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Returns the AAS registry for the endpoint in the setup of {@link AasPartRegistry}.
     * 
     * @return the registry, may be <b>null</b> if there is none
     */
    public static Registry getIipAasRegistry() {
        Registry reg = null; 
        try {
            reg = AasFactory.getInstance().obtainRegistry(AasPartRegistry.getSetup());
        } catch (IOException e) {
            LoggerFactory.getLogger(AasPartRegistry.class).error("Obtaining AAS registry: {}. AAS linking disabled.",
                e.getMessage());
        }
        return reg;
    }

    /**
     * Adds a property to {@code builder} pointing to an AAS endpoint for an AAS with id {@code serviceId} 
     * in registry {@code reg}.
     * 
     * @param reg the registry, may be <b>null</b> then the property will have an empty value.
     * @param builder the builder to add the property to
     * @param property the shortId of the property to create
     * @param aasId the id of the AAS
     * @return the created AAS property
     */
    private static Property addAasEndpointProperty(Registry reg, SubmodelElementCollectionBuilder builder, 
        String property, String aasId) {
        String ep = null == reg ? null : reg.getEndpoint(AasUtils.fixId(aasId));
        if (null == ep) {
            ep = "";
        }
        return builder.createPropertyBuilder(property) // no reference resolution in BaSyx so far
            .setValue(Type.STRING, ep)
            .build();
    }
    
    /**
     * Adds a property to {@code builder} pointing to an AAS endpoint for a service with id {@code serviceId} 
     * in registry {@code reg}.
     * 
     * @param reg the registry, may be <b>null</b> then the property will have an empty value.
     * @param builder the builder to add the property to
     * @param property the idShort of the property to create
     * @param serviceId the id of the service, may be empty leading to an empty property value
     * @return the created AAS property
     */
    public static Property addServiceAasEndpointProperty(Registry reg, SubmodelElementCollectionBuilder builder, 
        String property, String serviceId) {
        return addAasEndpointProperty(reg, builder, property, serviceId.length() == 0 ? "" : "service_" + serviceId);
    }
    
    /**
     * Adds a property to {@code builder} pointing to an AAS endpoint for a device with id {@code deviceId} in 
     * registry {@code reg}.
     * 
     * @param reg the registry, may be <b>null</b> then the property will have an empty value.
     * @param builder the builder to add the property to
     * @param property the idShort of the property to create
     * @param deviceId the id of the device, may be empty leading to an empty property value
     * @return the created AAS property
     */
    public static Property addDeviceAasEndpointProperty(Registry reg, SubmodelElementCollectionBuilder builder, 
        String property, String deviceId) {
        return addAasEndpointProperty(reg, builder, property, deviceId.length() == 0 ? "" : "device_" + deviceId);
    }

}
