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

    private Server qpid;

    static {
        addPluginLocation("tests", "test.amqp.qpid", true);
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
        if (null != qpid) {
            Server.stop(qpid, true);
            qpid = null;
        }
    }
    
    /**
     * Sets the instance, ensuring that if there is already an instance that it is stopped.
     * 
     * @param instance the new instance
     * @return {@code instance}
     */
    public Server setQpid(Server instance) {
        Server.stop(qpid, true);
        qpid = instance;
        return qpid;
    }

    /**
     * Sets and starts the instance, ensuring that if there is already an instance that it is stopped.
     * 
     * @param instance the new instance
     * @return {@code instance}
     */
    public Server setAndStartQpid(Server instance) {
        Server result = setQpid(instance);
        result.start();
        return result;
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
