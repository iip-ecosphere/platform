
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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.examples.modbusTcp.ManualConnector;
import de.iip_ecosphere.platform.examples.modbusTcp.ModbusCommandE;
import de.iip_ecosphere.platform.examples.modbusTcp.ModbusDataE;
import de.iip_ecosphere.platform.examples.modbusTcp.ModbusServer;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.support.resources.FolderResourceResolver;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import iip.datatypes.ModbusPhoenixEEM;
import iip.datatypes.ModbusPhoenixRwEEM;
import iip.datatypes.ModbusPhoenixRwEEMImpl;
import iip.datatypes.ModbusSiemensRwSentron;
import iip.datatypes.ModbusSiemensRwSentronImpl;
import iip.datatypes.ModbusSiemensSentron;
import iip.nodes.MyModbusConnExample;
import iip.nodes.MyModbusSentronConnExample;
import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusItem;
import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusTcpIpConnector;
import de.iip_ecosphere.platform.examples.modbusTcp.GeneratedConnector;
import de.iip_ecosphere.platform.examples.modbusTcp.GeneratedConnectorSentron;

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

    }

    /**
     * Test for ManualConnector.
     * 
     * @throws IOException
     */
    @Test
    public void manualConnectorTest() throws IOException {
        
        System.out.println("manualConnectorTest()");
        
        server = new ModbusServer(MyModbusConnExample.createConnectorParameter());
        server.start();
        
        functionTestManualConnector();

        server.stop();
        
        
        String[] args = {}; 
        //ManualConnector.main(args);
        
    }
    
    /**
     * Test for  GeneratedConnector.
     * 
     * @throws IOException
     */
//    @Test
//    public void generatedConnectorTest() throws IOException {
//       
//        System.out.println("generatedConnectorTest()");
//        
//        server = new ModbusServer(MyModbusConnExample.createConnectorParameter());
//        server.start();
//        
//        functionTestGeneratedConnector();
//          
//        server.stop();
//        
//        
//        String[] args = {}; 
//        GeneratedConnector.main(args);
//    }
    
    /**
     * Test for  GeneratedConnectorSentron.
     * 
     * @throws IOException
     */
//    @Test
//    public void generatedConnectorSentronTest() throws IOException {
//        
//        System.out.println("generatedConnectorSentronTest()");
//        
//        server = new ModbusServer(MyModbusSentronConnExample.createConnectorParameter());
//        server.start();
//        
//        functionTestGeneratedConnectorSentron();
//        
//        server.stop();
//        
//        
//        String[] args = {}; 
//        GeneratedConnectorSentron.main(args);
//    }
    
    
    /**
     * Functiontest for ManualConnector.
     * 
     * @throws IOException
     */
    public void functionTestManualConnector() throws IOException {     
 
        System.out.println("functionTestManualConnector -> start");
        
        ActiveAasBase.setNotificationMode(NotificationMode.NONE);
        AtomicReference<ModbusDataE> md = new AtomicReference<ModbusDataE>();
        
        //Create an instance
        Connector<ModbusItem, Object, ModbusDataE, ModbusCommandE> connector =
                ManualConnector.createConnector();
        
        connector.setReceptionCallback(new ReceptionCallback<ModbusDataE>() {

            @Override
            public void received(ModbusDataE data) {
                md.set(data);
            }

            @Override
            public Class<ModbusDataE> getType() {
                return ModbusDataE.class;
            }

        });
        
        connector.connect(MyModbusConnExample.createConnectorParameter());
        connector.request(true);
        
        ModbusDataE tmp = md.get();

        // Check if the values are 0
        Assert.assertEquals((short) 0, tmp.getDay());
        Assert.assertEquals((short) 0, tmp.getMonth());
        Assert.assertEquals((short) 0, tmp.getYear());
        Assert.assertTrue(tmp.getU12() <= 0.001);
        Assert.assertTrue(tmp.getU23() <= 0.001);
        Assert.assertTrue(tmp.getU31() <= 0.001);

        // Set values
        ModbusCommandE cmd = new ModbusCommandE();
        cmd.setDay((short) 27);
        cmd.setMonth((short) 5);
        cmd.setYear((short) 2024);
        connector.write(cmd);

        tmp = md.get();

        // Check the values set before
        Assert.assertEquals((short) 27, tmp.getDay());
        Assert.assertEquals((short) 5, tmp.getMonth());
        Assert.assertEquals((short) 2024, tmp.getYear());
        Assert.assertTrue(tmp.getU12() <= 0.001);
        Assert.assertTrue(tmp.getU23() <= 0.001);
        Assert.assertTrue(tmp.getU31() <= 0.001);

        // Set values back to 0
        cmd = new ModbusCommandE();
        cmd.setDay((short) 0);
        cmd.setMonth((short) 0);
        cmd.setYear((short) 0);
        connector.write(cmd);
        
        tmp = md.get();

        // Check if the values are 0
        Assert.assertEquals((short) 0, tmp.getDay());
        Assert.assertEquals((short) 0, tmp.getMonth());
        Assert.assertEquals((short) 0, tmp.getYear());
        Assert.assertTrue(tmp.getU12() <= 0.001);
        Assert.assertTrue(tmp.getU23() <= 0.001);
        Assert.assertTrue(tmp.getU31() <= 0.001);

        connector.disconnect();
        
        System.out.println("functionTestManualConnector -> success");
    }
    
    /**
     * Functiontest for GeneratedConnector.
     * 
     * @throws IOException
     */
    public void functionTestGeneratedConnector() throws IOException {
        
        System.out.println("functionTestGeneratedConnector -> start");
        
        ActiveAasBase.setNotificationMode(NotificationMode.NONE);
        AtomicReference<ModbusPhoenixEEM> md = new AtomicReference<ModbusPhoenixEEM>();
        
        ReceptionCallback<ModbusPhoenixEEM> cb = new ReceptionCallback<>() {

            @Override
            public void received(ModbusPhoenixEEM data) {
                md.set(data);
                //System.out.println("RECEIVED (" + count.get() + "): " + data);
            }

            @Override
            public Class<ModbusPhoenixEEM> getType() {
                return ModbusPhoenixEEM.class;
            }

        };
        
        ModbusTcpIpConnector<ModbusPhoenixEEM, ModbusPhoenixRwEEM> connector = 
                GeneratedConnector.createPlatformConnector(cb);
        
        connector.connect(MyModbusConnExample.createConnectorParameter());
        connector.request(true);
        
        ModbusPhoenixEEM tmp = md.get();
       
        // Check if the values are 0
        Assert.assertEquals((short) 0, tmp.getDay());
        Assert.assertEquals((short) 0, tmp.getMonth());
        Assert.assertEquals((short) 0, tmp.getYear());
        Assert.assertTrue(tmp.getU12() <= 0.001);
        Assert.assertTrue(tmp.getU23() <= 0.001);
        Assert.assertTrue(tmp.getU31() <= 0.001);
        
        // Set values
        ModbusPhoenixRwEEMImpl cmd = new ModbusPhoenixRwEEMImpl();
        cmd.setDay((short) 27);
        cmd.setMonth((short) 5);
        cmd.setYear((short) 2024);
        connector.write(cmd);
        
        tmp = md.get();
        
        // Check the values set before
        Assert.assertEquals((short) 27, tmp.getDay());
        Assert.assertEquals((short) 5, tmp.getMonth());
        Assert.assertEquals((short) 2024, tmp.getYear());
        Assert.assertTrue(tmp.getU12() <= 0.001);
        Assert.assertTrue(tmp.getU23() <= 0.001);
        Assert.assertTrue(tmp.getU31() <= 0.001);
        
        // Set values back to 0
        cmd = new ModbusPhoenixRwEEMImpl();
        cmd.setDay((short) 0);
        cmd.setMonth((short) 0);
        cmd.setYear((short) 0);
        connector.write(cmd);
        
        tmp = md.get();
        
        // Check if the values are 0
        Assert.assertEquals((short) 0, tmp.getDay());
        Assert.assertEquals((short) 0, tmp.getMonth());
        Assert.assertEquals((short) 0, tmp.getYear());
        Assert.assertTrue(tmp.getU12() <= 0.001);
        Assert.assertTrue(tmp.getU23() <= 0.001);
        Assert.assertTrue(tmp.getU31() <= 0.001);
        
        System.out.println("functionTestGeneratedConnector -> success");
    }
    
    /**
     * Functiontest for GeneratedConnectorSentron.
     * 
     * @throws IOException
     */
    public void functionTestGeneratedConnectorSentron() throws IOException {
        
        System.out.println("functionTestGeneratedConnectorSentron -> start");
        
        ActiveAasBase.setNotificationMode(NotificationMode.NONE);
        AtomicReference<ModbusSiemensSentron> md = new AtomicReference<ModbusSiemensSentron>();

        ReceptionCallback<ModbusSiemensSentron> cb = new ReceptionCallback<>() {

            @Override
            public void received(ModbusSiemensSentron data) {
                md.set(data);
                //System.out.println("RECEIVED (" + count.get() + "): " + data);
            }

            @Override
            public Class<ModbusSiemensSentron> getType() {
                return ModbusSiemensSentron.class;
            }

        };
        
        ModbusTcpIpConnector<ModbusSiemensSentron, ModbusSiemensRwSentron> connector = 
                GeneratedConnectorSentron.createPlatformConnector(cb);
        
        connector.connect(MyModbusSentronConnExample.createConnectorParameter());
        connector.request(true);
        
        ModbusSiemensSentron tmp = md.get();
        
        // Check if the values are 0
        //Assert.assertEquals((int) 0, tmp.getBetriebsstundenzaehler());
        //Assert.assertEquals((int) 0, tmp.getImpulszaehler0());
        //Assert.assertEquals((int) 0, tmp.getUniversalzaehler());
        Assert.assertTrue(tmp.getStromL1() <= 0.001);
        Assert.assertTrue(tmp.getStromL2() <= 0.001);
        Assert.assertTrue(tmp.getStromL3() <= 0.001);
        
        // Set values
        ModbusSiemensRwSentronImpl cmd = new ModbusSiemensRwSentronImpl();
        //cmd.setBetriebsstundenzaehler((int) 7);
        //cmd.setImpulszaehler0((int) 82);
        //cmd.setUniversalzaehler((int) 123);
        connector.write(cmd);
        
        tmp = md.get();
        
        // Check the values set before
        //Assert.assertEquals((int) 7, tmp.getBetriebsstundenzaehler());
        //Assert.assertEquals((int) 82, tmp.getImpulszaehler0());
        //Assert.assertEquals((int) 123, tmp.getUniversalzaehler());
        Assert.assertTrue(tmp.getStromL1() <= 0.001);
        Assert.assertTrue(tmp.getStromL2() <= 0.001);
        Assert.assertTrue(tmp.getStromL3() <= 0.001);
        
        // Set values back to 0
        cmd = new ModbusSiemensRwSentronImpl();
        //cmd.setBetriebsstundenzaehler((int) 0);
        //cmd.setImpulszaehler0((int) 0);
        //cmd.setUniversalzaehler((int) 0);
        connector.write(cmd);
        
        tmp = md.get();
        
        // Check if the values are 0
        //Assert.assertEquals((int) 0, tmp.getBetriebsstundenzaehler());
        //Assert.assertEquals((int) 0, tmp.getImpulszaehler0());
        //Assert.assertEquals((int) 0, tmp.getUniversalzaehler());
        Assert.assertTrue(tmp.getStromL1() <= 0.001);
        Assert.assertTrue(tmp.getStromL2() <= 0.001);
        Assert.assertTrue(tmp.getStromL3() <= 0.001);
        
        
        System.out.println("functionTestGeneratedConnectorSentron -> success");
    }
}
