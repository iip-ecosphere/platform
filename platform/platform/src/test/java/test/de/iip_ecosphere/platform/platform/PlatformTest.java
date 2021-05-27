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

package test.de.iip_ecosphere.platform.platform;

import org.junit.Test;

import de.iip_ecosphere.platform.platform.PersistentAasSetup.ConfiguredPersistenceType;
import de.iip_ecosphere.platform.platform.PlatformConfiguration;
import de.iip_ecosphere.platform.support.LifecycleHandler;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import org.junit.Assert;

/**
 * Platform test.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlatformTest {
    
    /**
     * Simple platform test.
     */
    @Test
    public void testPlatform() {
        PlatformConfiguration cfg = PlatformConfiguration.getInstance();

        Assert.assertEquals(8080, cfg.getAas().getServer().getPort());
        Assert.assertEquals(Schema.HTTPS, cfg.getAas().getServer().getSchema());
        Assert.assertEquals("127.0.0.1", cfg.getAas().getServer().getHost());
        Assert.assertEquals("aas", cfg.getAas().getServer().getPath());

        Assert.assertEquals(8081, cfg.getAas().getRegistry().getPort());
        Assert.assertEquals(Schema.HTTPS, cfg.getAas().getRegistry().getSchema());
        Assert.assertEquals("127.0.0.1", cfg.getAas().getRegistry().getHost());
        Assert.assertEquals("registry", cfg.getAas().getRegistry().getPath());

        Assert.assertEquals(8082, cfg.getAas().getImplementation().getPort());
        Assert.assertEquals(Schema.TCP, cfg.getAas().getImplementation().getSchema());
        Assert.assertEquals("127.0.0.1", cfg.getAas().getImplementation().getHost());
        Assert.assertEquals("VAB-IIP", cfg.getAas().getImplementation().getProtocol());
        
        Assert.assertEquals(ConfiguredPersistenceType.MONGO, cfg.getAas().getPersistence());

        // overwrite for local test execution
        AasSetup.createLocalEphemeralSetup(cfg.getAas(), false);
        cfg.getAas().setPersistence(ConfiguredPersistenceType.INMEMORY);
        
        LifecycleHandler.startup(new String[] {});
        // check server instances
        LifecycleHandler.shutdown();
    }
    
}
