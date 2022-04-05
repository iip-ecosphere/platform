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

    private static NetworkManager instance;

    /**
     * Returns the actual instance.
     * 
     * @return the actual instance
     */
    public static NetworkManager getInstance() {
        if (null == instance) {
            Optional<NetworkManagerDescriptor> first = ServiceLoaderUtils.findFirst(NetworkManagerDescriptor.class);
            if (first.isPresent()) {
                instance = first.get().createInstance();
                if (null != instance) {
                    getLogger().info("Network manager implementation registered: " + instance.getClass().getName());
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
