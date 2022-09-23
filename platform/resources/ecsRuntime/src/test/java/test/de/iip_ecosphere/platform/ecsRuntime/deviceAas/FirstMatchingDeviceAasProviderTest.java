/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.ecsRuntime.deviceAas;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iip_ecosphere.platform.ecsRuntime.deviceAas.DeviceAasProvider;
import de.iip_ecosphere.platform.ecsRuntime.deviceAas.FirstMatchingDeviceAasProvider;

/**
 * Tests {@link FirstMatchingDeviceAasProvider}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FirstMatchingDeviceAasProviderTest extends AbstractDeviceProviderTest {

    /**
     * Initializes the test.
     */
    @BeforeClass
    public static void startup() {
        AbstractDeviceProviderTest.startup();
    }
    
    /**
     * Shuts down the test.
     */
    @AfterClass
    public static void shutdown() {
        AbstractDeviceProviderTest.shutdown();
    }

    /**
     * Tests {@link FirstMatchingDeviceAasProvider}.
     */
    @Test
    public void testProvider() {
        DeviceAasProvider provider = DeviceAasProvider.getInstance();
        Assert.assertTrue(provider instanceof FirstMatchingDeviceAasProvider);
        String address = provider.getDeviceAasAddress();
        
        System.out.println(address);
        System.out.println(provider.getIdShort());
        System.out.println(provider.getURN());

        Assert.assertNotNull(address);
        Assert.assertTrue(address.length() > 0);
        Assert.assertNotNull(provider.getIdShort());
        Assert.assertNotNull(provider.getURN());
    }

}
