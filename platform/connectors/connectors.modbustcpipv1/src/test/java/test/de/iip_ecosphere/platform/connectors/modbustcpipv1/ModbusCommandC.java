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
 * Some Modbus machine command for testing.
 * 
 * @author Christian Nikolajew
 */
public class ModbusCommandC {

    private Short mShort = null;
    private Integer mInteger = null;
    private Float mFloat = null;
    private Long mLong = null;
    private Double mDouble = null;

    
    /**
     * Constructor.
     */
    public ModbusCommandC() {

    }
    
    /**
     * Setter for mShort.
     * 
     * @param aShort the short to set
     */
    public void setShort(Short aShort) {
        mShort = aShort;
    }

    /**
     * Setter for mInteger.
     * 
     * @param aInteger the integer to set
     */
    public void setInteger(Integer aInteger) {
        mInteger = aInteger;
    }

    /**
     * Setter for mFloat.
     * 
     * @param aFloat the float to set
     */
    public void setFloat(Float aFloat) {
        mFloat = aFloat;
    }

    /**
     * Setter for mLong.
     * 
     * @param aLong the long to set
     */
    public void setLong(Long aLong) {
        mLong = aLong;
    }

    /**
     * Setter for mDouble.
     * 
     * @param aDouble the double to set
     */
    public void setDouble(Double aDouble) {
        mDouble = aDouble;
    }

    /**
     * Getter for mShort.
     * 
     * @return the value of mShort
     */
    public Short getShort() {
        return mShort;
    }

    /**
     * Getter for month.
     * 
     * @return the value of mInteger
     */
    public Integer getInteger() {
        return mInteger;
    }

    /**
     * Getter for mFloat.
     * 
     * @return the value of mFloat
     */
    public Float getFloat() {
        return mFloat;
    }

    /**
     * Getter for mLong.
     * 
     * @return the value of mLong
     */
    public Long getLong() {
        return mLong;
    }

    /**
     * Getter for mDouble.
     * 
     * @return the value of mDouble
     */
    public Double getDouble() {
        return mDouble;
    }
    
}
