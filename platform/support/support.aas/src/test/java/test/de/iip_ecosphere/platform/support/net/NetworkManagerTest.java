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
import de.iip_ecosphere.platform.support.net.ManagedServerAddress;
import de.iip_ecosphere.platform.support.net.NetworkManager;
import de.iip_ecosphere.platform.support.net.NetworkManagerFactory;

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
        NetworkManager nm = NetworkManagerFactory.getInstance();
        Assert.assertTrue(nm instanceof LocalNetworkManagerImpl);
        testNetworkManager(nm, "");
        
        LocalNetworkManagerImpl parent = new LocalNetworkManagerImpl();
        ServerAddress resAdr = new ServerAddress(Schema.IGNORE, "me.here", 1223);
        parent.reservePort("reserved", resAdr);
        LocalNetworkManagerImpl mgr = new LocalNetworkManagerImpl(parent);
        ManagedServerAddress adr = mgr.getPort("reserved");
        Assert.assertNotNull(adr);
        Assert.assertEquals(resAdr.getSchema(), adr.getSchema());
        Assert.assertEquals(resAdr.getHost(), adr.getHost());
        Assert.assertEquals(resAdr.getPort(), adr.getPort());
        testNetworkManager(mgr, "");
    }
    
    /**
     * Tests the given network manager for self-managed addresses.
     * 
     * @param manager the manager instance, assumes a fresh/unallocated instance
     * @param suffix additional information to make keys unique for repeated tests, usually empty
     * @see #testPortReservation(NetworkManager, String)
     */
    public static void testNetworkManager(NetworkManager manager, String suffix) {
        Assert.assertTrue(manager.getLowPort() > 0);
        Assert.assertTrue(manager.getHighPort() > 0);
        Assert.assertTrue(manager.getLowPort() < manager.getHighPort());
        int port = NetUtils.getEphemeralPort();
        while (port < manager.getLowPort() || port > manager.getHighPort()) {
            port = NetUtils.getEphemeralPort();
        }
        ServerAddress adr = new ServerAddress(Schema.IGNORE, ServerAddress.LOCALHOST, port);
        Assert.assertFalse(manager.isInUse(port)); // new manager
        Assert.assertFalse(manager.isInUse(adr)); // new manager
        try {
            manager.obtainPort(null);
            Assert.fail("No exception");
        } catch (IllegalArgumentException e) {
        }
        try {
            manager.getPort(null);
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
        Assert.assertNull(manager.getPort(key1));
        // obtain an address, shall be new
        ManagedServerAddress adr1 = manager.obtainPort(key1);
        Assert.assertTrue(adr1.isNew());
        Assert.assertTrue(adr1.getPort() > 0);
        Assert.assertTrue(adr1.getHost().length() > 0);
        Assert.assertTrue(manager.isInUse(adr1.getPort()));
        Assert.assertTrue(manager.isInUse(adr1));
        ManagedServerAddress g1 = manager.getPort(key1);
        Assert.assertNotNull(g1);
        Assert.assertEquals(adr1.getPort(), g1.getPort());
        Assert.assertEquals(adr1.getHost(), g1.getHost());
        Assert.assertFalse(g1.isNew());

        // re-obtain the first address, shall be the same but not new
        ManagedServerAddress re1 = manager.obtainPort(key1);
        Assert.assertFalse(re1.isNew());
        Assert.assertEquals(adr1.getPort(), re1.getPort());
        Assert.assertEquals(adr1.getHost(), re1.getHost());
        Assert.assertTrue(manager.isInUse(re1.getPort()));
        Assert.assertTrue(manager.isInUse(re1));

        // obtain a second address, shall be new
        ManagedServerAddress adr2 = manager.obtainPort(key2);
        Assert.assertTrue(adr1.isNew());
        Assert.assertTrue(adr2.getPort() > 0);
        Assert.assertTrue(adr2.getHost().length() > 0);
        Assert.assertTrue(adr1.getPort() != adr2.getPort());
        Assert.assertTrue(manager.isInUse(adr2.getPort()));
        Assert.assertTrue(manager.isInUse(adr2));

        // re-obtain the second address, shall be the same but not new
        ManagedServerAddress re2 = manager.obtainPort(key2);
        Assert.assertFalse(re2.isNew());
        Assert.assertEquals(adr2.getPort(), re2.getPort());
        Assert.assertEquals(adr2.getHost(), re2.getHost());
        Assert.assertTrue(manager.isInUse(re2.getPort()));
        Assert.assertTrue(manager.isInUse(re2));

        // release address 1
        manager.releasePort(key1);
        Assert.assertFalse(manager.isInUse(adr1));
        Assert.assertTrue(manager.isInUse(adr2));

        // release address 2
        manager.releasePort(key2);
        Assert.assertFalse(manager.isInUse(adr1));
        Assert.assertFalse(manager.isInUse(adr2));
        testPortReservation(manager, suffix);
        testPrefixes(manager, suffix);
    }
    
    /**
     * Tests the port reservation vs. self-managed ports.
     * 
     * @param manager the manager instance
     * @param suffix additional information to make keys unique for repeated tests, usually empty
     */
    private static void testPortReservation(NetworkManager manager, String suffix) {
        final String httpKey = "external-http" + suffix;
        ServerAddress addr = new ServerAddress(Schema.HTTP, "external.de", 80);
        
        try {
            manager.reservePort(null, null);
            Assert.fail("No exception");
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            manager.reservePort(null, addr);
            Assert.fail("No exception");
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            manager.reservePort(httpKey, null);
            Assert.fail("No exception");
        } catch (IllegalArgumentException e) {
            // ok
        }
        
        // reserve it
        ManagedServerAddress tmp = manager.reservePort(httpKey, addr);
        Assert.assertNotNull(tmp);
        Assert.assertTrue(tmp.isNew());
        Assert.assertEquals(addr.getSchema(), tmp.getSchema());
        Assert.assertEquals(addr.getHost(), tmp.getHost());
        Assert.assertEquals(addr.getPort(), tmp.getPort());
        
        // override does not work
        tmp = manager.reservePort(httpKey, new ServerAddress(Schema.TCP, "here.local", 90));
        Assert.assertNotNull(tmp);
        Assert.assertFalse(tmp.isNew());
        Assert.assertEquals(addr.getSchema(), tmp.getSchema());
        Assert.assertEquals(addr.getHost(), tmp.getHost());
        Assert.assertEquals(addr.getPort(), tmp.getPort());
        
        // reserved remains there, no new address
        tmp = manager.obtainPort(httpKey);
        Assert.assertNotNull(tmp);
        Assert.assertFalse(tmp.isNew());
        Assert.assertEquals(addr.getSchema(), tmp.getSchema());
        Assert.assertEquals(addr.getHost(), tmp.getHost());
        Assert.assertEquals(addr.getPort(), tmp.getPort());

        manager.releasePort(httpKey);
        
        // self-managed, shall now be the local address, nothing from above
        tmp = manager.obtainPort(httpKey);
        Assert.assertNotNull(tmp);
        Assert.assertTrue(tmp.isNew());
        Assert.assertNotEquals(addr.getSchema(), tmp.getSchema()); 
    }
    
    /**
     * Tests prefixes.
     * 
     * @param manager the manager instance
     * @param suffix additional information to make keys unique for repeated tests, usually empty
     */
    private static void testPrefixes(NetworkManager manager, String suffix) {
        ServerAddress addr = new ServerAddress(Schema.TCP, "here.local", 90);

        // usual
        manager.reservePort("a.b.", addr);
        ManagedServerAddress res = manager.getPort("a.b.c");
        Assert.assertNotNull(res);
        Assert.assertEquals(addr.getSchema(), res.getSchema());
        Assert.assertEquals(addr.getHost(), res.getHost());
        Assert.assertEquals(addr.getPort(), res.getPort());
        manager.releasePort("a.b.");
        Assert.assertNull(manager.getPort("a.b.c"));
        Assert.assertNull(manager.getPort("a.b.c.d"));
        
        // unusual
        ManagedServerAddress addr1 = manager.obtainPort("a.");
        Assert.assertNotNull(addr1);
        Assert.assertTrue(addr1.isNew());
        res = manager.getPort("a.b.c");
        Assert.assertNotNull(res);
        Assert.assertEquals(addr1.getSchema(), res.getSchema());
        Assert.assertEquals(addr1.getHost(), res.getHost());
        Assert.assertEquals(addr1.getPort(), res.getPort());
        manager.releasePort("a.");
        Assert.assertNull(manager.getPort("a.b.c"));
        Assert.assertNull(manager.getPort("a.b.c.d"));
    }
    
}
