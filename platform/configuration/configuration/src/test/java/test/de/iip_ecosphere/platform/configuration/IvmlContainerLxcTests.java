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

import org.apache.commons.lang3.SystemUtils;
import org.junit.Assume;
import org.junit.Test;

import de.iip_ecosphere.platform.configuration.PlatformInstantiatorExecutor;

/**
 * Tests the creation of containers via IVML models.
 * 
 * @author Luca Schulz, SSE
 */
public class IvmlContainerLxcTests extends AbstractIvmlTests {

    /**
     * Tests loading, reasoning and instantiating "ContainerTest", a simple,
     * generated service chain for testing container creation. Here, we instantiate
     * the full platform as basis for container creation.
     * 
     * @throws ExecutionException shall not occur
     * @throws IOException        shall not occur
     */
    @Test
    public void testContainerTest() throws ExecutionException, IOException {
        // mvn: stdout now in target/surefire-reports/<qualifiedClassName>-output.txt
        boolean winAndLxcDisabled = false;
        boolean uxAndHomeOk = false;
        if (SystemUtils.IS_OS_WINDOWS) {
            if (Boolean.valueOf(System.getProperty("easy.lxc.failOnError", "true"))) {
                System.out.println("LXC does not support Windows. This test can run without "
                    + "container create if -Deasy.lxc.failOnError=false.");
            } else {
                winAndLxcDisabled = true;
            }
        } else {
            if (SystemUtils.USER_HOME.startsWith("/home/")) {
                uxAndHomeOk = true;
            } else {
                System.out.println("LXC runs on linx only if the user home is in /home/");
            }
        }
        Assume.assumeTrue(winAndLxcDisabled | uxAndHomeOk);

        File gen = new File("gen/tests/ContainerCreationLxc");
        PlatformInstantiatorExecutor
            .instantiate(new TestConfigurer("ContainerCreationLxc", new File("src/test/easy/single"), gen));
        assertAllFiles(gen);
        assertTemplateZip(gen, "impl.SimpleMeshTestingContainerApp");
    }

}
