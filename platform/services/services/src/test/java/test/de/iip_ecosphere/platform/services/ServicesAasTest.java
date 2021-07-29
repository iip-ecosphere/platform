/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.services;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iip_ecosphere.platform.services.ArtifactDescriptor;
import de.iip_ecosphere.platform.services.ServiceDescriptor;
import de.iip_ecosphere.platform.services.ServiceFactory;
import de.iip_ecosphere.platform.services.ServiceManager;
import de.iip_ecosphere.platform.services.ServicesAas;
import de.iip_ecosphere.platform.services.ServicesAasClient;
import de.iip_ecosphere.platform.services.TypedDataConnectorDescriptor;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.LifecycleHandler;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasPrintVisitor;
import de.iip_ecosphere.platform.support.aas.AasServer;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import test.de.iip_ecosphere.platform.test.amqp.qpid.TestQpidServer;

/**
 * Tests the {@link ServicesAas}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServicesAasTest {

    private static Server qpid;

    /**
     * Initializes the test.
     */
    @BeforeClass
    public static void startup() {
        ServerAddress broker = new ServerAddress(Schema.IGNORE);
        qpid = new TestQpidServer(broker);
        ServiceFactory.getTransport().setPort(broker.getPort());
        qpid.start();
    }
    
    /**
     * Shuts down the test.
     */
    @AfterClass
    public static void shutdown() {
        qpid.stop(true);
    }
    
    /**
     * Tests {@link ServicesAas}.
     * 
     * @throws IOException shall not occur
     * @throws ExecutionException shall not occur 
     * @throws URISyntaxException shall not occur
     */
    @Test
    public void testAas() throws IOException, ExecutionException, URISyntaxException {
        NotificationMode oldM = ActiveAasBase.setNotificationMode(NotificationMode.SYNCHRONOUS);
        Assert.assertTrue(AasPartRegistry.contributorClasses().contains(ServicesAas.class));
        AasSetup oldSetup = AasPartRegistry.setAasSetup(AasSetup.createLocalEphemeralSetup());
        AasPartRegistry.AasBuildResult res = AasPartRegistry.build(c -> c instanceof ServicesAas);
        
        // active AAS require two server instances and a deployment
        Server implServer = res.getProtocolServerBuilder().build();
        implServer.start();
        Server aasServer = AasPartRegistry.deploy(res.getAas()); 
        aasServer.start();
        AasPartRegistry.retrieveIipAas().accept(new AasPrintVisitor());
        
        ServicesAasClient client = new ServicesAasClient(Id.getDeviceIdAas());
        test(client);
        
        aasServer.stop(true);
        implServer.stop(true);
        AasPartRegistry.setAasSetup(oldSetup);
        ActiveAasBase.setNotificationMode(oldM);
    }

    /**
     * Tests the {@link EcsAas} via the lifecycle descriptors.
     * 
     * @throws IOException shall not occur
     * @throws ExecutionException shall not occur 
     * @throws URISyntaxException shall not occur
     */
    @Test
    public void testLifecycle() throws IOException, ExecutionException, URISyntaxException {
        NotificationMode oldM = ActiveAasBase.setNotificationMode(NotificationMode.SYNCHRONOUS);
        AasSetup aasSetup = AasSetup.createLocalEphemeralSetup(null, false);
        AasSetup oldSetup = AasPartRegistry.setAasSetup(aasSetup);
        ServiceFactory.setAasSetup(aasSetup);

        ServerRecipe rcp = AasFactory.getInstance().createServerRecipe();
        Endpoint regEndpoint = aasSetup.getRegistryEndpoint();
        Server registryServer = rcp
            .createRegistryServer(regEndpoint, ServerRecipe.LocalPersistenceType.INMEMORY)
            .start();
        AasServer aasServer = rcp
            .createAasServer(aasSetup.getServerEndpoint(), ServerRecipe.LocalPersistenceType.INMEMORY, regEndpoint)
            .start();

        LifecycleHandler.startup(new String[] {});

        ServicesAasClient client = new ServicesAasClient(Id.getDeviceIdAas());
        test(client);
        
        LifecycleHandler.shutdown();

        aasServer.stop(true);
        registryServer.stop(true);

        AasPartRegistry.setAasSetup(oldSetup);
        ActiveAasBase.setNotificationMode(oldM);
    }
    
    /**
     * Tests the serivces AAS client.
     * 
     * @param client the client
     * @throws IOException shall not occur
     * @throws ExecutionException shall not occur 
     * @throws URISyntaxException shall not occur
     */
    private void test(ServicesAasClient client) throws IOException, ExecutionException, URISyntaxException {
        ServiceManager mgr = ServiceFactory.getServiceManager(); // for x-checking

        final URI dummy = new URI("file:///dummy");
        String aId = client.addArtifact(dummy);
        Assert.assertNotNull(aId);
        ArtifactDescriptor aDesc = mgr.getArtifact(aId);
        Assert.assertNotNull(aId);
        Assert.assertTrue(mgr.getArtifactIds().contains(aId));
        Assert.assertTrue(mgr.getArtifacts().contains(aDesc));

        Assert.assertEquals(aId, aDesc.getId());
        Assert.assertTrue(aDesc.getName().length() > 0);
        Assert.assertTrue(aDesc.getServiceIds().size() > 0);
        List<String> sIds = CollectionUtils.toList(aDesc.getServiceIds().iterator());
        String sId = sIds.get(0);
        Assert.assertNotNull(sId);
        ServiceDescriptor sDesc = aDesc.getService(sId);
        Assert.assertNotNull(sDesc);
        Assert.assertTrue(aDesc.getServiceIds().contains(sDesc.getId()));
        Assert.assertTrue(aDesc.getServices().contains(sDesc));
        Assert.assertEquals(ServiceState.AVAILABLE, client.getServiceState(sId));
        String[] ids = client.getServices(aDesc.getId());
        Assert.assertEquals(aDesc.getServices().size(), ids.length);
        boolean foundAll = true;
        for (ServiceDescriptor s : aDesc.getServices()) {
            boolean found = false;
            for (int i = 0; !found && i < ids.length; i++) {
                found = ids[i].equals(s.getId());
            }
            foundAll &= found;
        }
        Assert.assertTrue(foundAll);
        Assert.assertNotNull(client.getArtifacts());
        Assert.assertNotNull(client.getRelations());
        Assert.assertNotNull(client.getServices());
        
        Predicate<TypedDataConnectorDescriptor> av = ServicesAas.createAvailabilityPredicate(1000, 200, false);
        Assert.assertFalse(av.test(sDesc.getOutputDataConnectors().get(0))); // not running/connected
        
        client.startService(sId);
        Assert.assertEquals(ServiceState.RUNNING, client.getServiceState(sId));
        AasPartRegistry.retrieveIipAas().accept(new AasPrintVisitor());
        
        client.passivateService(sId);
        Assert.assertEquals(ServiceState.PASSIVATED, client.getServiceState(sId));
        client.activateService(sId);
        Assert.assertEquals(ServiceState.RUNNING, client.getServiceState(sId));
        Map<String, String> vals = new HashMap<String, String>();
        vals.put("here", "1.23");
        vals.put("there", "{id:15, val:12}");
        client.reconfigureService(sId, vals);
        Assert.assertEquals(ServiceState.RUNNING, client.getServiceState(sId));
        client.setServiceState(sId, ServiceState.RUNNING); // no effect, just call
        Assert.assertTrue(av.test(sDesc.getOutputDataConnectors().get(0)));
        client.stopService(sId);
        Assert.assertEquals(ServiceState.STOPPED, client.getServiceState(sId));

        ServiceManagerTest.assertException(() -> mgr.cloneArtifact(aId, dummy));
        ServiceManagerTest.assertException(() -> mgr.migrateService(aId, "other"));
        ServiceManagerTest.assertException(() -> mgr.switchToService(aId, sId));
        mgr.updateService(aId, dummy);
        
        client.removeArtifact(aId);
        Assert.assertFalse(mgr.getArtifactIds().contains(aId));
        Assert.assertFalse(mgr.getArtifacts().contains(aDesc));
    }

}
