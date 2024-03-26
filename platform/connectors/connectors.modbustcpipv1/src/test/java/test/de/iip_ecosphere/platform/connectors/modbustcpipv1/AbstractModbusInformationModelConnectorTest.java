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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import test.de.iip_ecosphere.platform.connectors.ConnectorTest;
import test.de.iip_ecosphere.platform.connectors.MachineCommandInputTranslator.InputCustomizer;
import test.de.iip_ecosphere.platform.connectors.MachineDataOutputTranslator.OutputCustomizer;

/**
 * Implements a MODBUS TCP/IP connector test.
 * 
 * @author Christian Nikolajew
 *
 * @param <D> the internal data type of the connector under test
 */
public abstract class AbstractModbusInformationModelConnectorTest<D> implements InputCustomizer, OutputCustomizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractModbusInformationModelConnectorTest.class);
    private Class<? extends D> dataType;

    /**
     * Creates an instance and sets the internal data type.
     * 
     * @param dataType the internal data type
     */
    protected AbstractModbusInformationModelConnectorTest(Class<? extends D> dataType) {
        this.dataType = dataType;
    }

    /**
     * Tests the connector.
     * 
     * @param withNotifications operate with/without notifications (for testing)
     * @throws IOException in case that creating the connector fails
     */
    public void testConnector(boolean withNotifications) throws IOException {
        ConnectorTest.assertDescriptorRegistration(getConnectorDescriptor());
        AtomicReference<ModbusMachineData> md = new AtomicReference<ModbusMachineData>();
        AtomicInteger count = new AtomicInteger(0);

        Connector<D, Object, ModbusMachineData, ModbusMachineCommand> connector = createConnector(
                new TranslatingProtocolAdapter<D, Object, ModbusMachineData, ModbusMachineCommand>(
                        new ModbusMachineDataOutputTranslator<D>(withNotifications, dataType, this),
                        new ModbusMachineCommandInputTranslator<Object>(Object.class)));
                
        ConnectorTest.assertInstance(connector, false);
        ConnectorTest.assertConnectorProperties(connector);
        connector.setReceptionCallback(new ReceptionCallback<ModbusMachineData>() {

            @Override
            public void received(ModbusMachineData data) {
                md.set(data);
                count.incrementAndGet();
            }

            @Override
            public Class<ModbusMachineData> getType() {
                return ModbusMachineData.class;
            }
        });
        
        connector.connect(getConnectorParameter());
        ConnectorTest.assertInstance(connector, true);
        LOGGER.info("Connector '" + connector.getName() + "' started");
        
        block(count, 2);
        
        ModbusMachineData tmp = md.get();
        Assert.assertNotNull("We shall have received some data although the machine is not running", tmp);
        
        //All values should be 0
        Assert.assertEquals(0, tmp.getShortValue());
        Assert.assertEquals(0, tmp.getIntValue());
        Assert.assertEquals(0, tmp.getLongValue());
        
        Assert.assertTrue(tmp.getFloatValue() < 1);
        Assert.assertTrue(tmp.getDoubleValue() < 1);
            
        /*
        System.out.println(tmp.getShortValue());
        System.out.println(tmp.getIntValue());
        System.out.println(tmp.getLongValue());
        
        System.out.println(tmp.getFloatValue());
        System.out.println(tmp.getDoubleValue());
        */
        
        testShort(connector, count, md);
        testInt(connector, count, md);
        testFloat(connector, count, md);
        testLong(connector, count, md);
        testDouble(connector, count, md);
        testAllToZero(connector, count, md);
        
        ConnectorTest.assertInstance(connector, true);
        connector.disconnect();
        ConnectorTest.assertInstance(connector, false);
        LOGGER.info("Connector '" + connector.getName() + "' disconnected");
    }

    /**
     * Tries to write a short value.
     * 
     * @param connector the connector
     * @param count the test counter
     * @param md the machine data
     * @throws IOException if an I/O problem occurs
     */
    private void testShort(Connector<D, Object, ModbusMachineData, ModbusMachineCommand> connector, 
        AtomicInteger count, AtomicReference<ModbusMachineData> md) throws IOException {
        // try to write a short value to the machine
        ModbusMachineCommand cmd = new ModbusMachineCommand();
        cmd.setShortValue((short) 1);
        connector.write(cmd);
        
        block(count, 3);
        
        ModbusMachineData tmp = md.get();
        
        Assert.assertEquals(1, tmp.getShortValue());
        Assert.assertEquals(0, tmp.getIntValue());
        Assert.assertEquals(0, tmp.getLongValue());
        
        Assert.assertTrue(tmp.getFloatValue() < 1);
        Assert.assertTrue(tmp.getDoubleValue() < 1);
        
        /*
        System.out.println(tmp.getShortValue());
        System.out.println(tmp.getIntValue());
        System.out.println(tmp.getLongValue());
        
        System.out.println(tmp.getFloatValue());
        System.out.println(tmp.getDoubleValue());
        */
    }

    /**
     * Tries to write an int value.
     * 
     * @param connector the connector
     * @param count the test counter
     * @param md the machine data
     * @throws IOException if an I/O problem occurs
     */
    private void testInt(Connector<D, Object, ModbusMachineData, ModbusMachineCommand> connector, 
        AtomicInteger count, AtomicReference<ModbusMachineData> md) throws IOException {
        // try to write a int value to the machine
        ModbusMachineCommand cmd = new ModbusMachineCommand();
        cmd.setIntValue(5);
        connector.write(cmd);
        
        block(count, 4);
        
        ModbusMachineData tmp = md.get();
        
        Assert.assertEquals(1, tmp.getShortValue());
        Assert.assertEquals(5, tmp.getIntValue());
        Assert.assertEquals(0, tmp.getLongValue());
        
        Assert.assertTrue(tmp.getFloatValue() < 1);
        Assert.assertTrue(tmp.getDoubleValue() < 1);
        
        /*
        System.out.println(tmp.getShortValue());
        System.out.println(tmp.getIntValue());
        System.out.println(tmp.getLongValue());
        
        System.out.println(tmp.getFloatValue());
        System.out.println(tmp.getDoubleValue());
        */
    }

    /**
     * Tries to write a float value.
     * 
     * @param connector the connector
     * @param count the test counter
     * @param md the machine data
     * @throws IOException if an I/O problem occurs
     */
    private void testFloat(Connector<D, Object, ModbusMachineData, ModbusMachineCommand> connector, 
        AtomicInteger count, AtomicReference<ModbusMachineData> md) throws IOException {
        // try to write a float value to the machine
        ModbusMachineCommand cmd = new ModbusMachineCommand();
        cmd.setFloatValue((float) 17.23);
        connector.write(cmd);
        
        block(count, 5);
        
        ModbusMachineData tmp = md.get();
        
        Assert.assertEquals(1, tmp.getShortValue());
        Assert.assertEquals(5, tmp.getIntValue());
        Assert.assertEquals(0, tmp.getLongValue());
        
        Assert.assertTrue(tmp.getFloatValue() > 17);
        Assert.assertTrue(tmp.getDoubleValue() < 1);
        
        /*
        System.out.println(tmp.getShortValue());
        System.out.println(tmp.getIntValue());
        System.out.println(tmp.getLongValue());
        
        System.out.println(tmp.getFloatValue());
        System.out.println(tmp.getDoubleValue());
        */
    }

    /**
     * Tries to write a long value.
     * 
     * @param connector the connector
     * @param count the test counter
     * @param md the machine data
     * @throws IOException if an I/O problem occurs
     */
    private void testLong(Connector<D, Object, ModbusMachineData, ModbusMachineCommand> connector, 
        AtomicInteger count, AtomicReference<ModbusMachineData> md) throws IOException {
        //try to write a long value to the machine
        ModbusMachineCommand cmd = new ModbusMachineCommand();
        cmd.setLongValue((long) 21);
        connector.write(cmd);
        
        block(count, 6);
        
        ModbusMachineData tmp = md.get();
        
        Assert.assertEquals(1, tmp.getShortValue());
        Assert.assertEquals(5, tmp.getIntValue());
        Assert.assertEquals(21, tmp.getLongValue());
        
        Assert.assertTrue(tmp.getFloatValue() > 17);
        Assert.assertTrue(tmp.getDoubleValue() < 1);
        
        /*
        System.out.println(tmp.getShortValue());
        System.out.println(tmp.getIntValue());
        System.out.println(tmp.getLongValue());
        
        System.out.println(tmp.getFloatValue());
        System.out.println(tmp.getDoubleValue());
        */
    }

    /**
     * Tries to write a double value.
     * 
     * @param connector the connector
     * @param count the test counter
     * @param md the machine data
     * @throws IOException if an I/O problem occurs
     */
    private void testDouble(Connector<D, Object, ModbusMachineData, ModbusMachineCommand> connector, 
        AtomicInteger count, AtomicReference<ModbusMachineData> md) throws IOException {
        // try to write a double value to the machine
        ModbusMachineCommand cmd = new ModbusMachineCommand();
        cmd.setDoubleValue(12345.6789);
        connector.write(cmd);
        
        block(count, 7);
        
        ModbusMachineData tmp = md.get();
        
        Assert.assertEquals(1, tmp.getShortValue());
        Assert.assertEquals(5, tmp.getIntValue());
        Assert.assertEquals(21, tmp.getLongValue());
        
        Assert.assertTrue(tmp.getFloatValue() > 17);
        Assert.assertTrue(tmp.getDoubleValue() > 12345);
        
        /*
        System.out.println(tmp.getShortValue());
        System.out.println(tmp.getIntValue());
        System.out.println(tmp.getLongValue());
        
        System.out.println(tmp.getFloatValue());
        System.out.println(tmp.getDoubleValue());
        */
    }

    /**
     * Tries to write all values to 0.
     * 
     * @param connector the connector
     * @param count the test counter
     * @param md the machine data
     * @throws IOException if an I/O problem occurs
     */
    private void testAllToZero(Connector<D, Object, ModbusMachineData, ModbusMachineCommand> connector, 
        AtomicInteger count, AtomicReference<ModbusMachineData> md) throws IOException {
        // try to write all values to 0
        ModbusMachineCommand cmd = new ModbusMachineCommand();
        cmd.setShortValue((short) 0);
        cmd.setIntValue(0);
        cmd.setFloatValue(0);
        cmd.setLongValue(0);
        cmd.setDoubleValue(0);
        connector.write(cmd);
        
        block(count, 7); 
        
        ModbusMachineData tmp = md.get();
        
        Assert.assertEquals(0, tmp.getShortValue());
        Assert.assertEquals(0, tmp.getIntValue());
        Assert.assertEquals(0, tmp.getLongValue());
        
        Assert.assertTrue(tmp.getFloatValue() < 1);
        Assert.assertTrue(tmp.getDoubleValue() < 1);
            
        /*
        System.out.println(tmp.getShortValue());
        System.out.println(tmp.getIntValue());
        System.out.println(tmp.getLongValue());
        
        System.out.println(tmp.getFloatValue());
        System.out.println(tmp.getDoubleValue());
        */
    }
    
    /**
     * Blocks until a certain number of (accumulated) receptions is reached or fails after 4s.
     * 
     * @param count the counter
     * @param receptions the expected number of receptions 
     */
    protected void block(AtomicInteger count, int receptions) {
        int max = 35; // longer than polling interval in params, 30 may be required depending on machine speed
        while (count.get() < receptions && max > 0) {
            TimeUtils.sleep(200);
            max--;
        }
        Assert.assertTrue("Operation took too long", max > 0);
    }

    /**
     * Returns the connector descriptor for {@link #createConnector(ProtocolAdapter)}.
     * 
     * @return the connector descriptor
     */
    protected abstract Class<? extends ConnectorDescriptor> getConnectorDescriptor();

    /**
     * Creates the connector to be tested.
     * 
     * @param adapter the protocol adapter to use
     * @return the connector instance to test
     */
    protected abstract Connector<D, Object, ModbusMachineData, ModbusMachineCommand> createConnector(
            ProtocolAdapter<D, Object, ModbusMachineData, ModbusMachineCommand> adapter);

    /**
     * Returns the connector parameters for {@link Connector#connect(ConnectorParameter)}.
     * 
     * @return the connector parameters
     */
    protected abstract ConnectorParameter getConnectorParameter();
}
