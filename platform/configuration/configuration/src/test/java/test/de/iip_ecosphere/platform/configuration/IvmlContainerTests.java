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

import org.apache.commons.lang.SystemUtils;
import org.junit.Test;

import de.iip_ecosphere.platform.configuration.PlatformInstantiatorExecutor;

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
        // mvn: stdout now in target/surefire-reports/<qualifiedClassName>-output.txt
        String dockerFailProp;
        String dockerFailPropValue;
        if (SystemUtils.IS_OS_WINDOWS) { // windows: usually no docker, just skip with fixed return ID
            dockerFailProp = "easy.docker.skip";
            dockerFailPropValue = "a12cb01";
        } else { // CI fail sometimes due to unknown docker issue??
            dockerFailProp = "easy.docker.failOnError";
            dockerFailPropValue = "false";
        }
        File gen = new File("gen/tests/ContainerCreation");
        PlatformInstantiatorExecutor.instantiate(
            new TestConfigurer("ContainerCreation", new File("src/test/easy/single"), gen)
                .setProperty(dockerFailProp, dockerFailPropValue)); 
        assertAllFiles(gen);
        assertTemplateZip(gen, "impl.SimpleMeshTestingContainerApp");

        System.setProperty(dockerFailProp, ""); // in any case
    }
    
}
