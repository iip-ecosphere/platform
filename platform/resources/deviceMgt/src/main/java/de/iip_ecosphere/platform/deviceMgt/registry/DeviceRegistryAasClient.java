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

import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.iip_aas.SubmodelElementsCollectionClient;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper.fromJson;

/**
 * An AasClient which implements a {@link SubmodelElementsCollectionClient} and provides easy
 * access to the registry functions through the AAS.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class DeviceRegistryAasClient extends SubmodelElementsCollectionClient
        implements DeviceRegistryClient {

    /**
     * Creates an instance.
     * 
     * @throws IOException in case that the AAS cannot be found/connection cannot be established
     */
    public DeviceRegistryAasClient() throws IOException {
        super(DeviceRegistryAas.NAME_SUBMODEL, DeviceRegistryAas.NAME_COLL_DEVICE_REGISTRY);
    }

    @Override
    public Set<SubmodelElementCollection> getDevices() {
        Submodel resources = this.getSubmodel();

        return StreamSupport.stream(resources.submodelElements().spliterator(), false)
                .filter(e -> e instanceof SubmodelElementCollection)
                .map(e -> (SubmodelElementCollection) e)
                .filter(e -> e.getProperty(DeviceRegistryAas.NAME_PROP_MANAGED_DEVICE_ID) != null)
                .collect(Collectors.toSet());
    }

    @Override
    public SubmodelElementCollection getDevice(String resourceId) {
        return this.getDevices().stream()
                .filter(e -> e.getIdShort().equals(resourceId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public DeviceRegistrationResponse addDevice(String id, String ip) throws ExecutionException {
        String json = fromJson(getOperation(DeviceRegistryAas.NAME_OP_DEVICE_ADD).invoke(id, ip));
        DeviceRegistrationResponse result = null;
        if (null != json) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                result = objectMapper.readValue(json.toString(), DeviceRegistrationResponse.class);
            } catch (JsonProcessingException e) {
                // result = null;
            }
        }
        return result;        
    }

    @Override
    public void removeDevice(String id) throws ExecutionException {
        getOperation(DeviceRegistryAas.NAME_OP_DEVICE_REMOVE).invoke(id);
    }

    @Override
    public void imAlive(String id) throws ExecutionException {
        getOperation(DeviceRegistryAas.NAME_OP_IM_ALIVE).invoke(id);
    }

    @Override
    public void sendTelemetry(String id, String telemetryData) throws ExecutionException {
        getOperation(DeviceRegistryAas.NAME_OP_SEND_TELEMETRY).invoke(id, telemetryData);
    }
}
