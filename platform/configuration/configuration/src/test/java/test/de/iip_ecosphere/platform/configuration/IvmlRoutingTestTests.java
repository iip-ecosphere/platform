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

import de.iip_ecosphere.platform.configuration.PlatformInstantiatorExecutor;

/**
 * Tests the RoutingTest model.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IvmlRoutingTestTests extends AbstractIvmlTests {

    /**
     * Tests loading, reasoning and instantiating "RoutingTest" aiming at various routing issues.
     * 
     * @throws ExecutionException shall not occur
     * @throws IOException shall not occur
     */
    @Test
    public void testRoutingTest() throws ExecutionException, IOException {
        // mvn: stdout now in target/surefire-reports/<qualifiedClassName>-output.txt
        File gen = new File(TEST_BASE_FOLDER, "RoutingTest");
        PlatformInstantiatorExecutor.instantiate(
            genApps(new TestConfigurer("PlatformConfiguration", new File(MODEL_BASE_FOLDER, "routingTest"), gen)));
        assertAllFiles(gen);
        assertTemplateZip(gen, "impl.RoutingTestApp");
    }
    
}
