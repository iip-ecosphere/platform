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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryAas;
import de.iip_ecosphere.platform.support.iip_aas.SubmodelElementsCollectionClient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * An AasClient which implements a {@link SubmodelElementsCollectionClient} and provides easy
 * access to the registry functions through the aas.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class DeviceManagementAasClient extends SubmodelElementsCollectionClient
        implements DeviceFirmwareOperations, DeviceRemoteManagementOperations, DeviceResourceConfigOperations {

    /**
     * Default constructor.
     * @throws IOException if the resource submodel could not be found
     */
    public DeviceManagementAasClient() throws IOException {
        super(DeviceRegistryAas.NAME_SUBMODEL, DeviceManagementAas.NAME_COLL_DEVICE_MANAGER);
    }

    @Override
    public void updateRuntime(String id) throws ExecutionException {
        getOperation(DeviceManagementAas.NAME_OP_UPDATE_RUNTIME).invoke(id);
    }

    @Override
    public SSHConnectionDetails establishSsh(String id) throws ExecutionException {
        String operationResult = (String) getOperation(DeviceManagementAas.NAME_OP_ESTABLISH_SSH).invoke(id);
        SSHConnectionDetails connectionDetails = null;
        if (operationResult != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                connectionDetails = mapper.readValue(operationResult, SSHConnectionDetails.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return connectionDetails;
    }

    @Override
    public void setConfig(String id, String configPath) throws ExecutionException {
        getOperation(DeviceManagementAas.NAME_OP_SET_CONFIG).invoke(id, configPath);
    }
}
