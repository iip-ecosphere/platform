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


import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;

/**
 * Creates instances of {@link DeviceRegistryClient}.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class DeviceRegistryClientFactory {

    /**
     * Creates a device registry client instance.
     * 
     * @return the instance
     */
    public static DeviceRegistryClient createDeviceRegistryClient() {
        return ServiceLoaderUtils
            .findFirst(DeviceRegistryClient.class)
            .orElse(null);
    }

}
