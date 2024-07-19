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

/**
 * This Class stores type an offset of a variable.
 * 
 * @author Christian Nikolajew
 *
 */
public class ModbusVarItem {
    
    private String type;
    private int offset;

    
    /**
     * Setter for type.
     * 
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * Setter for offset.
     * 
     * @param offset the offset to set
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }
    /**
     * Getter for type.
     * 
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Getter for offset.
     * 
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }
    

    /**
     * Returs the count of registers needed to store this type.
     * 
     * @return the count of registers needed to store this type
     */
    public int getTypeRegisterSize() {
        
        int result = 0;
        
        if (type.equals("short") || type.equals("ushort")) {
            result = 1;
        } else if (type.equals("integer") || type.equals("uinteger")) {
            result = 2;
        } else if (type.equals("float")) {
            result = 2;
        } else if (type.equals("long") || type.equals("ulong")) {
            result = 4;
        } else if (type.equals("double")) {
            result = 4;
        } else if (type.equals("ascii")) {
            result = 2;
        } else if (type.equals("datetime")) {
            result = 4;
        }
        
        return result;
    }

}
