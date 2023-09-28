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

package de.iip_ecosphere.platform.support.net;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;

/**
 * Provides access to the network manager.
 * 
 * @author Holger Eichelberger, SSE
 */
public class NetworkManagerFactory {

    public static final String PROPERTY = "iip.networkManager";
    private static NetworkManager instance;

    /**
     * Returns the actual descriptor.
     * 
     * @return the actual instance
     * @see #loadFromProperty()
     */
    public static Optional<NetworkManagerDescriptor> getDescriptor() {
        Optional<NetworkManagerDescriptor> first = loadFromProperty();
        if (first.isEmpty()) {
            first = ServiceLoaderUtils.findFirst(NetworkManagerDescriptor.class);
        }
        return first;
    }

    /**
     * Returns the actual instance. Sets {@link #PROPERTY} if created by descriptor.
     * 
     * @return the actual instance
     * @see #getDescriptor()
     */
    public static NetworkManager getInstance() {
        if (null == instance) {
            Optional<NetworkManagerDescriptor> first = getDescriptor();
            if (first.isPresent()) {
                instance = first.get().createInstance();
                if (null != instance) {
                    System.setProperty(PROPERTY, first.get().getClass().getName());
                    getLogger().info("Network manager implementation registered: {}", instance.getClass().getName());
                }
            } else {
                getLogger().warn("No Network manager descriptor/implementation known. "
                    + "Falling back to local network manager.");
                instance = new LocalNetworkManagerImpl();
            }
        }
        return instance;
    }

    /**
     * Loads a descriptor from {@link #PROPERTY} if specified.
     * 
     * @return the loaded descriptor if not empty
     */
    public static Optional<NetworkManagerDescriptor> loadFromProperty() {
        String mgrProperty = System.getProperty(PROPERTY);
        Optional<NetworkManagerDescriptor> result = Optional.empty();
        if (null != mgrProperty) {
            try {
                Class<?> cls = Class.forName(mgrProperty);
                if (NetworkManagerDescriptor.class.isAssignableFrom(cls)) {
                    result = Optional.of(NetworkManagerDescriptor.class.cast(cls.getConstructor().newInstance()));
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException 
                | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException 
                | ClassCastException e) {
                
                getLogger().warn("Cannot instantiate {} as network manager: {}", mgrProperty, e.getMessage());
            }
        }
        return result;
    }
    
    /**
     * Returns the logger instance.
     * 
     * @return the logger instance
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(NetworkManagerFactory.class);
    }
    
    /**
     * Convenience method to configure the actual network manager instance.
     * 
     * @param setup instance containing the configuration (may be <b>null</b>, ignored then)
     */
    public static void configure(NetworkManagerSetup setup) {
        NetworkManager mgr = getInstance();
        if (null != mgr && null != setup) {
            mgr.configure(setup);
        }
    }

}
