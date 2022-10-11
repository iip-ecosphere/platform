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
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.iip_ecosphere.platform.configuration.ConfigurationAas;
import de.iip_ecosphere.platform.configuration.ConfigurationLifecycleDescriptor;
import de.iip_ecosphere.platform.configuration.ConfigurationManager;
import de.iip_ecosphere.platform.configuration.ConfigurationSetup;
import de.iip_ecosphere.platform.configuration.DrawflowGraphFormat;
import de.iip_ecosphere.platform.configuration.EasySetup;
import de.iip_ecosphere.platform.configuration.PlatformInstantiator.InstantiationConfigurer;
import de.iip_ecosphere.platform.configuration.PlatformInstantiator.NonCleaningInstantiationConfigurer;
import de.iip_ecosphere.platform.configuration.ivml.AasIvmlMapper;
import de.iip_ecosphere.platform.configuration.ivml.GraphFormat;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraph;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphEdge;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphNode;
import de.iip_ecosphere.platform.configuration.ivml.IvmlUtils;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.Configuration;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * Tests {@link AasIvmlMapper}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasIvmlMapperTest {
    
    private static File ivmlFolder;
    private static File origBase;
    private static File origIvmlMeta;
    private static File origIvmlConfig;
    private static String origIvmlModelName;
    
    /**
     * Sets up the tests.
     * 
     * @throws IOException if copying fails
     */
    @Before
    public void setup() throws IOException {
        EasySetup ep = ConfigurationSetup.getSetup().getEasyProducer();
        origBase = ep.getBase();
        origIvmlMeta = ep.getIvmlMetaModelFolder();
        origIvmlConfig = ep.getIvmlConfigFolder();
        origIvmlModelName = ep.getIvmlModelName();
        
        setupIvmlFiles();
        ep.setBase(ivmlFolder);
        ep.setIvmlMetaModelFolder(ivmlFolder);
        ep.setIvmlConfigFolder(ivmlFolder);
        ep.setIvmlModelName("TestConfiguration");
    }
    
    /**
     * Creates/cleans a temporary folder and copies the needed IVML files for testing.
     * 
     * @throws IOException if copying fails
     */
    private static void setupIvmlFiles() throws IOException {
        if (null != ivmlFolder) {
            org.apache.commons.io.FileUtils.deleteDirectory(ivmlFolder);
        }
        ivmlFolder = FileUtils.createTmpFolder("config.config");
        org.apache.commons.io.FileUtils.copyDirectory(origIvmlMeta, new File(ivmlFolder, "meta"));
        org.apache.commons.io.FileUtils.copyDirectory(origIvmlConfig == null ? origBase : origIvmlConfig, 
            ivmlFolder, f -> f.getName().endsWith(".ivml") || f.getName().endsWith(".text"));
        String[] testFileNames = {"SimpleMesh.ivml", "CommonSetup.ivml", "CommonSetupNoMonUi.ivml", 
            "TestConfiguration.ivml"};
        for (String n : testFileNames) {
            org.apache.commons.io.FileUtils.copyFile(new File("src/test/easy/" + n), 
                new File(ivmlFolder, n));
        }
    }
    
    /**
     * Cleans up after the tests.
     */
    @After
    public void shutdown() {
        EasySetup ep = ConfigurationSetup.getSetup().getEasyProducer();
        ep.setBase(origBase);
        ep.setIvmlMetaModelFolder(origIvmlMeta);
        ep.setIvmlConfigFolder(origIvmlConfig);
        ep.setIvmlModelName(origIvmlModelName);
        FileUtils.deleteQuietly(ivmlFolder);
        ep.reset();
    }

    /**
     * Tests {@link AasIvmlMapper}.
     * 
     * @throws ExecutionException shall not occur in a successful test
     */
    @Test
    public void testAasIvmlMapper() throws ExecutionException {
        InstantiationConfigurer configurer = new NonCleaningInstantiationConfigurer("SimpleMesh", 
            ivmlFolder, FileUtils.getTempDirectory());
        GraphFormat format = new DrawflowGraphFormat();
        IvmlGraph graph = assertGraphMesh(configurer, "myApp", 0, s -> {
            Assert.assertNotNull(s.getSubmodelElementCollection("myReceiverService"));
            Assert.assertNotNull(s.getSubmodelElementCollection("mySourceService"));
        }, format);
        
        IvmlGraph expected = new AbstractGraphTest.TestGraph();
        IvmlGraphNode mySource = new AbstractGraphTest.TestNode();
        mySource.setName("Simple Data Source");
        mySource.setXPos(10);
        mySource.setYPos(10);
        IvmlGraphNode myReceiver = new AbstractGraphTest.TestNode();
        myReceiver.setName("Simple Data Receiver");
        myReceiver.setXPos(50);
        myReceiver.setYPos(10);
        IvmlGraphEdge myEdge = new AbstractGraphTest.TestEdge(mySource, myReceiver);
        myEdge.setName("Source->Receiver");
        mySource.addEdge(myEdge);
        myReceiver.addEdge(myEdge);
        expected.addNode(mySource);
        expected.addNode(myReceiver);

        AbstractGraphTest.assertGraph(expected, graph);
    }
    
    /**
     * Starts EASy using an instantiation configurer.
     * 
     * @param configurer the configurer
     * @return the configuration lifecycle descriptor
     */
    private ConfigurationLifecycleDescriptor startEasy(InstantiationConfigurer configurer) {
        ConfigurationSetup setup = ConfigurationSetup.getSetup();
        configurer.configure(setup);
        ConfigurationLifecycleDescriptor lcd = configurer.obtainLifecycleDescriptor();
        lcd.startup(new String[0]); // shall register executor
        return lcd;
    }
    
    /**
     * Starts EASy using an instantiation configurer and validates/asserts the model.
     * 
     * @param configurer the configurer
     * @return the configuration lifecycle descriptor
     */
    private ConfigurationLifecycleDescriptor startEasyValidate(InstantiationConfigurer configurer) {
        ConfigurationLifecycleDescriptor lcd = startEasy(configurer);
        ReasoningResult rRes = ConfigurationManager.validateAndPropagate();
        Assert.assertNotNull("No model loaded", rRes);
        return lcd;
    }
    
    /**
     * Stops EASy using an instantiation configurer.
     * 
     * @param lcd the configuration lifecycle descriptor
     */
    private void stopEasy(ConfigurationLifecycleDescriptor lcd) {
        lcd.shutdown();
    }

    /**
     * Asserts a graph mesh.
     * 
     * @param configurer the instantiation configurer allowing to read an IVML model
     * @param appName the name of the app to assert/use
     * @param netIndex the 0-based index of the service net to assert
     * @param servicesAsserter generic services asserter
     * @param format the graph format to use
     * @return the graph as internal representation
     * @throws ExecutionException shall not occur in a successful test
     */
    private IvmlGraph assertGraphMesh(InstantiationConfigurer configurer, String appName, int netIndex, 
        Consumer<SubmodelElementCollection> servicesAsserter, GraphFormat format) throws ExecutionException {
        ConfigurationLifecycleDescriptor lcd = startEasyValidate(configurer);
        AasIvmlMapper mapper = getInstance();
        mapper.addGraphFormat(format);        
        
        AasFactory aasFactory = AasFactory.getInstance();
        SubmodelBuilder smb = aasFactory.createSubmodelBuilder(ConfigurationAas.NAME_SUBMODEL, null);
        InvocablesCreator iCreator = aasFactory.createInvocablesCreator(AasFactory.LOCAL_PROTOCOL, "localhost", 0);
        ProtocolServerBuilder psb = aasFactory.createProtocolServerBuilder(AasFactory.LOCAL_PROTOCOL, 0);
        mapper.mapByType(smb, iCreator);
        mapper.bindOperations(psb);
        String res = assertSubmodel(smb.build(), appName, netIndex, servicesAsserter);
        psb.build();
        stopEasy(lcd);
        return format.fromString(res, mapper.getGraphFactory(), mapper);
    }

    /**
     * Creates a mapper instance. Call {@link #startEasy(InstantiationConfigurer)} or 
     * {@code #startEasyValidate(InstantiationConfigurer)} before.
     * 
     * @return the instance
     */
    private AasIvmlMapper getInstance() {
        Configuration cfg = ConfigurationManager.getVilConfiguration();
        Assert.assertNotNull("No configuration available", cfg);
        return new AasIvmlMapper(() -> cfg, new ConfigurationAas.IipGraphMapper());
    }
    
    /**
     * Asserts the simple mesh configuration submodel.
     * 
     * @param sm the submodel instance
     * @param appName the name of the app to assert/use
     * @param netIndex the 0-based index of the service net to assert
     * @param servicesAsserter generic services asserter
     * @return the graph as String representation
     * @throws ExecutionException shall not occur in a successful test
     */
    private String assertSubmodel(Submodel sm, String appName, int netIndex, 
        Consumer<SubmodelElementCollection> servicesAsserter) throws ExecutionException {
        
        SubmodelElementCollection sec = sm.getSubmodelElementCollection("Service");
        Assert.assertNotNull(sec); // 2 variables of type service shall exist in the model

        // Accessing the net as intended
        sec = sm.getSubmodelElementCollection("Application");
        Assert.assertNotNull(sec); // 1 variables of type Application shall exist in the model
        sec = sec.getSubmodelElementCollection(appName);
        Assert.assertNotNull(sec); // this application shall be there
        sec = sec.getSubmodelElementCollection("services");
        Assert.assertNotNull(sec);
        Property prop = sec.getProperty("var_" + netIndex);
        Assert.assertNotNull(prop);
        String serviceNetName = prop.getValue().toString();
        Operation op = sm.getOperation(AasIvmlMapper.OP_GET_GRAPH);
        Assert.assertNotNull(op);
        String res = JsonResultWrapper.fromJson(op.invoke(serviceNetName, DrawflowGraphFormat.NAME));
        Assert.assertNotNull(res);
        return res;
    }
    
    /**
     * Tests {@link AasIvmlMapper#getVariable(String)}.
     * 
     * @throws ExecutionException shall not occur if successful
     */
    @Test
    public void testGetVariable() throws ExecutionException {
        InstantiationConfigurer configurer = new NonCleaningInstantiationConfigurer("TestConfiguration", 
            ivmlFolder, FileUtils.getTempDirectory());
        ConfigurationLifecycleDescriptor lcd = startEasyValidate(configurer);
        AasIvmlMapper mapper = getInstance();
        
        Assert.assertNotNull(mapper.getVariable("instDir"));
        Assert.assertNull(mapper.getVariable("instDir123"));
        Assert.assertNotNull(mapper.getVariable("IIPEcosphere::instDir"));
        Assert.assertNotNull(mapper.getVariable("aasServer"));
        
        stopEasy(lcd);
    }
    
    /**
     * Tests {@link AasIvmlMapper#changeValues(java.util.Map)}.
     * 
     * @throws ExecutionException shall not occur if successful
     * @throws IOException if resetting IVML files fails
     */
    @Test
    public void testChangeValues() throws ExecutionException, IOException {
        InstantiationConfigurer configurer = new NonCleaningInstantiationConfigurer("TestConfiguration", 
            ivmlFolder, FileUtils.getTempDirectory());
        ConfigurationLifecycleDescriptor lcd = startEasyValidate(configurer);
        AasIvmlMapper mapper = getInstance();
        
        Map<String, String> values = new HashMap<String, String>();
        values.put("instDir", "\"/home/iip\""); // IVML expression
        values.put("javaExe", "\"/usr/local/java\""); // IVML expression
        // do not overwrite already frozen variable
        values.put("deviceIdProvider", "HostnameDeviceIdProvider{class=\"a.b.C\", artifact=\"mg.art:art:1.2.3\"}");
        mapper.changeValues(values);
        
        assertStringVar("/home/iip", mapper.getVariable("instDir"));
        assertStringVar("/usr/local/java", mapper.getVariable("javaExe"));
        Assert.assertNotNull(mapper.getVariable("deviceIdProvider"));
        assertStringVar("a.b.C", mapper.getVariable("deviceIdProvider").getNestedElement("class"));
        assertStringVar("mg.art:art:1.2.3", mapper.getVariable("deviceIdProvider.artifact"));
        
        assertIvmlFileChange("TestConfiguration", false, "instDir", "javaExe", "deviceIdProvider");
    
        stopEasy(lcd);
        setupIvmlFiles(); // revert changes
    }

    /**
     * Tests the set graph function.
     * 
     * @throws IOException if copying/resetting files fails
     */
    @Ignore("Incomplete")
    @Test
    public void testSetGraph() throws IOException {
        InstantiationConfigurer configurer = new NonCleaningInstantiationConfigurer("TestConfiguration", 
            ivmlFolder, FileUtils.getTempDirectory());
        ConfigurationLifecycleDescriptor lcd = startEasyValidate(configurer);
        @SuppressWarnings("unused")
        AasIvmlMapper mapper = getInstance();
        
        // modify, create
        // get GraphString from artificial IvmlGraph
        // TODO setGraph
        
        stopEasy(lcd);
        setupIvmlFiles(); // revert changes
    }

    /**
     * Tests the create/delete variable function.
     * 
     * @throws IOException if copying/resetting files fails
     */
    @Test
    public void testCreateDeleteVariable() throws IOException, ExecutionException {
        InstantiationConfigurer configurer = new NonCleaningInstantiationConfigurer("TestConfiguration", 
            ivmlFolder, FileUtils.getTempDirectory());
        ConfigurationLifecycleDescriptor lcd = startEasyValidate(configurer);
        AasIvmlMapper mapper = getInstance();

        mapper.createVariable("rec1", "RecordType", "{}");

        String valueEx = "{"
            + "id=\"SimpleSource\"," 
            + "name=\"Simple Data Source\","
            + "description=\"\","
            + "ver=\"0.1.0\","
            + "deployable=true,"
            + "asynchronous=true,"
            + "class=\"de.iip_ecosphere.platform.test.apps.serviceImpl.SimpleSourceImpl\","
            + "artifact=\"de.iip-ecosphere.platform:apps.ServiceImpl:0.4.0\","
            + "kind=ServiceKind::SOURCE_SERVICE,"
            + "output={{type=rec1}}"
            + "}";
        
        mapper.createVariable("test1", "JavaService", valueEx);
        assertIvmlFileChange("TestConfiguration", false, "test1", "rec1");
        
        mapper.deleteVariable("test1");
        assertIvmlFileChange("TestConfiguration", true, "test1");

        stopEasy(lcd);
        setupIvmlFiles(); // revert changes
    }

    /**
     * Asserts that {@code var} has the {@code expected} String value.
     * 
     * @param expected the expected value
     * @param var the variable
     */
    private void assertStringVar(String expected, IDecisionVariable var) {
        Assert.assertNotNull(var);
        Assert.assertEquals(expected, IvmlUtils.getStringValue(var, ""));
    }

    /**
     * Asserts that {@code var} has the {@code expected} Boolean value.
     * 
     * @param expected the expected value
     * @param var the variable
     */
    @SuppressWarnings("unused")
    private void assertBooleanVar(boolean expected, IDecisionVariable var) {
        Assert.assertNotNull(var);
        Assert.assertEquals(expected, IvmlUtils.getBooleanValue(var, !expected));
    }

    /**
     * Asserts that {@code var} has the {@code expected} Boolean value.
     * 
     * @param expected the expected value
     * @param var the variable
     */
    @SuppressWarnings("unused")
    private void assertIntVar(int expected, IDecisionVariable var) {
        Assert.assertNotNull(var);
        Assert.assertEquals(expected, IvmlUtils.getIntValue(var, -1));
    }

    /**
     * Very simple check that an IVML file was changed and contains expected strings.
     * 
     * @param file the file to check, within {@code #ivmlFolder}
     * @param invert if {@code false} assert for existence, if {@code true} for absence
     * @param expected the expected strings
     */
    private void assertIvmlFileChange(String file, boolean invert, String... expected) {
        if (!file.endsWith(".ivml")) {
            file = file + ".ivml";
        }
        try {
            String contents = org.apache.commons.io.FileUtils.readFileToString(new File(ivmlFolder, file), 
                Charset.defaultCharset());
            for (String e : expected) {
                boolean found = contents.contains(e);
                if (invert) {
                    found = !found;
                }
                Assert.assertTrue(found);
            }
        } catch (IOException e) {
            Assert.fail("Cannot read " + file + ": " + e.getMessage());
        }
    }

}
