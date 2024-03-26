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

/**
 * Machine data from the ModbusTcpIpModelAccess.
 * 
 * @author Christian Nikolajew
 */
public class ModbusMachineData {

    private short shortValue;
    private int intValue;
    private float floatValue;
    private long longValue;
    private double doubleValue;

    /**
     * Creates an instance.
     */
    public ModbusMachineData() {
    }

    /**
     * Returns the value as short.
     * 
     * @return the short value
     */
    public short getShortValue() {
        return shortValue;
    }

    /**
     * Returns the value as int.
     * 
     * @return the int value
     */
    public int getIntValue() {
        return intValue;
    }

    /**
     * Returns the value as float.
     * 
     * @return the float value
     */
    public float getFloatValue() {
        return floatValue;
    }

    /**
     * Returns the value as long.
     * 
     * @return the long value
     */
    public long getLongValue() {
        return longValue;
    }

    /**
     * Returns the value as double.
     * 
     * @return the double value
     */
    public double getDoubleValue() {
        return doubleValue;
    }

    /**
     * Sets the value as short.
     * 
     * @param value the short value
     */
    public void setShortValue(short value) {
        shortValue = value;
    }

    /**
     * Sets the value as int.
     * 
     * @param value  the int value
     */
    public void setIntValue(int value) {
        intValue = value;
    }

    /**
     * Sets the value as float.
     * 
     * @param value the float value
     */
    public void setFloatValue(float value) {
        floatValue = value;
    }

    /**
     * Sets the value as long.
     * 
     * @param value the long value
     */
    public void setLongValue(long value) {
        longValue = value;
    }

    /**
     * Sets the value as double.
     * 
     * @param value the double value
     */
    public void setDoubleValue(double value) {
        doubleValue = value;
    }
    
}
