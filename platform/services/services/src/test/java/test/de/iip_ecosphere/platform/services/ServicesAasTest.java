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

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.ArtifactDescriptor;
import de.iip_ecosphere.platform.services.ServiceDescriptor;
import de.iip_ecosphere.platform.services.ServiceFactory;
import de.iip_ecosphere.platform.services.ServiceManager;
import de.iip_ecosphere.platform.services.ServiceState;
import de.iip_ecosphere.platform.services.ServicesAas;
import de.iip_ecosphere.platform.services.ServicesAasClient;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.AasPrintVisitor;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;

/**
 * Tests the {@link ServicesAas}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServicesAasTest {

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
        Endpoint oldEp = AasPartRegistry.setAasEndpoint(new Endpoint(Schema.HTTP, "registry"));
        ServerAddress oldImpl = AasPartRegistry.setProtocolAddress(new ServerAddress(Schema.TCP));
        AasPartRegistry.AasBuildResult res = AasPartRegistry.build(c -> c instanceof ServicesAas);
        
        // active AAS require two server instances and a deployment
        Server implServer = res.getProtocolServerBuilder().build();
        implServer.start();
        Server aasServer = AasPartRegistry.deploy(res.getAas()); 
        aasServer.start();
        AasPartRegistry.retrieveIipAas().accept(new AasPrintVisitor());
        
        ServicesAasClient client = new ServicesAasClient(Id.getDeviceIdAas());
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
        client.stopService(sId);
        Assert.assertEquals(ServiceState.STOPPED, client.getServiceState(sId));

        ServiceManagerTest.assertException(() -> mgr.cloneArtifact(aId, dummy));
        ServiceManagerTest.assertException(() -> mgr.migrateService(aId, "other"));
        ServiceManagerTest.assertException(() -> mgr.switchToService(aId, sId));
        mgr.updateService(aId, dummy);
        
        client.removeArtifact(aId);
        Assert.assertFalse(mgr.getArtifactIds().contains(aId));
        Assert.assertFalse(mgr.getArtifacts().contains(aDesc));
        
        aasServer.stop(true);
        implServer.stop(true);
        AasPartRegistry.setAasEndpoint(oldEp);
        AasPartRegistry.setProtocolAddress(oldImpl);
        ActiveAasBase.setNotificationMode(oldM);
    }

}
