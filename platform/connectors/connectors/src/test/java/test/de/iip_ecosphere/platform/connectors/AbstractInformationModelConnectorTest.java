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

package test.de.iip_ecosphere.platform.connectors;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.types.ChannelProtocolAdapter;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import test.de.iip_ecosphere.platform.connectors.MachineCommandInputTranslator.InputCustomizer;
import test.de.iip_ecosphere.platform.connectors.MachineDataOutputTranslator.OutputCustomizer;

/**
 * Implements a re-usable information model connector test.
 * 
 * @param <D> the internal data type of the connector under test
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractInformationModelConnectorTest<D> implements InputCustomizer, OutputCustomizer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractInformationModelConnectorTest.class);
    private Class<? extends D> dataType;
    
    protected enum Step {
        MACHINE_DATA_SENT,
        START_COMMAND_SENT,
        LOT_SIZE_CHANGED,
        STOP_COMMAND_SENT
    }

    /**
     * Creates an instance and sets the internal data type.
     * 
     * @param dataType the internal data type
     */
    protected AbstractInformationModelConnectorTest(Class<? extends D> dataType) {
        this.dataType = dataType;
    }
    
    /**
     * Returns the connector descriptor for {@link #createConnector(ChannelProtocolAdapter)}.
     * 
     * @return the connector descriptor
     */
    protected abstract Class<? extends ConnectorDescriptor> getConnectorDescriptor();

    /**
     * Asserts additional properties for the given step of testing.
     * 
     * @param step the testing step
     * @param received the received machine data from the connector
     */
    protected abstract void assertAdditionalProperties(Step step, MachineData received);
    
    /**
     * Do additional actions after testing.
     * 
     * @param connector the connector instance
     */
    protected abstract void afterActions(Connector<D, Object, MachineData, MachineCommand> connector);

    /**
     * Creates the connector to be tested.
     * 
     * @param adapter the protocol adapter to use
     * @return the connector instance to test
     */
    protected abstract Connector<D, Object, MachineData, MachineCommand> createConnector(
        ProtocolAdapter<D, Object, MachineData, MachineCommand> adapter);
    
    /**
     * Returns the connector parameters for {@link Connector#connect(ConnectorParameter)}.
     * 
     * @return the connector parameters
     */
    protected abstract ConnectorParameter getConnectorParameter();
    
    /**
     * Tests the connector.
     * 
     * @param withNotifications operate with/without notifications (for testing)
     * @throws IOException in case that creating the connector fails
     */
    public void testConnector(boolean withNotifications) throws IOException {
        ConnectorTest.assertDescriptorRegistration(getConnectorDescriptor());
        AtomicReference<MachineData> md = new AtomicReference<MachineData>();
        AtomicInteger count = new AtomicInteger(0);
        
        Connector<D, Object, MachineData, MachineCommand> connector = createConnector(
            new TranslatingProtocolAdapter<D, Object, MachineData, MachineCommand>(
                 new MachineDataOutputTranslator<D>(withNotifications, dataType, this),
                 new MachineCommandInputTranslator<Object>(Object.class, this)));
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
        connector.connect(getConnectorParameter());
        ConnectorTest.assertInstance(connector, true);
        LOGGER.info("Connector '" + connector.getName() + "' started");

        block(count, 2); // init changes powConsumption and lotSize
        
        MachineData tmp = md.get();
        Assert.assertNotNull("We shall have received some data although the machine is not running", tmp);
        Assert.assertEquals(1, tmp.getLotSize());
        Assert.assertTrue(tmp.getPowerConsumption() < 1);
        assertAdditionalProperties(Step.MACHINE_DATA_SENT, tmp);
        
        // try starting the machine
        MachineCommand cmd = new MachineCommand();
        cmd.setStart(true);
        connector.write(cmd);
        
        block(count, 3); // cmd changes powConsuption
        
        tmp = md.get();
        Assert.assertEquals(1, tmp.getLotSize());
        Assert.assertTrue(tmp.getPowerConsumption() > 5);
        assertAdditionalProperties(Step.START_COMMAND_SENT, tmp);
        
        cmd = new MachineCommand();
        cmd.setLotSize(5);
        connector.write(cmd);

        block(count, 4); // cmd changes lotSize

        tmp = md.get();
        Assert.assertEquals(5, tmp.getLotSize());
        Assert.assertTrue(tmp.getPowerConsumption() > 5);
        assertAdditionalProperties(Step.LOT_SIZE_CHANGED, tmp);

        cmd = new MachineCommand();
        cmd.setStop(true);
        connector.write(cmd);

        block(count, 6); // cmd changes powConsuption and lot size

        tmp = md.get();
        Assert.assertEquals(1, tmp.getLotSize());
        Assert.assertTrue(tmp.getPowerConsumption() < 1);
        assertAdditionalProperties(Step.STOP_COMMAND_SENT, tmp);

        ConnectorTest.assertInstance(connector, true);
        connector.disconnect();
        ConnectorTest.assertInstance(connector, false);
        LOGGER.info("Connector '" + connector.getName() + "' disconnected");
        afterActions(connector);
    }


    /**
     * Blocks until a certain number of (accumulated) receptions is reached or fails after 4s.
     * 
     * @param count the counter
     * @param receptions the expected number of receptions 
     */
    protected void block(AtomicInteger count, int receptions) {
        int max = 20; // longer than polling interval in params, 30 may be required depending on machine speed
        while (count.get() < receptions && max > 0) {
            TimeUtils.sleep(200);
            max--;
        }
        Assert.assertTrue("Operation took too long", max > 0);
    }

}
