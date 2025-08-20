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

import de.iip_ecosphere.platform.support.StringUtils;

/**
 * Some Modbus machine command for testing.
 * 
 * @author Christian Nikolajew
 */
public class ModbusCommandE {

    private Short day = null;
    private Short mounth = null;
    private Short year = null;
    private Float u12 = null;
    private Float u23 = null;
    private Float u31 = null;
    
    /**
     * Constructor.
     */
    public ModbusCommandE() {

    }
    
    /**
     * Setter for day.
     * 
     * @param day the day to set
     */
    public void setDay(Short day) {
        this.day = day;
    }

    /**
     * Setter for month.
     * 
     * @param month the month to set
     */
    public void setMonth(Short month) {
        this.mounth = month;
    }

    /**
     * Setter for year.
     * 
     * @param year the year to set
     */
    public void setYear(Short year) {
        this.year = year;
    }

    /**
     * Setter for u12.
     * 
     * @param u12 the u12 to set
     */
    public void setU12(Float u12) {
        this.u12 = u12;
    }

    /**
     * Setter for u23.
     * 
     * @param u23 the u23 to set
     */
    public void setU23(Float u23) {
        this.u23 = u23;
    }

    /**
     * Setter for u31.
     * 7
     * @param u31 the u31 to set
     */
    public void setU31(Float u31) {
        this.u31 = u31;
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
        return mounth;
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
     * Getter for u12.
     * 
     * @return the value of u12
     */
    public Float getU12() {
        return u12;
    }

    /**
     * Getter for u23.
     * 
     * @return the value of u23
     */
    public Float getU23() {
        return u23;
    }

    /**
     * Getter for u31.
     * 
     * @return the value of u31
     */
    public Float getU31() {
        return u31;
    }

    @Override
    public String toString() {
        return StringUtils.toStringShortStyle(this);
    }
}
