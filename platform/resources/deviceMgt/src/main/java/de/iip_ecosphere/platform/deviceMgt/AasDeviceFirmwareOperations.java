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

import de.iip_ecosphere.platform.deviceMgt.ecs.EcsAasClient;
import de.iip_ecosphere.platform.deviceMgt.storage.Storage;
import de.iip_ecosphere.platform.deviceMgt.storage.StorageFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * A service provider implementation for {@link DeviceFirmwareOperations} which
 * uses aas as the communication protocol. For this purpose it will notify the
 * DeviceManagementAas that a device needs an update.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class AasDeviceFirmwareOperations implements DeviceFirmwareOperations {
    
    @Override
    public void updateRuntime(String id) throws ExecutionException {
        Storage runtimeStorage = StorageFactory.getInstance().createRuntimeStorage();
        String downloadUrl = null;
        try {
            EcsAasClient ecsAasClient = new EcsAasClient(id);
            String runtimeName = ecsAasClient.getRuntimeName();
            Integer newRuntimeVersion = getHighestVersion(runtimeStorage, runtimeName);
            if (newRuntimeVersion != -1) {
                downloadUrl = runtimeStorage.generateDownloadUrl(runtimeName + "_" + newRuntimeVersion);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (downloadUrl != null) {
            DeviceManagementAas.notifyUpdateRuntime(id, downloadUrl);
        }
    }

    /**
     * Returns the highest version available in {@code storage}.
     * 
     * @param storage the storage
     * @param runtimeName the runtime name
     * @return the highest version, {@code -1} for none
     */
    private Integer getHighestVersion(Storage storage, String runtimeName) {
        return storage.list().stream()
            .filter(key -> key.startsWith(storage.getPrefix() + runtimeName + "_"))
            .map(key -> {
                int lastUnderscore = key.lastIndexOf("_");
                String versionString = key.substring(lastUnderscore + 1);
                return Integer.parseInt(versionString);
            })
            .findFirst()
            .orElse(-1);
    }
}
