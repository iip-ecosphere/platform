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
import java.util.concurrent.atomic.AtomicReference;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.examples.modbusTcp.ManualConnector;
import de.iip_ecosphere.platform.examples.modbusTcp.ModbusMachineCommand;
import de.iip_ecosphere.platform.examples.modbusTcp.ModbusMachineData;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.support.resources.FolderResourceResolver;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import iip.nodes.MyModbusConnExample;
import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusItem;
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
        // load generated server resources without copying them
        ResourceLoader.registerResourceResolver(
            new FolderResourceResolver("gen/modbus/SimpleModbusDemoApp/src/main/resources"));
        
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
        //ManualConnector.main();
        
        ActiveAasBase.setNotificationMode(NotificationMode.NONE);
        AtomicReference<ModbusMachineData> md = new AtomicReference<ModbusMachineData>();
        
        //Create an instance
        Connector<ModbusItem, Object, ModbusMachineData, ModbusMachineCommand> connector =
                ManualConnector.createConnector();
        
        connector.setReceptionCallback(new ReceptionCallback<ModbusMachineData>() {

            @Override
            public void received(ModbusMachineData data) {
                System.out.println("RECEIVED " + data);
                md.set(data);
            }

            @Override
            public Class<ModbusMachineData> getType() {
                return ModbusMachineData.class;
            }

        });
        
        connector.connect(MyModbusConnExample.createConnectorParameter());
        connector.request(true);
        
        ModbusMachineData tmp = md.get();

        // Check if the values are 0
        Assert.assertEquals((int) 0, tmp.getValue("Data"));
        Assert.assertEquals((int) 0, tmp.getValue("I1"));
        Assert.assertEquals((int) 0, tmp.getValue("S1"));
        Assert.assertEquals((int) 0, tmp.getValue("V1"));

        // Set values
        ModbusMachineCommand cmd = new ModbusMachineCommand();
        cmd.set("Data", 1);
        cmd.set("I1", 9999);
        cmd.set("S1", 123456789);
        cmd.set("V1", -512);
        connector.write(cmd);

        tmp = md.get();

        // Check the values set before
        Assert.assertEquals((int) 1, tmp.getValue("Data"));
        Assert.assertEquals((int) 9999, tmp.getValue("I1"));
        Assert.assertEquals((int) 123456789, tmp.getValue("S1"));
        Assert.assertEquals((int) -512, tmp.getValue("V1"));

        // Set values back to 0
        cmd = new ModbusMachineCommand();
        cmd.set("Data", (int) 0);
        cmd.set("I1", (int) 0);
        cmd.set("S1", (int) 0);
        cmd.set("V1", (int) 0);
        connector.write(cmd);
        
        tmp = md.get();

        // Check if the values are 0
        Assert.assertEquals((int) 0, tmp.getValue("Data"));
        Assert.assertEquals((int) 0, tmp.getValue("I1"));
        Assert.assertEquals((int) 0, tmp.getValue("S1"));
        Assert.assertEquals((int) 0, tmp.getValue("V1"));

        connector.disconnect();
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
