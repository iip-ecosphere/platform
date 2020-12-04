/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.connectors;

/**
 * Mimiks some form of model data type wrapping primitive types.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ModelDataType {

    private int intValue;
    private double doubleValue;
    private String stringValue;
    
    /**
     * Creates an instance for an int value.
     * 
     * @param intValue the int value
     */
    public ModelDataType(int intValue) {
        this.intValue = intValue;
    }

    /**
     * Creates an instance for a double value.
     * 
     * @param doubleValue the double value
     */
    public ModelDataType(double doubleValue) {
        this.doubleValue = doubleValue;
    }

    /**
     * Creates an instance for a String value.
     * 
     * @param stringValue the String value
     */
    public ModelDataType(String stringValue) {
        this.stringValue = stringValue;
    }

    /**
     * Returns the int value.
     * 
     * @return the int value
     */
    public int getIntValue() {
        return intValue;
    }

    /**
     * Returns the double value.
     * 
     * @return the double value
     */
    public double getDobuleValue() {
        return doubleValue;
    }

    /**
     * Returns the string value.
     * 
     * @return the string value
     */
    public String getStringValue() {
        return stringValue;
    }
    
}
