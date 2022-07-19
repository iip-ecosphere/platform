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

package test.de.iip_ecosphere.platform.platform;

import java.io.IOException;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.platform.PersistentAasSetup;
import de.iip_ecosphere.platform.platform.PlatformSetup;
import de.iip_ecosphere.platform.platform.ServiceAas;
import de.iip_ecosphere.platform.platform.ServiceAas.ServiceAasSetup;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.LocalPersistenceType;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.PersistenceType;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import org.junit.Assert;

/**
 * Tests {@link ServiceAas}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServiceAasTest {

    private static Server registryServer;
    private static Server aasServer;
    private static PersistentAasSetup orig;
    
    /**
     * Initializes the test.
     */
    @BeforeClass
    public static void startup() {
        // adjust the setup 
        AasSetup aasSetup = AasSetup.createLocalEphemeralSetup(null, false);
        String fullRegUri = AasFactory.getInstance().getFullRegistryUri(aasSetup.getRegistryEndpoint());
        System.out.println("Registry: " + fullRegUri);
        
        ServerRecipe rcp = AasFactory.getInstance().createServerRecipe();
        Endpoint regEndpoint = aasSetup.getRegistryEndpoint();
        PersistenceType pType = LocalPersistenceType.INMEMORY;
        LoggerFactory.getLogger(ServiceAasTest.class).info(
            "Starting " + pType + " AAS registry on " + regEndpoint.toUri());
        registryServer = rcp.createRegistryServer(regEndpoint, pType);
        registryServer.start();
        Endpoint serverEndpoint = aasSetup.getServerEndpoint();
        LoggerFactory.getLogger(ServiceAasTest.class).info(
            "Starting " + pType + " AAS server on " + serverEndpoint.toUri());
        aasServer = rcp.createAasServer(aasSetup.getServerEndpoint(), pType, regEndpoint);
        aasServer.start();
        
        // mock the setup
        orig = PlatformSetup.getInstance().getAas();
        PlatformSetup.getInstance().setAas(new PersistentAasSetup(aasSetup));
    }
    
    /**
     * Shuts down the test.
     */
    @AfterClass
    public static void shutdown() {
        registryServer.stop(true);
        aasServer.stop(true);        
        PlatformSetup.getInstance().setAas(orig);
    }
    
    /**
     * Tests the service AAS.
     * 
     * @throws IOException shall not occur in a successful test
     */
    @Test
    public void testServiceAas() throws IOException {
        ServiceAasSetup sSetup = ServiceAas.obtainNameplateSetup();
        Assert.assertEquals(2, sSetup.getServices().size());
        Map<String, String> sMap = ServiceAas.createAas();
        Assert.assertEquals(2, sMap.size());
    }

}
