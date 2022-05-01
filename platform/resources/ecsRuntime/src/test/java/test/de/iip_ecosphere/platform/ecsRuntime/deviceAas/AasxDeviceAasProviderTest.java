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

import org.junit.Test;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import de.iip_ecosphere.platform.ecsRuntime.deviceAas.AasxDeviceAasProvider;

/**
 * Tests {@link AasxDeviceAasProvider}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasxDeviceAasProviderTest extends AbstractDeviceProviderTest {

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
     * Tests {@link AasxDeviceAasProvider}.
     */
    @Test
    public void testProvider() {
        AasxDeviceAasProvider provider = new AasxDeviceAasProvider();
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
