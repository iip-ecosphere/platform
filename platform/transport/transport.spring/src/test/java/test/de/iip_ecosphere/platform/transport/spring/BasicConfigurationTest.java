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

package test.de.iip_ecosphere.platform.transport.spring;

import java.io.File;

import org.junit.Test;

import de.iip_ecosphere.platform.transport.spring.BasicConfiguration;
import org.junit.Assert;

/**
 * Tests the {@link BasicConfiguration}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BasicConfigurationTest {
    
    /**
     * Simple getter/setter test.
     */
    @Test
    public void testBasicConfiguration() {
        final String host = "host";
        final File keystore = new File("keys.jks");
        final int port = 1234;
        final String pass = "pass";
        final String alias = "alias";
        
        BasicConfiguration c = new BasicConfiguration();
        c.setHost(host);
        c.setKeystore(keystore);
        c.setPort(port);
        c.setKeyPassword(pass);
        c.setKeyAlias(alias);
        
        Assert.assertEquals(host, c.getHost());
        Assert.assertEquals(keystore, c.getKeystore());
        Assert.assertEquals(port, c.getPort());
        Assert.assertEquals(pass, c.getKeyPassword());
        Assert.assertEquals(alias, c.getKeyAlias());
    }

}
