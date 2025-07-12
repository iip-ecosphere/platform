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

import java.util.HashMap;
import java.util.Map;

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

    private Map<AasComponent, BasicComponentSetup> setups = new HashMap<>();
    
    /**
     * Implements the component setup.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected class BasicComponentSetup implements ComponentSetup {

        private ServerAddress serverAddress;
        private Endpoint endpoint;
        private KeyStoreDescriptor keyStore;
        private State state = State.STOPPED;
        private AuthenticationDescriptor authentication;
        private AasComponent component;
        
        /**
         * Represents the setup for a complete component.
         * 
         * @param component the component (type) to represent
         */
        protected BasicComponentSetup(AasComponent component) {
            this.component = component;
        }
        
        @Override
        public ServerAddress getServerAddress() {
            return null == serverAddress ? endpoint : serverAddress;
        }        

        @Override
        public Endpoint getEndpoint() {
            return endpoint;
        }

        @Override
        public KeyStoreDescriptor getKeyStore() {
            return keyStore;
        }

        @Override
        public AuthenticationDescriptor getAuthentication() {
            return authentication;
        }

        @Override
        public State getState() {
            return null == from ? state : from.getSetup(component).getState();
        }

        @Override
        public void notifyStateChange(State state) {
            if (null == from) {
                this.state = state;
            } else {
                from.getSetup(component).notifyStateChange(state);
            }
        }
        
    }
    
    {
        for (AasComponent c : AasComponent.values()) {
            setups.put(c, new BasicComponentSetup(c));
        }
    }
    
    private BasicSetupSpec from;
    private String assetServerProtocol = AasFactory.DEFAULT_PROTOCOL;

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
        this(registryEndpoint, registryEndpoint, (Endpoint) null, (Endpoint) null);
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
        this(registryEndpoint, repositoryAddress, desc, null);
    }

    /**
     * Creates a setup instance for old-style AAS with joined registry/repository. Will not work with metamodel v3,
     * just for migration purposes
     * 
     * @param registryEndpoint the AAS registry endpoint
     * @param repositoryAddress the address of the repository, used to construct an endpoint with empty path
     * @param kDesc an optional server keystore descriptor (may be <b>null</b> for none)
     * @param aDesc an optional authentication descriptor (may be <b>null</b> for none)
     */
    public BasicSetupSpec(Endpoint registryEndpoint, ServerAddress repositoryAddress, KeyStoreDescriptor kDesc, 
        AuthenticationDescriptor aDesc) {
        this(registryEndpoint, new Endpoint(repositoryAddress, ""), kDesc, aDesc);
    }

    /**
     * Creates a setup instance for old-style AAS with joined registry/repository/asset. Will not work with metamodel 
     * v3, just for migration purposes
     * 
     * @param registryEndpoint the AAS registry endpoint
     * @param repositoryEndpoint the repository endpoint
     * @param kDesc an optional server keystore descriptor (may be <b>null</b> for none)
     * @param aDesc an optional authentication descriptor (may be <b>null</b> for none)
     */
    public BasicSetupSpec(Endpoint registryEndpoint, Endpoint repositoryEndpoint, KeyStoreDescriptor kDesc, 
        AuthenticationDescriptor aDesc) {
        this(registryEndpoint, repositoryEndpoint);
        
        setAasRepositoryKeystore(kDesc);
        setSubmodelRepositoryKeystore(kDesc);
        setAasRegistryKeystore(kDesc);
        setSubmodelRegistryKeystore(kDesc);
        setAssetServerKeystore(kDesc);

        setAuthentication(aDesc);
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
        setups.get(AasComponent.AAS_REPOSITORY).endpoint = aasRepositoryEndpoint;
        setups.get(AasComponent.SUBMODEL_REPOSITORY).endpoint = smRepositoryEndpoint;
    }
    
    /**
     * Copies {@code spec} into this new instance.
     * 
     * @param spec the specification to take the information from
     */
    public BasicSetupSpec(SetupSpec spec) {
        if (spec instanceof BasicSetupSpec) {
            this.from = (BasicSetupSpec) spec;
            for (AasComponent c : AasComponent.values()) {
                setups.get(c).state = this.from.setups.get(c).state;
                setups.get(c).serverAddress = this.from.setups.get(c).serverAddress;
                setups.get(c).endpoint = this.from.setups.get(c).endpoint;
                setups.get(c).keyStore = this.from.setups.get(c).keyStore;
                setups.get(c).authentication = this.from.setups.get(c).authentication;
            }
        } else {
            for (AasComponent c : AasComponent.values()) {
                setups.get(c).state = spec.getSetup(c).getState();
                setups.get(c).serverAddress = spec.getSetup(c).getServerAddress(); // may be endpoint
                setups.get(c).endpoint = spec.getSetup(c).getEndpoint();
                setups.get(c).keyStore = spec.getSetup(c).getKeyStore();
                setups.get(c).authentication = spec.getSetup(c).getAuthentication();
            }
        }
        this.assetServerProtocol = spec.getAssetServerProtocol();
    }

    /**
     * Changes the authentication of all elements.
     * 
     * @param aDesc the authentication descriptor to use, may be <b>null</b> for none
     * @return <b>this</b> for chaining
     */
    public BasicSetupSpec setAuthentication(AuthenticationDescriptor aDesc) {
        setAasRepositoryAuthentication(aDesc);
        setSubmodelRepositoryAuthentication(aDesc);
        setAasRegistryAuthentication(aDesc);
        setSubmodelRegistryAuthentication(aDesc);
        setAssetServerAuthentication(aDesc);
        return this;
    }
    
    /**
     * Changes the AAS repository endpoint.
     * 
     * @param endpoint the new endpoint
     * @return <b>this</b> for chaining
     */
    public BasicSetupSpec setAasRepositoryEndpoint(Endpoint endpoint) {
        setups.get(AasComponent.AAS_REPOSITORY).endpoint = endpoint;
        return this;
    }

    /**
     * Sets the registry endpoints for a single registry setup. This is not intended for metamodel v3.
     * 
     * @param endpoint the registry endpoint
     * @return <b>this</b> for chaining
     */
    public BasicSetupSpec setRegistryEndpoint(Endpoint endpoint) {
        setups.get(AasComponent.AAS_REGISTRY).endpoint = endpoint;
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
        setups.get(AasComponent.AAS_REGISTRY).endpoint = aasRegistryEndpoint;
        setups.get(AasComponent.SUBMODEL_REGISTRY).endpoint = smRegistryEndpoint;
        return this;
    }
    
    /**
     * Sets the AAS repository keystore.
     * 
     * @param desc the keystore descriptor, may be <b>null</b> for none
     * @return <b>this</b> for chaining
     */
    public BasicSetupSpec setAasRepositoryKeystore(KeyStoreDescriptor desc) {
        setups.get(AasComponent.AAS_REPOSITORY).keyStore = desc;
        return this;
    }
    
    /**
     * Sets the AAS repository authentication descriptor.
     * 
     * @param desc the authentication descriptor, may be <b>null</b> for none
     * @return <b>this</b> for chaining
     */
    public BasicSetupSpec setAasRepositoryAuthentication(AuthenticationDescriptor desc) {
        setups.get(AasComponent.AAS_REPOSITORY).authentication = desc;
        return this;
    }    

    /**
     * Sets the submodel repository keystore.
     * 
     * @param desc the keystore descriptor, may be <b>null</b> for none
     * @return <b>this</b> for chaining
     */
    public BasicSetupSpec setSubmodelRepositoryKeystore(KeyStoreDescriptor desc) {
        setups.get(AasComponent.SUBMODEL_REPOSITORY).keyStore = desc;
        return this;
    }
    
    /**
     * Sets the submodel repository authentication descriptor.
     * 
     * @param desc the authentication descriptor, may be <b>null</b> for none
     * @return <b>this</b> for chaining
     */
    public BasicSetupSpec setSubmodelRepositoryAuthentication(AuthenticationDescriptor desc) {
        setups.get(AasComponent.SUBMODEL_REPOSITORY).authentication = desc;
        return this;
    }    

    /**
     * Sets the AAS registry keystore.
     * 
     * @param desc the keystore descriptor, may be <b>null</b> for none
     * @return <b>this</b> for chaining
     */
    public BasicSetupSpec setAasRegistryKeystore(KeyStoreDescriptor desc) {
        setups.get(AasComponent.AAS_REGISTRY).keyStore = desc;
        return this;
    }
    
    /**
     * Sets the AAS registry registry authentication descriptor.
     * 
     * @param desc the authentication descriptor, may be <b>null</b> for none
     * @return <b>this</b> for chaining
     */
    public BasicSetupSpec setAasRegistryAuthentication(AuthenticationDescriptor desc) {
        setups.get(AasComponent.AAS_REGISTRY).authentication = desc;
        return this;
    }    

    /**
     * Sets the submodel registry keystore.
     * 
     * @param desc the keystore descriptor, may be <b>null</b> for none
     * @return <b>this</b> for chaining
     */
    public BasicSetupSpec setSubmodelRegistryKeystore(KeyStoreDescriptor desc) {
        setups.get(AasComponent.SUBMODEL_REGISTRY).keyStore = desc;
        return this;
    }

    /**
     * Sets the submodel registry authentication descriptor.
     * 
     * @param desc the authentication descriptor, may be <b>null</b> for none
     * @return <b>this</b> for chaining
     */
    public BasicSetupSpec setSubmodelRegistryAuthentication(AuthenticationDescriptor desc) {
        setups.get(AasComponent.SUBMODEL_REGISTRY).authentication = desc;
        return this;
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
        setups.get(AasComponent.ASSET).serverAddress = address;
        this.assetServerProtocol = protocol;
        return this;
    }

    @Override
    public String getAssetServerProtocol() {
        return assetServerProtocol;
    }

    /**
     * Sets the asset server keystore.
     * 
     * @param desc the keystore descriptor, may be <b>null</b> for none
     * @return <b>this</b> for chaining
     */
    public BasicSetupSpec setAssetServerKeystore(KeyStoreDescriptor desc) {
        setups.get(AasComponent.ASSET).keyStore = desc;
        return this;
    }
    
    /**
     * Sets the asset server authentication descriptor.
     * 
     * @param desc the asset server authentication descriptor, may be <b>null</b> for none
     * @return <b>this</b> for chaining
     */
    public BasicSetupSpec setAssetServerAuthentication(AuthenticationDescriptor desc) {
        setups.get(AasComponent.ASSET).authentication = desc;
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

    /**
     * Turns a component setup into a string.
     * 
     * @param setup the setup, may be <b>null</b>
     * @return the textual form
     */
    private String toString(ComponentSetup setup) {
        return toString(setup.getEndpoint()) + toString(setup.getKeyStore());
    }

    @Override
    public ComponentSetup getSetup(AasComponent component) {
        return setups.get(component);
    }

    @Override
    public String toString() {
        String cr = System.lineSeparator();
        return "Basic AAS setup specification:" + cr 
            + " - AAS registry:        " + toString(getSetup(AasComponent.AAS_REGISTRY)) + cr
            + " - AAS repository:      " + toString(getSetup(AasComponent.AAS_REPOSITORY)) + cr
            + " - Submodel registry:   " + toString(getSetup(AasComponent.SUBMODEL_REGISTRY)) + cr
            + " - Submodel repository: " + toString(getSetup(AasComponent.SUBMODEL_REPOSITORY)) + cr
            + " - Asset server:        " + toString(getSetup(AasComponent.ASSET)) + " @ '" + assetServerProtocol + "'";
    }

}
