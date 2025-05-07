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

package de.iip_ecosphere.platform.support.aas;

import java.util.function.Function;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;

/**
 * Plain implementation of the {@link SetupSpec} interface. For registry use, the repository endpoints are used only
 * for registering new AAS/submodels, i.e., for the base URLs.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BasicSetupSpec implements SetupSpec {

    private BasicSetupSpec from;
    private Endpoint aasRegistryEndpoint;
    private KeyStoreDescriptor aasRegistryKeyStore;
    private State aasRegistryState = State.STOPPED;
    private Endpoint smRegistryEndpoint;
    private KeyStoreDescriptor smRegistryKeyStore;
    private State smRegistryState = State.STOPPED;
    private Endpoint aasRepoEndpoint;
    private State aasRepoState = State.STOPPED;
    private KeyStoreDescriptor aasRepoKeyStore;
    private Endpoint smRepoEndpoint;
    private State smRepoState = State.STOPPED;
    private KeyStoreDescriptor smRepoKeyStore;
    private ServerAddress assetServerAddress;
    private State assetServerState = State.STOPPED;
    private String assetServerProtocol = AasFactory.DEFAULT_PROTOCOL;
    private KeyStoreDescriptor assetServerKeyStore;

    /**
     * Creates an empty setup spec. Will not work with metamodel v3, just for testing.
     */
    public BasicSetupSpec() {
        this((Endpoint) null);
    }

    /**
     * Creates an empty setup spec, just for asset protocol implementation.
     */
    public BasicSetupSpec(String protocol, int port) {
        this(protocol, ServerAddress.LOCALHOST, port);
    }

    /**
     * Creates an empty setup spec, just for asset protocol implementation.
     */
    public BasicSetupSpec(String protocol, String host, int port) {
        this();
        setAssetServerAddress(new ServerAddress(Schema.IGNORE, host, port), protocol);
    }
    
    /**
     * Creates a setup instance for old-style AAS querying on a joined registry. Will not work with metamodel v3,
     * just for migration purposes
     * 
     * @param registryEndpoint the AAS registry endpoint
     */
    public BasicSetupSpec(Endpoint registryEndpoint) {
        this(registryEndpoint, registryEndpoint, null, null);
    }

    /**
     * Creates a setup instance for old-style AAS with joined registry/repository. Will not work with metamodel v3,
     * just for migration purposes
     * 
     * @param registryEndpoint the AAS registry endpoint
     * @param repositoryAddress the address of the repository, used to construct an endpoint with empty path
     */
    public BasicSetupSpec(Endpoint registryEndpoint, ServerAddress repositoryAddress) {
        this(registryEndpoint, repositoryAddress, null);
    }

    /**
     * Creates a setup instance for old-style AAS with joined registry/repository. Will not work with metamodel v3,
     * just for migration purposes
     * 
     * @param registryEndpoint the AAS registry endpoint
     * @param repositoryAddress the address of the repository, used to construct an endpoint with empty path
     * @param desc the server keystore descriptor
     */
    public BasicSetupSpec(Endpoint registryEndpoint, ServerAddress repositoryAddress, KeyStoreDescriptor desc) {
        this(registryEndpoint, new Endpoint(repositoryAddress, ""), desc);
    }

    /**
     * Creates a setup instance for old-style AAS with joined registry/repository/asset. Will not work with metamodel 
     * v3, just for migration purposes
     * 
     * @param registryEndpoint the AAS registry endpoint
     * @param repositoryEndpoint the repository endpoint
     * @param desc the server keystore descriptor
     */
    public BasicSetupSpec(Endpoint registryEndpoint, Endpoint repositoryEndpoint, KeyStoreDescriptor desc) {
        this(registryEndpoint, repositoryEndpoint);
        setAasRepositoryKeystore(desc);
        setSubmodelRepositoryKeystore(desc);
        setAasRegistryKeystore(desc);
        setSubmodelRegistryKeystore(desc);
        setAssetServerKeystore(desc);
    }

    /**
     * Creates a setup instance for old-style AAS with joined registry/repository. Adds ephemeral endpoints for 
     * submodel for metamodel v3.
     * 
     * @param registryEndpoint the AAS registry endpoint
     * @param repositoryEndpoint the AAS repository endpoint
     */
    public BasicSetupSpec(Endpoint registryEndpoint, Endpoint repositoryEndpoint) {
        this(registryEndpoint, new Endpoint(registryEndpoint.getSchema(), ""), 
            repositoryEndpoint, new Endpoint(repositoryEndpoint.getSchema(), ""));
    }
    
    /**
     * Creates a setup instance.
     * 
     * @param aasRegistryEndpoint the AAS registry endpoint
     * @param smRegistryEndpoint the submodel registry endpoint
     * @param aasRepositoryEndpoint the AAS repository endpoint
     * @param smRepositoryEndpoint the submode repository endpoint
     */
    public BasicSetupSpec(Endpoint aasRegistryEndpoint, Endpoint smRegistryEndpoint, Endpoint aasRepositoryEndpoint, 
        Endpoint smRepositoryEndpoint) {
        setRegistryEndpoints(aasRegistryEndpoint, smRegistryEndpoint);
        this.aasRepoEndpoint = aasRepositoryEndpoint;
        this.smRepoEndpoint = smRepositoryEndpoint;
    }
    
    /**
     * Copies {@code spec} into this new instance.
     * 
     * @param spec the specification to take the information from
     */
    public BasicSetupSpec(SetupSpec spec) {
        if (spec instanceof BasicSetupSpec) {
            this.from = (BasicSetupSpec) spec;
        }
        setRegistryEndpoints(spec.getAasRegistryEndpoint(), spec.getSubmodelRegistryEndpoint());
        this.aasRegistryState = spec.getAasRegistryState();
        this.aasRegistryKeyStore = spec.getAasRegistryKeyStore();
        this.smRegistryState = spec.getSubmodelRegistryState();
        this.smRegistryKeyStore = spec.getSubmodelRegistryKeyStore();
        this.aasRepoEndpoint = spec.getAasRepositoryEndpoint();
        this.aasRepoState = spec.getAasRepositoryState();
        this.aasRepoKeyStore = spec.getAasRepositoryKeyStore();
        this.smRepoEndpoint = spec.getAasRegistryEndpoint();
        this.smRepoState = spec.getSubmodelRepositoryState();
        this.smRepoKeyStore = spec.getSubmodelRepositoryKeyStore();
        this.assetServerAddress = spec.getAssetServerAddress();
        this.assetServerState = spec.getAssetServerState();
        this.assetServerKeyStore = spec.getAssetServerKeyStore();
        this.assetServerProtocol = spec.getAssetServerProtocol();
    }
    
    /**
     * Changes the AAS repository endpoint.
     * 
     * @param endpoint the new endpoint
     * @return <b>this</b> for chaining
     */
    public BasicSetupSpec setAasRepositoryEndpoint(Endpoint endpoint) {
        this.aasRepoEndpoint = endpoint;
        return this;
    }

    /**
     * Sets the registry endpoints for a single registry setup. This is not intended for metamodel v3.
     * 
     * @param endpoint the registry endpoint
     * @return <b>this</b> for chaining
     */
    public BasicSetupSpec setRegistryEndpoint(Endpoint endpoint) {
        setRegistryEndpoints(endpoint, endpoint);
        return this;
    }

    /**
     * Sets the registry endpoints.
     * 
     * @param aasRegistryEndpoint the AAS registry endpoint
     * @param smRegistryEndpoint the submodel registry endpoint
     * @return <b>this</b> for chaining
     */
    public BasicSetupSpec setRegistryEndpoints(Endpoint aasRegistryEndpoint, Endpoint smRegistryEndpoint) {
        this.aasRegistryEndpoint = aasRegistryEndpoint;
        this.smRegistryEndpoint = smRegistryEndpoint;
        return this;
    }
    
    /**
     * Sets the AAS repository keystore.
     * 
     * @param desc the keystore descriptor, may be <b>null</b> for none
     * @return <b>this</b> for chaining
     */
    public BasicSetupSpec setAasRepositoryKeystore(KeyStoreDescriptor desc) {
        this.aasRepoKeyStore = desc;
        return this;
    }

    /**
     * Sets the submodel repository keystore.
     * 
     * @param desc the keystore descriptor, may be <b>null</b> for none
     * @return <b>this</b> for chaining
     */
    public BasicSetupSpec setSubmodelRepositoryKeystore(KeyStoreDescriptor desc) {
        this.smRepoKeyStore = desc;
        return this;
    }

    @Override
    public Endpoint getAasRegistryEndpoint() {
        return aasRegistryEndpoint;
    }
    
    @Override
    public KeyStoreDescriptor getAasRegistryKeyStore() {
        return aasRegistryKeyStore;
    }

    /**
     * Sets the AAS registry keystore.
     * 
     * @param desc the keystore descriptor, may be <b>null</b> for none
     * @return <b>this</b> for chaining
     */
    public BasicSetupSpec setAasRegistryKeystore(KeyStoreDescriptor desc) {
        this.aasRegistryKeyStore = desc;
        return this;
    }

    @Override
    public State getAasRegistryState() {
        return getState(s -> s.aasRegistryState);
    }

    @Override
    public void notifyAasRegistryStateChange(State state) {
        setState((o, s) -> o.aasRegistryState = s, state);
    }

    @Override
    public Endpoint getSubmodelRegistryEndpoint() {
        return smRegistryEndpoint;
    }


    @Override
    public KeyStoreDescriptor getSubmodelRegistryKeyStore() {
        return smRegistryKeyStore;
    }

    /**
     * Sets the submodel registry keystore.
     * 
     * @param desc the keystore descriptor, may be <b>null</b> for none
     * @return <b>this</b> for chaining
     */
    public BasicSetupSpec setSubmodelRegistryKeystore(KeyStoreDescriptor desc) {
        this.smRegistryKeyStore = desc;
        return this;
    }

    @Override
    public State getSubmodelRegistryState() {
        return getState(s -> s.smRegistryState);
    }

    @Override
    public void notifySubmodelRegistryStateChange(State state) {
        setState((o, s) -> o.smRegistryState = s, state);
    }
    
    @Override
    public Endpoint getAasRepositoryEndpoint() {
        return aasRepoEndpoint;
    }

    @Override
    public State getAasRepositoryState() {
        return getState(s -> s.aasRepoState);
    }

    @Override
    public void notifyAasRepositoryStateChange(State state) {
        setState((o, s) -> o.aasRepoState = s, state);
    }
    
    @Override
    public KeyStoreDescriptor getAasRepositoryKeyStore() {
        return aasRepoKeyStore;
    }

    @Override
    public Endpoint getSubmodelRepositoryEndpoint() {
        return smRepoEndpoint;
    }

    @Override
    public State getSubmodelRepositoryState() {
        return getState(s -> s.smRepoState);
    }

    @Override
    public void notifySubmodelRepositoryStateChange(State state) {
        setState((o, s) -> o.smRepoState = s, state);
    }
    
    @Override
    public KeyStoreDescriptor getSubmodelRepositoryKeyStore() {
        return smRepoKeyStore;
    }

    @Override
    public boolean areRegistriesRunning() {
        return false;
    }

    /**
     * Changes the asset server address.
     * 
     * @param address the new address
     * @return <b>this</b> for chaining
     */
    public BasicSetupSpec setAssetServerAddress(ServerAddress address) {
        return setAssetServerAddress(address, assetServerProtocol);
    }

    /**
     * Changes the asset server address and protocol.
     * 
     * @param address the new address
     * @param protocol the protocol, empty for default
     * @return <b>this</b> for chaining
     */
    public BasicSetupSpec setAssetServerAddress(ServerAddress address, String protocol) {
        this.assetServerAddress = address;
        this.assetServerProtocol = protocol;
        return this;
    }

    @Override
    public ServerAddress getAssetServerAddress() {
        return assetServerAddress;
    }

    @Override
    public String getAssetServerProtocol() {
        return assetServerProtocol;
    }

    @Override
    public State getAssetServerState() {
        return getState(s -> s.assetServerState);
    }

    @Override
    public void notifyAssetServerStateChange(State state) {
        setState((o, s) -> o.assetServerState = s, state);
    }
    
    @Override
    public KeyStoreDescriptor getAssetServerKeyStore() {
        return assetServerKeyStore;
    }
    
    /**
     * A specific state setter functor.
     */
    private interface StateSetter {

        /**
         * Sets {@code state} on {@code spec}.
         * 
         * @param spec the specification object
         * @param state the new state
         */
        void set(BasicSetupSpec spec, State state);
        
    }

    /**
     * Changes the state via the given {@code setter} either on the copy source instance ({@link #from })
     * or on this instance.
     * 
     * @param setter the setter
     * @param state the new state
     */
    private void setState(StateSetter setter, State state) {
        setter.set(null == from ? this : from, state);
    }

    /**
     * Returns the state via the given {@code getter} either from the copy source instance ({@link #from })
     * or from this instance.
     * 
     * @param getter the getter
     * @return the state
     */
    private State getState(Function<BasicSetupSpec, State> getter) {
        return getter.apply(null == from ? this : from);
    }

    /**
     * Sets the asset server keystore.
     * 
     * @param desc the keystore descriptor, may be <b>null</b> for none
     * @return <b>this</b> for chaining
     */
    public BasicSetupSpec setAssetServerKeystore(KeyStoreDescriptor desc) {
        this.assetServerKeyStore = desc;
        return this;
    }

    /**
     * Turns a key store descriptor into an output for {@link #toString()}.
     * 
     * @param desc the descriptor, may be <b>null</b>
     * @return the textual form
     */
    private String toString(KeyStoreDescriptor desc) {
        return " keystore: " + (desc != null);
    }

    /**
     * Turns a server address into a string.
     * 
     * @param address the address, may be <b>null</b>
     * @return the textual form
     */
    private String toString(ServerAddress address) {
        return null == address ? "-" : address.toUri();
    }
    
    @Override
    public String toString() {
        String cr = System.lineSeparator();
        return "Basic AAS setup specification:" + cr 
            + " - AAS registry:        " + toString(aasRegistryEndpoint) + toString(aasRegistryKeyStore) + cr
            + " - AAS repository:      " + toString(aasRepoEndpoint) + toString(aasRepoKeyStore) + cr
            + " - Submodel registry:   " + toString(smRegistryEndpoint) + toString(smRegistryKeyStore) + cr
            + " - Submodel repository: " + toString(smRepoEndpoint) + toString(smRepoKeyStore) + cr
            + " - Asset server:        " + toString(assetServerAddress) + toString(assetServerKeyStore) 
                + " @ '" + assetServerProtocol + "'";
    }

}
