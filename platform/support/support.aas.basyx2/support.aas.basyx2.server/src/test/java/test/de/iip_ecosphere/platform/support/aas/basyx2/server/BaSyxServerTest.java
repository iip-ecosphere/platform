/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.aas.basyx2.server;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.aas.AasServerFactoryDescriptor;
import de.iip_ecosphere.platform.support.aas.BasicSetupSpec;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.LocalPersistenceType;
import de.iip_ecosphere.platform.support.aas.basyx2.server.BaSyxLocalServer;
import de.iip_ecosphere.platform.support.aas.basyx2.server.BaSyxServerFactoryDescriptor;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import de.iip_ecosphere.platform.support.plugins.CurrentClassloaderPluginSetupDescriptor;
import de.iip_ecosphere.platform.support.plugins.Plugin;
import de.iip_ecosphere.platform.support.plugins.PluginManager;

/**
 * Tests the AAS abstraction implementation for BaSyx.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxServerTest {

    /**
     * Tests the plugin setup.
     */
    @Test
    public void testPlugin() {
        PluginManager.registerPlugin(CurrentClassloaderPluginSetupDescriptor.INSTANCE);
        Plugin<AasServerFactoryDescriptor> plg = PluginManager.getPlugin(BaSyxServerFactoryDescriptor.PLUGIN_ID, 
            AasServerFactoryDescriptor.class);
        Assert.assertNotNull(plg);
        
        Optional<AasServerFactoryDescriptor> desc = ServiceLoaderUtils.findFirst(AasServerFactoryDescriptor.class);
        Assert.assertTrue(desc.isPresent());
    }
    
    /**
     * Tests starting/stopping the BaSyx servers.
     */
    @Test
    public void testServers() {
        BasicSetupSpec spec = new BasicSetupSpec(new Endpoint(Schema.HTTP, ""), new Endpoint(Schema.HTTP, ""), 
            new Endpoint(Schema.HTTP, ""), new Endpoint(Schema.HTTP, ""));
        BaSyxLocalServer server = new BaSyxLocalServer(spec, BaSyxLocalServer.ServerType.COMBINED, 
            LocalPersistenceType.INMEMORY);
        server.start();
        TimeUtils.sleep(3000);
        server.stop(false);
    }

}
