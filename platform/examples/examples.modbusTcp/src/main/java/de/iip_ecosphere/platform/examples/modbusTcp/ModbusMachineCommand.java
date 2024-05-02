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

import java.util.ArrayList;


/**
 * Some Modbus machine command for testing.
 * 
 * @author Christian Nikolajew
 */
public class ModbusMachineCommand {
    
    private ArrayList<String> keysToChange;
    private ArrayList<Object> valuesToWrite;

    /**
     * Creates an instance.
     */
    public ModbusMachineCommand() {
        
        keysToChange = new ArrayList<String>();
        valuesToWrite = new ArrayList<Object>();
    }
    
    /**
     * Get the amount of keys. 
     * 
     * @return the number of keys to change
     */
    public int getKeyCount() {
        return keysToChange.size();
    }
    
    /**
     * Getter for key.
     * 
     * @param index of the key to return 
     * @return the key at index
     */
    public String getKey(int index) {
        return keysToChange.get(index);
    }
    
    /**
     * Getter for value.
     * 
     * @param index of the value to return
     * @return value at index
     */
    public Object getValue(int index) {
        return valuesToWrite.get(index);
    }
    
    /**
     * Setter for key and value.
     * 
     * @param key that's value should be changed
     * @param value that should be set for key 
     */
    public void set(String key, Object value) {
        
        keysToChange.add(key);
        valuesToWrite.add(value);
        
    }

}
