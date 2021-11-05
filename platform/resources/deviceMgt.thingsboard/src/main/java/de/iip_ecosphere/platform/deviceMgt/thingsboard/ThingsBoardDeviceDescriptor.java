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

package de.iip_ecosphere.platform.deviceMgt.thingsboard;

import de.iip_ecosphere.platform.deviceMgt.DeviceDescriptor;
import org.thingsboard.rest.client.RestClient;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.kv.AttributeKvEntry;
import org.thingsboard.server.common.data.kv.TsKvEntry;

import java.util.*;

/**
 * Implements the things board device descriptor.
 * 
 * @author Dennis Pidun, University of Hildesheim
 */
public class ThingsBoardDeviceDescriptor implements DeviceDescriptor {

    private static int deviceTimeout = 15000;
    private Device tbDevice;
    private RestClient tbClient;

    /**
     * Creates the descriptor.
     * 
     * @param tbDevice the things board device
     * @param tbClient the things board client
     */
    public ThingsBoardDeviceDescriptor(Device tbDevice, RestClient tbClient) {
        this.tbDevice = tbDevice;
        this.tbClient = tbClient;
    }
    
    /**
     * Returns the device timeout.
     * 
     * @return the device timeout [ms]
     */
    public static int getDeviceTimeout() {
        return deviceTimeout;
    }
    
    /**
     * Changes the device timeout. [public for testing]
     * 
     * @param timeout the timeout [ms]
     */
    public static void setDeviceTimeout(int timeout) {
        deviceTimeout = timeout;
    }

    @Override
    public String getId() {
        return tbDevice.getName();
    }

    @Override
    public String getManagedId() {
        return tbDevice.getId().toString();
    }

    @Override
    public String getIp() {
        DeviceId entityId = new DeviceId(UUID.fromString(this.getManagedId()));
        List<String> attributesKeys = this.tbClient.getAttributeKeysByScope(entityId, "device");
        List<AttributeKvEntry> attributeKvEntries = this.tbClient.getAttributeKvEntries(entityId, attributesKeys);
        return attributeKvEntries.stream().filter(key -> key.getKey().equals("ip"))
                .map(kv -> (String) kv.getValue())
                .findFirst().orElse(null);
    }

    @Override
    public String getRuntimeVersion() {
        return null;
    }

    @Override
    public String getRuntimeName() {
        return null;
    }

    @Override
    public String getResourceId() {
        return tbDevice.getName();
    }

    @Override
    public State getState() {
        if (!this.tbClient.getTimeseriesKeys(this.tbDevice.getId()).contains("active")) {
            return State.STARTING;
        }
        List<TsKvEntry> active = this.tbClient.getLatestTimeseries(
                this.tbDevice.getId(), Collections.singletonList("active"));
        TsKvEntry latest = active.stream()
                .max(Comparator.comparingLong(TsKvEntry::getTs))
                .orElse(null);

        if (System.currentTimeMillis() - latest.getTs() < deviceTimeout) {
            return State.AVAILABLE;
        }

        return State.UNDEFINED;
    }
}
