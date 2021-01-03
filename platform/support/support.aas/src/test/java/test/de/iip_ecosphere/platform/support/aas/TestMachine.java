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

package test.de.iip_ecosphere.platform.support.aas;

/**
 * Implements a test machine.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestMachine {
    
    private int lotSize = 1;
    private double powerConsumption = 0.1;
    
    /**
     * Starts the machine.
     */
    public void start() {
        powerConsumption = 10.5;
    }
    
    /**
     * Stops the machine and resets the values.
     */
    public void stop() {
        powerConsumption = 0.1;
        lotSize = 1;
    }
    
    /**
     * Returns the actual lot size.
     * 
     * @return the lot size
     */
    public int getLotSize() {
        return lotSize;
    }
    
    /**
     * Configures the lot size.
     * 
     * @param lotSize the new lot size
     */
    public void setLotSize(int lotSize) {
        this.lotSize = lotSize;
    }
    
    /**
     * Configures the lot size.
     * 
     * @param lotSize the new lot size
     * @return if the new lot size is greater than the old one
     */
    public boolean reconfigure(int lotSize) {
        int old = lotSize;
        this.lotSize = lotSize;
        return lotSize > old;
    }
    
    /**
     * Returns the power consumption.
     *
     * @return the power consumption
     */
    public double getPowerConsumption() {
        return powerConsumption;
    }

}
