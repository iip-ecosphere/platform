/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.net;

/**
 * Configuration setup for the network manager (prepared for Yaml). Default values are 1024-65535 according to RFC 6056.
 * 
 * @author Holger Eichelberger, SSE
 */
public class NetworkManagerSetup {
    
    private int lowPort = 1024;
    private int highPort = 65535;
    private String netmask = "";
    
    /**
     * Returns the minimum port for automated/ephemeral port assignment.
     * 
     * @return the minimum port
     */
    public int getLowPort() {
        return lowPort;
    }

    /**
     * Returns the maximum port for automated/ephemeral port assignment.
     * 
     * @return the maximum port
     */
    public int getHighPort() {
        return highPort;
    }
    
    /**
     * Returns the netmask/network Java regex.
     * 
     * @return the netmask/network Java regex
     */
    public String getNetmask() {
        return netmask;
    }

    /**
     * Defines the netmask/network Java regex. [required by data mapper, snakeYaml]
     * 
     * @param netmask the netmask
     */
    public void setNetmask(String netmask) {
        this.netmask = netmask;
    }

    /**
     * Returns the minimum port for automated/ephemeral port assignment. [required by data mapper, snakeYaml]
     * 
     * @param lowPort the minimum port
     */
    public void setLowPort(int lowPort) {
        this.lowPort = lowPort;
    }

    /**
     * Returns the maximum port for automated/ephemeral port assignment. [required by data mapper, snakeYaml]
     * 
     * @param highPort the maximum port
     */
    public void setHighPort(int highPort) {
        this.highPort = highPort;
    }

}
