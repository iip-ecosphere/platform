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

package de.iip_ecosphere.platform.connectors.modbustcpipv1;

import java.util.HashMap;

/**
 * The ModbusItem is the data structure that is read from the machine and
 * written to the machine, to transfer data.
 * 
 * So far only the holding registers from the MODBUS data model are implemented.
 * 
 * @author Christian Nikolajew
 */
public class ModbusItem {

    private HashMap<Integer, Object> registers;

    /**
     * Creates an instance.
     * 
     * @param map the ModbusMap
     */
    public ModbusItem(ModbusMap map) {

        registers = new HashMap<Integer, Object>();

        for (ModbusMap.Entry<String, ModbusVarItem> entry : map.entrySet()) {
            
            ModbusVarItem varItem = entry.getValue();
            registers.put(varItem.getOffset(), 0);
        }
    }
    
    /**
     * Returns the Object at offset.
     * 
     * @param offset of the Object to get
     * @return the Object at offset
     */
    public Object getRegister(int offset) {
        
        return registers.get(offset);
    }
    
    /**
     * Sets the Object at offset to value.
     * 
     * @param offset of the Object to set
     * @param value to set for the offset
     */
    public void setRegister(int offset, Object value) {
        
        registers.put(offset, value);
    }

}
