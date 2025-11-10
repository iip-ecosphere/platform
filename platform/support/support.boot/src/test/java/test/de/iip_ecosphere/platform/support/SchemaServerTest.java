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

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;

/**
 * Combined test for schemas and server addresses.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SchemaServerTest {

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

}
