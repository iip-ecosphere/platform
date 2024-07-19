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
 * Some Modbus machine data for testing.
 * 
 * @author Christian Nikolajew
 */
public class ModbusDataC {

    private short mShort;
    private int mInteger;
    private float mFloat;
    private long mLong;
    private double mDouble;

    /**
     * Constructor.
     */
    public ModbusDataC() {

    }

    /**
     * Setter for mShort.
     * 
     * @param aShort the short to set
     */
    public void setShort(short aShort) {
        mShort = aShort;
    }

    /**
     * Setter for mInteger.
     * 
     * @param aInteger the integer to set
     */
    public void setInteger(int aInteger) {
        mInteger = aInteger;
    }

    /**
     * Setter for mFloat.
     * 
     * @param aFloat the float to set
     */
    public void setFloat(float aFloat) {
        mFloat = aFloat;
    }

    /**
     * Setter for mLong.
     * 
     * @param aLong the long to set
     */
    public void setLong(long aLong) {
        mLong = aLong;
    }

    /**
     * Setter for mDouble.
     * 
     * @param aDouble the double to set
     */
    public void setDouble(double aDouble) {
        mDouble = aDouble;
    }

    /**
     * Getter for mShort.
     * 
     * @return the value of mShort
     */
    public short getShort() {
        return mShort;
    }

    /**
     * Getter for month.
     * 
     * @return the value of mInteger
     */
    public int getInteger() {
        return mInteger;
    }

    /**
     * Getter for mFloat.
     * 
     * @return the value of mFloat
     */
    public float getFloat() {
        return mFloat;
    }

    /**
     * Getter for mLong.
     * 
     * @return the value of mLong
     */
    public long getLong() {
        return mLong;
    }

    /**
     * Getter for mDouble.
     * 
     * @return the value of mDouble
     */
    public double getDouble() {
        return mDouble;
    }

    
}
