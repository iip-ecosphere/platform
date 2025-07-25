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

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.LocalPersistenceType;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.PersistenceType;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import test.de.iip_ecosphere.platform.support.aas.TestWithPlugin;

/**
 * Abstract test for device providers.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractDeviceProviderTest extends TestWithPlugin {

    private static Server registryServer;
    private static Server aasServer;
    private static AasSetup orig;
    
    /**
     * Initializes the test.
     */
    @BeforeClass
    public static void startup() {
        loadPlugins();
        // adjust the setup 
        orig = AasPartRegistry.setAasSetup(AasSetup.createLocalEphemeralSetup(null, false));
        System.out.println("Registry: " + AasPartRegistry.getSetup().getRegistryEndpoint().toUri());
        
        ServerRecipe rcp = AasFactory.getInstance().createServerRecipe();
        Endpoint regEndpoint = AasPartRegistry.getSetup().getRegistryEndpoint();
        PersistenceType pType = LocalPersistenceType.INMEMORY;
        LoggerFactory.getLogger(AbstractDeviceProviderTest.class).info(
            "Starting " + pType + " AAS registry on " + regEndpoint.toUri());
        registryServer = rcp.createRegistryServer(AasPartRegistry.getSetup(), pType);
        registryServer.start();
        Endpoint serverEndpoint = AasPartRegistry.getSetup().getServerEndpoint();
        LoggerFactory.getLogger(AbstractDeviceProviderTest.class).info(
            "Starting " + pType + " AAS server on " + serverEndpoint.toUri());
        aasServer = rcp.createAasServer(AasPartRegistry.getSetup(), pType);
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
