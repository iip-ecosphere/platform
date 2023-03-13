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

import java.io.IOException;
import java.util.Optional;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import de.iip_ecosphere.platform.support.net.NetworkManagerSetup;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;

/**
 * Provides access to the service manager instance.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServiceFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceFactory.class.getName());
    private static ServiceFactoryDescriptor desc;
    private static ServiceManager manager = null;
    private static ServiceSetup service;
    private static AasSetup setup;
    private static TransportSetup transport;
    private static NetworkManagerSetup netwMgrSetup;

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
                LOGGER.warn("No Service manager implementation known.");
            }
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
    
    // TODO unify, simplify

    /**
     * Returns the actual service setup for the implementing service manager.
     * 
     * @return the service setup
     */
    public static ServiceSetup getSetup() {
        if (null == service) {
            init();
            if (null != desc) {
                service = desc.getSetup();
            }
            if (null == service) {
                try {
                    service = AbstractSetup.readFromYaml(ServiceSetup.class);
                } catch (IOException e) {
                    LoggerFactory.getLogger(ServiceFactory.class).warn("Cannot read setup: " + e.getMessage());
                }
                if (null == service) {
                    service = new ServiceSetup();
                }
            }
        }
        return service;
    }

    /**
     * Returns the actual AAS setup for the implementing service manager.
     * 
     * @return the AAS setup
     */
    public static AasSetup getAasSetup() {
        if (null == setup) {
            init();
            if (null != desc) {
                setup = desc.getAasSetup();
            }
            if (null == setup) {
                try {
                    ServiceSetup cfg = AbstractSetup.readFromYaml(ServiceSetup.class);
                    setup = cfg.getAas();
                } catch (IOException e) {
                    LoggerFactory.getLogger(ServiceFactory.class).warn("Cannot read setup: " + e.getMessage());
                }
                if (null == setup) {
                    setup = new AasSetup();
                }
            }
        }
        return setup;
    }
    
    /**
     * Returns the actual network manager setup.
     * 
     * @return the network manager setup
     */
    public static NetworkManagerSetup getNetworkManagerSetup() {
        if (null == netwMgrSetup) {
            init();
            try {
                ServiceSetup cfg = AbstractSetup.readFromYaml(ServiceSetup.class);
                netwMgrSetup = cfg.getNetMgr();
            } catch (IOException e) {
                LoggerFactory.getLogger(ServiceFactory.class).warn("Cannot read setup: " + e.getMessage());
            }
            if (null == setup) {
                netwMgrSetup = new NetworkManagerSetup();
            }
        } 
        return netwMgrSetup;
    }

    /**
     * Returns the actual transport setup for the implementing service manager.
     * 
     * @return the AAS setup
     */
    public static TransportSetup getTransport() {
        if (null == transport) {
            init();
            if (null != desc) {
                transport = desc.getTransport();
            }
            if (null == transport) {
                try {
                    ServiceSetup cfg = AbstractSetup.readFromYaml(ServiceSetup.class);
                    transport = cfg.getTransport();
                } catch (IOException e) {
                    LoggerFactory.getLogger(ServiceFactory.class).warn("Cannot read setup: " + e.getMessage());
                }
                if (null == transport) {
                    transport = new TransportSetup();
                }
            }
        }
        return transport;
    }

    /**
     * Defines the AAS setup instance.
     * 
     * @param instance the new setup instance
     */
    public static void setAasSetup(AasSetup instance) {
        setup = instance;
    }

    /**
     * Defines the network manager setup instance.
     * 
     * @param instance the new setup instance
     */
    public static void setNetworkManagerSetup(NetworkManagerSetup instance) {
        netwMgrSetup = instance;
    }

}
