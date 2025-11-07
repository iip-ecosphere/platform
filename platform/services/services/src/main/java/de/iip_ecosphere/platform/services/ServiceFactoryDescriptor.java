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

import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;

/**
 * A factory descriptor for Java Service loading.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ServiceFactoryDescriptor {
    
    /**
     * Creates the service manager instance.
     * 
     * @return the instance
     */
    public ServiceManager createInstance();

    /**
     * Returns the service setup.
     * 
     * @return the setup
     */
    public ServiceSetup getSetup();

    /**
     * Returns the AAS setup.
     * 
     * @return the setup
     */
    public AasSetup getAasSetup();
    
    /**
     * Returns the transport setup.
     * 
     * @return the transport setup
     */
    public TransportSetup getTransport();
    
    /**
     * Returns a test represented as it's class for execution in jUnit. This is required if a test running a service 
     * manager and other components like AAS as plugins shall get execute a test independently. Delegates the
     * work to {@link TestProviderDescriptor}. [testing]
     * 
     * @param index a 0-based index of the test/suite to return; usually test and implementing service manager are in 
     *    close relationship and know the valid indexes
     * @return the test classes or <b>null</b> if there is none for the given index
     */
    public default Class<?>[] getTests(int index) {
        Class<?>[] result = null;
        // be careful, usually not the app/context classloader
        ServiceLoader<TestProviderDescriptor> serviceLoader = ServiceLoader.load(
            TestProviderDescriptor.class, getClass().getClassLoader()); 
        Optional<TestProviderDescriptor> desc = ServiceLoaderUtils.findFirst(serviceLoader);
        if (desc.isPresent()) {
            result = desc.get().getTests(index);
        }
        return result;
    }
    
    /**
     * Returns the type of the setup class as fallback.
     * 
     * @return the type, by default {@link ServiceSetup}
     */
    public default Class<? extends ServiceSetup> getSetupClass() {
        return ServiceSetup.class;
    }
    
}
