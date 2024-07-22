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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;

import de.iip_ecosphere.platform.configuration.ConfigurationLifecycleDescriptor;
import de.iip_ecosphere.platform.configuration.ConfigurationSetup;
import de.iip_ecosphere.platform.configuration.EasyLogLevel;
import de.iip_ecosphere.platform.configuration.EasySetup;
import de.iip_ecosphere.platform.configuration.PlatformInstantiator;
import de.iip_ecosphere.platform.configuration.PlatformInstantiator.InstantiationConfigurer;
import de.iip_ecosphere.platform.services.environment.YamlArtifact;
import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.iip_ecosphere.platform.support.OsUtils;
import de.iip_ecosphere.platform.support.ZipUtils;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import net.ssehub.easy.varModel.confModel.Configuration;

import static test.de.iip_ecosphere.platform.services.environment.PythonEnvironmentTest.*;

/**
 * Base class for IVML model tests.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractIvmlTests {
    
    private static final Set<String> ASSERT_FILE_EXTENSIONS = new HashSet<>();
    private static final Set<String> ASSERT_FILE_NAME_EXCLUSIONS = new HashSet<>();
    private static Boolean isIipBuildInitial;
    private static File testModelBase = null;
    private static File testMetaModelFolder;

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
        PlatformInstantiator.setTraceFilter();
    }
    
    /**
     * Returns {@code file} relocated into {@link #testModelBase} if not <b>null</b>.
     * 
     * @param file the file to relocate
     * @return {@code file} or file relocated into {@link #testModelBase}.
     */
    protected static File relocateTestModel(File file) {
        return null == testModelBase ? file : new File(testModelBase, file.getPath());
    }

    /**
     * Sets {@code #testModelBase}.
     * 
     * @param base the base folder, may be <b>null</b> for none
     * @see #relocateTestModel(File)
     */
    public static void setTestModelBase(File base) {
        testModelBase = base;
    }
    
    /**
     * Sets the {@code #metaModelFolder}.
     * 
     * @param folder the meta model folder, may be <b>null</b> for "./src/main/easy"
     */
    public static void setTestMetaModelFolder(File folder) {
        testMetaModelFolder = folder;
    }
    
    /**
     * Returns whether a simplified build due to the first (CI) build shall be performed. Depends on JVM/system property
     * {@code iip.build.initial}.
     * 
     * @return {@code true} for initial build, {@code false} else
     */
    protected static boolean isIipBuildInitial() {
        if (null == isIipBuildInitial) {
            String tmp = OsUtils.getEnv("iipbuildinitial");
            if (null == tmp) {
                tmp = "false";
            }
            boolean result = Boolean.valueOf(System.getProperty("iip.build.initial", tmp));
            if (result) {
                System.out.println("Running in iip.build.initial mode, limiting to generateInterfaces");
            }
            isIipBuildInitial = result;
        }
        return isIipBuildInitial;
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
    public static class TestConfigurer extends InstantiationConfigurer {

        private File ivmlMetaModelFolder = null; // use the default in EasySetup
        private List<File> additionalIvmlFolders = null;
        private boolean exit = false;
        
        /**
         * Creates a configurer instance. Copies the IVML configuration meta model to {@code target/ivml}
         * and filters out the configuration templates. Uses that folder to load the model from. If the parent of 
         * {@code modelFolder} contains a folder named {@code common} (reusable, shared parts of tests), that 
         * folder is added as additional IVML folder to EASy setup and considered while loading the IVML meta model.
         * 
         * @param ivmlModelName the name of the IVML model representing the topmost platform configuration
         * @param modelFolder the folder where the model is located (ignored if <b>null</b>)
         * @param outputFolder the output folder for code generation
         */
        public TestConfigurer(String ivmlModelName, File modelFolder, File outputFolder) {
            super(ivmlModelName, relocateTestModel(modelFolder), outputFolder);
            if (isIipBuildInitial()) {
                super.setStartRuleName("generateInterfaces");
            }
            String srcName = null == testMetaModelFolder ? "./src/main/easy" : testMetaModelFolder.getPath();
            final String srcCfgName = (srcName + "/cfg/").replace('/', File.separatorChar);
            File src = new File(srcName);
            File tgt = new File("./target/ivml");
            try {
                FileUtils.deleteDirectory(tgt);
                tgt.mkdirs();
                // copy IVML metamodel and omit the managed configuration template
                FileUtils.copyDirectory(src, tgt, f -> !f.toString().startsWith(srcCfgName), true); 
                ivmlMetaModelFolder = tgt;
            } catch (IOException e) {
                Assert.fail("Cannot copy IVML meta model from " + src + " to " + tgt);
            }
            File commonIvml = relocateTestModel(new File(modelFolder.getParentFile(), "common"));
            if (commonIvml.exists()) {
                additionalIvmlFolders = new ArrayList<>();
                additionalIvmlFolders.add(commonIvml);
            }
        }
        
        /**
         * Creates a configurer instance from command line arguments delivered by {@link #toArgs(boolean)}.
         * 
         * @param args the command line arguments
         */
        public TestConfigurer(String[] args) {
            super(args);
            exit = true;
            int last = super.getLastArgsIndex(args);
            if (args.length > last) {
                ivmlMetaModelFolder = fromArg(args[last + 1]);
                if (args.length > last + 1) {
                    additionalIvmlFolders = new ArrayList<>();
                    for (int i = last + 2; i < args.length; i++) {
                        additionalIvmlFolders.add(fromArg(args[i]));
                    }
                }
            }
        }        

        @Override
        public String getMainClass() {
            return exit ? super.getMainClass() : PlatformInstantiatorTestMain.class.getName();
        }

        @Override
        public boolean inTesting() {
            return true;
        }

        @Override
        public String[] toArgs(boolean all) {
            String[] tmp = super.toArgs(all);
            String[] result = new String[tmp.length + 1 
                 + (additionalIvmlFolders == null ? 0 : additionalIvmlFolders.size())];
            System.arraycopy(tmp, 0, result, 0, tmp.length);
            result[tmp.length] = toArg(ivmlMetaModelFolder);
            if (null != additionalIvmlFolders) {
                for (int i = 0; i < additionalIvmlFolders.size(); i++) {
                    result[tmp.length + 1 + i] = toArg(additionalIvmlFolders.get(i));
                }
            }
            return result;
        }

        @Override
        public InstantiationConfigurer setStartRuleName(String startRuleName) {
            if (!isIipBuildInitial()) { // set in constructor, do not allow to override
                super.setStartRuleName(startRuleName);
            }
            return this;
        }

        /**
         * Obtains the lifecycle descriptor.
         * 
         * @return the descriptor
         */
        public ConfigurationLifecycleDescriptor obtainLifecycleDescriptor() {
            return assertLifecycleDescriptor();
        }
        
        @Override
        protected void validateConfiguration(Configuration conf) throws ExecutionException {
            if (exit) {
                if (null == conf) {
                    System.exit(-1);
                }
            } else {
                Assert.assertNotNull(conf);
            }
        }
        
        @Override
        protected void validateReasoningResult(ReasoningResult res) throws ExecutionException {
            if (exit) {
                if (res.hasConflict()) {
                    System.exit(-2);
                }
            } else {
                Assert.assertFalse(res.hasConflict());
            }
        }
        
        @Override
        protected void handleExecutionException(ExecutionException ex) throws ExecutionException {
            throw ex;
        }

        @Override
        public void configure(ConfigurationSetup setup) {
            EasySetup easySetup = setup.getEasyProducer();
            if (null != ivmlMetaModelFolder) {
                easySetup.setIvmlMetaModelFolder(ivmlMetaModelFolder);
            }
            if (null != additionalIvmlFolders) {
                easySetup.setAdditionalIvmlFolders(additionalIvmlFolders);
            }
            super.configure(setup);
            easySetup.setLogLevel(EasyLogLevel.VERBOSE); // override for debugging
        }

        @Override
        public TestConfigurer setProperty(String key, String value) {
            super.setProperty(key, value);
            return this;
        }
        
        /**
         * Returns the meta model folder. [for testing]
         * 
         * @return the meta model folder (may be <b>null</b> for none)
         */
        public File getIvmlMetaModelFolder() {
            return ivmlMetaModelFolder;
        }
        
        /**
         * Returns the additional IVML folders.
         *  
         * @return the additional IVML folders (may be <b>null</b> for none)
         */
        public List<File> getAdditionalIvmlFolders() {
            return null != additionalIvmlFolders ? new ArrayList<>(additionalIvmlFolders) : null;
        }
        
    }

    /**
     * Extracts the python service environment implementation (via Maven and main project). Required for 
     * {@link #pythonSourceCodeCheck(File, String)}.
     *  
     * @param srcMainPython the target folder where to extract the service environment to
     * @throws IOException if the service environment archive cannot be extracted
     */
    protected void extractPythonServiceEnv(File srcMainPython) throws IOException {
        FileInputStream zip = new FileInputStream(new File("target/python/services.environment-python.zip"));
        ZipUtils.extractZip(zip, srcMainPython.toPath());
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
     * Disabled if {@link #isIipBuildInitial()}.
     * 
     * @param base the base folder
     * @param name the name/path to the file
     */
    protected void assertDeploymentYaml(File base, String name) {
        if (!isIipBuildInitial()) {
            assertFile(base, name);
            File f = new File(base, name);
            try (FileInputStream in = new FileInputStream(f)) {
                YamlArtifact.readFromYaml(in);
                in.close();
            } catch (IOException e) {
                Assert.fail(f.getAbsolutePath() + ":" + e.getMessage());
            }
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
        /*if (!connector) {
            assertFile(new File(folder, "stubs"), name + "Stub.java");
        }*/
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
     * Asserts the eclipse template ZIP file, usually indicating the the template generation was executed successfully.
     * This assert is disabled if {@link #isIipBuildInitial()}.
     * 
     * @param gen the gen folder
     * @param name the name of the project
     * @return the actual asserted file
     */
    protected static File assertTemplateZip(File gen, String name) {
        File f = new File(gen, "templates/eclipse/" + name + ".zip");
        return assertFile(f);
    }
    
    /**
     * Asserts that the specified file exists and has contents.
     * This assert is disabled if {@link #isIipBuildInitial()}.
     * 
     * @param file the file
     * @return {@code file}
     */
    private static File assertFile(File file) {
        if (!isIipBuildInitial()) {
            Assert.assertTrue("File " + file + " does not exist", file.exists());
            Assert.assertTrue("File " + file + " is empty", file.length() > 0);
        }
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
                        try {
                            Assert.assertTrue("File " + f + " is empty", 
                                FileUtils.readFileToString(f, Charset.defaultCharset()).trim().length() > 0);
                        } catch (IOException e) {
                            Assert.fail("Cannot read " + f + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * Asserts that the specified file exists, has contents and contains the specified {@code search} string(s).
     * Is disabled when {@link #isIipBuildInitial()}.
     * 
     * @param base the base folder
     * @param name the name/path to the file
     * @param search the content/search strings to assert
     * @throws IOException if the file cannot be read
     */
    protected static void assertFileContains(File base, String name, String... search) throws IOException {
        if (!isIipBuildInitial()) {
            File f = assertFile(base, name);
            String contents = FileUtils.readFileToString(f, Charset.defaultCharset());
            for (String s : search) {
                Assert.assertTrue("File " + f + " must contain '" + s + "'", contents.contains(s));
            }
        }
    }

    /**
     * Helper method to configure for partial instantiation, i.e., generated platform APIs only.
     * 
     * @param cfg the configurer instance
     * @return {@code cfg}
     */
    public static InstantiationConfigurer genApi(InstantiationConfigurer cfg) {
        return cfg.setStartRuleName("generateApi");
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
    
    /**
     * Helper method to set the tracing level.
     * 
     * @param cfg the configurer instance
     * @param level ALL|FUNC|TOP
     * @return {@code cfg}
     */
    public static InstantiationConfigurer setTracing(InstantiationConfigurer cfg, String level) {
        return cfg.setProperty(PlatformInstantiator.KEY_PROPERTY_TRACING, level);
    }

}
