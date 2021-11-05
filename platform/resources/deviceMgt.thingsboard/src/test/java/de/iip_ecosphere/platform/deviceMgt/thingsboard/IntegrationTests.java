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

import de.iip_ecosphere.platform.deviceMgt.DeviceDescriptor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.thingsboard.rest.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Implements the setup.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IntegrationTests {

    public static final String A_DEVICE = "A_DEVICE";
    public static final String AN_IP = "AN_IP";

    private ThingsBoardDeviceRegistry registry;
    private RestClient restClient;

    /**
     * Configures the test before running.
     */
    @Before
    public void setUp() {
        ThingsBoardDeviceRegistryFactoryDescriptor factory =
                new ThingsBoardDeviceRegistryFactoryDescriptor();
        registry = (ThingsBoardDeviceRegistry) factory.createDeviceRegistryInstance();
        restClient = new RestClient(ThingsBoardDeviceRegistryFactoryDescriptor.BASE_URL);
        restClient.login(ThingsBoardDeviceRegistryFactoryDescriptor.USERNAME,
                ThingsBoardDeviceRegistryFactoryDescriptor.PASSWORD);
    }

    /**
     * Cleans up after testing.
     */
    @After
    public void tearDown() {
        registry.getIds().forEach(d -> {
            registry.removeDevice(d);
        });
    }

    /**
     * Tests that the REST client is able to connect.
     */
    @Test
    public void restClientIsAbleToConnect() {
        Assert.assertNotNull(restClient);
    }

    /**
     * Tests that creating a device registry creats an instance.
     */
    @Test
    public void createDeviceRegistryInstance_createsInstance() {
        Assert.assertNotNull(registry);
        Assert.assertNotNull(registry.getRestClient());
    }

    /**
     * Tests adding a valid device works.
     */
    @Test
    public void addDevice_withDevice_shouldAddDevice() {
        registry.addDevice(A_DEVICE, AN_IP);
        Assert.assertTrue(this.restClient.getTenantDevice(A_DEVICE).isPresent());
    }

    /**
     * Tests adding an invalid device does not work.
     */
    @Test
    public void addDevice_withOutDevice_shouldNotAddDevice() {
        int sizeBefore = registry.getIds().size();
        registry.addDevice("", AN_IP);
        registry.addDevice(null, AN_IP);
        registry.addDevice(A_DEVICE, "");
        registry.addDevice(A_DEVICE, null);
        Assert.assertEquals(sizeBefore, registry.getIds().size());
        Assert.assertFalse(this.restClient.getTenantDevice(A_DEVICE).isPresent());
    }

    /**
     * Tests that adding multiple devices adds all devices.
     */
    @Test
    public void addMultipleDevices_shouldAddAllDevices() {
        List<String> ids = generateRandomIds(10);
        ids.forEach(id -> {
            registry.addDevice(id, AN_IP);
        });

        ids.forEach(id -> {
            Assert.assertTrue(this.restClient.getTenantDevice(id).isPresent());
        });
    }

    /**
     * Tests that removing a device works.
     */
    @Test
    public void removeDevice_removesDevice() {
        registry.addDevice(A_DEVICE, AN_IP);
        Assert.assertTrue(this.restClient.getTenantDevice(A_DEVICE).isPresent());
        registry.removeDevice(A_DEVICE);
        Assert.assertFalse(this.restClient.getTenantDevice(A_DEVICE).isPresent());
    }

    /**
     * Tests that a device has a valid id/ip.
     */
    @Test
    public void addDevice_withDeviceAndIp_hasDeviceIdInternalIdAndIp() {
        registry.addDevice(A_DEVICE, AN_IP);
        DeviceDescriptor device = registry.getDevice(A_DEVICE);

        Assert.assertEquals(A_DEVICE, device.getId());
        Assert.assertEquals(AN_IP, device.getIp());
        Assert.assertNotNull(device.getManagedId());
        Assert.assertNotEquals("", device.getManagedId());
    }

    /**
     * Tests that an added, none alive device shall be starting.
     */
    @Test
    public void getState_withDeviceAddedButNoAlive_shouldBeSTARTING() {
        registry.addDevice(A_DEVICE, AN_IP);
        DeviceDescriptor device = registry.getDevice(A_DEVICE);

        Assert.assertEquals(DeviceDescriptor.State.STARTING, device.getState());
    }

    /**
     * Tests that an added, alive device shall be available.
     * 
     * @throws ExecutionException if AAS operations fail, shall not occur
     */
    @Test
    public void getState_withDeviceAddedAndAlive_shouldBeAVAILABLE() throws ExecutionException {
        registry.addDevice(A_DEVICE, AN_IP);
        registry.imAlive(A_DEVICE);
        DeviceDescriptor device = registry.getDevice(A_DEVICE);

        Assert.assertEquals(DeviceDescriptor.State.AVAILABLE, device.getState());
    }

    /**
     * Tests that a timed out device shall be undefined.
     * 
     * @throws ExecutionException if AAS operations fail, shall not occur
     * @throws InterruptedException if timeouts occur, shall not happe
     */
    @Test
    public void getState_withTimedOutDevice_shouldBeUNDEFINED() throws ExecutionException, InterruptedException {
        int timeOutBefore = ThingsBoardDeviceDescriptor.getDeviceTimeout();
        registry.addDevice(A_DEVICE, AN_IP);
        registry.imAlive(A_DEVICE);
        ThingsBoardDeviceDescriptor.setDeviceTimeout(250);
        DeviceDescriptor device = registry.getDevice(A_DEVICE);
        Thread.sleep(300);
        Assert.assertEquals(DeviceDescriptor.State.UNDEFINED, device.getState());
        ThingsBoardDeviceDescriptor.setDeviceTimeout(timeOutBefore);
    }

    /**
     * Tests that getting a device descriptor returns valid data.
     */
    @Test
    public void getRuntime_withValidDevice_returnsNull() {
        // Usually a device' runtimeName/runtimeVersion is managed by the platform
        registry.addDevice(A_DEVICE, AN_IP);
        DeviceDescriptor device = registry.getDevice(A_DEVICE);

        Assert.assertNull(device.getRuntimeName());
        Assert.assertNull(device.getRuntimeVersion());
    }

    /**
     * Generates a given number of random ids.
     * 
     * @param count the number of ids to generate
     * @return the random ids
     */
    static List<String> generateRandomIds(int count) {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add(UUID.randomUUID().toString());
        }
        return ids;
    }
    
}