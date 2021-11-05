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

import java.util.concurrent.ExecutionException;

/**
 * Partially implements {@link DeviceRegistry} and thus forms
 * the AAS part of the {@link DeviceRegistry}.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public abstract class AbstractDeviceRegistry implements DeviceRegistry {

    @Override
    public void addDevice(String id, String ip) throws ExecutionException {
        DeviceRegistryAas.notifyDeviceAdded(id, id, ip);
    }

    @Override
    public void removeDevice(String id) throws ExecutionException {
        DeviceRegistryAas.notifyDeviceRemoved(id);
    }
}
