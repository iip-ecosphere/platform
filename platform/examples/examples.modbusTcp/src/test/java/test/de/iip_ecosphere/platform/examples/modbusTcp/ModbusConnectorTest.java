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

package test.de.iip_ecosphere.platform.examples.modbusTcp;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import de.iip_ecosphere.platform.examples.modbusTcp.ModbusServer;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.support.resources.FolderResourceResolver;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import iip.nodes.MyModbusConnExample;
import iip.nodes.MyModbusSentronConnExample;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusTcpIpConnector;

/**
 * Tests the connector parts/plugins for the MODBUS server.
 * 
 * Plan: Parts of class shall be generated from the configuration model when the
 * connector is used in an application.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ModbusConnectorTest {

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

        ConnectorParameter param = MyModbusConnExample.createConnectorParameter();
        ConnectorParameterBuilder builder = ConnectorParameterBuilder.newBuilder(param);
        String serverSettings = EEMFunctionTest.createServerSettings();
        System.out.println(serverSettings);
        builder.setSpecificSetting("SERVER_STRUCTURE", serverSettings);
        param = builder.build();

        server = new ModbusServer(param);
        server.start();

        functionTestManualConnector(param);

        server.stop();

//        String[] args = {};
//        ManualConnector.main(args);

    }

    /**
     * Test for GeneratedConnector.
     * 
     * @throws IOException
     */
    @Test
    public void generatedConnectorTest() throws IOException {

        System.out.println("generatedConnectorTest()");

        ConnectorParameter param = MyModbusConnExample.createConnectorParameter();
        ConnectorParameterBuilder builder = ConnectorParameterBuilder.newBuilder(param);
        String serverSettings = EEMFunctionTest.createServerSettings();
        System.out.println(serverSettings);
        builder.setSpecificSetting("SERVER_STRUCTURE", serverSettings);
        param = builder.build();

        server = new ModbusServer(param);
        server.start();

        functionTestGeneratedConnector(param);

        server.stop();

        // String[] args = {};
        // GeneratedConnector.main(args);
    }

    /**
     * Test for GeneratedConnectorSentron.
     * 
     * @throws IOException
     */
    @Test
    public void generatedConnectorSentronTest() throws IOException {

        System.out.println("generatedConnectorSentronTest()");

        ConnectorParameter param = MyModbusSentronConnExample.createConnectorParameter();
        ConnectorParameterBuilder builder = ConnectorParameterBuilder.newBuilder(param);
        String serverSettings = SentronFunctionTest.createServerSettings();
        System.out.println(serverSettings);
        builder.setSpecificSetting("SERVER_STRUCTURE", serverSettings);
        param = builder.build();

        server = new ModbusServer(param);
        server.start();

        functionTestGeneratedConnectorSentron(param);

        server.stop();

//        String[] args = {}; 
//        GeneratedConnectorSentron.main(args);
    }

    /**
     * Functiontest for ManualConnector.
     * 
     * @throws IOException
     */
    public void functionTestManualConnector(ConnectorParameter param) throws IOException {

        System.out.println("functionTestManualConnector -> start");

        ActiveAasBase.setNotificationMode(NotificationMode.NONE);
        AtomicReference<EEMFunctionTest> md = new AtomicReference<EEMFunctionTest>();

        // Create an instance
        ModbusTcpIpConnector<EEMFunctionTest, EEMFunctionTestRw> connector = EEMFunctionTest
                .createFunctionTestConnector();

        connector.setReceptionCallback(new ReceptionCallback<EEMFunctionTest>() {

            @Override
            public void received(EEMFunctionTest data) {
                md.set(data);
            }

            @Override
            public Class<EEMFunctionTest> getType() {
                return EEMFunctionTest.class;
            }

        });

        connector.connect(param);
        connector.request(true);

        EEMFunctionTest tmp = md.get();

        // Check if the values are 0
        Assert.assertEquals((short) 0, tmp.getDay());
        Assert.assertEquals((short) 0, tmp.getMonth());
        Assert.assertEquals((short) 0, tmp.getYear());
        Assert.assertTrue(tmp.getU12() <= 0.001);
        Assert.assertTrue(tmp.getU23() <= 0.001);
        Assert.assertTrue(tmp.getU31() <= 0.001);

        // Set values
        EEMFunctionTestRw cmd = new EEMFunctionTestRw();
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
        cmd = new EEMFunctionTestRw();
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
    public void functionTestGeneratedConnector(ConnectorParameter param) throws IOException {

        System.out.println("functionTestGeneratedConnector -> start");

        ActiveAasBase.setNotificationMode(NotificationMode.NONE);
        AtomicReference<EEMFunctionTest> md = new AtomicReference<EEMFunctionTest>();

        ModbusTcpIpConnector<EEMFunctionTest, EEMFunctionTestRw> connector = EEMFunctionTest
                .createFunctionTestConnector();

        connector.setReceptionCallback(new ReceptionCallback<EEMFunctionTest>() {

            @Override
            public void received(EEMFunctionTest data) {
                md.set(data);
            }

            @Override
            public Class<EEMFunctionTest> getType() {
                return EEMFunctionTest.class;
            }

        });

        connector.connect(param);
        connector.request(true);

        EEMFunctionTest tmp = md.get();

        // Check if the values are 0
        Assert.assertEquals((short) 0, tmp.getDay());
        Assert.assertEquals((short) 0, tmp.getMonth());
        Assert.assertEquals((short) 0, tmp.getYear());
        Assert.assertTrue(tmp.getU12() <= 0.001);
        Assert.assertTrue(tmp.getU23() <= 0.001);
        Assert.assertTrue(tmp.getU31() <= 0.001);

        // Set values
        EEMFunctionTestRw cmd = new EEMFunctionTestRw();
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
        cmd = new EEMFunctionTestRw();
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

        System.out.println("functionTestGeneratedConnector -> success");
    }

    /**
     * Functiontest for GeneratedConnectorSentron.
     * 
     * @throws IOException
     */
    public void functionTestGeneratedConnectorSentron(ConnectorParameter param) throws IOException {

        System.out.println("functionTestGeneratedConnectorSentron -> start");

        ActiveAasBase.setNotificationMode(NotificationMode.NONE);
        AtomicReference<SentronFunctionTest> md = new AtomicReference<SentronFunctionTest>();

        ModbusTcpIpConnector<SentronFunctionTest, SentronFunctionTestRw> connector = SentronFunctionTest
                .createFunctionTestConnector();

        connector.setReceptionCallback(new ReceptionCallback<SentronFunctionTest>() {

            @Override
            public void received(SentronFunctionTest data) {
                md.set(data);
            }

            @Override
            public Class<SentronFunctionTest> getType() {
                return SentronFunctionTest.class;
            }

        });

        connector.connect(param);
        connector.request(true);

        SentronFunctionTest tmp = md.get();

        // Check if the values are 0
        Assert.assertEquals((int) 0, tmp.getBetriebsstundenzaehler());
        Assert.assertEquals((int) 0, tmp.getImpulszaehler());
        Assert.assertEquals((int) 0, tmp.getUniversalzaehler());
        Assert.assertTrue(tmp.getSpannungL1L3() <= 0.001);
        Assert.assertTrue(tmp.getSpannungL2L3() <= 0.001);
        Assert.assertTrue(tmp.getSpannungL3L1() <= 0.001);

        // Set values
        SentronFunctionTestRw cmd = new SentronFunctionTestRw();
        cmd.setBetriebsstundenzaehler((int) 7);
        cmd.setImpulszaehler((int) 82);
        cmd.setUniversalzaehler((int) 123);
        connector.write(cmd);

        tmp = md.get();

        // Check the values set before
        Assert.assertEquals((int) 7, tmp.getBetriebsstundenzaehler());
        Assert.assertEquals((int) 82, tmp.getImpulszaehler());
        Assert.assertEquals((int) 123, tmp.getUniversalzaehler());
        Assert.assertTrue(tmp.getSpannungL1L3() <= 0.001);
        Assert.assertTrue(tmp.getSpannungL2L3() <= 0.001);
        Assert.assertTrue(tmp.getSpannungL3L1() <= 0.001);

        // Set values back to 0
        cmd = new SentronFunctionTestRw();
        cmd.setBetriebsstundenzaehler((int) 0);
        cmd.setImpulszaehler((int) 0);
        cmd.setUniversalzaehler((int) 0);
        connector.write(cmd);

        tmp = md.get();

        // Check if the values are 0
        Assert.assertEquals((int) 0, tmp.getBetriebsstundenzaehler());
        Assert.assertEquals((int) 0, tmp.getImpulszaehler());
        Assert.assertEquals((int) 0, tmp.getUniversalzaehler());
        Assert.assertTrue(tmp.getSpannungL1L3() <= 0.001);
        Assert.assertTrue(tmp.getSpannungL2L3() <= 0.001);
        Assert.assertTrue(tmp.getSpannungL3L1() <= 0.001);

        System.out.println("functionTestGeneratedConnectorSentron -> success");
    }
}
