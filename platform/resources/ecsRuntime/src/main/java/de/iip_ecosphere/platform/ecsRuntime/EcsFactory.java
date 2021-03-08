/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.ecsRuntime;

import java.util.Optional;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to the ECS instances.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EcsFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EcsFactory.class.getName());
    private static ContainerManager manager = null;

    /**
     * Returns the service manager.
     * 
     * @return the service manager
     */
    public static ContainerManager getContainerManager() {
        if (null == manager) {
            ServiceLoader<EcsFactoryDescriptor> loader = ServiceLoader.load(EcsFactoryDescriptor.class);
            Optional<EcsFactoryDescriptor> first = loader.findFirst();
            if (first.isPresent()) {
                manager = first.get().createContainerManagerInstance();
                if (null != manager) {
                    LOGGER.warn("Container manager implementation registered: " + manager.getClass().getName());
                }
            } else {
                LOGGER.error("No Container manager implementation known.");
            }
        }
        return manager;
    }

}
