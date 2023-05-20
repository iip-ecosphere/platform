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

package test.de.iip_ecosphere.platform.support;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;

/**
 * Combined test for schemas, server addresses and endpoints.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SchemaServerEndpointTest {

    /**
     * Tests schemas.
     */
    @Test
    public void testSchema() {
        Assert.assertEquals("http://", Schema.HTTP.toUri());
        Assert.assertFalse(Schema.HTTP.isEncrypted());
    }
    
    /**
     * Tests server address.
     */
    @Test
    public void testServerAddress() {
        Assert.assertTrue(ServerAddress.isValidPort(0));
        Assert.assertTrue(ServerAddress.isValidPort(8080));
        Assert.assertFalse(ServerAddress.isValidPort(-1));
        Assert.assertFalse(ServerAddress.isValidPort(66000));
        Assert.assertEquals(0, ServerAddress.validatePort(0));
        Assert.assertEquals(1024, ServerAddress.validatePort(1024));
        Assert.assertTrue(ServerAddress.isValidPort(ServerAddress.validatePort(-1)));
        
        ServerAddress add = new ServerAddress(Schema.IGNORE);
        Assert.assertEquals(Schema.IGNORE, add.getSchema());
        Assert.assertTrue(add.getPort() > 0);
        Assert.assertEquals(ServerAddress.LOCALHOST, add.getHost());
        Assert.assertTrue(add.toUri().length() > 0);

        add = new ServerAddress(Schema.HTTP, 1234);
        Assert.assertEquals(Schema.HTTP, add.getSchema());
        Assert.assertEquals(1234, add.getPort());
        Assert.assertEquals(ServerAddress.LOCALHOST, add.getHost());
        Assert.assertEquals("http://localhost:1234", add.toUri());
        Assert.assertEquals("http://localhost:1234", add.toServerUri());
        
        add = new ServerAddress(Schema.HTTP, null, 1235);
        Assert.assertEquals(Schema.HTTP, add.getSchema());
        Assert.assertEquals(1235, add.getPort());
        Assert.assertEquals(ServerAddress.LOCALHOST, add.getHost());
        Assert.assertEquals("http://localhost:1235", add.toUri());
        Assert.assertEquals("http://localhost:1235", add.toServerUri());
        
        ServerAddress add1 = new ServerAddress(Schema.HTTP, "", 1235);
        Assert.assertEquals(Schema.HTTP, add1.getSchema());
        Assert.assertEquals(1235, add1.getPort());
        Assert.assertEquals(ServerAddress.LOCALHOST, add1.getHost());
        Assert.assertEquals("http://localhost:1235", add1.toUri());
        Assert.assertEquals("http://localhost:1235", add1.toServerUri());
        
        Assert.assertEquals(add, add1);
        Assert.assertEquals(add.hashCode(), add1.hashCode());
        
        add = new ServerAddress(Schema.HTTP, "xxx", 1235);
        Assert.assertEquals(Schema.HTTP, add.getSchema());
        Assert.assertEquals(1235, add.getPort());
        Assert.assertEquals("xxx", add.getHost());
        Assert.assertEquals("http://xxx:1235", add.toUri());
        Assert.assertEquals("http://xxx:1235", add.toServerUri());
    }

    /**
     * Tests server address.
     */
    @Test
    public void testEndpoint() {
        Endpoint ep = new Endpoint(Schema.HTTP, "aa");
        Assert.assertEquals(Schema.HTTP, ep.getSchema());
        Assert.assertTrue(ep.getPort() > 0);
        Assert.assertEquals(ServerAddress.LOCALHOST, ep.getHost());
        Assert.assertEquals("/aa", ep.getEndpoint());
        Assert.assertTrue(ep.toUri().length() > 0);

        ep = new Endpoint(Schema.HTTP, 1234, "");
        Assert.assertEquals(Schema.HTTP, ep.getSchema());
        Assert.assertEquals(1234, ep.getPort());
        Assert.assertEquals(ServerAddress.LOCALHOST, ep.getHost());
        Assert.assertEquals("", ep.getEndpoint());
        Assert.assertEquals("http://localhost:1234", ep.toUri());
        Assert.assertEquals("http://localhost:1234", ep.toServerUri());

        ep = new Endpoint(Schema.HTTP, null, 1235, "rep");
        Assert.assertEquals(Schema.HTTP, ep.getSchema());
        Assert.assertEquals(1235, ep.getPort());
        Assert.assertEquals(ServerAddress.LOCALHOST, ep.getHost());
        Assert.assertEquals("/rep", ep.getEndpoint());
        Assert.assertEquals("http://localhost:1235/rep", ep.toUri());
        Assert.assertEquals("http://localhost:1235", ep.toServerUri());

        Endpoint ep1 = new Endpoint(Schema.HTTP, "", 1235, "/rep");
        Assert.assertEquals(Schema.HTTP, ep1.getSchema());
        Assert.assertEquals(1235, ep1.getPort());
        Assert.assertEquals(ServerAddress.LOCALHOST, ep1.getHost());
        Assert.assertEquals("/rep", ep1.getEndpoint());
        Assert.assertEquals("http://localhost:1235/rep", ep1.toUri());
        Assert.assertEquals("http://localhost:1235", ep1.toServerUri());
        
        Assert.assertEquals(ep, ep1);
        Assert.assertEquals(ep.hashCode(), ep1.hashCode());

        ServerAddress server = new ServerAddress(Schema.HTTP, "xxx", 1235);
        ep = new Endpoint(server, "rep");
        Assert.assertEquals(Schema.HTTP, ep.getSchema());
        Assert.assertEquals(1235, ep.getPort());
        Assert.assertEquals("xxx", ep.getHost());
        Assert.assertEquals("/rep", ep.getEndpoint());
        Assert.assertEquals("http://xxx:1235/rep", ep.toUri());
        Assert.assertEquals("http://xxx:1235", ep.toServerUri());
    }

}
