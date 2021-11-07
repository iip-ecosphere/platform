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

package de.iip_ecosphere.platform.deviceMgt.basicReg;

import de.iip_ecosphere.platform.deviceMgt.DeviceDescriptor;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Implements the things board device descriptor.
 * 
 * @author Dennis Pidun, University of Hildesheim
 */
public class BasicRegistryDeviceDescriptor implements DeviceDescriptor {

    private static int deviceTimeout = 15000;
    private String id;
    private String ip;
    private State state = State.STARTING; // unsure
    private long stateUpdateActive;

    /**
     * Creates the descriptor.
     * 
     * @param id the device id
     * @param ip the device ip
     */
    public BasicRegistryDeviceDescriptor(String id, String ip) {
        this.id = id;
        this.ip = ip;
    }
    
    /**
     * Returns the device timeout.
     * 
     * @return the device timeout [ms]
     */
    public static int getDeviceTimeout() {
        return deviceTimeout;
    }
    
    /**
     * Changes the device timeout. [public for testing]
     * 
     * @param timeout the timeout [ms]
     */
    public static void setDeviceTimeout(int timeout) {
        deviceTimeout = timeout;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getManagedId() {
        return id;
    }

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public String getRuntimeVersion() {
        return null;
    }

    @Override
    public String getRuntimeName() {
        return null;
    }

    @Override
    public String getResourceId() {
        return id;
    }
    
    /**
     * Saves telemetry information.
     * 
     * @param telemetry the telemetry
     */
    void saveEntityTelemetry(JsonNode telemetry) {
        // adapted from thingsboard
        long now = System.currentTimeMillis();
        JsonNode active = telemetry.get("active");
        if (null == active || !active.asBoolean()) {
            state = State.STARTING;
        } else {
            stateUpdateActive = System.currentTimeMillis();
            if (stateUpdateActive > 0 && now - stateUpdateActive < deviceTimeout) {
                state = State.AVAILABLE;
            } else {
                state = State.UNDEFINED;
            }
        }
    }

    @Override
    public State getState() {
        return state;
    }
}
