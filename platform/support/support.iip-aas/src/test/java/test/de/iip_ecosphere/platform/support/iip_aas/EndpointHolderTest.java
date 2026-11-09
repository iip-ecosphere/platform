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

package test.de.iip_ecosphere.platform.support.iip_aas;

import java.io.File;

import org.junit.Test;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.iip_aas.config.BasicEndpointValidator;
import de.iip_ecosphere.platform.support.iip_aas.config.EndpointHolder;
import de.iip_ecosphere.platform.support.iip_aas.config.RuntimeSetup;
import de.iip_ecosphere.platform.support.iip_aas.config.RuntimeSetupEndpointValidator;
import de.iip_ecosphere.platform.support.iip_aas.config.ServerAddressHolder;
import de.iip_ecosphere.platform.support.json.JsonUtils;
import test.de.iip_ecosphere.platform.support.aas.TestWithPlugin;

import org.junit.Assert;

/**
 * Tests {@link EndpointHolder}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EndpointHolderTest extends TestWithPlugin {
    
    /**
     * Tests {@link EndpointHolder}, {@link BasicEndpointValidator}, {@link RuntimeSetupEndpointValidator}.
     */
    @Test
    public void testEndpointHolder() {
        File file = RuntimeSetup.getFile();
        FileUtils.deleteQuietly(file);
        RuntimeSetup setup = new RuntimeSetup();
        setup.setAasRegistry("https://me.here.de:9998/myPath");
        setup.store();
        
        EndpointHolder holder = new EndpointHolder(Schema.HTTP, Endpoint.LOCALHOST, -1, "path");
        Assert.assertEquals(holder.getSchema(), Schema.HTTP);
        Assert.assertEquals(holder.getHost(), Endpoint.LOCALHOST);
        Assert.assertEquals(holder.getPort(), -1);
        Assert.assertEquals(holder.getPath(), "path");
        
        holder.setValidator(RuntimeSetupEndpointValidator.create(r -> r.getAasRegistry()));
        Assert.assertEquals(holder.getSchema(), Schema.HTTPS);
        Assert.assertEquals(holder.getHost(), "me.here.de");
        Assert.assertEquals(holder.getPort(), 9998);
        Assert.assertEquals(holder.getPath(), "myPath");

        FileUtils.deleteQuietly(file);
        RuntimeSetup.clear();
    }
    
    /**
     * Tests the from/to JSON functions for {@link ServerAddress}.
     */
    @Test
    public void testServerAddress() {
        ServerAddress addr = null;
        String json = ServerAddressHolder.toJson(addr);
        ServerAddress tmp = ServerAddressHolder.serverAddressFromJson(json);
        Assert.assertNull(tmp);
        
        addr = new ServerAddress(Schema.TCP, "me.here", 10321);
        json = JsonUtils.toJson(addr);
        tmp = ServerAddressHolder.serverAddressFromJson(json);
        Assert.assertNotNull(tmp);
        Assert.assertEquals(addr.getSchema(), tmp.getSchema());
        Assert.assertEquals(addr.getHost(), tmp.getHost());
        Assert.assertEquals(addr.getPort(), tmp.getPort());
    }

}
