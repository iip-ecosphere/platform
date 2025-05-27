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

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;

/**
 * Returns the endpoints of a simple AAS setup. Older AAS implementations (joined registries/repositories) may take
 * only parts of the provided information into account.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface SetupSpec {
    
    /**
     * The setup/runtime state of a component.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum State {
        
        /**
         * Managed outside the platform, supposed to be running.
         */
        EXTERNAL,
        
        /**
         * It is running.
         */
        RUNNING,
        
        /**
         * It is stopped or was never running.
         */
        STOPPED
    }
    
    /**
     * Denotes the supported AAS component types.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum AasComponent {

        AAS_REGISTRY,
        AAS_REPOSITORY,
        SUBMODEL_REGISTRY,
        SUBMODEL_REPOSITORY,
        ASSET
        // initial, to be extended
    }
    
    /**
     * Describes the component setup.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface ComponentSetup {

        /**
         * Returns the component's endpoint.
         * 
         * @return the endpoint
         */
        public default ServerAddress getServerAddress() {
            return getEndpoint();
        }
        
        /**
         * Returns the component's endpoint.
         * 
         * @return the endpoint
         */
        public Endpoint getEndpoint();

        /**
         * Returns the component's keystore descriptor.
         * 
         * @return the keystore descriptor, may be <b>null</b> for none
         */
        public KeyStoreDescriptor getKeyStore();

        /**
         * Returns the component's authentication descriptor.
         * 
         * @return the authentication descriptor, may be <b>null</b> for none
         */
        public AuthenticationDescriptor getAuthentication();
        
        /**
         * Returns the component's state.
         * 
         * @return the state
         */
        public State getState();

        /**
         * Notifies about a component's state change.
         * 
         * @param state the new state
         */
        public void notifyStateChange(State state);

    }
    
    /**
     * Returns the setup of a component.
     * 
     * @param component the component
     * @return the setup
     */
    public ComponentSetup getSetup(AasComponent component);

    /**
     * Returns the AAS registry endpoint.
     * 
     * @return the endpoint
     */
    public default Endpoint getAasRegistryEndpoint() {
        return getSetup(AasComponent.AAS_REGISTRY).getEndpoint();
    }
    
    /**
     * Returns the keystore descriptor for the AAS registry.
     * 
     * @return the keystore descriptor, may be <b>null</b> for none
     */
    public default KeyStoreDescriptor getAasRegistryKeyStore() {
        return getSetup(AasComponent.AAS_REGISTRY).getKeyStore();
    }

    /**
     * Returns the state of the AAS registry .
     * 
     * @return the state
     */
    public default State getAasRegistryState() {
        return getSetup(AasComponent.AAS_REGISTRY).getState();
    }

    /**
     * Notifies about a state change of the AAS registry.
     * 
     * @param state the new state
     */
    public default void notifyAasRegistryStateChange(State state) {
        getSetup(AasComponent.AAS_REGISTRY).notifyStateChange(state);
    }

    /**
     * Returns the submodel registry endpoint.
     * 
     * @return the endpoint
     */
    public default Endpoint getSubmodelRegistryEndpoint() {
        return getSetup(AasComponent.SUBMODEL_REGISTRY).getEndpoint();
    }
    /**
     * Returns the keystore descriptor for the submodel registry.
     * 
     * @return the keystore descriptor, may be <b>null</b> for none
     */
    public default KeyStoreDescriptor getSubmodelRegistryKeyStore() {
        return getSetup(AasComponent.SUBMODEL_REGISTRY).getKeyStore();
    }

    /**
     * Returns the state of the submodel registry.
     * 
     * @return the state
     */
    public default State getSubmodelRegistryState() {
        return getSetup(AasComponent.SUBMODEL_REGISTRY).getState();
    }

    /**
     * Notifies about a state change of the submodel registry.
     * 
     * @param state the new state
     */
    public default void notifySubmodelRegistryStateChange(State state) {
        getSetup(AasComponent.SUBMODEL_REGISTRY).notifyStateChange(state);
    }

    /**
     * Returns the AAS repository endpoint.
     * 
     * @return the endpoint
     */
    public default Endpoint getAasRepositoryEndpoint() {
        return getSetup(AasComponent.AAS_REPOSITORY).getEndpoint();
    }
    
    /**
     * Returns the state of the AAS repository.
     * 
     * @return the state
     */
    public default State getAasRepositoryState() {
        return getSetup(AasComponent.AAS_REPOSITORY).getState();
    }

    /**
     * Notifies about a state change of the AAS repository.
     * 
     * @param state the new state
     */
    public default void notifyAasRepositoryStateChange(State state) {
        getSetup(AasComponent.AAS_REPOSITORY).notifyStateChange(state);
    }

    /**
     * Returns the keystore descriptor for the AAS repository.
     * 
     * @return the keystore descriptor, may be <b>null</b> for none
     */
    public default KeyStoreDescriptor getAasRepositoryKeyStore() {
        return getSetup(AasComponent.AAS_REPOSITORY).getKeyStore();
    }

    /**
     * Returns the submodel endpoint.
     * 
     * @return the endpoint
     */
    public default Endpoint getSubmodelRepositoryEndpoint() {
        return getSetup(AasComponent.SUBMODEL_REPOSITORY).getEndpoint();
    }

    /**
     * Returns the state of the submodel repository.
     * 
     * @return the state
     */
    public default State getSubmodelRepositoryState() {
        return getSetup(AasComponent.SUBMODEL_REPOSITORY).getState();
    }

    /**
     * Notifies about a state change of the submodel repository.
     * 
     * @param state the new state
     */
    public default void notifySubmodelRepositoryStateChange(State state) {
        getSetup(AasComponent.SUBMODEL_REPOSITORY).notifyStateChange(state);
    }

    /**
     * Returns the keystore descriptor for the AAS repository.
     * 
     * @return the keystore descriptor, may be <b>null</b> for none
     */
    public default KeyStoreDescriptor getSubmodelRepositoryKeyStore() {
        return getSetup(AasComponent.SUBMODEL_REPOSITORY).getKeyStore();
    }
    
    /**
     * Are both registries running.
     * 
     * @return {@code true} for already running, {@code false} else
     */
    public default boolean areRegistriesRunning() {
        return getAasRegistryState() == State.RUNNING && getSubmodelRegistryState() == State.RUNNING;
    }

    /**
     * Returns the asset server address.
     * 
     * @return the asset server address
     */
    public default ServerAddress getAssetServerAddress() {
        return getSetup(AasComponent.ASSET).getServerAddress();
    }
    
    /**
     * Returns the asset server protocol.
     * 
     * @return the asset server protocol, empty for default.
     */
    public String getAssetServerProtocol();

    /**
     * Returns the state of the asset server.
     * 
     * @return the state
     */
    public default State getAssetServerState() {
        return getSetup(AasComponent.ASSET).getState();
    }

    /**
     * Notifies about a state change of the asset server.
     * 
     * @param state the new state
     */
    public default void notifyAssetServerStateChange(State state) {
        getSetup(AasComponent.ASSET).notifyStateChange(state);
    }
    
    /**
     * Returns the keystore descriptor for the asset server repository.
     * 
     * @return the keystore descriptor, may be <b>null</b> for none
     */
    public default KeyStoreDescriptor getAssetServerKeyStore() {
        return getSetup(AasComponent.ASSET).getKeyStore();
    }
    
}
