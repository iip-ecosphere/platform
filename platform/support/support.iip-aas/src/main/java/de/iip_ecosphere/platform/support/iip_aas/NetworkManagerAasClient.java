/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
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

import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;
import de.iip_ecosphere.platform.support.net.AbstractNetworkManagerImpl;
import de.iip_ecosphere.platform.support.net.ManagedServerAddress;

import static de.iip_ecosphere.platform.support.iip_aas.SubmodelClient.*;

/**
 * Implementing a network manager acting as client for an AAS-based network manager. The AAS shall be provided by
 * {@link NetworkManagerAas}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class NetworkManagerAasClient extends AbstractNetworkManagerImpl {

    private Submodel submodel;
    
    /**
     * Creates a client instance based on a deployed IIP-AAS from {@link AasPartRegistry} based on a submodel with
     * {@link NetworkManagerAas#NAME_SUBMODEL name}.
     * 
     * @throws IOException if retrieving the IIP-AAS or the respective submodel fails
     */
    public NetworkManagerAasClient() throws IOException {
        this(ActiveAasBase.getSubmodel(NetworkManagerAas.NAME_SUBMODEL));
    }
    
    /**
     * Creates a client instance based on the submodel. The submodel shall conform to {@link NetworkManagerAas} with 
     * respect to the operations, signatures, but also the {@link NetworkManagerAas#NAME_SUBMODEL name},
     * 
     * @param submodel the submodel to use
     */
    public NetworkManagerAasClient(Submodel submodel) {
        this.submodel = submodel;
    }

    @Override
    public ManagedServerAddress obtainPort(String key) {
        try {
            checkKey(key);
            String tmp = checkString(getOperation(submodel, NetworkManagerAas.OP_OBTAIN_PORT).invoke(key));
            return checkNotNull(NetworkManagerAas.managedServerAddressFromJson(tmp));
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public ManagedServerAddress getPort(String key) {
        try {
            checkKey(key);
            Object tmp = getOperation(submodel, NetworkManagerAas.OP_GET_PORT).invoke(key);
            // result may be null here!!
            return NetworkManagerAas.managedServerAddressFromJson(tmp);
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public ManagedServerAddress reservePort(String key, ServerAddress address) {
        try {
            checkKey(key);
            checkAddress(address);
            String tmp = checkString(getOperation(submodel, NetworkManagerAas.OP_RESERVE_PORT).invoke(key, 
                JsonUtils.toJson(checkNotNull(address))));
            return checkNotNull(NetworkManagerAas.managedServerAddressFromJson(tmp));
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public void releasePort(String key) {
        try {
            checkKey(key);
            getOperation(submodel, NetworkManagerAas.OP_RELEASE_PORT).invoke(key);
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public boolean isInUse(ServerAddress address) {
        checkAddress(address);
        boolean result = false;
        try {
            Object tmp = getOperation(submodel, NetworkManagerAas.OP_IS_IN_USE_ADR).invoke(JsonUtils.toJson(address));
            if (tmp instanceof Boolean) {
                result = ((Boolean) tmp).booleanValue();
            }
        } catch (ExecutionException e) {
        }
        return result;
    }

    @Override
    public boolean isInUse(int port) {
        boolean result = false;
        try {
            Object tmp = getOperation(submodel, NetworkManagerAas.OP_IS_IN_USE_PORT).invoke(port);
            if (tmp instanceof Boolean) {
                result = ((Boolean) tmp).booleanValue();
            }
        } catch (ExecutionException e) {
        }
        return result;
    }

    @Override
    public int getLowPort() {
        int result = -1;
        try {
            Object tmp = getProperty(submodel, NetworkManagerAas.PROP_LOW_PORT).getValue();
            if (tmp instanceof Integer) {
                result = ((Integer) tmp).intValue();
            }
        } catch (ExecutionException e) {
        }
        return result;
    }

    @Override
    public int getHighPort() {
        int result = -1;
        try {
            Object tmp = getProperty(submodel, NetworkManagerAas.PROP_HIGH_PORT).getValue();
            if (tmp instanceof Integer) {
                result = ((Integer) tmp).intValue();
            }
        } catch (ExecutionException e) {
        }
        return result;
    }

    @Override
    public void registerInstance(String key, String hostId) {
        try {
            checkKey(key);
            getOperation(submodel, NetworkManagerAas.OP_REGISTER_INSTANCE).invoke(key, hostId);
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public void unregisterInstance(String key, String hostId) {
        try {
            checkKey(key);
            getOperation(submodel, NetworkManagerAas.OP_UNREGISTER_INSTANCE).invoke(key, hostId);
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public int getRegisteredInstances(String key) {
        int result = 0;
        try {
            checkKey(key);
            Object tmp = getOperation(submodel, NetworkManagerAas.OP_GET_REGISTERED_INSTANCES).invoke(key);
            if (tmp instanceof Integer) {
                result = ((Integer) tmp).intValue();
            }
        } catch (ExecutionException e) {
        }
        return result;
    }

}
