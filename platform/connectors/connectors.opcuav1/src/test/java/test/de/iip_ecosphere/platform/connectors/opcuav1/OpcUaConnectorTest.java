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

package test.de.iip_ecosphere.platform.connectors.opcuav1;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.NetUtils;
import test.de.iip_ecosphere.platform.connectors.opcuav1.simpleMachineNamespace.Namespace;

/**
 * Tests the OPC UA connector (not secure, polling.
 * 
 * @author Holger Eichelberger, SSE
 */
public class OpcUaConnectorTest extends AbstractOpcUaConnectorTest {
    
    private static TestServer testServer;
    private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaConnectorTest.class);
    
    /**
     * Sets the test up by starting an embedded OPC UA server.
     * 
     * @throws ExecutionException shall not occur
     * @throws InterruptedException shall not occur
     */
    @BeforeClass
    public static void init() throws ExecutionException, InterruptedException {
        setSetup(new NoSecuritySetup("milo", NetUtils.getEphemeralPort(), NetUtils.getEphemeralPort()));
        testServer = new TestServer((server) -> new Namespace(server), getSetup());
        testServer.startup().get();
        LOGGER.info("OPC UA server started");
    }
    
    /**
     * Shuts down the test server.
     * 
     * @throws ExecutionException shall not occur
     * @throws InterruptedException shall not occur
     */
    @AfterClass
    public static void shutdown() throws ExecutionException, InterruptedException {
        if (null != testServer) {
            testServer.shutdown().get();
            LOGGER.info("OPC UA server stopped");
            testServer = null;
        }
        AbstractOpcUaConnectorTest.dispose(); // this is dangerous and shall only be done at the very end
    }

    /**
     * Tests the connector in polling mode.
     * 
     * @throws IOException in case that creating the connector fails
     */
    @Test
    public void testWithPolling() throws IOException {
        testConnector(false);
    }
    
    /**
     * Tests the connector in event-based mode.
     * 
     * @throws IOException in case that creating the connector fails
     */
    @Test
    public void testWithNotifications() throws IOException {
        testConnector(true);
    }

}