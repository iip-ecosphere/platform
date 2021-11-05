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
import de.iip_ecosphere.platform.support.aas.*;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import org.junit.*;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;
import static de.iip_ecosphere.platform.deviceMgt.registry.StubDeviceRegistryFactoryDescriptor.mockDeviceRegistry;

/**
 * Tests the {@link DeviceRegistryAas}.
 * 
 * @author Dennis Pidun, University of Hildesheim
 */
public class DeviceRegistryAasTest {

    public static final String A_VALID_DEVICE = "A_VALID_DEVICE";
    public static final String AN_INVALID_DEVICE = "AN_INVALID_DEVICE";
    public static final String SOME_TELEMETRY_DATA = "{\"someField\": \"someData\"}";
    public static final String AN_IP = "1.1.1.1";
    private static final Class<?> CONTRIBUTOR_CLASS = DeviceRegistryAas.class;

    private Aas aas;
    private Server implServer;
    private Server aasServer;
    private Submodel resourcesSubmodel;
    private SubmodelElementCollection deviceRegistry;

    // checkstyle: stop exception type check

    /**
     * Sets up the test.
     * 
     * @throws Exception shall not occur
     */
    @Before
    public void setUp() throws Exception {
        ActiveAasBase.setNotificationMode(ActiveAasBase.NotificationMode.SYNCHRONOUS);

        AasPartRegistry.setAasSetup(AasSetup.createLocalEphemeralSetup());
        AasPartRegistry.AasBuildResult res = AasPartRegistry.build(CONTRIBUTOR_CLASS::isInstance);

        implServer = res.getProtocolServerBuilder().build();
        implServer.start();
        aasServer = AasPartRegistry.deploy(res.getAas());
        aasServer.start();

        aas = AasPartRegistry.retrieveIipAas();
        aas.accept(new AasPrintVisitor());

        resourcesSubmodel = aas.getSubmodel(AasPartRegistry.NAME_SUBMODEL_RESOURCES);
        deviceRegistry = resourcesSubmodel
                .getSubmodelElementCollection(DeviceRegistryAas.NAME_COLL_DEVICE_REGISTRY);
    }

    // checkstyle: resume exception type check

    /**
     * Teardown method, reset mocks.
     */
    @After
    public void tearDown() {
        implServer.stop(true);
        aasServer.stop(true);

        // reset Mocks, as Implementation build DeviceRegistry only once
        // otherwise old invocations-counter won't reset
        Mockito.reset(mockDeviceRegistry());
    }

    /**
     * Tests that the contributor class is loaded.
     */
    @Test
    public void init_contributorClassLoads() {
        Assert.assertTrue(AasPartRegistry.contributorClasses().contains(CONTRIBUTOR_CLASS));
    }

    /**
     * Tests that the AAS contribution is deployed.
     */
    @Test
    public void init_contributedAasIsDeployed() {
        Assert.assertNotNull(resourcesSubmodel);
        Assert.assertNotNull(deviceRegistry);
    }

    /**
     * Adding a valid device adds the device.
     * 
     * @throws ExecutionException AAS execution problems shall not occur
     * @throws IOException general IO problems shall not occur
     */
    @Test
    public void op_addDevice_withValidDeviceIdentifier_addsDevice() throws ExecutionException, IOException {
        mockDeviceResource(A_VALID_DEVICE);
        DeviceRegistryAasClient client = new DeviceRegistryAasClient();
        client.addDevice(A_VALID_DEVICE, AN_IP);

        Set<SubmodelElementCollection> devices = client.getDevices();
        Assert.assertNotNull(devices);

        int elementsCount = devices.size();
        Assert.assertEquals(1, elementsCount);
        SubmodelElementCollection device = devices.stream().findFirst().get();
        Assert.assertNotNull(device);
        Property managedId = device.getProperty(DeviceRegistryAas.NAME_PROP_MANAGED_DEVICE_ID);
        Assert.assertNotNull(managedId);
        Assert.assertEquals(A_VALID_DEVICE, managedId.getValue());
        AasPartRegistry.retrieveIipAas().accept(new AasPrintVisitor());
    }

    /**
     * Adding a valid device exposes the IP.
     * 
     * @throws ExecutionException AAS execution problems shall not occur
     * @throws IOException general IO problems shall not occur
     */
    @Test
    public void op_addDevice_validDevice_exposesIp() throws IOException, ExecutionException {
        mockDeviceResource(A_VALID_DEVICE);
        DeviceRegistryAasClient client = new DeviceRegistryAasClient();
        client.addDevice(A_VALID_DEVICE, AN_IP);

        SubmodelElementCollection device = client.getDevices().stream().findFirst().get();
        Property property = device.getProperty(DeviceRegistryAas.NAME_PROP_DEVICE_IP);
        Assert.assertNotNull(property);
        Assert.assertEquals(AN_IP, property.getValue());
    }

    /**
     * Removing a device with valid identifier causes removing the device.
     * 
     * @throws ExecutionException AAS execution problems shall not occur
     * @throws IOException general IO problems shall not occur
     */
    @Test
    public void op_removeDevice_withValidDeviceIdentifier_removesDevice() throws ExecutionException, IOException {
        mockDeviceResource(A_VALID_DEVICE);
        DeviceRegistryAasClient client = new DeviceRegistryAasClient();
        int beforeCount = client.getDevices().size();

        client.addDevice(A_VALID_DEVICE, AN_IP);

        // refresh client, as its underlying submodel is outdated
        client = new DeviceRegistryAasClient();
        int addCount = client.getDevices().size();
        Assert.assertEquals(1, addCount);

        client.removeDevice(A_VALID_DEVICE);

        client = new DeviceRegistryAasClient();
        int afterCount = client.getDevices().size();

        Assert.assertEquals(beforeCount, afterCount);
    }

    /**
     * Heartbeat leads to a callable.
     * 
     * @throws IOException shall not occur
     * @throws ExecutionException AAS execution problems shall not occur
     */
    @Test
    public void op_imAlive_isCallable() throws IOException, ExecutionException {
        Operation operation = getOperation(DeviceRegistryAas.NAME_OP_IM_ALIVE);

        Assert.assertNotNull(operation);

        Object invoke = operation.invoke(A_VALID_DEVICE);
        Assert.assertNotNull(invoke);
    }

    /**
     * Returns an AAS operation from the resources submodel/device registry collection.
     * 
     * @param operationName the operation name
     * @return the operation
     */
    private Operation getOperation(String operationName) {
        Operation operation = aas.getSubmodel(AasPartRegistry.NAME_SUBMODEL_RESOURCES)
                .getSubmodelElementCollection(DeviceRegistryAas.NAME_COLL_DEVICE_REGISTRY)
                .getOperation(operationName);
        return operation;
    }

    /**
     * Hardbeat leads to registry entry.
     * 
     * @throws ExecutionException AAS execution problems shall not occur
     */
    @Test
    public void op_imAlive_callsImAliveFromRegistry() throws ExecutionException {
        // create mockRegistry to check if it get Calls
        DeviceRegistry mockRegistry = mockDeviceRegistry();

        // invoke our operation under test
        getOperation(DeviceRegistryAas.NAME_OP_IM_ALIVE).invoke(A_VALID_DEVICE);

        // verify that the actual registry is getting called
        verify(mockRegistry).imAlive(eq(A_VALID_DEVICE));
    }

    /**
     * Sending telemetry leads to a callable.
     * 
     * @throws ExecutionException AAS execution problems shall not occur
     */
    @Test
    public void op_sendTelemetry_isCallable() throws ExecutionException {
        Operation operation = getOperation(DeviceRegistryAas.NAME_OP_SEND_TELEMETRY);

        Assert.assertNotNull(operation);

        Object invoke = operation.invoke(A_VALID_DEVICE, SOME_TELEMETRY_DATA);
        Assert.assertNotNull(invoke);
    }

    /**
     * Sending telemetry leads to telemetry in the registry.
     * 
     * @throws ExecutionException AAS execution problems shall not occur
     */
    @Test
    public void op_sendTelemetry_deliversTelemetryToRegistry() throws ExecutionException {
        DeviceRegistry mockRegistry = mockDeviceRegistry();

        getOperation(DeviceRegistryAas.NAME_OP_SEND_TELEMETRY).invoke(A_VALID_DEVICE, SOME_TELEMETRY_DATA);

        verify(mockRegistry).sendTelemetry(eq(A_VALID_DEVICE), eq(SOME_TELEMETRY_DATA));
    }

    /**
     * Tests removing an invalid device identifier does not cause removing the device.
     * 
     * @throws ExecutionException AAS execution problems shall not occur
     * @throws IOException general IO problems shall not occur
     */
    @Test
    public void op_removeDevice_withInvalidDeviceIdentifier_doesNotRemoveDevice() 
        throws ExecutionException, IOException {
        DeviceRegistryAasClient client = new DeviceRegistryAasClient();
        int beforeCount = client.getDevices().size();

        client.removeDevice(AN_INVALID_DEVICE);
        client = new DeviceRegistryAasClient();
        int afterCount = client.getDevices().size();

        Assert.assertEquals(beforeCount, afterCount);
    }

    /**
     * Mocks a device resource.
     * 
     * @param aDeviceId the id of the device to mock
     * @throws IOException shall not occur
     */
    public static void mockDeviceResource(String aDeviceId) throws IOException {
        ActiveAasBase.getSubmodel(AasPartRegistry.NAME_SUBMODEL_RESOURCES)
                .createSubmodelElementCollectionBuilder(aDeviceId, false, false)
                .build();
    }

}
