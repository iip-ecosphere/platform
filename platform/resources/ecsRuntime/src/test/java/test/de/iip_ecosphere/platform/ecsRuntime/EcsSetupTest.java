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

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.ecsRuntime.EcsSetup;
import de.iip_ecosphere.platform.ecsRuntime.EcsSetup.AbstractManagerSetup;
import de.iip_ecosphere.platform.support.net.NetworkManagerSetup;

/**
 * Tests {@link EcsSetup}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EcsSetupTest {
    
    private static class MySetup extends AbstractManagerSetup {
    }
    
    /**
     * Tests the configuration.
     */
    @Test
    public void testEcsSetup() throws IOException {
        // does not exist
        try {
            EcsSetup.readFromYaml(EcsSetup.class, "me.yml");
            Assert.fail("No exception");
        } catch (IOException e) {
            // ok
        }
        
        // for now no configuration content
        EcsSetup cfg = EcsSetup.readConfiguration();
        Assert.assertNotNull(cfg);
        Assert.assertTrue(cfg.getMonitoringUpdatePeriod() > 0);
        Assert.assertNotNull(cfg.getTransport());
        Assert.assertEquals("localhost", cfg.getTransport().getHost());
        NetworkManagerSetup netMgr = cfg.getNetMgr();
        Assert.assertEquals(1025, netMgr.getLowPort());
        Assert.assertEquals(65535, netMgr.getHighPort());
        
        MySetup ms = new MySetup();
        ms.setAuthenticationKey("key");
        Assert.assertEquals("key", ms.getAuthenticationKey());
    }

}
