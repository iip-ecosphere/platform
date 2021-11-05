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

import java.util.concurrent.ExecutionException;

/**
 * A service provider interface for DeviceFirmwareOperations.
 * Can be used to update the Runtime of a specific device.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public interface DeviceFirmwareOperations {

    /**
     * Update the runtime of the given device with the id.
     * @param id the id of the device
     * @throws ExecutionException if the execution of this operation fails
     */
    public void updateRuntime(String id) throws ExecutionException;

}
