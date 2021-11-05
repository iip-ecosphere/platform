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

import de.iip_ecosphere.platform.deviceMgt.DeviceDescriptor;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * THe DeviceRegistryProxy is a DeviceRegistry which can tunnel
 * the operations to the aas default implementation.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
class DeviceRegistryProxy extends AbstractDeviceRegistry {

    private DeviceRegistry sink;

    /**
     * Constructor for the proxy.
     * 
     * @param sink the sink, to which the proxy should tunnel the operations
     */
    public DeviceRegistryProxy(DeviceRegistry sink) {
        this.sink = sink;
    }

    /**
     * Calls the aas implementation and tunnels operation (addDevice) to sink.
     * See: {@link DeviceRegistry}
     *
     * @param id the id of the device
     * @param ip the ip of the device
     * @throws ExecutionException if the operation fails
     */
    @Override
    public void addDevice(String id, String ip) throws ExecutionException {
        super.addDevice(id, ip);
        sink.addDevice(id, ip);
    }

    /**
     * Calls the aas implementation and tunnels operation (removeDevice) to sink.
     * See: {@link DeviceRegistry}
     *
     * @param id the id of the device
     * @throws ExecutionException if the operation fails
     */
    @Override
    public void removeDevice(String id) throws ExecutionException {
        super.removeDevice(id);
        sink.removeDevice(id);
    }

    /**
     * Fully implemented by the sink.
     * See: {@link DeviceRegistry}
     *
     * @param id the id of the new device
     * @throws ExecutionException if the operation fails
     */
    @Override
    public void imAlive(String id) throws ExecutionException {
        sink.imAlive(id);
    }

    @Override
    public void sendTelemetry(String id, String telemetryData) throws ExecutionException {
        sink.sendTelemetry(id, telemetryData);
    }

    /**
     * Fully implemented by the sink.
     * See: {@link DeviceRegistry}
     *
     * @return device ids
     */
    @Override
    public Set<String> getIds() {
        return sink.getIds();
    }

    /**
     * Fully implemented by the sink.
     * See: {@link DeviceRegistry}
     *
     * @return managed device ids
     */
    @Override
    public Set<String> getManagedIds() {
        return sink.getManagedIds();
    }

    /**
     * Fully implemented by the sink.
     * See: {@link DeviceRegistry}
     *
     * @return devices
     */
    @Override
    public Collection<? extends DeviceDescriptor> getDevices() {
        return sink.getDevices();
    }

    /**
     * Fully implemented by the sink.
     * See: {@link DeviceRegistry}
     *
     * @return a device
     */
    @Override
    public DeviceDescriptor getDevice(String id) {
        return sink.getDevice(id);
    }

    /**
     * Fully implemented by the sink.
     * See: {@link DeviceRegistry}
     *
     * @return device
     */
    @Override
    public DeviceDescriptor getDeviceByManagedId(String id) {
        return sink.getDeviceByManagedId(id);
    }
}
