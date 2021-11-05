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

import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;

import java.util.Set;

/**
 * A DeviceRegistryClient is used for easy access to the DeviceRegistry.
 * Moreover, it adds some methods, so one can obtain the aas Devices and the
 * aas Device.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public interface DeviceRegistryClient extends DeviceRegistryOperations {

    /**
     * Collects all aas devices into a set.
     * @return a set of SubmodelElementCollections which contain information about the devices.
     */
    public Set<SubmodelElementCollection> getDevices();

    /**
     * Get a specific device.
     *
     * @param resourceId the resourceId the device is on
     * @return a SubmodelElementCollection containing information about the device
     */
    public SubmodelElementCollection getDevice(String resourceId);

}
