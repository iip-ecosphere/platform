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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

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
    private Map<String, Map<String, Integer>> instances = new HashMap<>();
    private String host;
    private transient NetworkManager parent;

    /**
     * Create a local network manager instance without delegation.
     */
    public LocalNetworkManagerImpl() {
        this.host = NetUtils.getOwnIP(getNetmask());
    }

    /**
     * Create a local network manager instance without delegation.
     * 
     * @param parent a network manager to delegate {@link #getPort(String) get requests} to, may be <b>null</b> 
     * for none
     */
    public LocalNetworkManagerImpl(NetworkManager parent) {
        this.parent = parent;
        this.host = NetUtils.getOwnIP(getNetmask());
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
                    LoggerFactory.getLogger(LocalNetworkManagerImpl.class).info("Allocated port " + key + " " + port);
                    notifyChanged();
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
    public ManagedServerAddress reserveGlobalPort(String key, ServerAddress address) {
        ManagedServerAddress result;
        if (parent != null) {
            result = parent.reserveGlobalPort(key, address);
        } else {
            result = reservePort(key, address);
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
            LoggerFactory.getLogger(LocalNetworkManagerImpl.class).info("Reserved port " + key 
                + " " + address.getHost() + " " + address.getPort());
            notifyChanged();
        } 
        return result;
    }

    @Override
    public synchronized void releasePort(String key) {
        checkKey(key);
        ServerAddress ex = keyToAddress.remove(key);
        if (null != ex) {
            portToKey.remove(ex.getPort());
            LoggerFactory.getLogger(LocalNetworkManagerImpl.class).info("Released port " + key);
            notifyChanged();
        } else if (parent != null) {
            parent.releasePort(key);
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
    public void configure(NetworkManagerSetup setup) {
        super.configure(setup);
        if (null != setup) {
            host = NetUtils.getOwnIP(getNetmask());
        }
    }
    
    @Override
    public synchronized void registerInstance(String key, String hostId) {
        if (null != key && null != hostId) {
            Map<String, Integer> inst = instances.get(key);
            if (null == inst) {
                inst = new HashMap<>();
                instances.put(key, inst);
            }
            Integer count = inst.get(hostId);
            if (null == count) {
                count = 1;
            } else {
                count++;
            }
            inst.put(hostId, count);
            notifyChanged();
        }
    }

    @Override
    public synchronized void unregisterInstance(String key, String hostId) {
        if (null != key && null != hostId) {
            Map<String, Integer> inst = instances.get(key);
            if (inst != null) {
                Integer count = inst.get(hostId);
                if (count != null) {
                    count--;
                    if (count == 0) {
                        inst.remove(hostId);
                    } else {
                        inst.put(hostId, count);
                    }
                }
                if (inst.isEmpty()) {
                    instances.remove(key);
                    notifyChanged();
                }
            }
        }
        
    }

    @Override
    public synchronized int getRegisteredInstances(String key) {
        int result = 0;
        if (null != key) {
            Map<String, Integer> inst = instances.get(key);
            if (null != inst) {
                for (Map.Entry<String, Integer> e : inst.entrySet()) {
                    result += e.getValue();
                }
            }
        }
        return result;
    }
    

    /**
     * Is called when some registration has been changed.
     */
    protected void notifyChanged() {
    }

    /**
     * Writes relevant data to {@code out}. 
     * 
     * @param out the stream to write to
     * @throws IOException if writing fails
     */
    public void writeTo(ObjectOutputStream out) throws IOException {
        // Does not work via "this". Prevents using serializable interface.
        out.writeObject(keyToAddress);
        out.writeObject(portToKey);
        out.writeObject(instances);
        out.writeUTF(host);
    }

    /**
     * Reads from an object input stream.
     * 
     * @param in the stream to read from
     * @throws IOException if reading fails
     */
    @SuppressWarnings("unchecked")
    public void readFrom(ObjectInputStream in) throws IOException {
        try {
            keyToAddress = (Map<String, ServerAddress>) in.readObject();
            portToKey = (Map<Integer, String>) in.readObject();
            instances = (Map<String, Map<String, Integer>>) in.readObject();
            host = in.readUTF();
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IOException(e);
        }
    }

}
