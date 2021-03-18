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
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.net.AbstractNetworkManagerImpl;
import de.iip_ecosphere.platform.support.net.ManagedServerAddress;

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
        Aas aas = AasPartRegistry.retrieveIipAas();
        if (null == aas) {
            throw new IOException("No IIP-AAS found");
        }
        this.submodel = aas.getSubmodel(NetworkManagerAas.NAME_SUBMODEL);
        if (null == this.submodel) {
            throw new IOException("No submodel '" + NetworkManagerAas.NAME_SUBMODEL + "' found");
        }
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
            String tmp = checkString(getOperation(NetworkManagerAas.OP_OBTAIN_PORT).invoke(key));
            return checkNotNull(NetworkManagerAas.readManagedServerAddress(tmp));
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public ManagedServerAddress getPort(String key) {
        try {
            checkKey(key);
            Object tmp = getOperation(NetworkManagerAas.OP_GET_PORT).invoke(key);
            // result may be null here!!
            return NetworkManagerAas.readManagedServerAddress(null == tmp ? null : tmp.toString());
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public ManagedServerAddress reservePort(String key, ServerAddress address) {
        try {
            checkKey(key);
            checkAddress(address);
            String tmp = checkString(getOperation(NetworkManagerAas.OP_RESERVE_PORT).invoke(key, 
                NetworkManagerAas.toJson(checkNotNull(address))));
            return checkNotNull(NetworkManagerAas.readManagedServerAddress(tmp));
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public void releasePort(String key) {
        try {
            checkKey(key);
            getOperation(NetworkManagerAas.OP_RELEASE_PORT).invoke(key);
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    
    /**
     * Checks for that {@code obj} is a non-empty string.
     * 
     * @param obj the object representing the string
     * @return the string
     * @throws IllegalArgumentException if there is no valid string
     */
    private static String checkString(Object obj) {
        String result = null == obj ? null : obj.toString();
        if (null == result || result.length() == 0) {
            throw new IllegalArgumentException("Not valid string/response");
        }
        return result;
    }

    /**
     * Checks for that {@code result} is not null.
     * 
     * @param <T> the type of object
     * @param obj the object to check
     * @return {@code obj}
     * @throws IllegalArgumentException if {@code obj} is <b>null</b>
     */
    private static <T> T checkNotNull(T obj) {
        if (null == obj) {
            throw new IllegalArgumentException("No valid object");
        }
        return obj;
    }

    /**
     * Returns the operation for the given {@code idShort} defined on {@link #submodel}.
     * 
     * @param idShort the short id
     * @return the operation
     * @throws ExecutionException if the operation was not found
     */
    private Operation getOperation(String idShort) throws ExecutionException {
        Operation result = submodel.getOperation(idShort);
        if (null == result) {
            throw new ExecutionException("Operation '" + idShort + "' not found", null);
        }
        return result;
    }
    
    /**
     * Returns the property for the given {@code idShort} defined on {@link #submodel}.
     * 
     * @param idShort the short id
     * @return the property
     * @throws ExecutionException if the property was not found
     */
    private Property getProperty(String idShort) throws ExecutionException {
        Property result = submodel.getProperty(idShort);
        if (null == result) {
            throw new ExecutionException("Property '" + idShort + "' not found", null);
        }
        return result;
    }

    @Override
    public boolean isInUse(ServerAddress address) {
        checkAddress(address);
        boolean result = false;
        try {
            Object tmp = getOperation(NetworkManagerAas.OP_IS_IN_USE_ADR).invoke(NetworkManagerAas.toJson(address));
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
            Object tmp = getOperation(NetworkManagerAas.OP_IS_IN_USE_PORT).invoke(port);
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
            Object tmp = getProperty(NetworkManagerAas.PROP_LOW_PORT).getValue();
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
            Object tmp = getProperty(NetworkManagerAas.PROP_HIGH_PORT).getValue();
            if (tmp instanceof Integer) {
                result = ((Integer) tmp).intValue();
            }
        } catch (ExecutionException e) {
        }
        return result;
    }

}
