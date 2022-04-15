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

package test.de.iip_ecosphere.platform.ecsRuntime;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.ecsRuntime.DeviceAasProvider;
import de.iip_ecosphere.platform.ecsRuntime.SelfDeviceAasProvider;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.LocalPersistenceType;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.PersistenceType;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import org.junit.Assert;

/**
 * Tests {@link SelfDeviceAasProvider}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SelfDeviceAasProviderTest {

    private static Server registryServer;
    private static Server aasServer;
    private static AasSetup orig;
    
    /**
     * Initializes the test.
     */
    @BeforeClass
    public static void startup() {
        // adjust the setup 
        orig = AasPartRegistry.setAasSetup(AasSetup.createLocalEphemeralSetup(null, false));
        System.out.println(AasPartRegistry.getSetup().getRegistryEndpoint().toServerUri() 
            + "/registry/api/v1/registry");
        
        ServerRecipe rcp = AasFactory.getInstance().createServerRecipe();
        Endpoint regEndpoint = AasPartRegistry.getSetup().getRegistryEndpoint();
        PersistenceType pType = LocalPersistenceType.INMEMORY;
        LoggerFactory.getLogger(SelfDeviceAasProviderTest.class).info(
            "Starting " + pType + " AAS registry on " + regEndpoint.toUri());
        registryServer = rcp.createRegistryServer(regEndpoint, pType);
        registryServer.start();
        Endpoint serverEndpoint = AasPartRegistry.getSetup().getServerEndpoint();
        LoggerFactory.getLogger(SelfDeviceAasProviderTest.class).info(
            "Starting " + pType + " AAS server on " + serverEndpoint.toUri());
        aasServer = rcp.createAasServer(serverEndpoint, pType, regEndpoint);
        aasServer.start();
    }
    
    /**
     * Shuts down the test.
     */
    @AfterClass
    public static void shutdown() {
        registryServer.stop(true);
        aasServer.stop(true);        
        AasPartRegistry.setAasSetup(orig);
    }
    
    /**
     * Tests the provider.
     */
    @Test
    public void testProvider() {
        DeviceAasProvider instance = DeviceAasProvider.getInstance();
        Assert.assertTrue(instance instanceof SelfDeviceAasProvider);
        String address = instance.getDeviceAasAddress();
        Assert.assertTrue(null != address && address.length() > 0); // there is an AAS
        Assert.assertTrue(instance.getIdShort().length() > 0);
        Assert.assertTrue(instance.getURN().length() > 0);
        System.out.println(address);
    }

}
