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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.opcuav1.DataItem;
import de.iip_ecosphere.platform.connectors.opcuav1.OpcUaConnector;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import test.de.iip_ecosphere.platform.connectors.ConnectorTest;
import test.de.iip_ecosphere.platform.connectors.MachineCommand;
import test.de.iip_ecosphere.platform.connectors.MachineCommandInputTranslator;
import test.de.iip_ecosphere.platform.connectors.MachineData;
import test.de.iip_ecosphere.platform.connectors.MachineDataOutputTranslator;
import test.de.iip_ecosphere.platform.connectors.MachineCommandInputTranslator.InputCustomizer;
import test.de.iip_ecosphere.platform.connectors.MachineDataOutputTranslator.OutputCustomizer;
import test.de.iip_ecosphere.platform.connectors.opcuav1.simpleMachineNamespace.Namespace;
import test.de.iip_ecosphere.platform.connectors.opcuav1.simpleMachineNamespace.VendorStruct;

/**
 * An abstract test setup for the {@code simpleMachineNamespace}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractOpcUaConnectorTest {

    public static final String VENDOR_NAME2 = "PhoenixContact";
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOpcUaConnectorTest.class);
    private static ServerSetup setup;
    private static OpcUaConnector<?, ?> lastConnector;

    private static class Customizer implements InputCustomizer, OutputCustomizer {

        @Override
        public String getQNameOperationStartMachine() {
            return Namespace.QNAME_METHOD_START;
        }

        @Override
        public String getQNameOperationStopMachine() {
            return Namespace.QNAME_METHOD_END;
        }

        @Override
        public String getQNameVarLotSize() {
            return Namespace.QNAME_VAR_LOT_SIZE;
        }

        @Override
        public String getTopLevelModelPartName() {
            return Namespace.QNAME_TOP_FOLDER;
        }

        @Override
        public void additionalFromActions(ModelAccess access, MachineCommand data) throws IOException {
            if (data.isStop()) {
                access.setStruct(Namespace.QNAME_VAR_STRUCT, 
                    new VendorStruct(Namespace.VENDOR_NAME, 2020, true));
            }
            if (data.getLotSize() > 0) {
                access.setStruct(Namespace.QNAME_VAR_STRUCT, 
                    new VendorStruct(VENDOR_NAME2, 2020, true));
            }
        }
        
        @Override
        public void initializeModelAccess(ModelAccess access, boolean withNotifications) throws IOException {
            if (withNotifications) { // for testing
                // access.setDetailNotifiedItem(true); may be set here, then source in "to" above will receive values 
                access.monitor(Namespace.QNAME_VAR_LOT_SIZE, Namespace.QNAME_VAR_POWER_CONSUMPTION);
            }
            access.registerCustomType(VendorStruct.class);
        }
        
        @Override
        public String getVendor(ModelAccess access) throws IOException {
            return access.getStruct(Namespace.QNAME_VAR_STRUCT, VendorStruct.class).getVendor();
        }
        
        @Override
        public String getQNameVarPowerConsumption() {
            return Namespace.QNAME_VAR_POWER_CONSUMPTION;
        }
        
    };
    
    /**
     * Defines the setup instance.
     * 
     * @param instance the setup instance
     */
    protected static void setSetup(ServerSetup instance) {
        setup = instance;
    }
    
    /**
     * Returns the setup instance.
     * 
     * @return the setup instance
     */
    protected static ServerSetup getSetup() {
        return setup;
    }
    
    /**
     * Blocks until a certain number of (accumulated) receptions is reached or fails after 4s.
     * 
     * @param count the counter
     * @param receptions the excpected number of receptions 
     */
    private void block(AtomicInteger count, int receptions) {
        int max = 20; // longer than polling interval in params, 30 may be required depending on machine speed
        while (count.get() < receptions && max > 0) {
            TimeUtils.sleep(200);
            max--;
        }
        Assert.assertTrue("Operation took too long", max > 0);
    }
    
    /**
     * Tests the connector.
     * 
     * @param withNotifications operate with/without notifications (for testing)
     * @throws IOException in case that creating the connector fails
     */
    public void testConnector(boolean withNotifications) throws IOException {
        ConnectorTest.assertDescriptorRegistration(OpcUaConnector.Descriptor.class);
        AtomicReference<MachineData> md = new AtomicReference<MachineData>();
        AtomicInteger count = new AtomicInteger(0);
        
        Customizer customizer = new Customizer();
        OpcUaConnector<MachineData, MachineCommand> connector = new OpcUaConnector<>(
            new TranslatingProtocolAdapter<DataItem, Object, MachineData, MachineCommand>(
                 new MachineDataOutputTranslator<DataItem>(withNotifications, DataItem.class, customizer),
                 new MachineCommandInputTranslator<Object>(Object.class, customizer)));
        ConnectorTest.assertInstance(connector, false);
        ConnectorTest.assertConnectorProperties(connector);
        connector.setReceptionCallback(new ReceptionCallback<MachineData>() {
            
            @Override
            public void received(MachineData data) {
                md.set(data);
                count.incrementAndGet();
            }
            
            @Override
            public Class<MachineData> getType() {
                return MachineData.class;
            }
        });
        connector.connect(setup.getConnectorParameter());
        ConnectorTest.assertInstance(connector, true);
        LOGGER.info("OPC connector started");

        block(count, 2); // init changes powConsumption and lotSize
        
        MachineData tmp = md.get();
        Assert.assertNotNull("We shall have received some data although the machine is not running", tmp);
        Assert.assertEquals(1, tmp.getLotSize());
        Assert.assertTrue(tmp.getPowerConsumption() < 1);
        Assert.assertEquals(Namespace.VENDOR_NAME, tmp.getVendor());
        
        // try starting the machine
        MachineCommand cmd = new MachineCommand();
        cmd.setStart(true);
        connector.write(cmd);
        
        block(count, 3); // cmd changes powConsuption
        
        tmp = md.get();
        Assert.assertEquals(1, tmp.getLotSize());
        Assert.assertTrue(tmp.getPowerConsumption() > 5);
        Assert.assertEquals(Namespace.VENDOR_NAME, tmp.getVendor());
        
        cmd = new MachineCommand();
        cmd.setLotSize(5);
        connector.write(cmd);

        block(count, 4); // cmd changes lotSize

        tmp = md.get();
        Assert.assertEquals(5, tmp.getLotSize());
        Assert.assertTrue(tmp.getPowerConsumption() > 5);
        Assert.assertEquals(VENDOR_NAME2, tmp.getVendor());

        cmd = new MachineCommand();
        cmd.setStop(true);
        connector.write(cmd);

        block(count, 6); // cmd changes powConsuption and lot size

        tmp = md.get();
        Assert.assertEquals(1, tmp.getLotSize());
        Assert.assertTrue(tmp.getPowerConsumption() < 1);
        Assert.assertEquals(Namespace.VENDOR_NAME, tmp.getVendor());

        ConnectorTest.assertInstance(connector, true);
        connector.disconnect();
        ConnectorTest.assertInstance(connector, false);
        LOGGER.info("OPC connector disconnected");
        lastConnector = connector;
    }
    
    /**
     * Disposes the last connector by freeing shared resources. Shall be called only once per test as afterwards
     * the Eclipse Milo does not work anymore.
     */
    static void dispose() {
        if (null != lastConnector) {
            lastConnector.dispose();
        }
    }
    
}
