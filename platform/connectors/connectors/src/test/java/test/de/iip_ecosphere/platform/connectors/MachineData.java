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
 * Test class representing machine data from the information model.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MachineData {
    
    private int lotSize;
    private double powerConsumption;
    private String vendor; // usually would go into an own class/instance
    
    /**
     * Creates a machine data object.
     * 
     * @param lotSize the lot size
     * @param powerConsumption the power consumption
     * @param vendor the vendor name
     */
    public MachineData(int lotSize, double powerConsumption, String vendor) {
        this.lotSize = lotSize;
        this.powerConsumption = powerConsumption;
        this.vendor = vendor;
    }

    /**
     * Returns the actual lot size.
     * 
     * @return the actual lot size
     */
    public int getLotSize() {
        return lotSize;
    }

    /**
     * Returns the power consumption.
     * 
     * @return the actual power consumption
     */
    public double getPowerConsumption() {
        return powerConsumption;
    }

    /**
     * Returns the machine vendor name.
     * 
     * @return the vendor name
     */
    public String getVendor() {
        return vendor;
    }
}
