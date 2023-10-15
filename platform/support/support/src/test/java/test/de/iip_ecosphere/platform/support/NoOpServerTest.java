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

package test.de.iip_ecosphere.platform.support;

import org.junit.Test;

import de.iip_ecosphere.platform.support.NoOpServer;
import de.iip_ecosphere.platform.support.Server;
import org.junit.Assert;

/**
 * Tests the {@code NoOpServer}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class NoOpServerTest {
    
    /**
     * Tests the basic "server" functions.
     */
    @Test
    public void test() {
        Server server = new NoOpServer();
        Assert.assertEquals(server, server.start());
        server.stop(true);
        Server.stop(server, false); // does not matter
    }

}
