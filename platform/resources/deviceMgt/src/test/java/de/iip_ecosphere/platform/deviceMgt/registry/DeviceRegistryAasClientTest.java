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

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.AasPrintVisitor;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static de.iip_ecosphere.platform.deviceMgt.registry.StubDeviceRegistryFactoryDescriptor.mockDeviceRegistry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link DeviceRegistryAas} with the help of {@link DeviceRegistryAasClient}.
 * 
 * @author Dennis Pidun, University of Hildesheim
 */
public class DeviceRegistryAasClientTest {

    public static final String A_DEVICE_ID = "A_DEVICE_ID";
    public static final String AN_INVALID_DEVICE_ID = "AN_INVALID_DEVICE";
    public static final String SOME_TELEMETRY = "{\"testField\": 123}";
    public static final String AN_IP = "1.1.1.1";
    private static final Class<?> CONTRIBUTOR_CLASS = DeviceRegistryAas.class;

    private Server implServer;
    private Server aasServer;
    private DeviceRegistryAasClient client;

    // checkstyle: stop exception type check
    
    /**
     * Setup method, create aas server/registry and deploy the DeviceRegistryAas contributor class.
     * 
     * @throws Exception should not be thrown
     */
    @Before
    public void setUp() throws Exception {
        ActiveAasBase.setNotificationMode(ActiveAasBase.NotificationMode.SYNCHRONOUS);

        AasPartRegistry.setAasSetup(AasPartRegistry.AasSetup.createLocalEphemeralSetup());
        AasPartRegistry.AasBuildResult res = AasPartRegistry.build(CONTRIBUTOR_CLASS::isInstance);

        implServer = res.getProtocolServerBuilder().build();
        implServer.start();
        aasServer = AasPartRegistry.deploy(res.getAas());
        aasServer.start();

        client = new DeviceRegistryAasClient();
    }
    
    // checkstyle: resume exception type check

    /**
     * Teardown method, reset mocks.
     */
    @After
    public void tearDown() {
        // reset Mocks, as Implementation build DeviceRegistry only once
        // otherwise old invocations-counter won't reset
        Mockito.reset(mockDeviceRegistry());
    }

    /**
     * Test getDevices, assert that devices-list is empty with no devices.
     */
    @Test
    public void getDevices_withNoDevices_shouldReturnEmptyCollection() {
        Assert.assertEquals(0, client.getDevices().size());
    }

    /**
     * Test getDevices, assert that devices-list has one device with added device.
     * 
     * @throws ExecutionException should not be thrown
     * @throws IOException should not be thrown
     */
    @Test
    public void getDevices_withOneDevice_shouldReturnCollectionWithTheOneDevice() 
        throws ExecutionException, IOException {
        DeviceRegistryAasTest.mockDeviceResource(A_DEVICE_ID);
        client.addDevice(A_DEVICE_ID, AN_IP);

        AasPartRegistry.retrieveIipAas().accept(new AasPrintVisitor());

        // update client
        client = new DeviceRegistryAasClient();

        Assert.assertEquals(1, client.getDevices().size());
        Assert.assertNotNull(client.getDevice(A_DEVICE_ID));
    }

    /**
     * Test getDevice, check that an invalid device is not registered.
     */
    @Test
    public void getDevice_withInvalidDevice_shouldReturnNull() {
        Assert.assertNull(client.getDevice(AN_INVALID_DEVICE_ID));
    }

    /**
     * Test getDevice, check that a valid device is registered.
     * 
     * @throws ExecutionException if AAS operations fail, shall not happen
     * @throws IOException if IO operations fail, shall not happen
     */
    @Test
    public void getDevice_withValidDevice_shouldNotReturnNull() throws ExecutionException, IOException {
        DeviceRegistryAasTest.mockDeviceResource(A_DEVICE_ID);
        client.addDevice(A_DEVICE_ID, AN_IP);

        client = new DeviceRegistryAasClient();
        SubmodelElementCollection device = client.getDevice(A_DEVICE_ID);
        Assert.assertNotNull(device);
        Assert.assertNotNull(device.getProperty(DeviceRegistryAas.NAME_PROP_MANAGED_DEVICE_ID).getValue());
    }

    /**
     * Test addDevice and provide a valid device. Should add the device.
     *
     * @throws ExecutionException should not be thrown
     * @throws IOException should not be thrown
     */
    @Test
    public void addDevice_withDevice_shouldAddDevice() throws ExecutionException, IOException {
        DeviceRegistryAasTest.mockDeviceResource(A_DEVICE_ID);
        client.addDevice(A_DEVICE_ID, AN_IP);
        client = new DeviceRegistryAasClient();
        Assert.assertNotNull(client.getDevice(A_DEVICE_ID));
    }

    /**
     * Tests sending telemetry leads to telemetry in the registry.
     * 
     * @throws ExecutionException should not be thrown
     */
    @Test
    public void sendTelemetry_withDeviceAndTelementry_shouldSendTelemetryToRegistry() throws ExecutionException {
        DeviceRegistry mockRegistry = mockDeviceRegistry();
        client.sendTelemetry(A_DEVICE_ID, SOME_TELEMETRY);
        verify(mockRegistry).sendTelemetry(eq(A_DEVICE_ID), eq(SOME_TELEMETRY));
    }

    /**
     * Test the imAlive method and verify that its sending the incomping aas operation
     * request to the registered device registry.
     *
     * @throws ExecutionException should not be thrown
     */
    @Test
    public void imAlive_withDevice_shouldCallImAliveFromRegistry() throws ExecutionException {
        DeviceRegistry mockRegistry = mockDeviceRegistry();
        client.imAlive(A_DEVICE_ID);
        verify(mockRegistry).imAlive(eq(A_DEVICE_ID));
    }
}