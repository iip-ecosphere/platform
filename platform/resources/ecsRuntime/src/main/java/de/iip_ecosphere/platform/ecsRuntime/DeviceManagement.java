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

import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistrationResponse;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryClient;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryClientFactory;
import de.iip_ecosphere.platform.ecsRuntime.ssh.RemoteAccessServer;
import de.iip_ecosphere.platform.ecsRuntime.ssh.RemoteAccessServerFactory;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.iip_aas.Id;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

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
     * Routine for device registration.
     * 
     * @param onboard does this operation add the device the first time intentionally to the 
     *   platform ({@code true}) or is this just the startup registration ({@code false})
     * @return <b>null</b> if no operation was needed, an instance if a device registration/onboarding (trial) 
     *   was performed
     */
    public static DeviceRegistrationResponse addDevice(boolean onboard) throws ExecutionException {
        DeviceRegistrationResponse result = null;
        DeviceRegistryClient registryClient = DeviceRegistryClientFactory
            .createDeviceRegistryClient();
        SubmodelElementCollection device = registryClient.getDevice(Id.getDeviceIdAas());

        if (null == device) {
            if (onboard) {
                String ip = NetUtils.getOwnIP();
                result = registryClient.addDevice(Id.getDeviceIdAas(), ip);
            } else {
                throw new ExecutionException("This decvice was not onboarded before. Stopping.", null);
            }
        }

        RemoteAccessServer remoteAccessServer = getRemoteAccessServer();
        remoteAccessServer.start();
        return result;
    }

    /**
     * Method for device removal.
     * 
     * @param offboard does this operation remove the device intentionally from the 
     *   platform ({@code true}) or is this just a shutdown unregistration ({@code false})
     */
    public static void removeDevice(boolean offboard) throws ExecutionException {
        try {
            DeviceRegistryClient registryClient = getRegistryClient();
            SubmodelElementCollection device = registryClient.getDevice(Id.getDeviceIdAas());
            if (null != device) {
                if (offboard) {
                    registryClient.removeDevice(Id.getDeviceIdAas());
                } 
            } else {
                throw new ExecutionException("Device was not registered.", null);
            }
        } catch (IOException | ExecutionException e) {
            throw new ExecutionException("Removing Device: " + e.getMessage(), null);
        }
    }

}
