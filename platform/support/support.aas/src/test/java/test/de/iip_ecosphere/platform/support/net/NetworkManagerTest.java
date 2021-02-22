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

package test.de.iip_ecosphere.platform.support.net;

import org.junit.Test;

import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.net.LocalNetworkManagerImpl;
import de.iip_ecosphere.platform.support.net.NetworkManager;
import org.junit.Assert;

/**
 * Some tests for {@link NetworkManager} and {@link LocalNetworkManagerImpl}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class NetworkManagerTest {

    /**
     * Tests available implementations.
     */
    @Test
    public void testNetworkManagers() {
        testNetworkManager(new LocalNetworkManagerImpl());
    }
    
    /**
     * Tests the given network manager.
     * 
     * @param manager the manager instance, assumes a fresh/unallocated instance
     */
    public static void testNetworkManager(NetworkManager manager) {
        Assert.assertTrue(manager.getLowPort() > 0);
        Assert.assertTrue(manager.getHighPort() > 0);
        Assert.assertTrue(manager.getLowPort() < manager.getHighPort());
        int port = NetUtils.getEphemeralPort();
        while (port < manager.getLowPort() || port > manager.getHighPort()) {
            port = NetUtils.getEphemeralPort();
        }
        ServerAddress adr = new ServerAddress(Schema.IGNORE, ServerAddress.LOCALHOST, port);
        Assert.assertFalse(manager.isInUse(adr)); // new manager
        try {
            manager.obtainPort(null);
            Assert.fail("No exception");
        } catch (IllegalArgumentException e) {
        }
        try {
            manager.releasePort(null);
            Assert.fail("No exception");
        } catch (IllegalArgumentException e) {
        }
        final String key1 = "key1";
        final String key2 = "key2";
        ServerAddress adr1 = manager.obtainPort(key1);
        Assert.assertTrue(adr1.getPort() > 0);
        Assert.assertTrue(adr1.getHost().length() > 0);
        Assert.assertTrue(manager.isInUse(adr1));
        ServerAddress adr2 = manager.obtainPort(key2);
        Assert.assertTrue(adr2.getPort() > 0);
        Assert.assertTrue(adr2.getHost().length() > 0);
        Assert.assertTrue(adr1.getPort() != adr2.getPort());
        Assert.assertTrue(manager.isInUse(adr2));
        
        Assert.assertTrue(manager.isInUse(adr2));
        manager.releasePort(key1);
        Assert.assertFalse(manager.isInUse(adr1));
        Assert.assertTrue(manager.isInUse(adr2));
        manager.releasePort(key2);
        Assert.assertFalse(manager.isInUse(adr1));
        Assert.assertFalse(manager.isInUse(adr2));
    }
    
}
