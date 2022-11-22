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

/**
 * Basic functions for serializer model tests.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class IvmlSerializerConfigTests extends IvmlTests {
    
    /**
     * Asserts file and contents of the application part.
     * 
     * @param base the source base folder
     * @param withFamily test as it we expect a AI family, else a single node
     * @throws IOException in case that expected files cannot be found or inspected
     */
    protected void assertApplication(File base, boolean withFamily) throws IOException {
        File srcMain = new File(base, "src/main");
        File srcMainJava = new File(srcMain, "java");
        File srcMainResources = new File(srcMain, "resources");
        //File srcMainPython = new File(srcMain, "python");
        //File srcMainAssembly = new File(srcMain, "assembly");
        File srcMainJavaIip = new File(srcMainJava, "iip");

        assertJavaNode(srcMainJavaIip, "MyAnonymizerExample", false);
        if (withFamily) {
            assertJavaNode(srcMainJavaIip, "KIFamilyExample", false);
            assertJavaNode(srcMainJavaIip, "KIFamilyExampleFamilyInterface", true);
            assertJavaNode(srcMainJavaIip, "AlternativeMyKiExampleFamilyKIFamilyExample", true);
            assertJavaNode(srcMainJavaIip, "MyKiExampleFamilyKIFamilyExample", true);
        } else {
            assertJavaNode(srcMainJavaIip, "MyKiExample", false);
        }
        assertJavaNode(srcMainJavaIip, "MyMqttConnExample", true);
        assertJavaNode(srcMainJavaIip, "MyOpcConnExample", true);
        assertJavaNode(srcMainJavaIip, "MySourceExample", false);
        
        assertFile(srcMainResources, "application.yml");
        assertDeploymentYaml(srcMainResources, "deployment.yml");
        assertFile(srcMainResources, "logback.xml");

        assertFileContains(base, "pom.xml", "transport.spring.amqp", "transport.amqp");
    }

    /**
     * Asserts the Serializer1 application interfaces.
     * 
     * @param base the base folder (shared or app-individual)
     * @param old old (separated) or new shared interface style
     * @throws IOException in case that expected files cannot be found or inspected
     */
    protected void assertAppInterfaces(File base, boolean old) throws IOException {
        File srcMain = new File(base, "src/main");
        File srcMainJava = new File(srcMain, "java");
        File srcMainJavaIip = new File(srcMainJava, "iip");

        assertJavaDatatype(srcMainJavaIip, "Rec1");
        assertJavaDatatype(srcMainJavaIip, "MyConnMachineIn");
        assertJavaDatatype(srcMainJavaIip, "MyConnMachineOut");

        assertJavaInterface(srcMainJavaIip, "MyAnonymizerExample", old, false);
        assertJavaInterface(srcMainJavaIip, "MyKiExample", old, false);
        assertJavaInterface(srcMainJavaIip, "MyMqttConnExample", old, true);
        assertJavaInterface(srcMainJavaIip, "MyOpcConnExample", old, true);
        assertJavaInterface(srcMainJavaIip, "MySourceExample", old, false);
        
        /* no python anymore if not used in application
        File srcMainPython = new File(srcMain, "python");
        File srcMainAssembly = new File(srcMain, "assembly");
        assertPythonDatatype(srcMainPython, "Rec1");
        if (!old) {
            assertPythonDatatypeImpl(srcMainPython, "Rec1Impl");
        }
        assertPythonDatatype(srcMainPython, "MyConnMachineIn");
        assertPythonDatatype(srcMainPython, "MyConnMachineOut");

        assertFile(srcMainAssembly, "pythonInterfaces.xml");

        extractPythonServiceEnv(srcMainPython);
        pythonSourceCodeCheck(srcMainPython, "datatypes/IdType.py");
        pythonSourceCodeCheck(srcMainPython, "datatypes/Rec1.py");
        if (!old) {
            pythonSourceCodeCheck(srcMainPython, "datatypes/Rec1Impl.py");
        }
        pythonSourceCodeCheck(srcMainPython, "serializers/Rec1Serializer.py");*/
    }
    
}
