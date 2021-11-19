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

package de.iip_ecosphere.platform.deviceMgt.thingsboard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.iip_ecosphere.platform.deviceMgt.DeviceDescriptor;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistrationResponse;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistry;
import org.thingsboard.rest.client.RestClient;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Implements a device registry frontend for things board.
 * 
 * @author Dennis Pidun, University of Hildesheim
 */
public class ThingsBoardDeviceRegistry implements DeviceRegistry {

    public static final int PAGE_SIZE = 10;
    public static final String DEVICE_TYPE = "ecs";
    private RestClient restClient;

    /**
     * Creates a things board device registry based on a given REST client.
     * 
     * @param restClient the rest client
     */
    public ThingsBoardDeviceRegistry(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Returns the REST client.
     * 
     * @return the REST client
     */
    public RestClient getRestClient() {
        return restClient;
    }

    @Override
    public Set<String> getIds() {
        return getTBDevices().stream()
                .map(Device::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Returns the things board devices list.
     * 
     * @return the devices list
     */
    private List<Device> getTBDevices() {
        List<Device> devices = new ArrayList<>();
        int page = 0;
        PageData<Device> devicePage = restClient.getTenantDevices(DEVICE_TYPE, new PageLink(PAGE_SIZE, page));
        do {
            devices.addAll(devicePage.getData());
            devicePage = restClient.getTenantDevices(DEVICE_TYPE, new PageLink(PAGE_SIZE, page++));
        } while(devicePage.hasNext());
        return devices;
    }

    @Override
    public Set<String> getManagedIds() {
        return getTBDevices()
                .stream()
                .map(d -> d.getId().toString())
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends DeviceDescriptor> getDevices() {
        return getTBDevices()
                .stream()
                .map(d -> new ThingsBoardDeviceDescriptor(d, this.restClient))
                .collect(Collectors.toSet());
    }

    @Override
    public DeviceDescriptor getDevice(String id) {
        return this.restClient.getTenantDevice(id)
                .map(d -> new ThingsBoardDeviceDescriptor(d, this.restClient))
                .orElse(null);

    }

    @Override
    public DeviceDescriptor getDeviceByManagedId(String id) {
        return this.restClient.getDeviceById(new DeviceId(UUID.fromString(id)))
                .map(tbDevice -> new ThingsBoardDeviceDescriptor(tbDevice, this.restClient))
                .orElse(null);
    }

    @Override
    public DeviceRegistrationResponse addDevice(String id, String ip) {
        DeviceRegistrationResponse result = new DeviceRegistrationResponse();
        if (id == null || id.isEmpty() || ip == null || ip.isEmpty()) {
            result.setSuccessful(false);
            result.setMessage("No id given");
            return result;
        }

        Device tbDevice = this.restClient.getTenantDevice(id).orElse(null);
        if (tbDevice == null) {
            Device device = new Device();
            device.setType(DEVICE_TYPE);
            device.setName(id);
            tbDevice = this.restClient.saveDevice(device);
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode attribute = mapper.readTree("{\"ip\": \"" + ip + "\"}");
            this.restClient.saveDeviceAttributes(tbDevice.getId(), "SERVER_SCOPE", attribute);
        } catch (JsonProcessingException ignore) {
        }
        // TODO add tokens, certificates
        result.setSuccessful(true);
        return result;
    }

    @Override
    public void removeDevice(String id) {
        if (id == null || id.isEmpty()) {
            return;
        }

        this.restClient.getTenantDevice(id)
            .ifPresent(tbDevice -> this.restClient.deleteDevice(tbDevice.getId()));
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
            this.restClient.getTenantDevice(id)
                    .ifPresent(tbDevice ->
                            this.restClient.saveEntityTelemetry(tbDevice.getId(), "SERVER_SCOPE", telemetry));
        } catch (JsonProcessingException e) {
            throw new ExecutionException("TelemetryData is not json: ", e);
        }
    }
}
