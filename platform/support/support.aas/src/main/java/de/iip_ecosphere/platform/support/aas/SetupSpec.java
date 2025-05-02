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
     * Returns the AAS registry endpoint.
     * 
     * @return the endpoint
     */
    public Endpoint getAasRegistryEndpoint();
    
    /**
     * Returns the keystore descriptor for the AAS registry.
     * 
     * @return the keystore descriptor, may be <b>null</b> for none
     */
    public KeyStoreDescriptor getAasRegistryKeyStore();

    /**
     * Returns the state of the AAS registry .
     * 
     * @return the state
     */
    public State getAasRegistryState();

    /**
     * Notifies about a state change of the AAS registry.
     * 
     * @param state the new state
     */
    public void notifyAasRegistryStateChange(State state);

    /**
     * Returns the submodel registry endpoint.
     * 
     * @return the endpoint
     */
    public Endpoint getSubmodelRegistryEndpoint();

    /**
     * Returns the keystore descriptor for the submodel registry.
     * 
     * @return the keystore descriptor, may be <b>null</b> for none
     */
    public KeyStoreDescriptor getSubmodelRegistryKeyStore();

    /**
     * Returns the state of the submodel registry.
     * 
     * @return the state
     */
    public State getSubmodelRegistryState();

    /**
     * Notifies about a state change of the submodel registry.
     * 
     * @param state the new state
     */
    public void notifySubmodelRegistryStateChange(State state);

    /**
     * Returns the AAS repository endpoint.
     * 
     * @return the endpoint
     */
    public Endpoint getAasRepositoryEndpoint();

    /**
     * Returns the state of the AAS repository.
     * 
     * @return the state
     */
    public State getAasRepositoryState();

    /**
     * Notifies about a state change of the AAS repository.
     * 
     * @param state the new state
     */
    public void notifyAasRepositoryStateChange(State state);

    /**
     * Returns the keystore descriptor for the AAS repository.
     * 
     * @return the keystore descriptor, may be <b>null</b> for none
     */
    public KeyStoreDescriptor getAasRepositoryKeyStore();

    /**
     * Returns the submodel endpoint.
     * 
     * @return the endpoint
     */
    public Endpoint getSubmodelRepositoryEndpoint();

    /**
     * Returns the state of the submodel repository.
     * 
     * @return the state
     */
    public State getSubmodelRepositoryState();

    /**
     * Notifies about a state change of the submodel repository.
     * 
     * @param state the new state
     */
    public void notifySubmodelRepositoryStateChange(State state);

    /**
     * Returns the keystore descriptor for the AAS repository.
     * 
     * @return the keystore descriptor, may be <b>null</b> for none
     */
    public KeyStoreDescriptor getSubmodelRepositoryKeyStore();
    
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
    public ServerAddress getAssetServerAddress();
    
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
    public State getAssetServerState();

    /**
     * Notifies about a state change of the asset server.
     * 
     * @param state the new state
     */
    public void notifyAssetServerStateChange(State state);
    
    /**
     * Returns the keystore descriptor for the asset server repository.
     * 
     * @return the keystore descriptor, may be <b>null</b> for none
     */
    public KeyStoreDescriptor getAssetServerKeyStore();
    
}
