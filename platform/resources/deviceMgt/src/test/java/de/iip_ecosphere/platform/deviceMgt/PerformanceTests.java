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

import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryClient;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryClientFactory;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasPrintVisitor;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static de.iip_ecosphere.platform.support.iip_aas.AasUtils.fixId;

/**
 * Performance tests.
 * 
 * @author Dennis Pidun, University of Hildesheim
 */
public class PerformanceTests {

    @SuppressWarnings("unused")
    private static Aas aas;
    private static Server implServer;
    private static Server aasServer;

    /**
     * Initializes the test.
     * 
     * @throws IOException shall not occur
     */
    @BeforeClass
    public static void startup() throws IOException {
        AasPartRegistry.AasBuildResult res = AasPartRegistry.build(); //c -> c instanceof DeviceManagementAas
        AasPartRegistry.setAasSetup(AasPartRegistry.AasSetup.createLocalEphemeralSetup());
        implServer = res.getProtocolServerBuilder().build();
        implServer.start();
        aasServer = AasPartRegistry.deploy(res.getAas());
        aasServer.start();
        aas = AasPartRegistry.retrieveIipAas();

        ActiveAasBase.setNotificationMode(ActiveAasBase.NotificationMode.SYNCHRONOUS);
    }

    /**
     * Tests adding a single device.
     * 
     * @throws IOException in case of IO problems, shall not occur
     */
    @Test
    public void addDevice_single() throws IOException {
        DeviceRegistryClient deviceRegistryClient = DeviceRegistryClientFactory.createDeviceRegistryClient();

        List<String> ids = generateIds(10);
        createDevices(ids);
        System.out.println("time = " + add(ids, deviceRegistryClient));

        for (int i = 0; i < 3; i++) {
            ids = generateIds(1);
            createDevices(ids);
            System.out.println("time = " + add(ids, deviceRegistryClient));
        }
    }

    /**
     * Tests mass addition of devices.
     * 
     * @throws IOException in case of IO problems, shall not occur
     */
    @Test
    public void addDevice_mass() throws IOException {
        DeviceRegistryClient deviceRegistryClient = DeviceRegistryClientFactory.createDeviceRegistryClient();

        List<String> ids = generateIds(500);
        createDevices(ids);
        System.out.println("time = " + add(ids, deviceRegistryClient));
    }

    /**
     * Tests the removal of a device.
     * 
     * @throws IOException in case of IO problems, shall not occur
     */
    @Test
    public void removeDevice_single() throws IOException {
        DeviceRegistryClient deviceRegistryClient = DeviceRegistryClientFactory.createDeviceRegistryClient();

        List<String> ids = generateIds(10);
        createDevices(ids);
        add(ids, deviceRegistryClient);
        System.out.println("time = " + remove(ids, deviceRegistryClient));

        for (int i = 0; i < 3; i++) {
            ids = generateIds(1);
            createDevices(ids);
            add(ids, deviceRegistryClient);
            System.out.println("time = " + remove(ids, deviceRegistryClient));
        }
    }

    /**
     * Tests mass removal of devices.
     * 
     * @throws IOException in case of IO problems, shall not occur
     * @throws InterruptedException in case of timeouts, shall not occur
     */
    @Test
    public void removeDevice_mass() throws IOException, InterruptedException {
        DeviceRegistryClient deviceRegistryClient = DeviceRegistryClientFactory.createDeviceRegistryClient();

        List<String> ids = generateIds(100);
        createDevices(ids);
        add(ids, deviceRegistryClient);
        Thread.sleep(200);
        System.out.println("time = " + remove(ids, deviceRegistryClient));
    }

    /**
     * Adds devices based on their ids.
     * 
     * @param ids the ids
     * @param deviceRegistryClient the device registry client to test
     * @return the elapsed wall time [ms]
     */
    private long add(List<String> ids, DeviceRegistryClient deviceRegistryClient) {
        long start = System.currentTimeMillis();
        ids.forEach(id -> {
            try {
                deviceRegistryClient.addDevice(id, "AN_IP");
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
        long end = System.currentTimeMillis();
        return end - start;
    }

    /**
     * Removes devices based on their ids.
     * 
     * @param ids the ids
     * @param deviceRegistryClient the device registry client to test
     * @return the elapsed wall time [ms]
     */
    private long remove(List<String> ids, DeviceRegistryClient deviceRegistryClient) {
        long start = System.currentTimeMillis();
        ids.forEach(id -> {
            try {
                deviceRegistryClient.removeDevice(id);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
        long end = System.currentTimeMillis();
        return end - start;
    }

    /**
     * Creates a sequence of ids.
     * 
     * @param count the number of ids to create
     * @return the created ids
     */
    private List<String> generateIds(int count) {
        List<String> ids = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            ids.add("device" + i);
        }
        return ids;
    }

    /**
     * Creates devices for the given {@code ids}.
     * 
     * @param ids the device identifiers
     * @throws IOException if creation fails
     */
    private void createDevices(List<String> ids) throws IOException {
        ids.forEach(id -> {
            try {
                AasPartRegistry
                        .retrieveIipAas()
                        .getSubmodel(AasPartRegistry.NAME_SUBMODEL_RESOURCES)
                        .createSubmodelElementCollectionBuilder(fixId(id), false, false)
                        .build();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        AasPartRegistry.retrieveIipAas().accept(new AasPrintVisitor());
    }
}
