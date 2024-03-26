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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ModbusItem is the data structure that is read from the machine and
 * written to the machine, to transfer data.
 * 
 * So far only the holding registers from the MODBUS data model are implemented.
 * 
 * @author Christian Nikolajew
 */
public class ModbusItem {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModbusItem.class);

    /**
     * Holding registers size.
     */
    private final int mHoldingRegisterSize = 13;

    /**
     * Holding registers values.
     */
    private short[] mHoldingRegister;

    /**
     * Creates an instance.
     */
    public ModbusItem() {
        mHoldingRegister = new short[mHoldingRegisterSize];
    }

    /**
     * Sets the holding register at aIndex to aValue.
     * 
     * @param aIndex of the register to write to
     * @param aValue to write
     */
    public void setHoldingRegister(short aIndex, short aValue) {

        if (aIndex < mHoldingRegisterSize) {
            mHoldingRegister[aIndex] = aValue;
        } else {
            LOGGER.info("setHoldingRegister : aIndex = " + aIndex + " is out of range");
        }

    }

    /**
     * Returns the holding registers size.
     * 
     * @return  holding registers size
     */
    public int getHoldingRegisterSize() {
        return mHoldingRegisterSize;
    }

    /**
     * Get the holding registers value at aIndex.
     * 
     * @param aIndex of the register to read from
     * @return holding registers value at aIndex or -1 if aIndex is out of range
     */
    public short getHoldingRegister(int aIndex) {
        
        if (aIndex < mHoldingRegisterSize) {
            return mHoldingRegister[aIndex];
        } else {
            LOGGER.info("getHoldingRegister : aIndex = " + aIndex + " is out of range");
            return -1;
        }    
    }
}
