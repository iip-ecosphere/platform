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
 * Tests the SerializerConfig1Old model.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IvmlSerializerConfig1OldTests extends IvmlSerializerConfigTests {
    
    /**
     * Tests loading, reasoning and instantiating "SerializerConfig1Old" (legacy name, originally only for serializer) 
     * here with non-shared interfaces and without platform configurations. Depending on Maven setup/exclusions, 
     * this Test may require Java 11.
     * 
     * @throws ExecutionException shall not occur
     * @throws IOException shall not occur
     */
    @Test
    public void testSerializerConfig1Old() throws ExecutionException, IOException {
        File gen = new File("gen/tests/SerializerConfig1old");
        PlatformInstantiator.instantiate(
            genApps(new TestConfigurer("SerializerConfig1Old", new File("src/test/easy"), gen)));
        
        File base = new File(gen, "MyAppExampleOld");
        assertAppInterfaces(base, true); // old style
        assertApplication(base, false);
        assertAllFiles(base);
        
        // specific files only generated here for testing
        /* no python anymore if not used in application
        File srcMain = new File(base, "src/main");
        File srcMainPython = new File(srcMain, "python");
        pythonSourceCodeCheck(srcMainPython, "datatypes/TestType.py");
        pythonSourceCodeCheck(srcMainPython, "datatypes/MyTestEnum.py");*/
        assertTemplateZip(gen, "impl.MyAppExampleOld");
    }
    
}
