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
import java.util.HashSet;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.qpid.server.util.FileUtils;
import org.junit.Assert;

import de.iip_ecosphere.platform.configuration.ConfigurationLifecycleDescriptor;
import de.iip_ecosphere.platform.configuration.ConfigurationSetup;
import de.iip_ecosphere.platform.configuration.EasyLogLevel;
import de.iip_ecosphere.platform.configuration.PlatformInstantiator.InstantiationConfigurer;
import de.iip_ecosphere.platform.services.environment.YamlArtifact;
import de.iip_ecosphere.platform.support.JarUtils;
import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import net.ssehub.easy.varModel.confModel.Configuration;

import static test.de.iip_ecosphere.platform.services.environment.PythonEnvironmentTest.*;

/**
 * Base class for IVML model tests.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class IvmlTests {
    
    private static final Set<String> ASSERT_FILE_EXTENSIONS = new HashSet<>();
    private static final Set<String> ASSERT_FILE_NAME_EXCLUSIONS = new HashSet<>();

    static  {
        ASSERT_FILE_EXTENSIONS.add(".java");
        ASSERT_FILE_EXTENSIONS.add(".py");
        ASSERT_FILE_EXTENSIONS.add(".yml");
        ASSERT_FILE_EXTENSIONS.add(".xml");
        
        ASSERT_FILE_NAME_EXCLUSIONS.add("__init__.py");

        // setup binary resources for testing (fallback) and if IPR-based resources are available
        File f = new File("resources.ipr");
        if (!f.exists()) {
            f = new File("resources");
        }
        System.setProperty("iip.resources", f.getAbsolutePath());
    }

    /**
     * Asserts and returns an instance of the configuration lifecycle descriptor.
     * 
     * @return the configuration lifecycle descriptor instance 
     */
    protected static ConfigurationLifecycleDescriptor assertLifecycleDescriptor() {
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
     * Reusable test configuration/setup.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected static class TestConfigurer extends InstantiationConfigurer {

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
     * Extracts the python service environment implementation (via Maven and main project). Required for 
     * {@link #pythonSourceCodeCheck(File, String)}.
     *  
     * @param srcMainPython the target folder where to extract the service environment to
     */
    protected void extractPythonServiceEnv(File srcMainPython) throws IOException {
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
    protected void pythonSourceCodeCheck(File srcMainPython, String pyFile) throws IOException {
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
    protected void assertDeploymentYaml(File base, String name) {
        assertFile(base, name);
        File f = new File(base, name);
        try (FileInputStream in = new FileInputStream(f)) {
            YamlArtifact.readFromYaml(in);
            in.close();
        } catch (IOException e) {
            Assert.fail(f.getAbsolutePath() + ":" + e.getMessage());
        }
    }

    /**
     * Tests for the existence of all files related to a Java Service Mesh Node.
     * 
     * @param folder the basic source folder including base package
     * @param name the name of the service (as identifier)
     * @param old old (separated) or new shared interface style
     * @param connector whether the node is a connector
     */
    protected void assertJavaInterface(File folder, String name, boolean old, boolean connector) {
        if (!connector) {
            String add = old ? "Service" : "Interface";
            assertFile(new File(folder, "interfaces"), name + add + ".java");
        }
    }
    
    /**
     * Tests for the existence of all files related to a Java Service Mesh Node.
     * 
     * @param folder the basic source folder including base package
     * @param name the name of the service (as identifier)
     * @param connector whether the node is a connector
     */
    protected void assertJavaNode(File folder, String name, boolean connector) {
        assertFile(new File(folder, "nodes"), name + ".java");
        if (!connector) {
            assertFile(new File(folder, "stubs"), name + "Stub.java");
        }
    }

    /**
     * Tests for the existence of all files related to a Java Data Type.
     * 
     * @param folder the basic source folder including base package
     * @param name the name of the datatype (as identifier)
     */
    protected void assertJavaDatatype(File folder, String name) {
        assertDatatype(new File(folder, "datatypes"), new File(folder, "serializers"), name, "java");
    }

    /**
     * Tests for the existence of all files related to a Python Data Type implementation.
     * 
     * @param folder the basic source folder
     * @param name the name of the datatype (as identifier)
     */
    protected void assertPythonDatatypeImpl(File folder, String name) {
        assertDatatype(new File(folder, "datatypes"), null, name, "py");
    }

    /**
     * Tests for the existence of all files related to a Python Data Type.
     * 
     * @param folder the basic source folder
     * @param name the name of the datatype (as identifier)
     */
    protected void assertPythonDatatype(File folder, String name) {
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
    protected void assertEcsRuntime(File gen) throws IOException {
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
    protected void assertServiceManager(File gen) throws IOException {
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
    protected void assertPlatform(File gen) throws IOException {
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
    protected static File assertFile(File base, String name) {
        return assertFile(new File(base, name));
    }
    
    /**
     * Asserts that the specified file exists and has contents.
     * 
     * @param file the file
     * @return {@code file}
     */
    private static File assertFile(File file) {
        Assert.assertTrue("File " + file + " does not exist", file.exists());
        Assert.assertTrue("File " + file + " is empty", file.length() > 0);
        return file;
    }
    
    /**
     * Generically asserts all files in {@code folder} and recursively in contained folders.
     * 
     * @param folder the folder to asserts the files within
     */
    protected static final void assertAllFiles(File folder) {
        File[] files = folder.listFiles();
        if (null != files) {
            for (File f : files) {
                if (f.isDirectory()) {
                    assertAllFiles(f);
                } else {
                    String name = f.getName();
                    int pos = name.lastIndexOf('.');
                    String extension = "";
                    if (pos > 0) {
                        extension = name.substring(pos);
                    }
                    if (ASSERT_FILE_EXTENSIONS.contains(extension) && !ASSERT_FILE_NAME_EXCLUSIONS.contains(name)) {
                        Assert.assertTrue("File " + f + " is empty", FileUtils.readFileAsString(f).trim().length() > 0);
                    }
                }
            }
        }
    }

    /**
     * Asserts that the specified file exists, has contents and contains the specified {@code search} string(s).
     * 
     * @param base the base folder
     * @param name the name/path to the file
     * @param search the content/search strings to assert
     * @throws IOException if the file cannot be read
     */
    protected static void assertFileContains(File base, String name, String... search) throws IOException {
        File f = assertFile(base, name);
        String contents = org.apache.commons.io.FileUtils.readFileToString(f, Charset.defaultCharset());
        for (String s : search) {
            Assert.assertTrue("File " + f + " must contain '" + s + "'", contents.contains(s));
        }
    }

    /**
     * Helper method to configure for partial instantiation, i.e., apps only and no platform components.
     * 
     * @param cfg the configurer instance
     * @return {@code cfg}
     */
    public static InstantiationConfigurer genApps(InstantiationConfigurer cfg) {
        return cfg.setStartRuleName("generateApps");
    }

    /**
     * Helper method to configure for partial instantiation, i.e., apps without dependencies and no platform components.
     * 
     * @param cfg the configurer instance
     * @return {@code cfg}
     */
    public static InstantiationConfigurer genAppsNoDeps(InstantiationConfigurer cfg) {
        return cfg.setStartRuleName("generateAppsNoDeps");
    }

}
