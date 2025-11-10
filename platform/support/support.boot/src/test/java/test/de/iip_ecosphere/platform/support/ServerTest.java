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

package test.de.iip_ecosphere.platform.support;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import de.iip_ecosphere.platform.support.Server;
import org.junit.Assert;

/**
 * Tests {@link Server}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServerTest {

    /**
     * Tests {@link Server}.
     */
    @Test
    public void testServer() {
        AtomicBoolean called = new AtomicBoolean();
        Server server = new Server() {

            @Override
            public Server start() {
                return this;
            }

            @Override
            public void stop(boolean dispose) {
                called.set(true);
            }
            
        };
        Assert.assertTrue(server == server.start());
        
        Assert.assertEquals(false, called.get());
        Server.stop(null, false);
        Assert.assertEquals(false, called.get());
        Server.stop(server, false);
        Assert.assertEquals(true, called.get());
    }

}
