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

package de.iip_ecosphere.platform.support.net;

import java.util.HashMap;
import java.util.Map;

import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;

/**
 * A simple network manager implementation, which just uses the full space of potential ephemeral ports.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LocalNetworkManagerImpl extends AbstractNetworkManagerImpl {

    /**
     * The descriptor for hooking the manager into the {@link NetworkManagerFactory} via Java Service Loading.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Descriptor implements NetworkManagerDescriptor {

        @Override
        public NetworkManager createInstance() {
            return new LocalNetworkManagerImpl();
        }
        
    }
    
    private Map<String, ServerAddress> keyToAddress = new HashMap<>();
    private Map<Integer, String> portToKey = new HashMap<>();
    private String host = NetUtils.getOwnIP();
    private NetworkManager parent;

    /**
     * Create a local network manager instance without delegation.
     */
    public LocalNetworkManagerImpl() {
    }

    /**
     * Create a local network manager instance without delegation.
     * 
     * @param parent a network manager to delegate {@link #getPort(String) get requests} to, may be <b>null</b> 
     * for none
     */
    public LocalNetworkManagerImpl(NetworkManager parent) {
        this.parent = parent;
    }

    @Override
    public synchronized ManagedServerAddress obtainPort(String key) {
        ManagedServerAddress result = getPort(key); // calls checkKey(key)
        if (null == result) {
            do {
                int port = NetUtils.getEphemeralPort();
                if (!portToKey.containsKey(port)) {
                    ServerAddress address = new ServerAddress(Schema.IGNORE, host, port);
                    keyToAddress.put(key, address);
                    portToKey.put(port, key);
                    result = new ManagedServerAddress(address, true);
                } else {
                    result = null;
                }
            } while (result == null);
        }
        return result;
    }

    /**
     * Returns a managed address if {@code key} is already known. Calls {@link #checkKey(String)}.
     * 
     * @param key the key
     * @return the address if known, <b>null</b> else
     */
    public ManagedServerAddress getPort(String key) {
        checkKey(key);
        ServerAddress ex = null;
        
        // delegation to parent
        if (null != parent) {
            ex = parent.getPort(key);
        }
        
        if (null == ex) {
            // is there a prefix match?
            int pos = key.lastIndexOf(PREFIX_SEPARATOR);
            while (pos > 0 && null == ex) {
                String sub = key.substring(0, pos + 1);
                ex = keyToAddress.get(sub);
                pos = key.lastIndexOf(PREFIX_SEPARATOR, pos - 1);
            }
        }
        
        if (null == ex) {
            // is there a direct match?
            ex = keyToAddress.get(key);
        }

        ManagedServerAddress result = null;
        if (null != ex) {
            result = new ManagedServerAddress(ex, false);
        }
        return result;
    }
    
    @Override
    public ManagedServerAddress reservePort(String key, ServerAddress address) {
        checkAddress(address);
        ManagedServerAddress result = getPort(key); // calls checkKey(key)
        if (null == result) {
            keyToAddress.put(key, address);
            portToKey.put(address.getPort(), key);
            result = new ManagedServerAddress(address, true);
        } 
        return result;
    }

    @Override
    public synchronized void releasePort(String key) {
        checkKey(key);
        ServerAddress ex = keyToAddress.remove(key);
        if (null != ex) {
            portToKey.remove(ex.getPort());
        }
    }

    @Override
    public synchronized boolean isInUse(int port) {
        return portToKey.containsKey(port);
    }

    @Override
    public boolean isInUse(ServerAddress adr) {
        checkAddress(adr);
        return host.equals(adr.getHost()) && isInUse(adr.getPort());
    }

    @Override
    public int getLowPort() {
        return 1025;
    }

    @Override
    public int getHighPort() {
        return 65535;
    }

}
