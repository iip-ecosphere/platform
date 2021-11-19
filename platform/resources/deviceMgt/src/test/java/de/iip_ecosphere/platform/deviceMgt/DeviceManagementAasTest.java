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

package de.iip_ecosphere.platform.deviceMgt;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryAasClient;
import de.iip_ecosphere.platform.deviceMgt.registry.StubDeviceRegistryFactoryDescriptor;
import de.iip_ecosphere.platform.deviceMgt.storage.Storage;
import de.iip_ecosphere.platform.deviceMgt.storage.StubStorageFactoryDescriptor;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import org.junit.*;

import de.iip_ecosphere.platform.support.aas.*;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static de.iip_ecosphere.platform.deviceMgt.registry.StubDeviceRegistryFactoryDescriptor.mockDeviceRegistry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link DeviceManagementAas} with respect to {@link DeviceManagementAasClient}.
 * 
 * @author Dennis Pidun, University of Hildesheim
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceManagementAasTest {

    public static final String A_DEVICE_ID = "A_DEVICE";
    public static final String A_DEVICE_IP = "A_DEVICE_IP";
    public static final String A_CONFIG_PATH = "A_CONFIG_PATH";
    public static final String A_CONFIG_DOWNLOAD_URI = AasDeviceResourceConfigOperations.A_CONFIG_DOWNLOAD_URI;
    public static final String A_LOCATION = AasDeviceResourceConfigOperations.A_LOCATION;
    public static final String A_DOWNLOADURL = "A_DOWNLOADURL";

    private static Aas aas;
    private static Server implServer;
    private static Server aasServer;

    private static MockedStatic<ServiceLoaderUtils> serviceLoader;

    /**
     * Initializes the test.
     * 
     * @throws IOException shouldn't be thrown
     */
    @BeforeClass
    public static void startup() throws IOException {
        AasPartRegistry.AasBuildResult res = AasPartRegistry.build(); //c -> c instanceof DeviceManagementAas
        AasPartRegistry.setAasSetup(AasSetup.createLocalEphemeralSetup());
        implServer = res.getProtocolServerBuilder().build();
        implServer.start();
        aasServer = AasPartRegistry.deploy(res.getAas());
        aasServer.start();
        aas = AasPartRegistry.retrieveIipAas();

        ActiveAasBase.setNotificationMode(ActiveAasBase.NotificationMode.SYNCHRONOUS);
    }
    
    /**
     * Shuts down the test.
     */
    @AfterClass
    public static void shutdown() {
        implServer.stop(false);
        aasServer.stop(false);
    }

    // checkstyle: stop exception type check
    
    /**
     * Test teardown method, basically resets all mocks
     * and the service loader mechanism.
     *
     * @throws Exception shouldn't be thrown
     */
    @After
    public void tearDown() throws Exception {
        Mockito.reset(mockDeviceRegistry());
        Mockito.reset(StubDeviceManagement.mockFirmwareOperations());
        Mockito.reset(StubDeviceManagement.mockResourceConfigOperations());
        Mockito.reset(StubEcsAas.getCreateRemoteConnectionCredentialsMock());
        Mockito.reset(StubEcsAas.getUpdateRuntimeMock());
        Mockito.reset(StubEcsAas.getSetConfigMock());
        if (serviceLoader != null && !serviceLoader.isClosed()) {
            serviceLoader.close();
            DeviceManagementFactory.resetDeviceManagement();
        }

        AasPartRegistry.retrieveIipAas().accept(new AasPrintVisitor());
    }
    
    /**
     * Test setup method.
     *
     * @throws Exception shouldn't be thrown
     */
    @Before
    public void setUp() throws Exception {
        AasPartRegistry.retrieveIipAas().accept(new AasPrintVisitor());
    }
    
    // checkstyle: resume exception type check

    /**
     * Tests if the AasContributorClass is loaded.
     */
    @Test
    public void init_contributorClassLoads() {
        Assert.assertTrue(AasPartRegistry.contributorClasses().contains(DeviceManagementAas.class));
    }

    /**
     * Tests if the AasContributor is deployed.
     *
     * @throws IOException shouldn't be thrown
     */
    @Test
    public void init_contributedAasIsDeployed() throws IOException {
        AasPartRegistry.retrieveIipAas().accept(new AasPrintVisitor());

        Submodel resourcesSubmodel = aas.getSubmodel(AasPartRegistry.NAME_SUBMODEL_RESOURCES);
        Assert.assertNotNull(resourcesSubmodel);
        SubmodelElementCollection deviceManager = resourcesSubmodel
                .getSubmodelElementCollection("deviceManager");
        Assert.assertNotNull(deviceManager);
    }

    /**
     * Tests if the kind is set to active.
     */
    @Test
    public void getKind_shouldBeActive() {
        AasContributor.Kind kind = new DeviceManagementAas().getKind();
        Assert.assertEquals(AasContributor.Kind.ACTIVE, kind);
    }

    /**
     * Tests if the AAS Operation "updateRuntime" won't update the
     * runtime on an invalid device through internal aas deviceManagement.
     *
     * It is using the default implementation for runtime management (eg. AAS connection).
     * @throws IOException shouldn't be thrown
     * @throws ExecutionException shouldn't be thrown
     */
    @Test
    public void op_updateRuntime_withInvalidDeviceAndAasDevManager_wontUpdateRuntime() 
        throws IOException, ExecutionException {
        unloadFirmwareOperations();

        new DeviceManagementAasClient().updateRuntime(A_DEVICE_ID);

        verify(StubEcsAas.getUpdateRuntimeMock(), times(0)).apply(any());
    }

    /**
     * Tests if the AAS Operation "updateRuntime" updates the runtime
     * on a valid device through internal aas deviceManagement.
     *
     * It is using the default implementation for runtime management (eg. AAS connection)
     * @throws IOException shouldn't be thrown
     * @throws ExecutionException shouldn't be thrown
     */
    @Test
    public void op_updateRuntime_withValidDeviceAndAasDevManager_updatesRuntimeOnDevice()
            throws IOException, ExecutionException {
        // Make Device available in Registry and make it a managed device
        makeDeviceAvailable();

        // ServiceLoader loads StubDeviceManagement, but we want to test
        // the default Implementation if ServiceLoader cant find any
        unloadFirmwareOperations();

        Storage stubStorage = mock(Storage.class);
        when(stubStorage.list()).thenReturn(validRuntimesReducedListing());
        when(stubStorage.generateDownloadUrl(any())).thenReturn(A_DOWNLOADURL);
        when(stubStorage.getPrefix()).thenReturn("runtimes/");
        StubStorageFactoryDescriptor.setRuntimeStorage(stubStorage);

        new DeviceManagementAasClient().updateRuntime(A_DEVICE_ID);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(stubStorage).generateDownloadUrl(keyCaptor.capture());

        verify(StubEcsAas.getUpdateRuntimeMock(), times(1))
                .apply(eq(new String[]{A_DOWNLOADURL}));

        Assert.assertEquals("stubRuntime_3", keyCaptor.getValue());
    }

    /**
     * Returns a listing of valid runtimes.
     * 
     * @return the listing
     */
    private Set<String> validRuntimesReducedListing() {
        Set<String> listing = new HashSet<>();
        listing.add("runtimes/stubRuntime_1");
        listing.add("runtimes/stubRuntime_2");
        listing.add("runtimes/stubRuntime_3");
        listing.add("runtimes/def");
        listing.add("runtimes/ghi");
        return listing;
    }

    /**
     * Tests if the AAS Operation "updateRuntime" won't update the
     * runtime on an invalid device.
     *
     * It is using a third party implementation for runtime management
     * @throws IOException shouldn't be thrown
     * @throws ExecutionException shouldn't be thrown
     */
    @Test
    public void op_updateRuntime_withInvalidDeviceAndLoadedDevManager_wontUpdateRuntime() 
        throws IOException, ExecutionException {
        DeviceFirmwareOperations deviceFirmwareOperations = StubDeviceManagement.mockFirmwareOperations();

        new DeviceManagementAasClient().updateRuntime(A_DEVICE_ID);

        verify(deviceFirmwareOperations, never()).updateRuntime(eq(A_DEVICE_ID));
    }

    /**
     * Tests if the AAS Operation "updateRuntime" updates the
     * runtime on a valid device.
     *
     * It is using a third party implementation for runtime management
     * @throws IOException shouldn't be thrown
     * @throws ExecutionException shouldn't be thrown
     */
    @Test
    public void op_updateRuntime_withValidDeviceAndLoadedDevManager_updatesRuntimeOnDevice() 
        throws IOException, ExecutionException {
        DeviceFirmwareOperations deviceFirmwareOperations = StubDeviceManagement.mockFirmwareOperations();

        // Make Device available in Registry and make it a managed device
        makeDeviceAvailable();

        new DeviceManagementAasClient().updateRuntime(A_DEVICE_ID);

        verify(deviceFirmwareOperations, times(1)).updateRuntime(eq(A_DEVICE_ID));
    }

    /**
     * Tests if the AAS Operation "setConfig" won't set the config
     * on an invalid device through internal aas DeviceManagement.
     *
     * It is using the default implementation for configuration
     * management (eg. AAS connection)
     * @throws IOException shouldn't be thrown
     * @throws ExecutionException shouldn't be thrown
     */
    @Test
    public void op_setConfig_withInvalidDeviceAndAasDevManager_wontSetConfig() throws IOException, ExecutionException {
        unloadFirmwareOperations();

        new DeviceManagementAasClient().setConfig(A_DEVICE_ID, A_CONFIG_PATH);

        verify(StubEcsAas.getSetConfigMock(), times(0)).apply(any());
    }

    /**
     * Tests if the AAS Operation "setConfig" set the config
     * on a valid device through internal aas DeviceManagement.
     *
     * It is using the default implementation for configuration
     * management (eg. AAS connection)
     * @throws IOException shouldn't be thrown
     * @throws ExecutionException shouldn't be thrown
     */
    @Test
    public void op_setConfig_withValidDeviceAndAasDevManager_setsConfigOnDevice()
            throws IOException, ExecutionException {
        makeDeviceAvailable();
        unloadResourceConfigOperations();

        new DeviceManagementAasClient().setConfig(A_DEVICE_ID, A_CONFIG_PATH);

        verify(StubEcsAas.getSetConfigMock(), times(1))
                .apply(eq(new String[]{A_CONFIG_DOWNLOAD_URI, A_LOCATION}));
    }

    /**
     * Tests if the AAS Operation "setConfig" won't set the config
     * on an invalid device.
     *
     * It is using a third party implementation for configuration management
     * @throws IOException shouldn't be thrown
     * @throws ExecutionException shouldn't be thrown
     */
    @Test
    public void op_setConfig_withInvalidDeviceAndLoadedDevManager_wontSetConfig() 
        throws IOException, ExecutionException {
        DeviceResourceConfigOperations configOperations = StubDeviceManagement
                .mockResourceConfigOperations();

        // missing makeDeviceAvailable() does the effect: the device is no managed device then

        new DeviceManagementAasClient().setConfig(A_DEVICE_ID, A_CONFIG_PATH);

        verify(configOperations, never()).setConfig(eq(A_DEVICE_ID), eq(A_CONFIG_PATH));
    }

    /**
     * Tests if the AAS Operation "setConfig" sets the config
     * on a valid device.
     *
     * It is using a third party implementation for configuration management
     * @throws IOException shouldn't be thrown
     * @throws ExecutionException shouldn't be thrown
     */
    @Test
    public void op_setConfig_withValidDeviceAndLoadedDevManager_setsConfigOnDevice() 
        throws IOException, ExecutionException {
        DeviceResourceConfigOperations configOperations = StubDeviceManagement
                .mockResourceConfigOperations();
        makeDeviceAvailable();

        new DeviceManagementAasClient().setConfig(A_DEVICE_ID, A_CONFIG_PATH);

        verify(configOperations, times(1))
                .setConfig(eq(A_DEVICE_ID), eq(A_CONFIG_PATH));
    }

    /**
     * Tests if the AAS Operation "establishSSH" returns valid
     * ssh details under the condition that a real device is
     * given, which responses with a valid result.
     *
     * It is using a third party implementation for ssh servers
     * @throws IOException shouldn't be thrown
     * @throws ExecutionException shouldn't be thrown
     */
    @Test
    public void op_establishSsh_withValidDeviceAndLoadedDevManager_createsConnectionDetails() 
        throws IOException, ExecutionException {
        DeviceRemoteManagementOperations deviceRemoteManagementOperations =
                StubDeviceManagement.mockRemoteManagementOperations();
        DeviceRemoteManagementOperations.SSHConnectionDetails expectedConnectionDetails
                = new DeviceRemoteManagementOperations.SSHConnectionDetails(
                A_DEVICE_IP,
                1234,
                "username",
                "password"
        );
        makeDeviceAvailable();
        when(deviceRemoteManagementOperations.establishSsh(eq(A_DEVICE_ID)))
                .thenReturn(expectedConnectionDetails);

        DeviceRemoteManagementOperations.SSHConnectionDetails connectionDetails
                = new DeviceManagementAasClient().establishSsh(A_DEVICE_ID);

        Assert.assertEquals(expectedConnectionDetails, connectionDetails);
        verify(deviceRemoteManagementOperations, times(1))
                .establishSsh(eq(A_DEVICE_ID));

    }

    /**
     * Tests if the AAS Operation "establishSSH" returns valid
     * connection details under the condition that its a valid
     * device and using the default implementation.
     *
     * It is using the default implementation for ssh servers (proxy &lt;-&gt; apachesshd)
     * @throws IOException shouldn't be thrown
     * @throws ExecutionException shouldn't be thrown
     */
    @Test
    public void op_establishSsh_withValidDeviceAndDefaultDevManager_letsDeviceCreateCredentials() 
        throws IOException, ExecutionException {
        unloadRemoteAccessOperations();
        makeDeviceAvailable();
        DeviceRemoteManagementOperations.SSHConnectionDetails expectedConnectionDetails
                = new DeviceRemoteManagementOperations.SSHConnectionDetails(
                A_DEVICE_IP,
                5555,
                "username",
                "password"
        );
        when(StubEcsAas.getCreateRemoteConnectionCredentialsMock().apply(any()))
                .thenReturn("{\"key\": \"username\", \"secret\": \"password\"}");

        DeviceRemoteManagementOperations.SSHConnectionDetails connectionDetails =
                new DeviceManagementAasClient().establishSsh(A_DEVICE_ID);

        verify(StubEcsAas.getCreateRemoteConnectionCredentialsMock(), times(1))
                .apply(eq(new String[]{}));
        Assert.assertEquals(expectedConnectionDetails, connectionDetails);
    }

    /**
     * Tests if the AAS Operation "establishSSH" returns null
     * under the condition that a real device is given, which
     * responses with an invalid result.
     *
     * It is using the default implementation for ssh servers (proxy &lt;-&gt; apachesshd)
     * @throws IOException shouldn't be thrown
     * @throws ExecutionException shouldn't be thrown
     */
    @Test
    public void op_establishSsh_withValidDeviceAndInvalidDeviceResponse_returnsNull() 
        throws IOException, ExecutionException {
        unloadRemoteAccessOperations();
        makeDeviceAvailable();

        when(StubEcsAas.getCreateRemoteConnectionCredentialsMock().apply(any()))
                .thenReturn("{\"jsonWithoutTheRightKeys\": \"some_nonesense\", \"anotherField\": \"anotherValue\"}");

        DeviceRemoteManagementOperations.SSHConnectionDetails connectionDetails =
                new DeviceManagementAasClient().establishSsh(A_DEVICE_ID);

        verify(StubEcsAas.getCreateRemoteConnectionCredentialsMock(), times(1))
                .apply(eq(new String[]{}));

        Assert.assertNull(connectionDetails);
    }

    /**
     * Removes loaded FirmwareOperations from ServiceLoaderMechanism
     *
     * Uses static mocks for this process, which should be avoided,
     * but needed in this case.
     */
    static void unloadFirmwareOperations() {
        serviceLoader = mockStatic(ServiceLoaderUtils.class);
        serviceLoader.when(() -> ServiceLoaderUtils.findFirst(DeviceFirmwareOperations.class))
                .thenReturn(Optional.empty());
        resetDeviceManagement();
    }

    /**
     * Removes loaded ResourceConfigOperations from ServiceLoaderMechanism
     *
     * Uses static mocks for this process, which should be avoided,
     * but needed in this case.
     */
    static void unloadResourceConfigOperations() {
        serviceLoader = mockStatic(ServiceLoaderUtils.class);
        serviceLoader.when(() -> ServiceLoaderUtils.findFirst(DeviceResourceConfigOperations.class))
                .thenReturn(Optional.empty());
        resetDeviceManagement();
    }

    /**
     * Removes loaded RemoteAccessOperations from ServiceLoaderMechanism
     *
     * Uses static mocks for this process, which should be avoided,
     * but needed in this case.
     */
    static void unloadRemoteAccessOperations() {
        serviceLoader = mockStatic(ServiceLoaderUtils.class);
        serviceLoader.when(() -> ServiceLoaderUtils.findFirst(DeviceRemoteManagementOperations.class))
                .thenReturn(Optional.empty());
        resetDeviceManagement();
    }

    /**
     * Registers a device and turns it into a managed device
     * Uses mock DeviceRegistry.
     *
     * @throws ExecutionException shouldn't be thrown
     * @throws IOException shouldn't be thrown
     */
    static void makeDeviceAvailable() throws ExecutionException, IOException {
        new DeviceRegistryAasClient().addDevice(StubEcsAas.A_DEVICE, A_DEVICE_IP);
        // No actual DeviceRegistry is present: make Device visible
        DeviceDescriptor mockDevice = mock(DeviceDescriptor.class);
        when(StubDeviceRegistryFactoryDescriptor.mockDeviceRegistry().getDevice(eq(A_DEVICE_ID)))
            .thenReturn(mockDevice);
    }

    /**
     * Resets the device management.
     */
    private static void resetDeviceManagement() {
        DeviceManagementFactory.resetDeviceManagement();
        DeviceManagementFactory.getDeviceManagement();
    }
}
