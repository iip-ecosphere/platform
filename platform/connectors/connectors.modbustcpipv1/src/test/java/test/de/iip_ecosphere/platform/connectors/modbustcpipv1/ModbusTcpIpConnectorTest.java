/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.connectors.modbustcpipv1;

import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusItem;
import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusTcpIpConnector;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;

/**
 * Tests the MODBUS TCP/IP connector (not secure, polling).
 * 
 * @author Christian Nikolajew
 */
public class ModbusTcpIpConnectorTest extends AbstractModbusTcpIpConnectorTest {

    private static TestServer testServer;
    private static final Logger LOGGER = LoggerFactory.getLogger(ModbusTcpIpConnectorTest.class);

    /**
     * Creates an instance of this test.
     */
    public ModbusTcpIpConnectorTest() {
        super(ModbusItem.class);
    }

    /**
     * Sets the test up by starting an embedded MODBUS TCP/IP server.
     */
    @BeforeClass
    public static void init() {
        testServer = new TestServer(false);
        testServer.start();
        LOGGER.info("MODBUS TCP/IP server started at " + testServer.getHost() + ":" + testServer.getPort());
        
    }

    /**
     * Shuts down the test server.
     */
    @AfterClass
    public static void shutdown() {
        testServer.stop();
        LOGGER.info("MODBUS TCP/IP server stopped");
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

    @Override
    protected Class<? extends ConnectorDescriptor> getConnectorDescriptor() {
        return ModbusTcpIpConnector.Descriptor.class;
    }

    @Override
    protected ConnectorParameter getConnectorParameter() {
        Endpoint registryEndpoint = new Endpoint(Schema.TCP, testServer.getHost(), testServer.getPort(), "");
        ConnectorParameterBuilder testParameter = ConnectorParameterBuilder.newBuilder(registryEndpoint);
        testParameter.setApplicationInformation("App_Id", "App_Description");
        testParameter.setEndpointPath(registryEndpoint.getSchema() + ":" + registryEndpoint.getEndpoint());
        
        //So läuft der Test wieder durch, aber das Einstellen der SERVER_STRUCTURE verursacht Fehler...
        String serverStructure = testServer.getServerStructure();
        
        Charset charset = Charset.forName("UTF-8");
        byte[] bytes = serverStructure.getBytes(charset);
        
        String str = new String(bytes, charset);
        
        testParameter.setSpecificSetting("SERVER_STRUCTURE", str);
        
        return testParameter.build();

    }

    @Override
    protected Connector<ModbusItem, Object, ModbusMachineData, ModbusMachineCommand> createConnector(
            ProtocolAdapter<ModbusItem, Object, ModbusMachineData, ModbusMachineCommand> adapter) {

        return new ModbusTcpIpConnector<ModbusMachineData, ModbusMachineCommand>(adapter);
    }
    
    /**
     * Returns the TestServer.
     * 
     * @return the TestServer
     */
    public static TestServer getTestServer() {
        return testServer;
    }
}
