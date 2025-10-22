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

package test.de.iip_ecosphere.platform.configuration.easyProducer;

import static org.junit.Assume.assumeFalse;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import de.iip_ecosphere.platform.configuration.easyProducer.PlatformInstantiatorExecutor;

/**
 * Tests the SerializerConfig1 model.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IvmlSerializerConfig1Tests extends AbstractIvmlSerializerConfigTests {
    
    /**
     * Tests loading, reasoning and instantiating "SerializerConfig1" (legacy name, originally only for serializer) and
     * all relevant steps to instantiate a platform. Depending on Maven setup/exclusions, this Test may require Java 11.
     * 
     * @throws ExecutionException shall not occur
     * @throws IOException shall not occur
     */
    @Test
    public void testSerializerConfig1() throws ExecutionException, IOException {
        // mvn: stdout now in target/surefire-reports/<qualifiedClassName>-output.txt
        assumeFalse(isIipBuildInitial()); // first build, allow for initialization
        File gen = new File(TEST_BASE_FOLDER, "SerializerConfig1");
        PlatformInstantiatorExecutor.instantiate(
            new TestConfigurer("SerializerConfig1", new File(MODEL_BASE_FOLDER, "single"), gen));

        assertAppInterfaces(new File(gen, "ApplicationInterfaces"), false);
        assertApplication(new File(gen, "MyAppExample"), true);
        assertEcsRuntime(gen);
        assertServiceManager(gen);
        assertPlatform(gen);
        assertAllFiles(gen);
        assertTemplateZip(gen, "impl.MyAppExample");
    }    
}
