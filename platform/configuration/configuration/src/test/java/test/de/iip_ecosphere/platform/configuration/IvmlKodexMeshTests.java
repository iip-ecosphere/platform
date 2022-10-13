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
 * Tests the KodexMesh model.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IvmlKodexMeshTests extends IvmlTests {
    
    /**
     * Tests loading, reasoning and instantiating "KodexMesh", a simple, generated service chain for testing the 
     * integration of the default platform service KODEX developed by KI-Protect. Here, we do not instantiate the full 
     * platform rather than only the configured apps. Depending on Maven setup/exclusions, this Test may require 
     * Java 11.
     * 
     * @throws ExecutionException shall not occur
     * @throws IOException shall not occur
     */
    @Test
    public void testKodexMesh() throws ExecutionException, IOException {
        File gen = new File("gen/tests/KodexMesh");
        PlatformInstantiator.instantiate(genApps(new TestConfigurer("KodexMesh", new File("src/test/easy"), gen)));

        File base = new File(gen, "ApplicationInterfaces");
        File srcMain = new File(base, "src/main");
        File srcMainPython = new File(srcMain, "python");
        File srcMainAssembly = new File(srcMain, "assembly");

        assertFile(srcMainAssembly, "pythonInterfaces.xml");
        //assertFile(srcMainAssembly, "javaInterfaces.xml"); // new style

        extractPythonServiceEnv(srcMainPython);
        pythonSourceCodeCheck(srcMainPython, "datatypes/KRec13.py");
        pythonSourceCodeCheck(srcMainPython, "datatypes/KRec13Impl.py");
        pythonSourceCodeCheck(srcMainPython, "datatypes/KRec13Anon.py");
        pythonSourceCodeCheck(srcMainPython, "datatypes/KRec13AnonImpl.py");
        pythonSourceCodeCheck(srcMainPython, "serializers/KRec13Serializer.py");
        pythonSourceCodeCheck(srcMainPython, "serializers/KRec13AnonSerializer.py");
        pythonSourceCodeCheck(srcMainPython, "interfaces/KodexPythonServiceInterface.py");
        
        base = new File(gen, "SimpleKodexTestingApp");
        srcMain = new File(base, "src/main");
        srcMainPython = new File(srcMain, "python");
        srcMainAssembly = new File(srcMain, "assembly");
        
        assertFile(srcMainAssembly, "kodex_pseudonymizer.xml");
        assertFile(srcMainAssembly, "python_kodexPythonService.xml");
        assertAllFiles(gen);
        assertTemplateZip(gen, "impl.SimpleKodexTestingApp");
    }
    
}
