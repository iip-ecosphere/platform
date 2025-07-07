/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.transport;

import org.junit.After;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import test.de.iip_ecosphere.platform.support.aas.TestWithPlugin;

/**
 * Adds QPID as plugin into the tests.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestWithQpid extends TestWithPlugin {

    protected Server qpid;

    static {
        addPluginLocation("tests", "test.amqp.qpid", "qpid", true);
    }
    
    /**
     * Adds the Qpid plugin (if inheriting is not possible).
     */
    public static void addPlugin() {
        // implicitly triggers the static initializer
    }

    /**
     * Shuts down qpid.
     */
    @After
    public void stopQpid() {
        Server.stop(qpid, true);
        qpid = null;
    }
    
    /**
     * Creates a qpid instance via plugin.
     * 
     * @param broker the server address
     * @return the instance
     */
    public static Server fromPlugin(ServerAddress broker) {
        return TestServerBuilder.fromPlugin("test-qpid", broker);
    }
    
}
