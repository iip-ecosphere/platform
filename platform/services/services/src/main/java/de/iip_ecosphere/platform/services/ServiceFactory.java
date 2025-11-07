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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.setup.AbstractSetup;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import de.iip_ecosphere.platform.support.net.NetworkManagerSetup;
import de.iip_ecosphere.platform.support.plugins.Plugin;
import de.iip_ecosphere.platform.support.plugins.PluginManager;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

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
    private static String yamlPath;
    
    /**
     * Sets the YAML path if setup YAML must be read from a nested object. Shall be maintained by implementing 
     * class or tests. [testing]
     * 
     * @param path the path, by default none/<b>null</b>
     */
    public static void setYamlPath(String path) {
        yamlPath = path;
    }

    /**
     * Initializes this factory.
     */
    private static void init() {
        if (null == desc) {
            Plugin<ServiceFactoryDescriptor> plugin =  PluginManager.getPlugin(ServiceFactoryDescriptor.class);
            if (null != plugin) {
                desc = plugin.getInstance();
            }
            if (null == desc) {
                ServiceLoader<ServiceFactoryDescriptor> loader = ServiceLoaderUtils.load(
                    ServiceFactoryDescriptor.class);
                Optional<ServiceFactoryDescriptor> first = ServiceLoaderUtils.findFirst(loader);
                if (first.isPresent()) {
                    desc = first.get();
                } else {
                    LOGGER.warn("No Service manager implementation known.");
                }
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
    
    /**
     * Returns the service setup class.
     * 
     * @return the service setup class, either from the descriptor or {@link ServiceSetup}
     */
    public static Class<? extends ServiceSetup> getSetupClass() {
        init();
        return null == desc ? ServiceSetup.class : desc.getSetupClass();
    }

    /**
     * Returns the actual service setup for the implementing service manager.
     * 
     * @return the service setup
     */
    public static ServiceSetup getSetup() {
        return getFromSetup(service, d -> d.getSetup(), s -> s, 
            () -> new ServiceSetup(), t -> service = t);
    }

    /**
     * Returns the actual AAS setup for the implementing service manager.
     * 
     * @return the AAS setup
     */
    public static AasSetup getAasSetup() {
        return getFromSetup(setup, d -> d.getAasSetup(), s -> s.getAas(), 
            () -> new AasSetup(), t -> setup = t);
    }
    
    /**
     * Returns the actual network manager setup.
     * 
     * @return the network manager setup
     */
    public static NetworkManagerSetup getNetworkManagerSetup() {
        return getFromSetup(netwMgrSetup, d -> null, s -> s.getNetMgr(), 
            () -> new NetworkManagerSetup(), t -> netwMgrSetup = t);
    }

    /**
     * Returns the actual transport setup for the implementing service manager.
     * 
     * @return the AAS setup
     */
    public static TransportSetup getTransport() {
        return getFromSetup(transport, d -> d.getTransport(), s -> s.getTransport(), 
            () -> new TransportSetup(), t -> transport = t);
    }
    
    /**
     * Returns an object from setup. If not anyway present, calls {@code #init()}, then first tries to get the object
     * from the descriptor via {@code fromDesc}, then by directly loading the setup via {@link #getSetupClass()} and 
     * {@link #yamlPath}, and finally via {@code constructor}.
     * 
     * @param <T> the type of object to read
     * @param object the actual value, returned if not <b>null</b>
     * @param fromDesc the projection from the descriptor
     * @param fromSetup the projection from a freshly read setup as first fallback
     * @param constructor the constructor as second fallback
     * @param setter if a new object is obtained, store it for future use
     * @return the {@code object} or if <b>null</b> from descriptor, setup or constructor
     */
    private static <T> T getFromSetup(T object, Function<ServiceFactoryDescriptor, T> fromDesc, 
        Function<ServiceSetup, T> fromSetup, Supplier<T> constructor, Consumer<T> setter) {
        T result = object;
        if (null == result) {
            init();
            if (null != desc) {
                result = fromDesc.apply(desc);
            }
            if (null == result) { // fallback
                try {
                    ServiceSetup cfg = AbstractSetup.readFromYamlWithPath(getSetupClass(), yamlPath);
                    result = fromSetup.apply(cfg);
                } catch (IOException e) {
                    LoggerFactory.getLogger(ServiceFactory.class).warn("Cannot read setup: " + e.getMessage());
                }
                if (null == result) { // second fallback
                    result = constructor.get();
                }
            }
            setter.accept(result);
        }
        return result;
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

    /**
     * Returns a test represented as it's class for execution in jUnit. This is required if a test running a service 
     * manager and other components like AAS as plugins shall get execute a test independently. Delegates the
     * work to {@link ServiceFactoryDescriptor}. [testing]
     * 
     * @param index a 0-based index of the test/suite to return; usually test and implementing service manager are in 
     *    close relationship and know the valid indexes
     * @return the test classes or <b>null</b> if there is none for the given index
     */
    public static Class<?>[] getTests(int index) {
        Class<?>[] result = null;
        init();
        if (null != desc) {
            result = desc.getTests(index);
        }
        return result;
    }

}
