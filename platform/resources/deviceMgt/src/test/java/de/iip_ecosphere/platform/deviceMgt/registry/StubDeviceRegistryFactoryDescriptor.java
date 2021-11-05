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

import static org.mockito.Mockito.mock;

/**
 * Implements a stub device registry factory descriptor for mocking.
 * 
 * @author Dennis Pidun, University of Hildesheim
 */
public class StubDeviceRegistryFactoryDescriptor implements DeviceRegistryFactoryDescriptor {

    private static DeviceRegistry stub;

    /**
     * Returns the mocked device registry.
     * 
     * @return the device registry
     */
    public static DeviceRegistry mockDeviceRegistry() {
        if (stub == null) {
            stub = mock(DeviceRegistry.class);
        }
        return stub;
    }

    @Override
    public DeviceRegistry createDeviceRegistryInstance() {
        return mockDeviceRegistry();
    }

}
