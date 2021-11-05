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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * The DeviceRegistryFactory is capable of creating the DeviceRegistry.
 * It uses the ServiceLoader so new functionality can be added easily.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class DeviceRegistryFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceRegistryFactory.class.getName());

    private static DeviceRegistryFactoryDescriptor desc;
    private static DeviceRegistryProxy proxy;

    /**
     * Gets or creates a DeviceRegistry using the first found service provider implementations for
     * {@link DeviceRegistryFactoryDescriptor}, which is able to create the instance.
     *
     * @return a DeviceRegistry or null if no {@link DeviceRegistryFactoryDescriptor} is found
     */
    public static DeviceRegistry getDeviceRegistry() {
        if (null == desc) {
            Optional<DeviceRegistryFactoryDescriptor> first = ServiceLoaderUtils
                    .findFirst(DeviceRegistryFactoryDescriptor.class);
            if (first.isPresent()) {
                desc = first.get();
            } else {
                LOGGER.error("No Device Registry implementation available.");
            }
        }

        if (null == proxy) {
            if (null != desc) {
                proxy = new DeviceRegistryProxy(desc.createDeviceRegistryInstance());
            }
        }

        return proxy;
    }

    /**
     * Resets the factory. Only meant for testing!
     */
    static void resetDeviceRegistryFactory() {
        desc = null;
        proxy = null;
    }
}
