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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import de.iip_ecosphere.platform.configuration.PlatformInstantiator;

/**
 * Tests the generation of the platform APIs.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IvmlApiTests extends AbstractIvmlTests {

    /**
     * Tests loading, reasoning and instantiating the generated platform APIs, e.g., for AAS.
     * 
     * @throws ExecutionException shall not occur
     * @throws IOException shall not occur
     */
    @Test
    public void testApi() throws ExecutionException, IOException {
        //System.setProperty(PlatformInstantiator.KEY_PROPERTY_MVNARGS, "-o");
        //System.setProperty(PlatformInstantiator.KEY_PROPERTY_TRACING, "ALL");
        //PlatformInstantiator.setTraceFilter();
        File gen = new File("gen/tests/api");
        PlatformInstantiator.instantiate(
            genApi(new TestConfigurer("PlatformConfiguration", new File("src/test/easy/api"), gen)));
        assertAllFiles(gen);
    }
    
}
