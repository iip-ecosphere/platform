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

package test.de.iip_ecosphere.platform.configuration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import de.iip_ecosphere.platform.configuration.PlatformInstantiator;

/**
 * Tests the SimpleMesh model.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IvmlSimpleMeshTests extends AbstractIvmlTests {

    /**
     * Tests loading, reasoning and instantiating "SimpleMesh", a simple, generated service chain for testing. Here, we 
     * do not instantiate the full platform rather than only the configured apps. Depending on Maven setup/exclusions, 
     * this Test may require Java 11.
     * 
     * @throws ExecutionException shall not occur
     * @throws IOException shall not occur
     */
    @Test
    public void testSimpleMesh() throws ExecutionException, IOException {
        File gen = new File("gen/tests/SimpleMesh");
        PlatformInstantiator.instantiate(
            genApps(new TestConfigurer("PlatformConfiguration", new File("src/test/easy/simpleMesh"), gen)));
        assertAllFiles(gen);
        assertTemplateZip(gen, "impl.SimpleMeshTestingApp");
    }
    
}
