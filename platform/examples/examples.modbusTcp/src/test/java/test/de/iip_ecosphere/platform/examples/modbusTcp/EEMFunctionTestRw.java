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

package test.de.iip_ecosphere.platform.examples.modbusTcp;

/**
 * Implements the input datatype from oktoflow to the connected machine for the EEM function test.
 * 
 * @author Christian Nikolajew
 */
public class EEMFunctionTestRw {

    private Short day = null;
    private Short month = null;
    private Short year = null;
    
    /**
     * Constructor.
     */
    public EEMFunctionTestRw() {
        
    }
    
    /**
     * Getter for day.
     * 
     * @return the value of day
     */
    public Short getDay() {
        return day;
    }
    
    /**
     * Getter for month.
     * 
     * @return the value of month
     */
    public Short getMonth() {
        return month;
    }
    
    /**
     * Getter for year.
     * 
     * @return the value of year
     */
    public Short getYear() {
        return year;
    }
    
    /**
     * Setter for day.
     * 
     * @param val the short to set
     */
    public void setDay(short val) {
        day = val;
    }
    
    /**
     * Setter for month.
     * 
     * @param val the short to set
     */
    public void setMonth(short val) {
        month = val;
    }
    
    /**
     * Setter for year.
     * 
     * @param val the short to set
     */
    public void setYear(short val) {
        year = val;
    }
}
