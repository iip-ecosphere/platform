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

import de.iip_ecosphere.platform.deviceMgt.DeviceDescriptor;

import java.util.Collection;
import java.util.Set;

/**
 * A place to interact with and locate registered devices.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public interface DeviceRegistry extends DeviceRegistryOperations {

    /**
     * Returns the ids of all registered devices.
     *
     * @return the ids
     */
    public Set<String> getIds();

    /**
     * Returns the managed ids of all registered devices.
     *
     * @return the ids
     */
    public Set<String> getManagedIds();

    /**
     * Returns the available (registered and active) devices.
     *
     * @return the device descriptors
     */
    public Collection<? extends DeviceDescriptor> getDevices();

    /**
     * Returns a device descriptor of an available device.
     *
     * @param id the resource id of the device (might be <b>null</b> or invalid)
     * @return the related device descriptor or <b>null</b> if the device is not known at all
     */
    public DeviceDescriptor getDevice(String id);

    /**
     * Returns a device descriptor of an available device.
     *
     * @param id the managed id of the device (might be <b>null</b> or invalid)
     * @return the related device descriptor or <b>null</b> if the device is not known at all
     */
    public DeviceDescriptor getDeviceByManagedId(String id);

}
