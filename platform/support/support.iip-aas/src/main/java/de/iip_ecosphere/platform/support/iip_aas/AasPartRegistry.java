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
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe.ImmediateDeploymentRecipe;
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe.RegistryDeploymentRecipe;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.iip_aas.config.EndpointHolder;
import de.iip_ecosphere.platform.support.iip_aas.config.ProtocolAddressHolder;
import de.iip_ecosphere.platform.support.jsl.ExcludeFirst;

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
    public static final String NAME_SUBMODEL_RESOURCES = "resources"; 
    
    /**
     * The URN of the top-level AAS created by this registry in {@link #build()}.
     */
    public static final String URN_AAS = "urn:::AAS:::iipEcosphere#";
    
    public static final Schema DEFAULT_SCHEMA = Schema.HTTP;
    public static final String DEFAULT_HOST = ServerAddress.LOCALHOST;
    public static final int DEFAULT_PORT = 8080;
    public static final int DEFAULT_REGISTRY_PORT = 8081; // shall also be on 8080; two processes in one needs revision
    public static final int DEFAULT_PROTOCOL_PORT = 9000;
    public static final String DEFAULT_AAS_ENDPOINT = "";
    public static final String DEFAULT_REGISTRY_ENDPOINT = "registry";
    public static final String DEFAULT_PROTOCOL = AasFactory.DEFAULT_PROTOCOL;
    
    private static AasSetup setup = new AasSetup();

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
    public static class AasSetup {

        private EndpointHolder server = new EndpointHolder(AasPartRegistry.DEFAULT_SCHEMA, 
            DEFAULT_HOST, DEFAULT_PORT, DEFAULT_AAS_ENDPOINT);

        private EndpointHolder registry = new EndpointHolder(DEFAULT_SCHEMA, DEFAULT_HOST, 
            DEFAULT_REGISTRY_PORT, DEFAULT_REGISTRY_ENDPOINT);

        private ProtocolAddressHolder implementation = new ProtocolAddressHolder(Schema.IGNORE, 
            DEFAULT_HOST, DEFAULT_PROTOCOL_PORT, DEFAULT_PROTOCOL);
        
        private AasMode mode = AasMode.REMOTE_DEPLOY;
        
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
         * Defines the AAS server information. [required by data mapper]
         * 
         * @param aas the AAS server information
         */
        public void setServer(EndpointHolder aas) {
            this.server = aas;
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
         * Returns the AAS mode.
         * 
         * @return the AAS mode
         */
        public AasMode getMode() {
            return mode;
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
         * Defines the registry information. [required by data mapper, snakeyaml]
         * 
         * @param registry the registry information
         */
        public void setRegistry(EndpointHolder registry) {
            this.registry = registry;
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
            result.getRegistry().setHost(ServerAddress.LOCALHOST);
            result.getRegistry().setPort(regPortSame ? result.getServer().getPort() : NetUtils.getEphemeralPort());
            result.getImplementation().setHost(ServerAddress.LOCALHOST);
            result.getImplementation().setPort(NetUtils.getEphemeralPort()); // could both be the same?
            return result;
        }

    }
    
    /**
     * Defines the AAS endpoint.
     * 
     * @param endpoint the registry endpoint 
     * @return the endpoint before this call
     * @deprecated use {@link #setAasSetup(AasSetup)} instead
     */
    @Deprecated
    public static Endpoint setAasEndpoint(Endpoint endpoint) {
        Endpoint aas = setup.getServerEndpoint();
        Endpoint reg = setup.getRegistryEndpoint();
        
        Endpoint old = new Endpoint(aas.getSchema(), aas.getHost(), aas.getPort(), reg.getEndpoint());
        setup.setServer(new EndpointHolder(endpoint.getSchema(), endpoint.getHost(), endpoint.getPort(), 
            DEFAULT_AAS_ENDPOINT));
        setup.setRegistry(new EndpointHolder(endpoint.getSchema(), endpoint.getHost(), endpoint.getPort(), 
            endpoint.getEndpoint()));
        return old;
    }

    /**
     * Defines the operation/property implementation protocol address.
     * 
     * @param address the address
     * @return the address before this call
     * @deprecated use {@link #setAasSetup(AasSetup)} instead
     */
    @Deprecated
    public static ServerAddress setProtocolAddress(ServerAddress address) {
        ServerAddress old = setup.getImplementationServer();
        setup.setImplementation(new ProtocolAddressHolder(address.getSchema(), address.getHost(), address.getPort(), 
            DEFAULT_PROTOCOL));
        return old;
    }

    /**
     * Defines the AAS setup.
     * 
     * @param aasSetup the setup information
     * @return the setup information before this call
     */
    public static AasSetup setAasSetup(AasSetup aasSetup) {
        AasSetup old = aasSetup;
        setup = aasSetup;
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
        List<Aas> aas = new ArrayList<>();
        AasFactory factory = AasFactory.getInstance();
        
        AasBuilder aasBuilder;
        try {
            aasBuilder = retrieveIipAas().createAasBuilder();
        } catch (IOException e) {
            // fallback, AAS does not yet exist, top-level
            aasBuilder = factory.createAasBuilder(NAME_AAS, URN_AAS);
        }
        
        ProtocolAddressHolder impl = setup.getImplementation();
        int implPort = ServerAddress.validatePort(impl.getPort());
        String implHost = impl.getHost();
        if (implHost.equals("127.0.0.1")) {
            implHost = NetUtils.getOwnIP(impl.getNetmask()); // make AAS implementation server externally available
            LoggerFactory.getLogger(AasPartRegistry.class).warn("Using IP " + implHost 
                + " for AAS implementation server");
        }
        InvocablesCreator iCreator = factory.createInvocablesCreator(impl.getProtocol(), implHost, implPort);
        ProtocolServerBuilder sBuilder = factory.createProtocolServerBuilder(impl.getProtocol(), implPort);
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
            LoggerFactory.getLogger(AasPartRegistry.class).info("Starting implementation server on " + implPort);
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
        Registry reg = AasFactory.getInstance().obtainRegistry(setup.getRegistryEndpoint());
        if (null == reg) {
            throw new IOException("No AAS registry at " + setup.getRegistryEndpoint().toUri());
        }
        try {
            return AasFactory.getInstance().obtainRegistry(setup.getRegistryEndpoint()).retrieveAas(URN_AAS);
        } catch (Throwable t) {
            throw new IOException(t);
        }
    }

    // checkstyle: start exception type check
    
    /**
     * Deploy the given AAS to a local server. [testing]
     * 
     * @param aas the list of aas, e.g., from {@link #build()}
     * @param options optional server creation options
     * @return the server instance
     */
    public static Server deploy(List<Aas> aas, String... options) {
        ImmediateDeploymentRecipe dBuilder = AasFactory.getInstance()
            .createDeploymentRecipe(setup.getServerEndpoint())
            .addInMemoryRegistry(setup.getRegistry().getPath());
        for (Aas a: aas) {
            dBuilder.deploy(a);
        }
        return dBuilder.createServer(options);
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
        RegistryDeploymentRecipe dBuilder = AasFactory.getInstance()
            .createDeploymentRecipe(setup.getServerEndpoint())
            .setRegistryUrl(registry);
        Registry reg = dBuilder.obtainRegistry();
        for (Aas a: aas) {
            for (Submodel s : a.submodels()) {
                reg.register(a, s, null);
            }
        }
        return dBuilder.createServer(options);
    }

    /**
     * Performs a remote deployment of the given {@code aas}.
     * 
     * @param aas the list of AAS, e.g., from {@link #build()}
     * @throws IOException if the deployment of an AAS fails or access to the AAS registry fails
     */
    public static void remoteDeploy(List<Aas> aas) throws IOException {
        Endpoint aasEndpoint = setup.getServerEndpoint();
        RegistryDeploymentRecipe regD = AasFactory.getInstance()
            .createDeploymentRecipe(aasEndpoint)
            .setRegistryUrl(setup.getRegistryEndpoint());
        
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

}
