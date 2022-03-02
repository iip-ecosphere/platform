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

import de.iip_ecosphere.platform.deviceMgt.ssh.SshRemoteManagementOperations;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * The DeviceManagementFactory is capable of creating the DeviceManagement.
 * It uses the ServiceLoader so new functionality can be added easily.
 *
 * In case there are no service providers for the operations default implementations
 * will be used. E.g.:
 * <ul>
 *     <li>
 *         {@link AasDeviceFirmwareOperations}
 *     </li>
 *     <li>
 *         {@link AasDeviceResourceConfigOperations}
 *     </li>
 *     <li>
 *         {@link SshRemoteManagementOperations}
 *     </li>
 * </ul>
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class DeviceManagementFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceManagementFactory.class.getName());

    private static DeviceManagement management;
    private static DeviceRemoteManagementOperations remoteManagementOperations;
    private static DeviceFirmwareOperations firmwareOperations;
    private static DeviceResourceConfigOperations resourceConfigOperations;

    /**
     * Returns the device management instance.
     * 
     * @return the device management instance
     */
    public static DeviceManagement getDeviceManagement() {
        if (null == management) {
            if (null == firmwareOperations) {
                Optional<DeviceFirmwareOperations> first = ServiceLoaderUtils
                        .findFirst(DeviceFirmwareOperations.class);
                if (first.isPresent()) {
                    firmwareOperations = first.get();
                } else {
                    firmwareOperations = new AasDeviceFirmwareOperations();
                    LOGGER.warn("No DeviceFirmwareOperations implementation available, " 
                        + "fall back to default implementation: Direct AAS Method");
                }
            }

            if (null == resourceConfigOperations) {
                Optional<DeviceResourceConfigOperations> first = ServiceLoaderUtils
                        .findFirst(DeviceResourceConfigOperations.class);
                if (first.isPresent()) {
                    resourceConfigOperations = first.get();
                } else {
                    resourceConfigOperations = new AasDeviceResourceConfigOperations();
                    LOGGER.warn("No DeviceResourceConfigOperations implementation available, " 
                        + "fall back to default implementation: Direct AAS Method");
                }
            }

            if (null == remoteManagementOperations) {
                Optional<DeviceRemoteManagementOperations> first = ServiceLoaderUtils
                        .findFirst(DeviceRemoteManagementOperations.class);
                if (first.isPresent()) {
                    remoteManagementOperations = first.get();
                } else {
                    remoteManagementOperations = new SshRemoteManagementOperations();
                    LOGGER.warn("No DeviceRemoteManagementOperations implementation available, " 
                        + "fall back to default implementation: SSHProxy.");
                }
            }
            management = new DeviceManagementImpl(firmwareOperations,
                    remoteManagementOperations,
                    resourceConfigOperations);
        }

        return management;
    }

    /**
     * Resets the device management to its default state.
     * Used for testing only.
     */
    protected static void resetDeviceManagement() {
        management = null;
        firmwareOperations = null;
        remoteManagementOperations = null;
        resourceConfigOperations = null;
    }
}
