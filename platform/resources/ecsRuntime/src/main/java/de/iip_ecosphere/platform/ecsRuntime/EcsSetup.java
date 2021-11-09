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

package de.iip_ecosphere.platform.ecsRuntime;

import java.io.IOException;

import de.iip_ecosphere.platform.support.iip_aas.AasBasedSetup;
import de.iip_ecosphere.platform.support.net.NetworkManagerSetup;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;

/**
 * ECS runtime setup (poor man's spring approach). Implementing components shall extend this class and add
 * their specific configuration settings. Subclasses must have a no-arg constructor and getters/setters for all
 * configuration values.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EcsSetup extends AasBasedSetup {
    
    private TransportSetup transport = new TransportSetup();
    private NetworkManagerSetup netMgr = new NetworkManagerSetup();
    private int monitoringUpdatePeriod = 2000;
    private boolean autoOnOffboarding = true;

    /**
     * Returns the monitoring update period.
     * 
     * @return the monitoring update period in ms
     */
    public int getMonitoringUpdatePeriod() {
        return monitoringUpdatePeriod;
    }
    
    /**
     * Returns the transport setup.
     * 
     * @return the transport setup
     */
    public TransportSetup getTransport() {
        return transport;
    }
    
    /**
     * Returns the network manager setup.
     * 
     * @return the network manager setup
     */
    public NetworkManagerSetup getNetMgr() {
        return netMgr;
    }

    /**
     * Returns whether automatic on/offboarding is enabled.
     * 
     * @return {@code true} on/offboard always, if {@code false} explicit on/offboarding is required
     */
    public boolean getAutoOnOffboarding() {
        return autoOnOffboarding;
    }

    /**
     * Changes the monitoring update period. [snakeyaml]
     * 
     * @param monitoringUpdatePeriod in ms, values below 200 are turned to 200
     */
    public void setMonitoringUpdatePeriod(int monitoringUpdatePeriod) {
        this.monitoringUpdatePeriod = Math.max(200, monitoringUpdatePeriod);
    }
    
    /**
     * Defines the transport setup. [snakeyaml]
     * 
     * @param transport the transport setup
     */
    public void setTransport(TransportSetup transport) {
        this.transport = transport;
    }

    /**
     * Defines the network manager setup. [snakeyaml]
     * 
     * @param netMgr the network manager setup
     */
    public void setNetMgr(NetworkManagerSetup netMgr) {
        this.netMgr = netMgr;
    }

    /**
     * Changes the automatic on/offboarding behavior. [snakeyaml]
     * 
     * @param implicitOnOffboarding if {@code true} on/offboard always, if {@code false} explicit on/offboarding 
     *     is required
     */
    public void setAutoOnOffboarding(boolean implicitOnOffboarding) {
        this.autoOnOffboarding = implicitOnOffboarding;
    }

    /**
     * Reads a {@link EcsSetup} instance from {@link AbstractSetup#DEFAULT_FNAME) in the root folder of 
     * the jar/classpath. This method shall be used by subclasses akin to {@link #readFromYaml()}. 
     *
     * @param <C> the specific type of configuration to read (extended from {@code Configuration}}
     * @param cls the class of configuration to read
     * @return the configuration instance
     * @see #readFromYaml(Class, String)
     */
    public static <C extends EcsSetup> C readConfiguration(Class<C> cls) throws IOException {
        return readFromYaml(cls);
    }
    
    /**
     * Reads a {@link EcsSetup} instance from {@link AbstractSetup#DEFAULT_FNAME) in the root folder of 
     * the jar/classpath. 
     *
     * @return the configuration instance
     * @see #readFromYaml(Class)
     */
    public static EcsSetup readConfiguration() throws IOException {
        return readFromYaml(EcsSetup.class);
    }

}
