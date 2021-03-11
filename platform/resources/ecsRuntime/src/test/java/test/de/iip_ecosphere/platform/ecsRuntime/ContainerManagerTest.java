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

package test.de.iip_ecosphere.platform.ecsRuntime;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.ecsRuntime.ContainerDescriptor;
import de.iip_ecosphere.platform.ecsRuntime.ContainerManager;
import de.iip_ecosphere.platform.ecsRuntime.ContainerState;
import de.iip_ecosphere.platform.ecsRuntime.EcsFactory;

/**
 * Template test.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ContainerManagerTest {
    
    /**
     * Template test.
     * 
     * @throws ExecutionException shall not occur
     * @throws URISyntaxException shall not occur
     */
    @Test
    public void testApp() throws ExecutionException, URISyntaxException {
        URI dummy = new URI("file:///dummy");
        ContainerManager mgr = EcsFactory.getContainerManager();
        Assert.assertNotNull(mgr);
        String id = mgr.addContainer(dummy);
        Assert.assertNotNull(id);
        Assert.assertTrue(id.length() > 0);
        
        ContainerDescriptor cnt = mgr.getContainer("");
        Assert.assertNull(cnt); // does not exist
        cnt = mgr.getContainer(id);
        Assert.assertNotNull(cnt);
        Assert.assertEquals(id, cnt.getId());
        Assert.assertNotNull(cnt.getName());
        Assert.assertEquals(ContainerState.AVAILABLE, cnt.getState());
        Assert.assertEquals(ContainerState.AVAILABLE, mgr.getState(id));
        Assert.assertNotNull(cnt.getVersion());
        
        Assert.assertTrue(mgr.getContainers().contains(cnt));
        Assert.assertTrue(mgr.getIds().contains(id));
        try {
            mgr.startContainer("");
            Assert.fail("No exception");
        } catch (ExecutionException e) {
            // this is ok
        }
        mgr.startContainer(id);
        Assert.assertEquals(ContainerState.DEPLOYED, cnt.getState());
        Assert.assertEquals(ContainerState.DEPLOYED, mgr.getState(id));
        mgr.updateContainer(id, dummy);
        mgr.stopContainer(id);
        Assert.assertEquals(ContainerState.STOPPED, cnt.getState());
        Assert.assertEquals(ContainerState.STOPPED, mgr.getState(id));

        mgr.startContainer(id);
        Assert.assertEquals(ContainerState.DEPLOYED, cnt.getState());
        Assert.assertEquals(ContainerState.DEPLOYED, mgr.getState(id));

        try {
            mgr.undeployContainer(id);
            Assert.fail("No exception");
        } catch (ExecutionException e) {
            // this is ok
        }

        mgr.stopContainer(id);
        Assert.assertEquals(ContainerState.STOPPED, cnt.getState());
        Assert.assertEquals(ContainerState.STOPPED, mgr.getState(id));
        mgr.undeployContainer(id);
        Assert.assertEquals(ContainerState.UNKOWN, cnt.getState());
        Assert.assertEquals(ContainerState.UNKOWN, mgr.getState(id));
        
        id = mgr.addContainer(dummy);
        cnt = mgr.getContainer(id);
        mgr.startContainer(id);
        mgr.migrateContainer(id, dummy);
        if (ContainerState.STOPPED == cnt.getState()) {
            mgr.undeployContainer(id);
        }
        Assert.assertEquals(ContainerState.UNKOWN, cnt.getState());
        Assert.assertEquals(ContainerState.UNKOWN, mgr.getState(id));

        // cnt is gone, but there is a new instance on "bla"
        Assert.assertFalse(mgr.getContainers().contains(cnt));
        Assert.assertTrue(mgr.getContainers().size() > 0);
        Assert.assertFalse(mgr.getIds().contains(id));
        Assert.assertTrue(mgr.getIds().size() > 0);
    }
    
}
