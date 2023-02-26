/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
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
 * Tests the creation of containers via IVML models.
 * 
 * @author Monika Staciwa, SSE
 */
public class IvmlContainerTests extends AbstractIvmlTests {
       
    /**
     * Tests loading, reasoning and instantiating "ContainerTest", a simple, generated service chain for testing 
     * container creation. Here, we instantiate the full platform as basis for container creation.
     * 
     * @throws ExecutionException shall not occur
     * @throws IOException shall not occur
     */
    @Test
    public void testContainerTest() throws ExecutionException, IOException {
        final String dockerFailProp = "easy.docker.failOnError";
        System.setProperty(dockerFailProp, "false"); // fails sometimes in CI due to unknown docker problems??
        
        File gen = new File("gen/tests/ContainerCreation");
        PlatformInstantiator.instantiate(
            new TestConfigurer("ContainerCreation", new File("src/test/easy/single"), gen));
        assertAllFiles(gen);
        assertTemplateZip(gen, "impl.SimpleMeshTestingContainerApp");

        System.setProperty(dockerFailProp, "true");
    }
    
}
