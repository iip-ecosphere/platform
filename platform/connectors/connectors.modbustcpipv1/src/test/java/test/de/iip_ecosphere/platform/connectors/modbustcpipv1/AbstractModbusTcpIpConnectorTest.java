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

import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import test.de.iip_ecosphere.platform.connectors.MachineCommand;

import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusItem;

/**
 * Intermediate class implementing unnecessary methods for MODBUS TCP/IP.
 * 
 * @author Christian Nikolajew
 */
public abstract class AbstractModbusTcpIpConnectorTest 
    extends AbstractModbusInformationModelConnectorTest<ModbusItem>  {
    
    /**
     * Creates an instance.
     * 
     * @param dataType the datatype
     */
    protected AbstractModbusTcpIpConnectorTest(Class<? extends ModbusItem> dataType) {
        super(dataType);
    }

    @Override
    public void initializeModelAccess(ModelAccess access, boolean withNotifications) throws IOException {
        //Not used for MODBUS TCP/IP
    }

    @Override
    public String getVendor(ModelAccess access) throws IOException {
        //Not used for MODBUS TCP/IP
        return null;
    }

    @Override
    public String getQNameVarPowerConsumption() {
        //Not used for MODBUS TCP/IP
        return null;
    }

    @Override
    public String getQNameOperationStartMachine() {
        //Not used for MODBUS TCP/IP
        return null;
    }

    @Override
    public String getQNameOperationStopMachine() {
        //Not used for MODBUS TCP/IP
        return null;
    }

    
    @Override
    public String getQNameVarLotSize() {
        //Not used for MODBUS TCP/IP
        return null;
    }
    
    
    @Override
    public String getTopLevelModelPartName() {
        //Not used for MODBUS TCP/IP
        return null;
    }

    @Override
    public void additionalFromActions(ModelAccess access, MachineCommand data) throws IOException {
        //Not used for MODBUS TCP/IP    
    }
}
