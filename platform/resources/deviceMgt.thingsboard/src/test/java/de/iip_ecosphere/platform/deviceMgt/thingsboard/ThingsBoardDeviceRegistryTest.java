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
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.thingsboard.rest.client.RestClient;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.page.PageData;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the things board registry.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ThingsBoardDeviceRegistryTest {

    public static final String AN_IP = "AN_IP";
    public static final String ANOTHER_IP = "ANOTHER_IP";
    public static final String ANOTHER_DEVICE = "ANOTHER_DEVICE";
    private static final String A_DEVICE = "A_DEVICE";
    private ThingsBoardDeviceRegistry deviceRegistry;
    private RestClient thingsBoardMock;

    /**
     * Configures the test before running.
     */
    @Before
    public void setUp() {
        thingsBoardMock = mock(RestClient.class);
        deviceRegistry = new ThingsBoardDeviceRegistry(thingsBoardMock);
    }

    /**
     * Cleans up after testing.
     */
    @Test
    public void getIds_withTwoDevices_shouldReturnListWithTwoIds() {
        List<Device> devices = new ArrayList<>();
        Device d1 = new Device(new DeviceId(UUID.randomUUID()));
        d1.setName(A_DEVICE);
        Device d2 = new Device(new DeviceId(UUID.randomUUID()));
        d2.setName(ANOTHER_DEVICE);
        devices.add(d1);
        devices.add(d2);

        @SuppressWarnings("unchecked")
        PageData<Device> pageData = mock(PageData.class);
        when(pageData.hasNext()).thenReturn(false); // only one page
        when(pageData.getData()).thenReturn(devices);

        when(thingsBoardMock.getTenantDevices(
                eq(ThingsBoardDeviceRegistry.DEVICE_TYPE), any()))
                .thenReturn(pageData);

        Set<String> ids = deviceRegistry.getIds();

        Assert.assertNotNull(ids);
        Assert.assertTrue(ids.contains(A_DEVICE));
        Assert.assertTrue(ids.contains(ANOTHER_DEVICE));
        Assert.assertEquals(2, ids.size());
    }

    /**
     * Tests that requesting managed devices with two devices returns a list with two IDs.
     */
    @Test
    public void getManagedIds_withTwoDevices_shouldReturnListWithTwoIds() {
        List<Device> devices = new ArrayList<>();
        Device d1 = new Device(new DeviceId(UUID.randomUUID()));
        d1.setName(A_DEVICE);
        Device d2 = new Device(new DeviceId(UUID.randomUUID()));
        d2.setName(ANOTHER_DEVICE);
        devices.add(d1);
        devices.add(d2);

        @SuppressWarnings("unchecked")
        PageData<Device> pageData = mock(PageData.class);
        when(pageData.hasNext()).thenReturn(false); // only one page
        when(pageData.getData()).thenReturn(devices);

        when(thingsBoardMock.getTenantDevices(
                eq(ThingsBoardDeviceRegistry.DEVICE_TYPE), any()))
                .thenReturn(pageData);

        Set<String> ids = deviceRegistry.getManagedIds();

        Assert.assertNotNull(ids);
        Assert.assertTrue(ids.contains(d1.getId().toString()));
        Assert.assertTrue(ids.contains(d2.getId().toString()));
        Assert.assertEquals(2, ids.size());
    }

    /**
     * Tests that requesting devices with two devices returns a list with two IDs.
     */
    @Test
    public void getDevices_withTwoDevices_shouldReturnListWithTwoIds() {
        List<Device> devices = new ArrayList<>();
        Device d1 = new Device(new DeviceId(UUID.randomUUID()));
        d1.setName(A_DEVICE);
        Device d2 = new Device(new DeviceId(UUID.randomUUID()));
        d2.setName(ANOTHER_DEVICE);
        devices.add(d1);
        devices.add(d2);

        @SuppressWarnings("unchecked")
        PageData<Device> pageData = mock(PageData.class);
        when(pageData.hasNext()).thenReturn(false); // only one page
        when(pageData.getData()).thenReturn(devices);

        when(thingsBoardMock.getTenantDevices(
                eq(ThingsBoardDeviceRegistry.DEVICE_TYPE), any()))
                .thenReturn(pageData);

        Collection<? extends DeviceDescriptor> actualDevices = deviceRegistry.getDevices();
        Assert.assertEquals(2, actualDevices.size());
    }

    /**
     * Tests that requesting a device with valid ID returns the device.
     */
    @Test
    public void getDevice_withValidId_returnsDevice() {
        Device device = mockDevice();

        DeviceDescriptor desc = deviceRegistry.getDevice(A_DEVICE);
        Assert.assertEquals(A_DEVICE, desc.getId());
        Assert.assertEquals(A_DEVICE, desc.getResourceId());
        Assert.assertEquals(device.getId().toString(), desc.getManagedId());
    }

    /**
     * Tests that requesting a device with invalid ID returns <b>null</b>.
     */
    @Test
    public void getDevice_withInvalidId_returnsNull() {
        when(thingsBoardMock.getTenantDevice(eq(A_DEVICE))).thenReturn(Optional.empty());

        DeviceDescriptor desc = deviceRegistry.getDevice(A_DEVICE);
        Assert.assertNull(desc);
    }

    /**
     * Tests that requesting a managed device with valid ID returns the device.
     */
    @Test
    public void getDeviceByManagedId_withValidId_returnsDevice() {
        Device device = mockDevice();

        String id = device.getId().toString();
        DeviceDescriptor desc = deviceRegistry.getDeviceByManagedId(id);
        Assert.assertNotNull(desc);
        Assert.assertEquals(id, desc.getManagedId());
        Assert.assertEquals(device.getName(), desc.getId());
        Assert.assertEquals(device.getName(), desc.getResourceId());
    }

    /**
     * Tests that requesting a managed device with invalid ID returns <b>null</b>.
     */
    @Test
    public void getDeviceByManagedId_withInvalidId_returnsNull() {
        String uuid = UUID.randomUUID().toString();
        when(thingsBoardMock.getTenantDevice(eq(uuid))).thenReturn(Optional.empty());

        DeviceDescriptor desc = deviceRegistry.getDeviceByManagedId(uuid);
        Assert.assertNull(desc);
    }

    /**
     * Tests that adding a valid device causes adding the device.
     * 
     * @throws JsonProcessingException if JSON processing problems occur, shall not happen
     */
    @Test
    public void addDevice_withValidDevice_addsADevice() throws JsonProcessingException {
        DeviceId id = new DeviceId(UUID.randomUUID());
        Device device = new Device();
        device.setType(ThingsBoardDeviceRegistry.DEVICE_TYPE);
        device.setName(A_DEVICE);
        when(thingsBoardMock.saveDevice(eq(device))).thenAnswer((a) -> {
            Device d = a.getArgument(0);
            d.setId(id);
            return d;
        });

        deviceRegistry.addDevice(A_DEVICE, AN_IP);

        JsonNode attribute = getJsonNode("{\"ip\": \"" + AN_IP + "\"}");
        verify(thingsBoardMock).saveDeviceAttributes(eq(id), any(), eq(attribute));
        verify(thingsBoardMock, times(1)).saveDevice(any());
    }

    /**
     * Tests that adding a device with no IP does not cause adding the device.
     */
    @Test
    public void addDevice_withNoIp_wontAddDevice() {
        deviceRegistry.addDevice(A_DEVICE, null);
        deviceRegistry.addDevice(A_DEVICE, "");

        verify(thingsBoardMock, never()).saveDeviceAttributes(any(), any(), any());
        verify(thingsBoardMock, never()).saveDevice(any());
    }

    /**
     * Tests that adding a device with no identifier does not cause adding the device.
     */
    @Test
    public void addDevice_withNoDeviceIdentifier_wontAddDevice() {
        deviceRegistry.addDevice(null, AN_IP);
        deviceRegistry.addDevice("", AN_IP);

        verify(thingsBoardMock, never()).saveDeviceAttributes(any(), any(), any());
        verify(thingsBoardMock, never()).saveDevice(any());
    }

    /**
     * Tests that adding an already registered device updates the device.
     */
    @Test
    public void addDevice_withAlreadyRegisteredDevice_updatesDevice() {
        mockDevice();
        deviceRegistry.addDevice(A_DEVICE, ANOTHER_IP);

        ArgumentCaptor<JsonNode> captor = ArgumentCaptor.forClass(JsonNode.class);
        verify(thingsBoardMock, times(1)).saveDeviceAttributes(any(), any(), captor.capture());
        verify(thingsBoardMock, times(0)).saveDevice(any());

        Assert.assertEquals(ANOTHER_IP, captor.getValue().findValue("ip").asText());
    }

    /**
     * Tests that removing an valid device removes the device.
     */
    @Test
    public void removeDevice_withValidDevice_removesDevice() {
        Device device = mockDevice();

        deviceRegistry.removeDevice(A_DEVICE);

        verify(thingsBoardMock, times(1)).deleteDevice(eq(device.getId()));
    }

    /**
     * Tests that removing an invalid device does not remove a device.
     */
    @Test
    public void removeDevice_withInvalidDevice_wontRemoveDevice() {
        deviceRegistry.removeDevice(A_DEVICE);

        verify(thingsBoardMock, times(0)).deleteDevice(any());
    }

    /**
     * Tests that removing an invalid device (no device information) does not remove a device.
     */
    @Test
    public void removeDevice_withNoDevice_wontRemoveDevice() {
        deviceRegistry.removeDevice("");
        deviceRegistry.removeDevice(null);

        verify(thingsBoardMock, times(0)).deleteDevice(any());
    }

    /**
     * Tests that sending valid telemetry leads to saved data.
     * 
     * @throws JsonProcessingException JSON processing shall not fail
     * @throws ExecutionException AAS execution shall not fail
     */
    @Test
    public void sendTelemetry_withValidData_shouldSaveTelemetry() throws ExecutionException, JsonProcessingException {
        Device device = mockDevice();
        deviceRegistry.sendTelemetry(A_DEVICE, "{\"telemetryKey\": \"telemetryValue\"}");

        verify(thingsBoardMock).saveEntityTelemetry(eq(device.getId()), any(), 
            eq(getJsonNode("{\"telemetryKey\": \"telemetryValue\"}")));
    }

    /**
     * Tests that sending invalid telemetry leads to an exception.
     * 
     * @throws ExecutionException AAS execution shall not fail
     */
    @Test(expected = ExecutionException.class)
    public void sendTelemetry_withInvalidData_shouldThrowException() throws ExecutionException {
        deviceRegistry.sendTelemetry(A_DEVICE, "{someNonsense: \"telemetryValue\"}");

        verify(thingsBoardMock, never()).saveEntityTelemetry(any(), any(), any());
    }

    /**
     * Tests that heartbeats with a valid device leads to telemetry.
     * 
     * @throws JsonProcessingException JSON processing shall not fail
     * @throws ExecutionException AAS execution shall not fail
     */
    @Test
    public void imAlive_sendImAliveAsTelemetry() throws JsonProcessingException, ExecutionException {
        Device device = mockDevice();
        deviceRegistry.imAlive(A_DEVICE);

        verify(thingsBoardMock).saveEntityTelemetry(eq(device.getId()), any(), eq(getJsonNode("{\"active\": true}")));
    }

    /**
     * Tests that heartbeats with unknown device does not lead to telemetry.
     * 
     * @throws JsonProcessingException JSON processing shall not fail
     * @throws ExecutionException AAS execution shall not fail
     */
    @Test
    public void imAlive_withUnknownDevice_shouldNotSendImAliveAsTelemetry() throws JsonProcessingException, 
        ExecutionException {
        deviceRegistry.imAlive(A_DEVICE);

        verify(thingsBoardMock, never()).saveEntityTelemetry(any(), any(), any());
    }

    /**
     * Mocks a device.
     * 
     * @return the mocked device
     */
    @NotNull
    private Device mockDevice() {
        UUID deviceId = UUID.randomUUID();
        Device device = new Device(new DeviceId(deviceId));
        device.setName(A_DEVICE);
        Optional<Device> deviceOptional = Optional.of(device);
        when(thingsBoardMock.getTenantDevice(eq(A_DEVICE))).thenReturn(deviceOptional);
        when(thingsBoardMock.getDeviceById(eq(new DeviceId(deviceId)))).thenReturn(deviceOptional);
        return device;
    }

    /**
     * Returns a JSON note from the given {@code string}.
     * 
     * @param string the string
     * @return the JSON node
     * @throws JsonProcessingException in case of failures
     */
    private JsonNode getJsonNode(String string) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(string);
    }

}