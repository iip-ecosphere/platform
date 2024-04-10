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

import java.util.ArrayList;

import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusKeys;
import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusVarItem;

/**
 * Machine data from the ModbusTcpIpModelAccess.
 * 
 * @author Christian Nikolajew
 */
public class ModbusMachineData {
    
    private ArrayList<ModbusVarItem> valueTypesAndOffsets;
    private ArrayList<Object> values;
    
    /**
     * Creates an instance.
     */
    public ModbusMachineData() {
        
        valueTypesAndOffsets = new ArrayList<ModbusVarItem>();
        values = new ArrayList<Object>();
    }
    
    /**
     * Adds a value to the ModbusMachineData.
     * 
     * @param valueTypeAndOffset to add
     * @param value as Object
     */
    public void addValue(ModbusVarItem valueTypeAndOffset, Object value) {
        
        valueTypesAndOffsets.add(valueTypeAndOffset);
        values.add(value);

    }
    
    /**
     * Returns the value for a given key.
     * 
     * @param key
     * @return the value for key
     */
    public Object getValue(String key) {
        
        String[] keys = ModbusKeys.getKeys();
        
        Object result = new Object();
        
        for (int i = 0; i < keys.length; i++) {
            
            if (key.equals(keys[i])) {
                
                result = values.get(i);
            }
        }
        
        return result;
    }
    
}
