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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.configuration.ConfigurationLifecycleDescriptor;
import de.iip_ecosphere.platform.configuration.ConfigurationManager;
import de.iip_ecosphere.platform.configuration.ConfigurationSetup;
import de.iip_ecosphere.platform.configuration.EasyLogLevel;
import de.iip_ecosphere.platform.configuration.PlatformInstantiator;
import de.iip_ecosphere.platform.configuration.PlatformInstantiator.InstantiationConfigurer;
import de.iip_ecosphere.platform.services.environment.YamlArtifact;
import de.iip_ecosphere.platform.support.JarUtils;
import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import net.ssehub.easy.producer.core.mgmt.EasyExecutor;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import net.ssehub.easy.varModel.confModel.Configuration;

import static test.de.iip_ecosphere.platform.services.environment.PythonEnvironmentTest.*;

/**
 * Tests the configuration component, in particular the IVML models.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IvmlTests {

    /**
     * Asserts and returns an instance of the configuration lifecycle descriptor.
     * 
     * @return the configuration lifecycle descriptor instance 
     */
    private static ConfigurationLifecycleDescriptor assertLifecycleDescriptor() {
        // check that the registration works, but do not execute all descriptors
        ServiceLoader<LifecycleDescriptor> loader = ServiceLoader.load(LifecycleDescriptor.class);
        Optional<LifecycleDescriptor> first = ServiceLoaderUtils
            .stream(loader)
            .filter(s -> s instanceof ConfigurationLifecycleDescriptor)
            .findFirst();
        Assert.assertTrue(first.isPresent());
        ConfigurationLifecycleDescriptor lcd = (ConfigurationLifecycleDescriptor) first.get(); 
        Assert.assertNotNull(lcd);
        return lcd;
    }
    
    /**
     * Tests loading the meta model.
     */
    @Test
    public void testMetaModel() {
        ConfigurationLifecycleDescriptor lcd = assertLifecycleDescriptor();
        lcd.startup(new String[0]); // shall register executor
        Assert.assertNotNull(ConfigurationManager.getIvmlConfiguration());
        // not much to do, no configuration, shall work anyway, not complete without configuration
        ReasoningResult rRes = ConfigurationManager.validateAndPropagate();
        EasyExecutor.printReasoningMessages(rRes);
        lcd.shutdown();
    }
    
    private static class TestConfigurer extends InstantiationConfigurer {

        /**
         * Creates a configurer instance.
         * 
         * @param ivmlModelName the name of the IVML model representing the topmost platform configuration
         * @param modelFolder the folder where the model is located (ignored if <b>null</b>)
         * @param outputFolder the output folder for code generation
         */
        public TestConfigurer(String ivmlModelName, File modelFolder, File outputFolder) {
            super(ivmlModelName, modelFolder, outputFolder);
        }

        /**
         * Obtains the lifecycle descriptor.
         * 
         * @return the descriptor
         */
        protected ConfigurationLifecycleDescriptor obtainLifecycleDescriptor() {
            return assertLifecycleDescriptor();
        }
        
        @Override
        protected void validateConfiguration(Configuration conf) throws ExecutionException {
            Assert.assertNotNull(conf);
        }
        
        @Override
        protected void validateReasoningResult(ReasoningResult res) throws ExecutionException {
            Assert.assertFalse(res.hasConflict());
        }
        
        @Override
        protected void handleExecutionException(ExecutionException ex) throws ExecutionException {
            throw ex;
        }

        @Override
        protected void configure(ConfigurationSetup setup) {
            super.configure(setup);
            setup.getEasySetup().setLogLevel(EasyLogLevel.VERBOSE); // override for debugging
        }

    }

    /**
     * Tests loading, reasoning and instantiating "SerializerConfig1" (legacy name, originally only for serializer) and
     * all relevant steps to instantiate a platform. Depending on Maven setup/exclusions, this Test may require Java 11.
     * 
     * @throws ExecutionException shall not occur
     * @throws IOException shall not occur
     */
    @Test
    //@Ignore("for release, is part of model")
    public void testSerializerConfig1() throws ExecutionException, IOException {
        File gen = new File("gen/tests/SerializerConfig1");
        PlatformInstantiator.instantiate(new TestConfigurer("SerializerConfig1", new File("src/test/easy"), gen));

        assertAppInterfaces(new File(gen, "ApplicationInterfaces"), false);
        assertApplication(new File(gen, "MyAppExample"));
        assertEcsRuntime(gen);
        assertServiceManager(gen);
        assertPlatform(gen);
    }

    /**
     * Asserts file and contents of the application part.
     * 
     * @param base the source base folder
     * @throws IOException in case that expected files cannot be found or inspected
     */
    private void assertApplication(File base) throws IOException {
        File srcMain = new File(base, "src/main");
        File srcMainJava = new File(srcMain, "java");
        File srcMainResources = new File(srcMain, "resources");
        //File srcMainPython = new File(srcMain, "python");
        //File srcMainAssembly = new File(srcMain, "assembly");
        File srcMainJavaIip = new File(srcMainJava, "iip");

        assertJavaNode(srcMainJavaIip, "MyAnonymizerExample");
        assertJavaNode(srcMainJavaIip, "MyKiExample");
        assertJavaNode(srcMainJavaIip, "MyMqttConnExample");
        assertJavaNode(srcMainJavaIip, "MyOpcConnExample");
        assertJavaNode(srcMainJavaIip, "MySourceExample");
        
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
    private void assertAppInterfaces(File base, boolean old) throws IOException {
        File srcMain = new File(base, "src/main");
        File srcMainPython = new File(srcMain, "python");
        File srcMainAssembly = new File(srcMain, "assembly");
        File srcMainJava = new File(srcMain, "java");
        File srcMainJavaIip = new File(srcMainJava, "iip");

        assertJavaDatatype(srcMainJavaIip, "Rec1");
        assertJavaDatatype(srcMainJavaIip, "MyConnMachineIn");
        assertJavaDatatype(srcMainJavaIip, "MyConnMachineOut");

        assertJavaInterface(srcMainJavaIip, "MyAnonymizerExample", old);
        assertJavaInterface(srcMainJavaIip, "MyKiExample", old);
        assertJavaInterface(srcMainJavaIip, "MyMqttConnExample", old);
        assertJavaInterface(srcMainJavaIip, "MyOpcConnExample", old);
        assertJavaInterface(srcMainJavaIip, "MySourceExample", old);
        
        assertPythonDatatype(srcMainPython, "Rec1");
        if (!old) {
            assertPythonDatatypeImpl(srcMainPython, "Rec1Impl");
        }
        assertPythonDatatype(srcMainPython, "MyConnMachineIn");
        assertPythonDatatype(srcMainPython, "MyConnMachineOut");

        assertFile(srcMainAssembly, "pythonInterfaces.xml");

        extractPythonServiceEnv(srcMainPython);
        pythonSourceCodeCheck(srcMainPython, "datatypes/Rec1.py");
        if (!old) {
            pythonSourceCodeCheck(srcMainPython, "datatypes/Rec1Impl.py");
        }
        pythonSourceCodeCheck(srcMainPython, "serializers/Rec1Serializer.py");
    }

    /**
     * Extracts the python service environment implementation (via Maven and main project). Required for 
     * {@link #pythonSourceCodeCheck(File, String)}.
     *  
     * @param srcMainPython the target folder where to extract the service environment to
     */
    private void extractPythonServiceEnv(File srcMainPython) throws IOException {
        FileInputStream zip = new FileInputStream(new File("target/python/services.environment-python.zip"));
        JarUtils.extractZip(zip, srcMainPython.toPath());
        zip.close();
    }
    
    /**
     * Checks a python source code file (in packages with folders).
     * 
     * @param srcMainPython the main (parent) source folder for python files
     * @param pyFile the python file to check (packages as folders)
     * @throws IOException in case that expected files cannot be found or inspected
     */
    private void pythonSourceCodeCheck(File srcMainPython, String pyFile) throws IOException {
        try {
            int res = createPythonProcess(srcMainPython, "-m", "py_compile", pyFile).waitFor();
            Assert.assertEquals("Source code checking " + pyFile, 0, res);
        } catch (InterruptedException e) {
            Assert.fail("Python code check shall not be interrupted: " + e.getMessage());
        }
    }
    
    /**
     * Asserts the structure of a deployment Yaml file (from service.environment perspective).
     * 
     * @param base the base folder
     * @param name the name/path to the file
     */
    private void assertDeploymentYaml(File base, String name) {
        assertFile(base, name);
        try (FileInputStream in = new FileInputStream(new File(base, name))) {
            YamlArtifact.readFromYaml(in);
            in.close();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Tests for the existence of all files related to a Java Service Mesh Node.
     * 
     * @param folder the basic source folder including base package
     * @param name the name of the service (as identifier)
     * @param old old (separated) or new shared interface style
     */
    private void assertJavaInterface(File folder, String name, boolean old) {
        String add = old ? "Service" : "Interface";
        assertFile(new File(folder, "interfaces"), name + add + ".java");
    }
    
    /**
     * Tests for the existence of all files related to a Java Service Mesh Node.
     * 
     * @param folder the basic source folder including base package
     * @param name the name of the service (as identifier)
     */
    private void assertJavaNode(File folder, String name) {
        assertFile(new File(folder, "nodes"), name + ".java");
        assertFile(new File(folder, "stubs"), name + "Stub.java");
    }

    /**
     * Tests for the existence of all files related to a Java Data Type.
     * 
     * @param folder the basic source folder including base package
     * @param name the name of the datatype (as identifier)
     */
    private void assertJavaDatatype(File folder, String name) {
        assertDatatype(new File(folder, "datatypes"), new File(folder, "serializers"), name, "java");
    }

    /**
     * Tests for the existence of all files related to a Python Data Type implementation.
     * 
     * @param folder the basic source folder
     * @param name the name of the datatype (as identifier)
     */
    private void assertPythonDatatypeImpl(File folder, String name) {
        assertDatatype(new File(folder, "datatypes"), null, name, "py");
    }

    /**
     * Tests for the existence of all files related to a Python Data Type.
     * 
     * @param folder the basic source folder
     * @param name the name of the datatype (as identifier)
     */
    private void assertPythonDatatype(File folder, String name) {
        assertDatatype(new File(folder, "datatypes"), new File(folder, "serializers"), name, "py");
    }

    /**
     * Tests for the existence of all files related to a Data Type.
     * 
     * @param typeFolder the folder containing the datatypes
     * @param serFolder the folder containing the datatypes serializers (may be <code>typeFolder</code>, ignored 
     *     if null)
     * @param name the name of the datatype (as identifier)
     * @param extension the file name extension
     */
    private void assertDatatype(File typeFolder, File serFolder, String name, String extension) {
        assertFile(typeFolder, name + "." + extension);
        if (null != serFolder) {
            assertFile(serFolder, name + "Serializer." + extension);
        }
    }

    /**
     * Asserts file and contents of the ECS runtime component.
     * 
     * @param gen the generation base folder
     * @throws IOException in case that expected files cannot be found or inspected
     */
    private void assertEcsRuntime(File gen) throws IOException {
        File base = new File(gen, "ecsRuntime");
        File srcMain = new File(base, "src/main");
        File srcMainResources = new File(srcMain, "resources");

        assertFile(srcMainResources, "iipecosphere.yml");
        assertFile(srcMainResources, "logback.xml");

        assertFileContains(base, "pom.xml", "ecsRuntime.docker", "transport.amqp", "support.aas.basyx");
        assertFile(base, "src/main/resources/iipecosphere.yml");
    }

    /**
     * Asserts file and contents of the service manager component.
     * 
     * @param gen the generation base folder
     * @throws IOException in case that expected files cannot be found or inspected
     */
    private void assertServiceManager(File gen) throws IOException {
        File base = new File(gen, "serviceMgr");
        File srcMain = new File(base, "src/main");
        File srcMainResources = new File(srcMain, "resources");

        assertFile(srcMainResources, "iipecosphere.yml");
        assertFile(srcMainResources, "logback.xml");

        assertFileContains(base, "pom.xml", "services.spring", "transport.amqp", "support.aas.basyx");
        assertFile(base, "src/main/resources/iipecosphere.yml");
    }

    /**
     * Asserts file and contents of the platform (server) component.
     * 
     * @param gen the generation base folder
     * @throws IOException in case that expected files cannot be found or inspected
     */
    private void assertPlatform(File gen) throws IOException {
        File base = new File(gen, "platform");
        File srcMain = new File(base, "src/main");
        File srcMainResources = new File(srcMain, "resources");

        assertFile(srcMainResources, "iipecosphere.yml");
        assertFile(srcMainResources, "logback.xml");

        assertFileContains(base, "pom.xml", "support.aas.basyx.server", "support.aas.basyx", 
            "configuration.configuration", "transport.amqp");
        assertFile(base, "src/main/resources/iipecosphere.yml");
    }
    
    /**
     * Asserts that the specified file exists and has contents.
     * 
     * @param base the base folder
     * @param name the name/path to the file
     * @return the actual asserted file ({@code base} + {@code name})
     */
    private static File assertFile(File base, String name) {
        File f = new File(base, name);
        Assert.assertTrue("File " + f + " does not exist", f.exists());
        Assert.assertTrue("File " + f + " is empty", f.length() > 0);
        return f;
    }

    /**
     * Asserts that the specified file exists, has contents and contains the specified {@code search} string(s).
     * 
     * @param base the base folder
     * @param name the name/path to the file
     * @param search the content/search strings to assert
     * @throws IOException if the file cannot be read
     */
    private static void assertFileContains(File base, String name, String... search) throws IOException {
        File f = assertFile(base, name);
        String contents = org.apache.commons.io.FileUtils.readFileToString(f, Charset.defaultCharset());
        for (String s : search) {
            Assert.assertTrue("File " + f + " must contain '" + s + "'", contents.contains(s));
        }
    }

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
        PlatformInstantiator.instantiate(genApps(new TestConfigurer("SimpleMesh", new File("src/test/easy"), gen)));
    }

    /**
     * Tests loading, reasoning and instantiating "SimpleMesh", a simple, generated service chain for testing of three 
     * elements. Here, we do not instantiate the full platform rather than only the configured apps. Depending on 
     * Maven setup/exclusions, this Test may require Java 11.
     * 
     * @throws ExecutionException shall not occur
     * @throws IOException shall not occur
     */
    @Test
    public void testSimpleMesh3() throws ExecutionException, IOException {
        File gen = new File("gen/tests/SimpleMesh3");
        PlatformInstantiator.instantiate(genApps(new TestConfigurer("SimpleMesh3", new File("src/test/easy"), gen)));
    }
    
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
        assertFile(srcMainAssembly, "javaInterfaces.xml");

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
        
        assertFile(srcMainAssembly, "pseudonymizer.xml");
        assertFile(srcMainAssembly, "python_kodexPythonService.xml");
    }

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
        assertApplication(base);
    }
    
    /**
     * Helper method to configure for partial instantiation, i.e., apps only and no platform components.
     * 
     * @param cfg the configurer instance
     * @return {@code cfg}
     */
    private static InstantiationConfigurer genApps(InstantiationConfigurer cfg) {
        return cfg.setStartRuleName("generateApps");
    }
    
}
