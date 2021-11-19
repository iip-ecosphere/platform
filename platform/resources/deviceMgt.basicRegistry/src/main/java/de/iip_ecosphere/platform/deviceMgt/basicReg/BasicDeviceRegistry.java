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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.iip_ecosphere.platform.deviceMgt.DeviceDescriptor;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistrationResponse;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistry;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Implements a device registry frontend for things board.
 * 
 * @author Dennis Pidun, University of Hildesheim
 */
public class BasicDeviceRegistry implements DeviceRegistry {

    public static final int PAGE_SIZE = 10;
    public static final String DEVICE_TYPE = "ecs";
    private Map<String, BasicRegistryDeviceDescriptor> devices = Collections.synchronizedMap(new HashMap<>());

    /**
     * Creates a things board device registry based on a given REST client.
     */
    public BasicDeviceRegistry() {
    }

    @Override
    public Set<String> getIds() {
        return devices.keySet();
    }

    @Override
    public Set<String> getManagedIds() {
        return devices.keySet();
    }

    @Override
    public Collection<? extends DeviceDescriptor> getDevices() {
        return devices.values();
    }

    @Override
    public BasicRegistryDeviceDescriptor getDevice(String id) {
        return null == id ? null : devices.get(id);
    }

    @Override
    public BasicRegistryDeviceDescriptor getDeviceByManagedId(String id) {
        return null == id ? null : devices.get(id);
    }

    @Override
    public DeviceRegistrationResponse addDevice(String id, String ip) {
        DeviceRegistrationResponse result = new DeviceRegistrationResponse();
        if (id == null || id.isEmpty() || ip == null || ip.isEmpty()) {
            result.setSuccessful(false);
            result.setMessage("No id given");
            return result;
        }
        
        devices.put(id, new BasicRegistryDeviceDescriptor(id, ip));
        // TODO add tokens, certificates
        result.setSuccessful(true);
        return result;
    }

    @Override
    public void removeDevice(String id) {
        if (id == null || id.isEmpty()) {
            return;
        }

        devices.remove(id);
    }

    @Override
    public void imAlive(String id) throws ExecutionException {
        sendTelemetry(id, "{\"active\": true}");
    }

    @Override
    public void sendTelemetry(String id, String telemetryData) throws ExecutionException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode telemetry = mapper.readTree(telemetryData);
            BasicRegistryDeviceDescriptor desc = getDevice(id);
            if (null != desc) {
                desc.saveEntityTelemetry(telemetry);
            }
        } catch (JsonProcessingException e) {
            throw new ExecutionException("TelemetryData is not json: ", e);
        }
    }
}
