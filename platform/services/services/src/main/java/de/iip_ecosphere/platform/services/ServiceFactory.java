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

package de.iip_ecosphere.platform.services;

import java.util.Optional;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;

/**
 * Provides access to the service manager instance.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServiceFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceFactory.class.getName());
    private static ServiceFactoryDescriptor desc;
    private static ServiceManager manager = null;
    private static AasSetup setup;

    /**
     * Initializes this factory.
     */
    private static void init() {
        if (null == desc) {
            ServiceLoader<ServiceFactoryDescriptor> loader = ServiceLoader.load(ServiceFactoryDescriptor.class);
            Optional<ServiceFactoryDescriptor> first = ServiceLoaderUtils.findFirst(loader);
            if (first.isPresent()) {
                desc = first.get();
            } else {
                LOGGER.error("No Service manager implementation known.");
            }
        }
        if (null != desc) {
            setup = desc.getAasSetup();
        } else {
            setup = new AasSetup();
        }
    }
    
    /**
     * Returns the service manager.
     * 
     * @return the service manager
     */
    public static ServiceManager getServiceManager() {
        if (null == manager) {
            init();
            if (null != desc) {
                manager = desc.createInstance();
                if (null != manager) {
                    LOGGER.info("Service manager implementation registered: " + manager.getClass().getName());
                }
            }
        }
        return manager;
    }
    
    /**
     * Returns the actual AAS setup for the implementing service manager.
     * 
     * @return the AAS setup
     */
    public static AasSetup getAasSetup() {
        if (null == setup) {
            init();
        }
        return setup;
    }
    
    /**
     * Defines the AAs setup instance [for testing].
     * 
     * @param instance the new setup instance
     */
    public static void setAasSetup(AasSetup instance) {
        setup = instance;
    }

}
