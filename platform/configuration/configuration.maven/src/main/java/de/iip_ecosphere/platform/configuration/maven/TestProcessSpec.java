/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration.maven;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * Defines an additional process to be executed in the test build process.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestProcessSpec extends BasicProcessSpec {
    
    @Parameter(required = false, defaultValue = "0")
    private int networkPort;

    @Parameter(required = false, defaultValue = "")
    private String networkPortProperty;
    
    @Parameter(required = false, defaultValue = "true")
    private boolean waitFor;

    /**
     * Returns the network port.
     * 
     * @return the networkPort: negative ephemeral, 0 disabled, positive given port
     */
    public int getNetworkPort() {
        return networkPort;
    }

    /**
     * Defines the network port. [mvn]
     * 
     * @param networkPort the networkPort to set: negative ephemeral, 0 disabled, positive given port
     */
    public void setNetworkPort(int networkPort) {
        this.networkPort = networkPort;
    }

    /**
     * Returns the maven property name for the network port.
     * 
     * @return the networkPortProperty (may be <b>null</b> or empty for none)
     */
    public String getNetworkPortProperty() {
        return networkPortProperty;
    }

    /**
     * Sets the maven property name for the network port. [mvn]
     * 
     * @param networkPortProperty the networkPortProperty to set (may be <b>null</b> or empty for none)
     */
    public void setNetworkPortProperty(String networkPortProperty) {
        this.networkPortProperty = networkPortProperty;
    }

    /**
     * Returns whether the process shall be completely executed before continuing.
     * 
     * @return {@code true} for wait for completion, {@code false} for parallel execution
     */
    public boolean isWaitFor() {
        return waitFor;
    }

    /**
     * Sets whether the process shall be completely executed before continuing. [mvn]
     * 
     * @param waitFor the flag to set
     */
    public void setWaitFor(boolean waitFor) {
        this.waitFor = waitFor;
    }    

}
