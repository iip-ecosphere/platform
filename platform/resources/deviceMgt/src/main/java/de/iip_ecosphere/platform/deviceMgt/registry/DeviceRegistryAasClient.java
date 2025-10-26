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
import de.iip_ecosphere.platform.support.json.Json;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static de.iip_ecosphere.platform.support.json.JsonResultWrapper.fromJson;

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
     */
    public DeviceRegistryAasClient() {
        super(DeviceRegistryAas.NAME_SUBMODEL, DeviceRegistryAas.NAME_COLL_DEVICE_REGISTRY, null);
    }

    @Override
    public Set<SubmodelElementCollection> getDevices() {
        Submodel resources = getSubmodel();
        if (null != resources) {
            return StreamSupport.stream(resources.submodelElements().spliterator(), false)
                .filter(e -> e instanceof SubmodelElementCollection)
                .map(e -> (SubmodelElementCollection) e)
                .filter(e -> e.getProperty(DeviceRegistryAas.NAME_PROP_MANAGED_DEVICE_ID) != null)
                .collect(Collectors.toSet());
        } else {
            return new HashSet<>();
        }
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
        DeviceRegistrationResponse result = null;
        if (null != getSubmodel()) {
            String json = fromJson(getOperation(DeviceRegistryAas.NAME_OP_DEVICE_ADD).invoke(id, ip));
            if (null != json) {
                try {
                    result = Json.fromJsonDflt(json.toString(), DeviceRegistrationResponse.class);
                } catch (IOException e) {
                    // result = null;
                }
            }
            requestRefresh();
        }
        return result;        
    }

    @Override
    public void removeDevice(String id) throws ExecutionException {
        if (null != getSubmodel()) {
            getOperation(DeviceRegistryAas.NAME_OP_DEVICE_REMOVE).invoke(id);
            requestRefresh();
        }
    }

    @Override
    public void imAlive(String id) throws ExecutionException {
        if (null != getSubmodel()) {
            getOperation(DeviceRegistryAas.NAME_OP_IM_ALIVE).invoke(id);
        }
    }

    @Override
    public void sendTelemetry(String id, String telemetryData) throws ExecutionException {
        if (null != getSubmodel()) {
            getOperation(DeviceRegistryAas.NAME_OP_SEND_TELEMETRY).invoke(id, telemetryData);
        }
    }
}
