/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration;

import java.io.File;
import java.util.function.Consumer;

/**
 * Provides access to configuration resources and functions.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ConfigurationFactoryDescriptor {

    /**
     * Returns the configuration setup instance.
     * 
     * @return the setup instance
     */
    public ConfigurationSetup getSetup();

    /**
     * Creates a new AAS changes instance.
     * 
     * @return the instance
     */
    public default AasChanges createAasChanges() {
        return new AasChanges();
    }
    
    /**
     * Creates a platform instantiator instance.
     * 
     * @param localRepo the local Maven repository, may be <b>null</b>
     * @param warn a warning message consumer
     * @param info an information message consumer
     * @param executionTimeConsumer optional consumer for the (successful) process execution time, may be <b>null</b> 
     *     for none
     */
    public PlatformInstantiation createInstantiator(File localRepo, Consumer<String> warn, Consumer<String> info, 
        Consumer<Long> executionTimeConsumer);
    
    /**
     * Hints the following executors regarding the apps to instantiate. Maven legacy.
     * 
     * @param apps the app ids as comma separated list or empty
     */
    public void hintAppsToInstantiate(String apps);
    
}
