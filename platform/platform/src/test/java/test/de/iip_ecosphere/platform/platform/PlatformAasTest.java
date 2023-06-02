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

package test.de.iip_ecosphere.platform.platform;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iip_ecosphere.platform.ecsRuntime.EcsAas;
import de.iip_ecosphere.platform.ecsRuntime.EcsFactory;
import de.iip_ecosphere.platform.platform.PersistentAasSetup;
import de.iip_ecosphere.platform.platform.PlatformAas;
import de.iip_ecosphere.platform.platform.PlatformSetup;
import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsAasConstructor;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.LocalPersistenceType;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.PersistenceType;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationInstanceAasConstructor;
import de.iip_ecosphere.platform.transport.Transport;
import test.de.iip_ecosphere.platform.test.amqp.qpid.TestQpidServer;


/**
 * Tests {@link PlatformAas}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlatformAasTest {

    private static Server qpid;
    
    /**
     * Initializes the test.
     */
    @BeforeClass
    public static void startup() {
        ServerAddress broker = new ServerAddress(Schema.IGNORE);
        qpid = new TestQpidServer(broker);
        EcsFactory.getSetup().getTransport().setPort(broker.getPort());
        qpid.start();
        Transport.setTransportSetup(() -> EcsFactory.getSetup().getTransport());
    }
    
    /**
     * Shuts down the test.
     */
    @AfterClass
    public static void shutdown() {
        qpid.stop(true);
        Transport.setTransportSetup(null);
    }
    
    /**
     * Tests {@link PlatformAas}.
     * 
     * @throws IOException shall not occur in a successful test
     */
    @Test
    public void testPlatformAas() throws IOException {
        NotificationMode oldM = ActiveAasBase.setNotificationMode(NotificationMode.SYNCHRONOUS);
        Assert.assertTrue(AasPartRegistry.contributorClasses().contains(EcsAas.class));
        
        AasSetup mySetup = AasSetup.createLocalEphemeralSetup(null, false);
        AasSetup oldSetup = AasPartRegistry.setAasSetup(mySetup);
        PersistentAasSetup oldPSetup = PlatformSetup.getInstance().getAas(); 
        PlatformSetup.getInstance().setAas(new PersistentAasSetup(mySetup));
        // like in an usual platform - platform server goes first
        ServerRecipe rcp = AasFactory.getInstance().createServerRecipe();
        Endpoint regEndpoint = AasPartRegistry.getSetup().getRegistryEndpoint();
        PersistenceType pType = LocalPersistenceType.INMEMORY;
        System.out.println("Starting " + pType + " AAS registry on " 
            + AasFactory.getInstance().getFullRegistryUri(regEndpoint));
        Server registryServer = rcp.createRegistryServer(regEndpoint, pType);
        registryServer.start();
        Endpoint serverEndpoint = AasPartRegistry.getSetup().getServerEndpoint();
        System.out.println("Starting " + pType + " AAS server on " + serverEndpoint.toUri());
        Server aasServer = rcp.createAasServer(serverEndpoint, pType, regEndpoint);
        aasServer.start();
        
        AasPartRegistry.AasBuildResult res = AasPartRegistry.build(c -> c instanceof PlatformAas);
        
        // active AAS require two server instances and a deployment
        Server implServer = res.getProtocolServerBuilder().build();
        implServer.start();
        AasPartRegistry.remoteDeploy(res.getAas()); 

        String id1 = PlatformAas.notifyAppNewInstance("app-1", "plan-1");
        if (ApplicationInstanceAasConstructor.firstAppWithoutAppId()) {
            Assert.assertNull(id1); // it's the first one
        } else {
            Assert.assertNotNull(id1); // it's the first one
        }        
        String id2 = PlatformAas.notifyAppNewInstance("app-1", "plan-1");
        Assert.assertNotNull(id2);
        
        int count = PlatformAas.notifyAppInstanceStopped("app-1", id2);
        Assert.assertEquals(1, count); // already one gone
        count = PlatformAas.notifyAppInstanceStopped("app-1", id1);
        Assert.assertEquals(0, count); // both gone

        aasServer.stop(true);
        implServer.stop(true);
        AasPartRegistry.setAasSetup(oldSetup);
        ActiveAasBase.setNotificationMode(oldM);
        MetricsAasConstructor.clear();
        PlatformSetup.getInstance().setAas(oldPSetup);
    }
    
}
