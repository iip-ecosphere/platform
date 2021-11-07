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
import de.iip_ecosphere.platform.deviceMgt.DeviceDescriptor;
import de.iip_ecosphere.platform.deviceMgt.DeviceDescriptor.State;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Tests the basic registry.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BasicDeviceRegistryTest {

    public static final String AN_IP = "AN_IP";
    public static final String ANOTHER_IP = "ANOTHER_IP";
    public static final String ANOTHER_DEVICE = "ANOTHER_DEVICE";
    private static final String A_DEVICE = "A_DEVICE";
    private BasicDeviceRegistry deviceRegistry;

    /**
     * Configures the test before running.
     */
    @Before
    public void setUp() {
        deviceRegistry = new BasicDeviceRegistry();
    }

    /**
     * Cleans up after testing.
     */
    @Test
    public void getIds_withTwoDevices_shouldReturnListWithTwoIds() {
        Assert.assertTrue(deviceRegistry.getIds().isEmpty());
        deviceRegistry.addDevice(A_DEVICE, AN_IP);
        deviceRegistry.addDevice(ANOTHER_DEVICE, ANOTHER_IP);
        Set<String> ids = deviceRegistry.getIds();
        Assert.assertEquals(2, ids.size());
        Assert.assertTrue(ids.contains(A_DEVICE));
        Assert.assertTrue(ids.contains(ANOTHER_DEVICE));
        deviceRegistry.removeDevice(A_DEVICE);
        deviceRegistry.removeDevice(ANOTHER_DEVICE);
    }

    /**
     * Tests that requesting managed devices with two devices returns a list with two IDs.
     */
    @Test
    public void getManagedIds_withTwoDevices_shouldReturnListWithTwoIds() {
        Assert.assertTrue(deviceRegistry.getIds().isEmpty());
        deviceRegistry.addDevice(A_DEVICE, AN_IP);
        deviceRegistry.addDevice(ANOTHER_DEVICE, ANOTHER_IP);
        Set<String> ids = deviceRegistry.getManagedIds();
        Assert.assertEquals(2, ids.size());
        Assert.assertTrue(ids.contains(A_DEVICE));
        Assert.assertTrue(ids.contains(ANOTHER_DEVICE));
        deviceRegistry.removeDevice(A_DEVICE);
        deviceRegistry.removeDevice(ANOTHER_DEVICE);
    }

    /**
     * Tests that requesting devices with two devices returns a list with two IDs.
     */
    @Test
    public void getDevices_withTwoDevices_shouldReturnListWithTwoIds() {
        Assert.assertTrue(deviceRegistry.getIds().isEmpty());
        deviceRegistry.addDevice(A_DEVICE, AN_IP);
        deviceRegistry.addDevice(ANOTHER_DEVICE, ANOTHER_IP);
        Collection<? extends DeviceDescriptor> actualDevices = deviceRegistry.getDevices();
        Assert.assertEquals(2, actualDevices.size());
        Assert.assertEquals(1, actualDevices.stream().filter(d -> d.getId().equals(A_DEVICE)).count());
        Assert.assertEquals(1, actualDevices.stream().filter(d -> d.getId().equals(ANOTHER_DEVICE)).count());
        deviceRegistry.removeDevice(A_DEVICE);
        deviceRegistry.removeDevice(ANOTHER_DEVICE);
    }

    /**
     * Tests that requesting a device with valid ID returns the device.
     */
    @Test
    public void getDevice_withValidId_returnsDevice() {
        Assert.assertTrue(deviceRegistry.getIds().isEmpty());
        deviceRegistry.addDevice(A_DEVICE, AN_IP);
        DeviceDescriptor desc = deviceRegistry.getDevice(A_DEVICE);
        Assert.assertNotNull(desc);
        desc = deviceRegistry.getDevice(A_DEVICE);
        Assert.assertNotNull(desc);
        Assert.assertEquals(A_DEVICE, desc.getId());
        Assert.assertEquals(AN_IP, desc.getIp());
        Assert.assertEquals(A_DEVICE, desc.getManagedId());
        Assert.assertEquals(A_DEVICE, desc.getResourceId());
        deviceRegistry.removeDevice(A_DEVICE);
        Assert.assertTrue(deviceRegistry.getIds().isEmpty());
    }

    /**
     * Tests that requesting a device with invalid ID returns <b>null</b>.
     */
    @Test
    public void getDevice_withInvalidId_returnsNull() {
        Assert.assertTrue(deviceRegistry.getIds().isEmpty());
        DeviceDescriptor desc = deviceRegistry.getDevice(A_DEVICE);
        Assert.assertNull(desc);
        Assert.assertTrue(deviceRegistry.getIds().isEmpty());
    }

    /**
     * Tests that requesting a managed device with valid ID returns the device.
     */
    @Test
    public void getDeviceByManagedId_withValidId_returnsDevice() {
        Assert.assertTrue(deviceRegistry.getIds().isEmpty());
        deviceRegistry.addDevice(A_DEVICE, AN_IP);
        DeviceDescriptor desc = deviceRegistry.getDevice(A_DEVICE);
        Assert.assertNotNull(desc);
        desc = deviceRegistry.getDeviceByManagedId(A_DEVICE);
        Assert.assertNotNull(desc);
        Assert.assertEquals(A_DEVICE, desc.getId());
        Assert.assertEquals(AN_IP, desc.getIp());
        Assert.assertEquals(A_DEVICE, desc.getManagedId());
        Assert.assertEquals(A_DEVICE, desc.getResourceId());
        deviceRegistry.removeDevice(A_DEVICE);
        Assert.assertTrue(deviceRegistry.getIds().isEmpty());
    }

    /**
     * Tests that requesting a managed device with invalid ID returns <b>null</b>.
     */
    @Test
    public void getDeviceByManagedId_withInvalidId_returnsNull() {
        String uuid = UUID.randomUUID().toString();

        DeviceDescriptor desc = deviceRegistry.getDeviceByManagedId(uuid);
        Assert.assertNull(desc);
    }

    /**
     * Tests that adding a device with no IP does not cause adding the device.
     */
    @Test
    public void addDevice_withNoIp_wontAddDevice() {
        Assert.assertTrue(deviceRegistry.getIds().isEmpty());
        deviceRegistry.addDevice(A_DEVICE, null);
        deviceRegistry.addDevice(A_DEVICE, "");
        Assert.assertTrue(deviceRegistry.getIds().isEmpty());
    }

    /**
     * Tests that adding a device with no identifier does not cause adding the device.
     */
    @Test
    public void addDevice_withNoDeviceIdentifier_wontAddDevice() {
        Assert.assertTrue(deviceRegistry.getIds().isEmpty());
        deviceRegistry.addDevice(null, AN_IP);
        deviceRegistry.addDevice("", AN_IP);
        Assert.assertTrue(deviceRegistry.getIds().isEmpty());
    }

    /**
     * Tests that adding an already registered device updates the device.
     */
    @Test
    public void addDevice_withAlreadyRegisteredDevice_updatesDevice() {
        Assert.assertTrue(deviceRegistry.getIds().isEmpty());
        deviceRegistry.addDevice(A_DEVICE, AN_IP);
        deviceRegistry.addDevice(A_DEVICE, ANOTHER_IP);
        DeviceDescriptor desc = deviceRegistry.getDevice(A_DEVICE);
        Assert.assertNotNull(desc);
        Assert.assertEquals(A_DEVICE, desc.getId());
        Assert.assertEquals(ANOTHER_IP, desc.getIp());
        Assert.assertEquals(A_DEVICE, desc.getManagedId());
        Assert.assertEquals(A_DEVICE, desc.getResourceId());
        deviceRegistry.removeDevice(A_DEVICE);
        Assert.assertTrue(deviceRegistry.getIds().isEmpty());
    }

    /**
     * Tests that removing an valid device adds/removes the device.
     */
    @Test
    public void removeDevice_withValidDevice_addRemovesDevice() {
        Assert.assertTrue(deviceRegistry.getIds().isEmpty());
        deviceRegistry.addDevice(A_DEVICE, AN_IP);
        DeviceDescriptor desc = deviceRegistry.getDevice(A_DEVICE);
        Assert.assertNotNull(desc);
        Assert.assertEquals(desc.getId(), A_DEVICE);
        Assert.assertEquals(desc.getIp(), AN_IP);
        Assert.assertEquals(desc.getManagedId(), A_DEVICE);
        Assert.assertEquals(desc.getResourceId(), A_DEVICE);
        deviceRegistry.removeDevice(A_DEVICE);
        Assert.assertTrue(deviceRegistry.getIds().isEmpty());
    }

    /**
     * Tests that removing an invalid device (no device information) does not remove a device.
     */
    @Test
    public void removeDevice_withNoDevice_wontRemoveDevice() {
        Assert.assertTrue(deviceRegistry.getIds().isEmpty());
        deviceRegistry.addDevice(A_DEVICE, AN_IP);
        deviceRegistry.removeDevice("");
        deviceRegistry.removeDevice(null);
        deviceRegistry.removeDevice(ANOTHER_DEVICE);
        Assert.assertNotNull(deviceRegistry.getDevice(A_DEVICE));
        deviceRegistry.removeDevice(A_DEVICE);
        Assert.assertTrue(deviceRegistry.getIds().isEmpty());
    }

    /**
     * Tests that sending valid telemetry leads to saved data.
     * 
     * @throws JsonProcessingException JSON processing shall not fail
     * @throws ExecutionException AAS execution shall not fail
     */
    @Test
    public void sendTelemetry_withValidData_shouldSaveTelemetry() throws ExecutionException, JsonProcessingException {
        Assert.assertTrue(deviceRegistry.getIds().isEmpty());
        deviceRegistry.addDevice(A_DEVICE, AN_IP);
        DeviceDescriptor desc = deviceRegistry.getDevice(A_DEVICE);
        Assert.assertNotNull(desc);
        State before = desc.getState();
        Assert.assertNotNull(before);
        deviceRegistry.sendTelemetry(A_DEVICE, "{\"telemetryKey\": \"telemetryValue\"}"); // does not matter

        Assert.assertEquals(before, desc.getState());

        deviceRegistry.removeDevice(A_DEVICE);
    }

    /**
     * Tests that heartbeats with a valid device leads to telemetry.
     * 
     * @throws JsonProcessingException JSON processing shall not fail
     * @throws ExecutionException AAS execution shall not fail
     */
    @Test
    public void imAlive_sendImAliveAsTelemetry() throws JsonProcessingException, ExecutionException {
        Assert.assertTrue(deviceRegistry.getIds().isEmpty());
        deviceRegistry.addDevice(A_DEVICE, AN_IP);
        deviceRegistry.sendTelemetry(A_DEVICE, "{\"active\": true}");

        DeviceDescriptor desc = deviceRegistry.getDevice(A_DEVICE);
        Assert.assertNotNull(desc);
        Assert.assertEquals(State.AVAILABLE, desc.getState());

        deviceRegistry.removeDevice(A_DEVICE);
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
        Assert.assertTrue(deviceRegistry.getIds().isEmpty());
        deviceRegistry.addDevice(A_DEVICE, AN_IP);
        deviceRegistry.imAlive(A_DEVICE);

        DeviceDescriptor desc = deviceRegistry.getDevice(A_DEVICE);
        Assert.assertNotNull(desc);
        Assert.assertEquals(State.AVAILABLE, desc.getState());
        
        deviceRegistry.removeDevice(A_DEVICE);
    }

}