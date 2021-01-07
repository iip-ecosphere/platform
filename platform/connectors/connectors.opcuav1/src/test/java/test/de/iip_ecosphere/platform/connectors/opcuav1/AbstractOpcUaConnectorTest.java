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

import org.junit.Assert;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.opcuav1.DataItem;
import de.iip_ecosphere.platform.connectors.opcuav1.OpcUaConnector;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import test.de.iip_ecosphere.platform.connectors.AbstractInformationModelConnectorTest;
import test.de.iip_ecosphere.platform.connectors.MachineCommand;
import test.de.iip_ecosphere.platform.connectors.MachineData;
import test.de.iip_ecosphere.platform.connectors.opcuav1.simpleMachineNamespace.Namespace;
import test.de.iip_ecosphere.platform.connectors.opcuav1.simpleMachineNamespace.VendorStruct;

/**
 * An abstract test setup for the {@code simpleMachineNamespace}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractOpcUaConnectorTest extends AbstractInformationModelConnectorTest<DataItem> {

    public static final String VENDOR_NAME2 = "PhoenixContact";
    private static ServerSetup setup;
    private static Connector<DataItem, Object, MachineData, MachineCommand> lastConnector;
    
    /**
     * Creates an instance of this test.
     */
    public AbstractOpcUaConnectorTest() {
        super(DataItem.class);
    }
    
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
     * Disposes the last connector by freeing shared resources. Shall be called only once per test as afterwards
     * the Eclipse Milo does not work anymore.
     */
    static void dispose() {
        if (null != lastConnector) {
            lastConnector.dispose();
        }
    }
    
    @Override
    protected Class<? extends ConnectorDescriptor> getConnectorDescriptor() {
        return OpcUaConnector.Descriptor.class;
    }

    @Override
    protected Connector<DataItem, Object, MachineData, MachineCommand> createConnector(
        ProtocolAdapter<DataItem, Object, MachineData, MachineCommand> adapter) {
        return new OpcUaConnector<MachineData, MachineCommand>(adapter);
    }

    @Override
    protected ConnectorParameter getConnectorParameter() {
        return setup.getConnectorParameter();
    }

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

    @Override
    public void assertAdditionalProperties(Step step, MachineData received) {
        switch (step) {
        case MACHINE_DATA_SENT:
            Assert.assertEquals(Namespace.VENDOR_NAME, received.getVendor());
            break;
        case START_COMMAND_SENT:
            Assert.assertEquals(Namespace.VENDOR_NAME, received.getVendor());
            break;
        case LOT_SIZE_CHANGED:
            Assert.assertEquals(VENDOR_NAME2, received.getVendor());
            break;
        case STOP_COMMAND_SENT:
            Assert.assertEquals(Namespace.VENDOR_NAME, received.getVendor());
            break;
        default:
            break;
        }
    }

    @Override
    public void afterActions(Connector<DataItem, Object, MachineData, MachineCommand> connector) {
        lastConnector = connector;
    }

}
