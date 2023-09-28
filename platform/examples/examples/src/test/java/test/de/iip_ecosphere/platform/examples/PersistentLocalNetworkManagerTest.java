/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.examples;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.examples.PersistentLocalNetworkManagerDescriptor;
import de.iip_ecosphere.platform.examples.PersistentLocalNetworkManagerImpl;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.net.ManagedServerAddress;
import de.iip_ecosphere.platform.support.net.NetworkManager;

/**
 * Tests {@link PersistentLocalNetworkManagerDescriptor} and {@link PersistentLocalNetworkManagerImpl}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PersistentLocalNetworkManagerTest {
    
    /**
     * Tests {@link PersistentLocalNetworkManagerDescriptor} and {@link PersistentLocalNetworkManagerImpl}.
     */
    @Test
    public void testDescriptor() {
        File f = PersistentLocalNetworkManagerDescriptor.getFile();
        FileUtils.deleteQuietly(f);
        PersistentLocalNetworkManagerDescriptor desc = new PersistentLocalNetworkManagerDescriptor();
        NetworkManager mgr = desc.createInstance();
        ServerAddress addr = new ServerAddress(Schema.HTTP, "localhost", 1234);
        mgr.reservePort("key", addr);
        Assert.assertTrue(f.exists());
        NetworkManager mgr2 = desc.createInstance();
        ManagedServerAddress mAddr = mgr2.getPort("key");
        Assert.assertNotNull(mAddr);
        Assert.assertEquals(addr.getPort(), mAddr.getPort());
        Assert.assertEquals(addr.getHost(), mAddr.getHost());
    }

}
