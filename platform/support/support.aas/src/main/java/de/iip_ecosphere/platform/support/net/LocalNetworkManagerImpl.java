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

    private Map<String, ServerAddress> keyToPorts = new HashMap<>();
    private Map<ServerAddress, String> portsToKeys = new HashMap<>();
    
    @Override
    public ServerAddress obtainPort(String key) {
        if (null == key) {
            throw new IllegalArgumentException("Key must be given");
        }
        ServerAddress result = null;
        ServerAddress ex = keyToPorts.get(key);
        if (null != ex) {
            result = ex;
        } else {
            do {
                result = new ServerAddress(Schema.IGNORE, NetUtils.getOwnIP(), NetUtils.getEphemeralPort());
                if (!portsToKeys.containsKey(result)) {
                    portsToKeys.put(result, key);
                    keyToPorts.put(key, result);
                } else {
                    result = null;
                }
            } while (result == null);
        }
        return result;
    }

    @Override
    public void releasePort(String key) {
        if (null == key) {
            throw new IllegalArgumentException("Key must be given");
        }
        ServerAddress adr = keyToPorts.remove(key);
        if (null != adr) {
            portsToKeys.remove(adr);
        }
    }

    @Override
    public boolean isInUse(ServerAddress adr) {
        return portsToKeys.containsKey(adr);
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
