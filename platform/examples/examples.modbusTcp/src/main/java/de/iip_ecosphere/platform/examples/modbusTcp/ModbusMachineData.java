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

package de.iip_ecosphere.platform.examples.modbusTcp;

import java.util.HashMap;
import java.util.Set;

import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusMap;
import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusVarItem;

/**
 * Machine data from the ModbusTcpIpModelAccess.
 * 
 * @author Christian Nikolajew
 */
public class ModbusMachineData {
    
    private ModbusMap serverMap;
    private HashMap<String, Object> mapValues;
    
    /**
     * Creates an instance.
     * 
     * @param map the ModbusMap
     */
    public ModbusMachineData(ModbusMap map) {
        
        this.serverMap = map;
        mapValues = new HashMap<String, Object>();
    }
    
    /**
     * Adds a value to the ModbusMachineData by given key.
     * 
     * @param key for the value to add
     * @param valueTypeAndOffset to add
     * @param value as Object
     */
    public void addValue(String key, ModbusVarItem valueTypeAndOffset, Object value) {
        
        mapValues.put(key, value);
        

    }
    
    /**
     * Returns the value for a given key.
     * 
     * @param key
     * @return the value for key
     */
    public Object getValue(String key) {
        
        Set<String> keys = serverMap.keySet();
        
        Object result = new Object();
        
        for (String mapKey : keys) {
        
            if (key.equals(mapKey)) {
                result = mapValues.get(key);
            }
        }

        return result;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + mapValues.toString();
    }
    
}
