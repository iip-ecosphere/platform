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
import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusItem;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
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

    /**
     * Creates an instance and sets the internal data type.
     * 
     * @param dataType the internal data type
     */
    protected AbstractModbusInformationModelConnectorTest() {

    }

    /**
     * Tests the connector.
     * 
     * @param withNotifications operate with/without notifications (for testing)
     * @throws IOException in case that creating the connector fails
     */
    public void testConnector(boolean withNotifications) throws IOException {
        ActiveAasBase.setNotificationMode(NotificationMode.NONE);
        ConnectorTest.assertDescriptorRegistration(getConnectorDescriptor());

        AtomicReference<ModbusDataC> md = new AtomicReference<ModbusDataC>();
        AtomicInteger count = new AtomicInteger(0);

        Connector<ModbusItem, Object, ModbusDataC, ModbusCommandC> connector = createConnector(
                new TranslatingProtocolAdapter<ModbusItem, Object, ModbusDataC, ModbusCommandC>(
                        new ModbusDataCOutputTranslator<ModbusItem>(false, ModbusItem.class),
                        new ModbusCommandCInputTranslator<Object>(Object.class)));

        ConnectorTest.assertInstance(connector, false);
        ConnectorTest.assertConnectorProperties(connector);
        connector.setReceptionCallback(new ReceptionCallback<ModbusDataC>() {

            @Override
            public void received(ModbusDataC data) {
                md.set(data);
                count.incrementAndGet();
            }

            @Override
            public Class<ModbusDataC> getType() {
                return ModbusDataC.class;
            }
        });

        connector.connect(getConnectorParameter());
        ConnectorTest.assertInstance(connector, true);
        LOGGER.info("Connector '" + connector.getName() + "' started");

        block(count, 2);

        ModbusDataC tmp = md.get();
        Assert.assertNotNull("We shall have received some data although the machine is not running", tmp);

        // All values should be 0
        Assert.assertEquals((short) 0, tmp.getShort());
        Assert.assertEquals((int) 0, tmp.getInteger());
        Assert.assertTrue((float) tmp.getFloat() < 1);
        Assert.assertEquals((long) 0, tmp.getLong());
        Assert.assertTrue((double) tmp.getDouble() < 1);

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
     * @param count     the test counter
     * @param md        the machine data
     * @throws IOException if an I/O problem occurs
     */
    private void testShort(Connector<ModbusItem, Object, ModbusDataC, ModbusCommandC> 
        connector, AtomicInteger count, AtomicReference<ModbusDataC> md) throws IOException {

        // try to write a short value to the machine
        Object value = (short) 1;

        ModbusCommandC cmd = new ModbusCommandC();
        cmd.setShort((short) value);
        connector.write(cmd);

        block(count, 3);

        ModbusDataC tmp = md.get();
        
        Assert.assertEquals((short) value, tmp.getShort());
        Assert.assertEquals((int) 0, tmp.getInteger());
        Assert.assertTrue((float) tmp.getFloat() < 1);
        Assert.assertEquals((long) 0, tmp.getLong());
        Assert.assertTrue((double) tmp.getDouble() < 1);
    }

    /**
     * Tries to write an int value.
     * 
     * @param connector the connector
     * @param count     the test counter
     * @param md        the machine data
     * @throws IOException if an I/O problem occurs
     */
    private void testInt(Connector<ModbusItem, Object, ModbusDataC, ModbusCommandC> connector, AtomicInteger count,
            AtomicReference<ModbusDataC> md) throws IOException {

        // try to write a int value to the machine
        Object value =  (int) 5;

        ModbusCommandC cmd = new ModbusCommandC();
        cmd.setInteger((int) value);
        connector.write(cmd);

        block(count, 4);

        ModbusDataC tmp = md.get();
        
        Assert.assertEquals((short) 1, tmp.getShort());
        Assert.assertEquals((int) value, tmp.getInteger());
        Assert.assertTrue((float) tmp.getFloat() < 1);
        Assert.assertEquals((long) 0, tmp.getLong());
        Assert.assertTrue((double) tmp.getDouble() < 1);

    }

    /**
     * Tries to write a float value.
     * 
     * @param connector the connector
     * @param count     the test counter
     * @param md        the machine data
     * @throws IOException if an I/O problem occurs
     */
    private void testFloat(Connector<ModbusItem, Object, ModbusDataC, ModbusCommandC> connector, AtomicInteger count,
            AtomicReference<ModbusDataC> md) throws IOException {

        // try to write a float value to the machine
        Object value = (float) 17.23;

        ModbusCommandC cmd = new ModbusCommandC();
        cmd.setFloat((float) value);
        connector.write(cmd);

        block(count, 5);

        ModbusDataC tmp = md.get();

        Assert.assertEquals((short) 1, tmp.getShort());
        Assert.assertEquals((int) 5, tmp.getInteger());
        Assert.assertTrue((float) tmp.getFloat() > 17);
        Assert.assertEquals((long) 0, tmp.getLong());
        Assert.assertTrue((double) tmp.getDouble() < 1);

    }

    /**
     * Tries to write a long value.
     * 
     * @param connector the connector
     * @param count     the test counter
     * @param md        the machine data
     * @throws IOException if an I/O problem occurs
     */
    private void testLong(Connector<ModbusItem, Object, ModbusDataC, ModbusCommandC> connector, AtomicInteger count,
            AtomicReference<ModbusDataC> md) throws IOException {

        // try to write a long value to the machine
        Object value = (long) 21;

        ModbusCommandC cmd = new ModbusCommandC();
        cmd.setLong((long) value);
        connector.write(cmd);

        block(count, 6);

        ModbusDataC tmp = md.get();

        Assert.assertEquals((short) 1, tmp.getShort());
        Assert.assertEquals((int) 5, tmp.getInteger());
        Assert.assertTrue((float) tmp.getFloat() > 17);
        Assert.assertEquals((long) value, tmp.getLong());
        Assert.assertTrue((double) tmp.getDouble() < 1);

    }

    /**
     * Tries to write a double value.
     * 
     * @param connector the connector
     * @param count     the test counter
     * @param md        the machine data
     * @throws IOException if an I/O problem occurs
     */
    private void testDouble(Connector<ModbusItem, Object, ModbusDataC, ModbusCommandC> connector,
            AtomicInteger count, AtomicReference<ModbusDataC> md) throws IOException {

        // try to write a double value to the machine
        Object value = (double) 12345.6789;

        ModbusCommandC cmd = new ModbusCommandC();
        cmd.setDouble((double) value);
        connector.write(cmd);

        block(count, 7);

        ModbusDataC tmp = md.get();
        Assert.assertEquals((short) 1, tmp.getShort());
        Assert.assertEquals((int) 5, tmp.getInteger());
        Assert.assertTrue((float) tmp.getFloat() > 17);
        Assert.assertEquals((long) 21, tmp.getLong());
        Assert.assertTrue((double) tmp.getDouble() > 12345);

    }

    /**
     * Tries to write all values to 0.
     * 
     * @param connector the connector
     * @param count     the test counter
     * @param md        the machine data
     * @throws IOException if an I/O problem occurs
     */
    private void testAllToZero(Connector<ModbusItem, Object, ModbusDataC, ModbusCommandC> connector,
            AtomicInteger count, AtomicReference<ModbusDataC> md) throws IOException {

        // try to write all values to 0
        ModbusCommandC cmd = new ModbusCommandC();
        cmd.setShort((short) 0);
        cmd.setInteger((int) 0);
        cmd.setFloat((float) 0);
        cmd.setLong((long) 0);
        cmd.setDouble((double) 0);

        connector.write(cmd);

        block(count, 7);

        ModbusDataC tmp = md.get();

        Assert.assertEquals((short) 0, tmp.getShort());
        Assert.assertEquals((int) 0, tmp.getInteger());
        Assert.assertTrue((float) tmp.getFloat() < 1);
        Assert.assertEquals((long) 0, tmp.getLong());
        Assert.assertTrue((double) tmp.getDouble() < 1);

    }

    /**
     * Blocks until a certain number of (accumulated) receptions is reached or fails
     * after 4s.
     * 
     * @param count      the counter
     * @param receptions the expected number of receptions
     */
    protected void block(AtomicInteger count, int receptions) {
        int max = 500; // longer than polling interval in params, 30 may be required depending on
                      // machine speed
        while (count.get() < receptions && max > 0) {
            TimeUtils.sleep(200);
            max--;
        }
        Assert.assertTrue("Operation took too long", max > 0);
    }

    /**
     * Returns the connector descriptor for
     * {@link #createConnector(ProtocolAdapter)}.
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
    protected abstract Connector<ModbusItem, Object, ModbusDataC, ModbusCommandC> createConnector(
            ProtocolAdapter<ModbusItem, Object, ModbusDataC, ModbusCommandC> adapter);

    /**
     * Returns the connector parameters for
     * {@link Connector#connect(ConnectorParameter)}.
     * 
     * @return the connector parameters
     */
    protected abstract ConnectorParameter getConnectorParameter();
}
