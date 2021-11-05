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

package de.iip_ecosphere.platform.deviceMgt.registry;

import java.util.concurrent.ExecutionException;

/**
 * A service provider interface for registering devices in the IIP-Ecosphere platform
 * and interact with them.
 *
 * All methods listed here are intended to be called by the devices via aas.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public interface DeviceRegistryOperations {

    /**
     * Register a specific device, which can be used for on-boarding.
     *
     * @param id the id of the new device
     * @param ip the ip of the new device
     * @throws ExecutionException if the operation fails
     */
    void addDevice(String id, String ip) throws ExecutionException;

    /**
     * Removes a registered device, which can be used for off-boarding.
     *
     * @param id the id of the new device
     * @throws ExecutionException if the operation fails
     */
    void removeDevice(String id) throws ExecutionException;

    /**
     * Send a heartbeat to device management.
     * Should be called in a 10-30 seconds interval by the device.
     *
     * @param id the id of the new device
     * @throws ExecutionException if the operation fails
     */
    void imAlive(String id) throws ExecutionException;

    /**
     * Sends telemetry data to device management.
     * 
     * @param id the id of the device
     * @param telemetryData the telemetry data (JSON format?)
     * @throws ExecutionException if the operation fails
     */
    void sendTelemetry(String id, String telemetryData) throws ExecutionException;
}
