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
public class ModbusMachineCommand {

    private boolean changeShortValue;
    private boolean changeIntValue;
    private boolean changeFloatValue;
    private boolean changeLongValue;
    private boolean changeDoubleValue;

    private short shortValue;
    private int intValue;
    private float floatValue;
    private long longValue;
    private double doubleValue;

    /**
     * Creates an instance.
     */
    public ModbusMachineCommand() {
    }

    /**
     * Returns whether the short value was changed.
     * 
     * @return {@code true} for change, {@code false} else
     */
    public boolean getChangeShortValue() {
        return changeShortValue;
    }

    /**
     * Returns whether the int value was changed.
     * 
     * @return {@code true} for change, {@code false} else
     */
    public boolean getChangeIntValue() {
        return changeIntValue;
    }

    /**
     * Returns whether the short float was changed.
     * 
     * @return {@code true} for change, {@code false} else
     */
    public boolean getChangeFloatValue() {
        return changeFloatValue;
    }

    /**
     * Returns whether the long value was changed.
     * 
     * @return {@code true} for change, {@code false} else
     */
    public boolean getChangeLongValue() {
        return changeLongValue;
    }

    /**
     * Returns whether the double value was changed.
     * 
     * @return {@code true} for change, {@code false} else
     */
    public boolean getChangeDoubleValue() {
        return changeDoubleValue;
    }

    /**
     * Returns the short value.
     * 
     * @return the value
     */
    public short getShortValue() {
        return shortValue;
    }

    /**
     * Returns the int value.
     * 
     * @return the value
     */
    public int getIntValue() {
        return intValue;
    }

    /**
     * Returns the float value.
     * 
     * @return the value
     */
    public float getFloatValue() {
        return floatValue;
    }

    /**
     * Returns the long value.
     * 
     * @return the value
     */
    public long getLongValue() {
        return longValue;
    }

    /**
     * Returns the double value.
     * 
     * @return the value
     */
    public double getDoubleValue() {
        return doubleValue;
    }

    /**
     * Changes the short value. Changes {@link #changeShortValue}.
     * 
     * @param value the value
     */
    public void setShortValue(short value) {
        shortValue = value;
        changeShortValue = true;
    }

    /**
     * Changes the int value. Changes {@link #changeIntValue}.
     * 
     * @param value the value
     */
    public void setIntValue(int value) {
        intValue = value;
        changeIntValue = true;
    }

    /**
     * Changes the float value. Changes {@link #changeFloatValue}.
     * 
     * @param value the value
     */
    public void setFloatValue(float value) {
        floatValue = value;
        changeFloatValue = true;
    }

    /**
     * Changes the long value. Changes {@link #changeLongValue}.
     * 
     * @param value the value
     */
    public void setLongValue(long value) {
        longValue = value;
        changeLongValue = true;
    }

    /**
     * Changes the double value. Changes {@link #changeDoubleValue}.
     * 
     * @param value the value
     */
    public void setDoubleValue(double value) {
        doubleValue = value;
        changeDoubleValue = true;
    }
}
