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

/**
 * The {@code DeviceDescriptor} is the foundation to describe a specific device.
 * Implementing {@code DeviceRegistry} Services should provide an implementation
 * for this interface.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public interface DeviceDescriptor {

    /**
     * Gets the resource id of the device.
     *
     * @return the id of the device
     */
    String getId();

    /**
     * Gets the internal id of the device. May be the same as resourceId
     *
     * @return the managedId of the device
     */
    String getManagedId();

    /**
     * Gets the ip of the device.
     *
     * @return the ip of the device
     */
    String getIp();

    /**
     * Gets the runtime version of the device.
     *
     * @return the runtime version of the device
     */
    String getRuntimeVersion();

    /**
     * Gets the runtime name of the device.
     *
     * @return the runtime name of the device
     */
    String getRuntimeName();

    /**
     * Gets the identifier of the device.
     *
     * @return the identifier of the device
     */
    String getResourceId();

    /**
     * Gets the state of the device.
     *
     * @return the state of the device
     */
    State getState();

    /**
     * The sasic states.
     */
    enum State {
        STARTING(),
        AVAILABLE(),
        STOPPING(),
        STOPPED(),
        UNDEFINED(),
    }

}
