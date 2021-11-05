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

import static org.mockito.Mockito.mock;

/**
 * Stub Service Implementation for {@link DeviceManagement}.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class StubDeviceManagement implements DeviceFirmwareOperations, DeviceResourceConfigOperations, 
    DeviceRemoteManagementOperations {

    private static DeviceRemoteManagementOperations managementOperationsStub;
    private static DeviceFirmwareOperations firmwareOperationsStub;
    private static DeviceResourceConfigOperations resourceConfigOperationsStub;

    /**
     * Creates or gets the firmwareOperations mock.
     * @return the mock
     */
    static DeviceFirmwareOperations mockFirmwareOperations() {
        if (firmwareOperationsStub == null) {
            firmwareOperationsStub = mock(DeviceFirmwareOperations.class);
        }
        return firmwareOperationsStub;
    }

    /**
     * Creates or gets the resourceConfigOperations mock.
     * @return the mock
     */
    static DeviceResourceConfigOperations mockResourceConfigOperations() {
        if (resourceConfigOperationsStub == null) {
            resourceConfigOperationsStub = mock(DeviceResourceConfigOperations.class);
        }
        return resourceConfigOperationsStub;
    }

    /**
     * Creates or gets the remoteManagementOperations mock.
     * @return the mock
     */
    static DeviceRemoteManagementOperations mockRemoteManagementOperations() {
        if (managementOperationsStub == null) {
            managementOperationsStub = mock(DeviceRemoteManagementOperations.class);
        }
        return managementOperationsStub;
    }

    @Override
    public void updateRuntime(String id) throws ExecutionException {
        if (firmwareOperationsStub != null) {
            firmwareOperationsStub.updateRuntime(id);
        }
    }

    @Override
    public SSHConnectionDetails establishSsh(String id) throws ExecutionException {
        if (managementOperationsStub != null) {
            return managementOperationsStub.establishSsh(id);
        }

        return null;
    }

    @Override
    public void setConfig(String id, String configPath) throws ExecutionException {
        if (resourceConfigOperationsStub != null) {
            resourceConfigOperationsStub.setConfig(id, configPath);
        }
    }
}
