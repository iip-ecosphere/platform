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

package test.de.iip_ecosphere.platform.configuration.easyProducer;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.Assume;
import org.junit.Test;

/**
 * Tests the creation of containers via IVML models.
 * 
 * @author Monika Staciwa, SSE
 */
public class IvmlContainerInitTests extends IvmlContainerTests {
       
    /**
     * Tests loading, reasoning and instantiating "ContainerTest", a simple, generated service chain for testing 
     * container creation. We need to run it once during initial builds, but do not want to run it in normal tests.
     * 
     * @throws ExecutionException shall not occur
     * @throws IOException shall not occur
     */
    @Test
    public void testContainerTest() throws ExecutionException, IOException {
        Assume.assumeTrue(AbstractIvmlTests.isIipBuildInitial());
        super.testContainerTest();
    }
    
}
