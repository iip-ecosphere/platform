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

package de.iip_ecosphere.platform.services;

import java.util.Optional;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to the service manager instance.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServiceFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceFactory.class.getName());
    private static ServiceManager manager = null;

    /**
     * Returns the service manager.
     * 
     * @return the service manager
     */
    public static ServiceManager getServiceManager() {
        if (null == manager) {
            ServiceLoader<ServiceFactoryDescriptor> loader = ServiceLoader.load(ServiceFactoryDescriptor.class);
            Optional<ServiceFactoryDescriptor> first = loader.findFirst();
            if (first.isPresent()) {
                manager = first.get().createInstance();
                if (null != manager) {
                    LOGGER.warn("Service manager implementation registered: " + manager.getClass().getName());
                }
            } else {
                LOGGER.error("No Service manager implementation known.");
            }
        }
        return manager;
    }

}
