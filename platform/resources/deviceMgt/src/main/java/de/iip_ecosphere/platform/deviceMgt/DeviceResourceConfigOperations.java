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
 * Defines the device resource operations.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public interface DeviceResourceConfigOperations {

    /**
     * Sets the device configuration.
     * 
     * @param id the identifier
     * @param configPath the configuration path
     * @throws ExecutionException in case that setting the configuration fails
     */
    void setConfig(String id, String configPath) throws ExecutionException;
}
