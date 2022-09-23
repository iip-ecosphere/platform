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

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iip_ecosphere.platform.ecsRuntime.deviceAas.DeviceAasProvider;
import de.iip_ecosphere.platform.ecsRuntime.deviceAas.YamlDeviceAasProvider;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.iip_aas.NameplateSetup;

import org.junit.Assert;

/**
 * Tests {@link YamlDeviceAasProvider}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlDeviceAasProviderTest extends AbstractDeviceProviderTest {

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
     * Tests the provider.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testProvider() throws IOException {
        DeviceAasProvider instance = new YamlDeviceAasProvider();
        String address = instance.getDeviceAasAddress();
        Assert.assertTrue(null != address && address.length() > 0); // there is an AAS
        Assert.assertTrue(instance.getIdShort().length() > 0);
        Assert.assertTrue(instance.getURN().length() > 0);

        System.out.println(address);
        Assert.assertNotNull(NameplateSetup.resolve(instance.getURN()));
        Aas aas = NameplateSetup.resolve(address);
        Assert.assertNotNull(aas);
    }

}
