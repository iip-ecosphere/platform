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

package de.iip_ecosphere.platform.deviceMgt.ssh;

import de.iip_ecosphere.platform.deviceMgt.Credentials;
import de.iip_ecosphere.platform.deviceMgt.DeviceRemoteManagementOperations;
import de.iip_ecosphere.platform.deviceMgt.ecs.EcsAasClient;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryAas;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryAasClient;
import de.iip_ecosphere.platform.support.aas.Property;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * A service provider implementation for {@link DeviceRemoteManagementOperations} which
 * uses ssh as the communication protocol.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class SshRemoteManagementOperations implements DeviceRemoteManagementOperations {


    @Override
    public SSHConnectionDetails establishSsh(String id) throws ExecutionException {

        String deviceIp = null;
        Credentials credentials = null;
        try {
            credentials = new EcsAasClient(id).createRemoteConnectionCredentials();
            Property ipProp = new DeviceRegistryAasClient().getDevice(id).getProperty(
                DeviceRegistryAas.NAME_PROP_DEVICE_IP);
            deviceIp = (String) ipProp.getValue();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (credentials == null) {
            return null;
        }

        return new SSHConnectionDetails(deviceIp, 5555, credentials.getKey(), credentials.getSecret());
    }
}
