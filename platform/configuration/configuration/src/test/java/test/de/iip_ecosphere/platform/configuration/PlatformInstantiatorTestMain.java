/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.configuration;

import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.configuration.PlatformInstantiator;

/**
 * The main class if the {@link PlatformInstantiator} shall be executed as process during testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlatformInstantiatorTestMain {
    
    /**
     * Executes the platform instantiation in test mode as part of running it in an own process.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            PlatformInstantiator.instantiate(new AbstractIvmlTests.TestConfigurer(args));
        } catch (ExecutionException e) {
            System.err.println("Instantiation failed: " + e.getMessage());
        }
    }

}
