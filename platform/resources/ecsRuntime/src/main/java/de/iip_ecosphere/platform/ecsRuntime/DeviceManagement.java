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

package de.iip_ecosphere.platform.ecsRuntime;

import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryClient;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryClientFactory;
import de.iip_ecosphere.platform.ecsRuntime.ssh.RemoteAccessServer;
import de.iip_ecosphere.platform.ecsRuntime.ssh.RemoteAccessServerFactory;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.iip_aas.Id;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.slf4j.LoggerFactory;

/**
 * DeviceManagement lifecycle methods.
 * This class provides easy access to the device registry
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class DeviceManagement {

    private static RemoteAccessServer remoteAccessServer;

    /**
     * Get the DeviceRegistryClient.
     *
     * @return the DeviceRegistryClient
     * @throws IOException if the resource submodel is not available.
     */
    public static DeviceRegistryClient getRegistryClient() throws IOException {
        return DeviceRegistryClientFactory.createDeviceRegistryClient();
    }

    /**
     * Get the RemoteAccessServer singleton.
     * @return the RemoteAccessServer
     */
    public static RemoteAccessServer getRemoteAccessServer() {
        if (null == remoteAccessServer) {
            remoteAccessServer = RemoteAccessServerFactory.create();
        }
        return remoteAccessServer;
    }

    /**
     * Initialization routine for device registration.
     */
    public static void initializeDevice() {
        try {
            DeviceRegistryClient registryClient = DeviceRegistryClientFactory
                .createDeviceRegistryClient();
            SubmodelElementCollection device = registryClient.getDevice(Id.getDeviceIdAas());

            if (null == device) {
                String ip = NetUtils.getOwnIP();
                registryClient.addDevice(Id.getDeviceIdAas(), ip);
            }

            RemoteAccessServer remoteAccessServer = getRemoteAccessServer();
            remoteAccessServer.start();
        } catch (ExecutionException e) {
            LoggerFactory.getLogger(DeviceManagement.class).error("Initializing Device: " + e.getMessage());
        }
    }

    /**
     * Method for device removal.
     */
    public static void removeDevice() {
        try {
            DeviceRegistryClient registryClient = getRegistryClient();
            SubmodelElementCollection device = registryClient.getDevice(Id.getDeviceIdAas());
            if (null != device) {
                registryClient.removeDevice(Id.getDeviceIdAas());
            }
        } catch (IOException | ExecutionException e) {
            LoggerFactory.getLogger(DeviceManagement.class).error("Removing Device: " + e.getMessage());
        }
    }
}
