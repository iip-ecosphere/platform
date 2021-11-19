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
        
        assertApplication(gen);
        assertEcsRuntime(gen);
        assertServiceManager(gen);
        assertPlatform(gen);
    }

    /**
     * Asserts file and contents of the application part.
     * 
     * @param gen the generation base folder
     * @throws IOException in case that expected files cannot be found or inspected
     */
    private void assertApplication(File gen) throws IOException {
        File base = new File(gen, "MyAppExample");
        File srcMain = new File(base, "src/main");
        File srcMainJava = new File(srcMain, "java");
        File srcMainResources = new File(srcMain, "resources");
        File srcMainPython = new File(srcMain, "python");
        File srcMainAssembly = new File(srcMain, "assembly");
        
        File srcMainJavaIip = new File(srcMainJava, "iip");
        assertJavaDatatype(srcMainJavaIip, "Rec1");
        assertJavaDatatype(srcMainJavaIip, "MyConnMachineIn");
        assertJavaDatatype(srcMainJavaIip, "MyConnMachineOut");
        assertJavaNode(srcMainJavaIip, "MyAnonymizerExample");
        assertJavaNode(srcMainJavaIip, "MyKiExample");
        assertJavaNode(srcMainJavaIip, "MyMqttConnExample");
        assertJavaNode(srcMainJavaIip, "MyOpcConnExample");
        assertJavaNode(srcMainJavaIip, "MySourceExample");
        
        assertFile(srcMainResources, "application.yml");
        assertDeploymentYaml(srcMainResources, "deployment.yml");
        assertFile(srcMainResources, "logback.xml");
        
        assertPythonDatatype(srcMainPython, "Rec1");
        assertPythonDatatype(srcMainPython, "MyConnMachineIn");
        assertPythonDatatype(srcMainPython, "MyConnMachineOut");
        
        assertFile(srcMainAssembly, "python.xml");

        assertFileContains(base, "pom.xml", "transport.spring.amqp", "transport.amqp");
        
        FileInputStream zip = new FileInputStream(new File("target/python/services.environment-python.zip"));
        JarUtils.extractZip(zip, srcMainPython.toPath());
        zip.close();
        
        try {
            int res = createPythonProcess(srcMainPython, "-m", "py_compile", "Rec1.py").waitFor();
            Assert.assertEquals("Source code checking Rec1.py", 0, res);
            res = createPythonProcess(srcMainPython, "-m", "py_compile", "Rec1Serializer.py").waitFor();
            Assert.assertEquals("Source code checking Rec1Serializer.py", 0, res);
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
     */
    private void assertJavaNode(File folder, String name) {
        assertFile(new File(folder, "interfaces"), name + "Service.java");
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
     * Tests for the existence of all files related to a Python Data Type.
     * 
     * @param folder the basic source folder
     * @param name the name of the datatype (as identifier)
     */
    private void assertPythonDatatype(File folder, String name) {
        assertDatatype(folder, folder, name, "py");
    }

    /**
     * Tests for the existence of all files related to a Data Type.
     * 
     * @param typeFolder the folder containing the datatypes
     * @param serFolder the folder containing the datatypes serializers (may be <code>typeFolder</code>)
     * @param name the name of the datatype (as identifier)
     * @param extension the file name extension
     */
    private void assertDatatype(File typeFolder, File serFolder, String name, String extension) {
        assertFile(typeFolder, name + "." + extension);
        assertFile(serFolder, name + "Serializer." + extension);
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
     * do not instantiate the full platforms rather than only the configured apps. Depending on Maven setup/exclusions, 
     * this Test may require Java 11.
     * 
     * @throws ExecutionException shall not occur
     * @throws IOException shall not occur
     */
    @Test
    public void testSimpleMesh() throws ExecutionException, IOException {
        File gen = new File("gen/tests/SimpleMesh");
        PlatformInstantiator.instantiate(
            new TestConfigurer("SimpleMesh", new File("src/test/easy"), gen)
                .setStartRuleName("generateApps"));
    }

    /**
     * Tests loading, reasoning and instantiating "SimpleMesh", a simple, generated service chain for testing of three 
     * elements. Here, we do not instantiate the full platforms rather than only the configured apps. Depending on 
     * Maven setup/exclusions, this Test may require Java 11.
     * 
     * @throws ExecutionException shall not occur
     * @throws IOException shall not occur
     */
    @Test
    public void testSimpleMesh3() throws ExecutionException, IOException {
        File gen = new File("gen/tests/SimpleMesh3");
        PlatformInstantiator.instantiate(
            new TestConfigurer("SimpleMesh3", new File("src/test/easy"), gen)
                .setStartRuleName("generateApps"));
    }

}
