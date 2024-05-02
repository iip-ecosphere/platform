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

package test.de.iip_ecosphere.platform.examples.modbusTcp;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.examples.modbusTcp.ManualConnector;
import de.iip_ecosphere.platform.examples.modbusTcp.GeneratedConnector;

/**
 * Tests the connector parts/plugins for the MODBUS server.
 * 
 * Plan: Parts of class shall be generated from the configuration model when the
 * connector is used in an application.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ModbusConnectorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModbusConnectorTest.class);
    private static ModbusServer server;

    /**
     * Sets the test up by starting an embedded MODBUS TCP/IP server.
     */
    @BeforeClass
    public static void init() {

        server = new ModbusServer();
        server.start();
        LOGGER.info("MODBUS TCP/IP server started");
    }

    /**
     * Shuts down the server.
     */
    @AfterClass
    public static void shutdown() {
        
        server.stop();
        LOGGER.info("MODBUS TCP/IP server stopped");
    }
    
    /**
     * Test for ManualConnector.
     * 
     * @throws IOException
     */
    @Test
    public void testManualConnector() throws IOException {
        System.out.println("testManualConnector()");
        ManualConnector.main();
    }
    
    /**
     * Test for  GeneratedConnector.
     * 
     * @throws IOException
     */
    @Test
    public void testGeneratedConnector() throws IOException {
        String[] args = {}; 
        System.out.println("testGeneratedConnector()");
        GeneratedConnector.main(args);
    }
    
}
