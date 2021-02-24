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
 * A simple network manager implementation, which just uses the full space of potential ephemerial ports.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LocalNetworkManagerImpl implements NetworkManager {

    private Map<String, Integer> keyToPorts = new HashMap<>();
    private Map<Integer, String> portsToKeys = new HashMap<>();
    private String host = NetUtils.getOwnIP();
    
    @Override
    public synchronized ManagedServerAddress obtainPort(String key) {
        if (null == key) {
            throw new IllegalArgumentException("Key must be given");
        }
        ManagedServerAddress result = null;
        Integer ex = keyToPorts.get(key);
        if (null != ex) {
            result = new ManagedServerAddress(Schema.IGNORE, host, ex, false);
        } else {
            do {
                int port = NetUtils.getEphemeralPort();
                if (!portsToKeys.containsKey(port)) {
                    keyToPorts.put(key, port);
                    portsToKeys.put(port, key);
                    result = new ManagedServerAddress(Schema.IGNORE, host, port, true);
                } else {
                    result = null;
                }
            } while (result == null);
        }
        return result;
    }

    @Override
    public synchronized void releasePort(String key) {
        if (null == key) {
            throw new IllegalArgumentException("Key must be given");
        }
        Integer port = keyToPorts.remove(key);
        if (null != port) {
            portsToKeys.remove(port);
        }
    }

    @Override
    public synchronized boolean isInUse(int port) {
        return portsToKeys.containsKey(port);
    }

    @Override
    public boolean isInUse(ServerAddress adr) {
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
