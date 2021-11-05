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
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static de.iip_ecosphere.platform.deviceMgt.registry.StubDeviceRegistryFactoryDescriptor.mockDeviceRegistry;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link DeviceRegistryFactory}.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class DeviceRegistryFactoryTest {

    public static final String A_DEVICE_ID = "A_DEVICE_ID";
    public static final String SOME_TELEMETRY = "someTelemetry";
    public static final String AN_IP = "1.1.1.1";
    public static final String A_SECRET = "A_SECRET";

    // checkstyle: stop exception type check

    /**
     * Shuts down the test.
     * 
     * @throws Exception shall not occur
     */
    @After
    public void tearDown() throws Exception {
        // reset Mocks, as Implementation build DeviceRegistry only once
        // otherwise old invocations-counter won't reset
        Mockito.reset(mockDeviceRegistry());
    }

    // checkstyle: resume exception type check

    /**
     * Tests that a device registry with service loader configuration returns a device registry.
     */
    @Test
    public void getDeviceRegistry_withServiceLoaderConfiguration_returnsDeviceRegistry() {
        DeviceRegistry stubRegistry = StubDeviceRegistryFactoryDescriptor.mockDeviceRegistry();
        when(stubRegistry.getIds()).thenReturn(Collections.singleton(A_DEVICE_ID));

        Assert.assertNotNull(DeviceRegistryFactory.getDeviceRegistry());

        DeviceRegistry dReg = DeviceRegistryFactory.getDeviceRegistry();
        Assert.assertTrue(dReg.getIds().contains(A_DEVICE_ID));
    }


    // ignore VAB-Exception: ProviderException, this test does not focus on AAS
    /**
     * Tests all functions with a fake registry leading to downstream calls.
     * 
     * @throws ExecutionException AAS execution problems shall not occur
     */
    @Test
    public void allFunctions_withFakeRegistry_callsDownstreamRegistryFunction() throws ExecutionException {
        NotificationMode origMode = ActiveAasBase.setNotificationMode(NotificationMode.NONE);
        DeviceRegistry stubRegistry = StubDeviceRegistryFactoryDescriptor.mockDeviceRegistry();
        DeviceRegistry deviceRegistry = DeviceRegistryFactory.getDeviceRegistry();

        deviceRegistry.getDevices();
        verify(stubRegistry).getDevices();

        deviceRegistry.getDevice(A_DEVICE_ID);
        verify(stubRegistry).getDevice(eq(A_DEVICE_ID));

        deviceRegistry.removeDevice(A_DEVICE_ID);
        verify(stubRegistry).removeDevice(eq(A_DEVICE_ID));

        deviceRegistry.addDevice(A_DEVICE_ID, AN_IP);
        verify(stubRegistry).addDevice(eq(A_DEVICE_ID), eq(AN_IP));

        deviceRegistry.imAlive(A_DEVICE_ID);
        verify(stubRegistry).imAlive(eq(A_DEVICE_ID));

        deviceRegistry.sendTelemetry(A_DEVICE_ID, SOME_TELEMETRY);
        verify(stubRegistry).sendTelemetry(eq(A_DEVICE_ID), eq(SOME_TELEMETRY));

        deviceRegistry.getDeviceByManagedId(A_DEVICE_ID);
        verify(stubRegistry).getDeviceByManagedId(eq(A_DEVICE_ID));

        deviceRegistry.getManagedIds();
        verify(stubRegistry).getManagedIds();

        deviceRegistry.getIds();
        verify(stubRegistry).getIds();
        ActiveAasBase.setNotificationMode(origMode);
    }

    /**
     * Returns that getting a device also returns a device.
     */
    @Test
    public void getDevice_returnsDeviceFromDeviceRegistry() {
        DeviceRegistry reg = StubDeviceRegistryFactoryDescriptor.mockDeviceRegistry();
        DeviceDescriptor deviceDescriptor = mock(DeviceDescriptor.class);
        DeviceDescriptor.State aState = DeviceDescriptor.State.AVAILABLE;
        when(deviceDescriptor.getState()).thenReturn(aState);
        when(reg.getDevice(eq(A_DEVICE_ID))).thenReturn(deviceDescriptor);


        DeviceDescriptor device = DeviceRegistryFactory.getDeviceRegistry().getDevice(A_DEVICE_ID);
        Assert.assertEquals(deviceDescriptor, device);
        Assert.assertEquals(aState, device.getState());
    }

    /**
     * Returns that getting a device with no plugin returns <b>null</b>.
     */
    @Test
    public void getDeviceRegistry_withNoPlugin_returnsNull() {

        MockedStatic<ServiceLoaderUtils> serviceLoader = mockStatic(ServiceLoaderUtils.class);
        serviceLoader.when(() -> ServiceLoaderUtils.findFirst(DeviceRegistryFactoryDescriptor.class))
                .thenReturn(Optional.empty());
        DeviceRegistryFactory.resetDeviceRegistryFactory();

        Assert.assertNull(DeviceRegistryFactory.getDeviceRegistry());
        serviceLoader.close();
    }
}
