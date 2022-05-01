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
import org.junit.BeforeClass;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.LocalPersistenceType;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.PersistenceType;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;

/**
 * Abstract test for device providers.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractDeviceProviderTest {

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
        String fullRegUri = AasFactory.getInstance().getFullRegistryUri(
            AasPartRegistry.getSetup().getRegistryEndpoint());
        System.out.println("Registry: " + fullRegUri);
        
        ServerRecipe rcp = AasFactory.getInstance().createServerRecipe();
        Endpoint regEndpoint = AasPartRegistry.getSetup().getRegistryEndpoint();
        PersistenceType pType = LocalPersistenceType.INMEMORY;
        LoggerFactory.getLogger(AbstractDeviceProviderTest.class).info(
            "Starting " + pType + " AAS registry on " + regEndpoint.toUri());
        registryServer = rcp.createRegistryServer(regEndpoint, pType);
        registryServer.start();
        Endpoint serverEndpoint = AasPartRegistry.getSetup().getServerEndpoint();
        LoggerFactory.getLogger(AbstractDeviceProviderTest.class).info(
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

}
