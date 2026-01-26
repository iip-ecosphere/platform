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

package test.de.iip_ecosphere.platform.configuration.easyProducer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.iip_ecosphere.platform.configuration.cfg.ConfigurationChangeType;
import de.iip_ecosphere.platform.configuration.easyProducer.ConfigurationLifecycleDescriptor;
import de.iip_ecosphere.platform.configuration.easyProducer.ConfigurationManager;
import de.iip_ecosphere.platform.configuration.easyProducer.ConfigurationSetup;
import de.iip_ecosphere.platform.configuration.easyProducer.DrawflowGraphFormat;
import de.iip_ecosphere.platform.configuration.easyProducer.EasySetup;
import de.iip_ecosphere.platform.configuration.easyProducer.ConfigurationLifecycleDescriptor.ExecutionMode;
import de.iip_ecosphere.platform.configuration.easyProducer.PlatformInstantiator.InstantiationConfigurer;
import de.iip_ecosphere.platform.configuration.easyProducer.PlatformInstantiator.NonCleaningInstantiationConfigurer;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.AasIvmlMapper;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.AasIvmlMapper.InstantiationMode;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.DefaultEdge;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.DefaultGraph;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.DefaultNode;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.GraphFormat;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.IvmlGraphMapper;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.IvmlGraphMapper.IvmlGraph;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.IvmlGraphMapper.IvmlGraphEdge;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.IvmlGraphMapper.IvmlGraphNode;
import de.iip_ecosphere.platform.configuration.easyProducer.serviceMesh.ServiceMeshGraphMapper;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.IvmlUtils;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.LocalPersistenceType;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.PersistenceType;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.ElementsAccess;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.json.Json;
import de.iip_ecosphere.platform.support.json.JsonResultWrapper;
import de.iip_ecosphere.platform.support.json.JsonUtils;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.Configuration;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.IvmlModelQuery;
import net.ssehub.easy.varModel.model.ModelQuery;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.ProjectImport;
import test.de.iip_ecosphere.platform.support.aas.TestWithPlugin;

/**
 * Tests {@link AasIvmlMapper}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasIvmlMapperTest extends TestWithPlugin {
    
    private static final String MODEL_NAME = "PlatformConfiguration";
    
    private static File ivmlFolder;
    private static File origBase;
    private static File origIvmlMeta;
    private static File origIvmlConfig;
    private static String origIvmlModelName;
    private static Set<String> varNames = new HashSet<>();
    
    /**
     * Sets up the tests.
     */
    @Before
    public void setup() {
        super.setup();
        try {
            EasySetup ep = ConfigurationSetup.getSetup().getEasyProducer();
            origBase = ep.getBase();
            origIvmlMeta = ep.getIvmlMetaModelFolder();
            origIvmlConfig = ep.getIvmlConfigFolder();
            origIvmlModelName = ep.getIvmlModelName();
            
            setupIvmlFiles();
            ep.setBase(ivmlFolder);
            ep.setIvmlMetaModelFolder(ivmlFolder);
            ep.setIvmlConfigFolder(ivmlFolder);
            ep.setIvmlModelName(MODEL_NAME);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
    
    /**
     * Creates/cleans a temporary folder and copies the needed IVML files for testing.
     * 
     * @throws IOException if copying fails
     */
    private static void setupIvmlFiles() throws IOException {
        if (null != ivmlFolder) {
            FileUtils.deleteDirectory(ivmlFolder);
        }
        ivmlFolder = FileUtils.createTmpFolder("config.config").getCanonicalFile();
        
        String srcCfg = (origIvmlMeta.toString() + "/cfg/").replace('/', File.separatorChar);
        FileUtils.copyDirectory(origIvmlMeta, new File(ivmlFolder, "meta"), 
            f -> !f.toString().startsWith(srcCfg));
        FileUtils.copyDirectory(origIvmlConfig == null ? origBase : origIvmlConfig, 
            ivmlFolder, f -> f.getName().endsWith(".ivml") || f.getName().endsWith(".text"));
        FileUtils.copyDirectory(new File(EasySetup.getTestingEasyModelParent(), 
            "src/test/easy/simpleMesh"), ivmlFolder);
        copy(EasySetup.getTestingEasyModelParent() + "/src/test/easy/common", "CommonSetup.ivml", 
            "CommonSetupNoMonUi.ivml");
    }
    
    /**
     * Copies files given by their simple names within the same {@code baseFolder} into {@link #ivmlFolder}.
     * 
     * @param baseFolder the base folder
     * @param fileNames the file names
     * @throws IOException if copying fails
     */
    private static void copy(String baseFolder, String... fileNames) throws IOException {
        for (String n : fileNames) {
            FileUtils.copyFile(new File(baseFolder, n), new File(ivmlFolder, n));
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
        InstantiationConfigurer configurer = new NonCleaningInstantiationConfigurer(MODEL_NAME, 
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
        myReceiver.setXPos(300);
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
        ConfigurationSetup setup = ConfigurationSetup.getSetup(false);
        configurer.configure(setup);
        ConfigurationLifecycleDescriptor lcd = configurer.obtainLifecycleDescriptor();
        lcd.startup(ExecutionMode.IVML, new String[0]); // shall register executor
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
        ConfigurationManager.reInit();        
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
        AasIvmlMapper mapper = getInstance(false);
        mapper.addGraphFormat(format);        
        
        AasSetup aasSetup = AasSetup.createLocalEphemeralSetup(null, false);
        AasPartRegistry.setAasSetup(aasSetup);
        ServerRecipe rcp = AasFactory.getInstance().createServerRecipe();
        Endpoint regEndpoint = aasSetup.adaptEndpoint(aasSetup.getRegistryEndpoint());
        System.out.println("ServerHost " + aasSetup.getServerHost() + " " + regEndpoint.toUri());
        PersistenceType pType = LocalPersistenceType.INMEMORY;
        System.out.println("Starting " + pType + " AAS registry on " + regEndpoint.toUri());
        Server registryServer = rcp.createRegistryServer(aasSetup, pType);
        registryServer.start();
        Endpoint serverEndpoint = aasSetup.adaptEndpoint(aasSetup.getServerEndpoint());
        System.out.println("ServerHost " + aasSetup.getServerHost() + " " + serverEndpoint.toUri());
        System.out.println("Starting " + pType + " AAS server on " + serverEndpoint.toUri());
        Server aasServer = rcp.createAasServer(aasSetup, pType);
        aasServer.start();

        AasFactory aasFactory = AasFactory.getInstance();
        AasBuilder aasBuilder = aasFactory.createAasBuilder("Platform", null);
        SubmodelBuilder smb = AasPartRegistry.createSubmodelBuilder(aasBuilder, 
            AasPartRegistry.NAME_SUBMODEL_CONFIGURATION);
        InvocablesCreator iCreator = aasFactory.createInvocablesCreator(aasSetup);
        ProtocolServerBuilder psb = aasFactory.createProtocolServerBuilder(aasSetup);
        mapper.mapByType(smb, iCreator);
        mapper.bindOperations(psb);
        Submodel sm = smb.build();
        Aas aas = aasBuilder.build();
        Server implServer = psb.build().start();
        try {
            AasPartRegistry.remoteDeploy(CollectionUtils.toList(aas)); 
        } catch (IOException e) {
            e.printStackTrace();
        }

        String res = assertSubmodel(sm, appName, netIndex, servicesAsserter);
        stopEasy(lcd);
        
        implServer.stop(false);
        aasServer.stop(true);
        registryServer.stop(true);
        
        return format.fromString(res, mapper.getGraphFactory(), mapper);
    }

    /**
     * A slightly extended graph mapper that changes an extended managed model structure with wildcards
     * for AllTypes and AllServices into the usual structure while modifying a graph. This specialized testing
     * mapper records all changes and makes them available via {@link #getChanges()}.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class MyAasIvmlMapper extends AasIvmlMapper {
        
        private boolean adapt;
        private Map<IDecisionVariable, ConfigurationChangeType> changes = new HashMap<>();
        
        /**
         * Creates a mapper with default settings.
         * 
         * @param cfgSupplier a supplier providing the actual configuration instance
         * @param graphMapper maps a graph from IVML to an internal structure
         * @param adapt allow for adaptation/rewriting
         * @throws IllegalArgumentException if {@code cfgSupplier} is <b>null</b>
         */
        public MyAasIvmlMapper(Supplier<Configuration> cfgSupplier, IvmlGraphMapper graphMapper, boolean adapt) {
            super(cfgSupplier, graphMapper, null);
            setChangeListener(new ConfigurationChangeListener() {
                
                @Override
                public void configurationChanged(IDecisionVariable var, ConfigurationChangeType type) {
                    changes.put(var,  type);
                }
                
            });
            this.adapt = adapt;
        }

        /**
         * Asserts the number of changes.
         * 
         * @param count the expected number of changes
         */
        private void assertChangesCount(int count) {
            Assert.assertEquals("Number of changes differs", count, changes.size());
        }

        /**
         * Asserts the number of changes.
         * 
         * @param pred the predicate that must apply to the number of changes
         */
        private void assertChangesCount(Predicate<Integer> pred) {
            Assert.assertTrue("Number of changes differs", pred.test(changes.size()));
        }

        /**
         * Asserts a change on the specified variable.
         * 
         * @param varPred predicate identifying the variable to test for, via its name
         * @param type the expected change type
         */
        private void assertChange(Predicate<String> varPred, ConfigurationChangeType type) {
            Map.Entry<IDecisionVariable, ConfigurationChangeType> found = null;
            for (Map.Entry<IDecisionVariable, ConfigurationChangeType> ent : changes.entrySet()) {
                if (varPred.test(ent.getKey().getDeclaration().getName())) {
                    found = ent;
                    break;
                }
            }
            Assert.assertNotNull("Change for specified variable not found/recorded.", found);
            Assert.assertEquals("Change type mismatch", type, found.getValue());
        }
        
        /**
         * Convenience method for {@link #assertChangesCount(int)}, 
         * {@link #assertChange(Predicate, ConfigurationChangeType)} and {@link #clearChanges()}.
         * 
         * @param count the expected number of changes
         * @param varPred predicate identifying the variable to test for, via its name
         * @param type the expected change type
         */
        private void assertChangeAndClear(int count, Predicate<String> varPred, ConfigurationChangeType type) {
            assertChangesCount(count);
            assertChange(varPred, type);
            clearChanges();
        }
        
        /**
         * Asserts {@code type\ on all variables in {@code names} found by equality.
         * 
         * @param type the expected change type
         * @param names variable names to be asserted
         */
        private void assertChanges(ConfigurationChangeType type, String... names) {
            for (String name: names) {
                assertChange(n -> n.equals(name), type);
            }
        }
        
        /**
         * Clears all recorded changes.
         */
        private void clearChanges() {
            changes.clear();
        }

        @Override
        protected Project adaptTarget(Project root, Project project) throws ExecutionException {
            String prjName = project.getName();
            if (adapt && (PRJ_NAME_ALLSERVICES.equals(prjName) || PRJ_NAME_ALLTYPES.equals(prjName))) {
                boolean found = false;
                for (int i = project.getImportsCount() - 1; i >= 0; i--) {
                    ProjectImport imp = project.getImport(i);
                    if (imp.isWildcard()) {
                        project.removeImport(imp);
                        found = true;
                    }
                }
                if (found) {
                    // this is the simple variant. Full solution: copy over model elements.
                    try {
                        if (PRJ_NAME_ALLSERVICES.equals(prjName)) {
                            ProjectImport imp = new ProjectImport(PRJ_NAME_ALLTYPES);
                            imp.setResolved(ModelQuery.findProject(root, PRJ_NAME_ALLTYPES));
                            project.addImport(imp);
                        } else if (PRJ_NAME_ALLTYPES.equals(prjName)) {
                            ProjectImport imp = new ProjectImport("IIPEcosphere");
                            imp.setResolved(ModelQuery.findProject(root, "IIPEcosphere"));
                            project.addImport(imp);
                        }
                    } catch (ModelManagementException e) {
                        throw new ExecutionException(e);
                    }
                }
            }
            return project;
        }

    }

    /**
     * Creates a mapper instance. Call {@link #startEasy(InstantiationConfigurer)} or 
     * {@code #startEasyValidate(InstantiationConfigurer)} before.
     * 
     * @param adapt allow for adaptation/rewriting of the model structure from an extended managed model to 
     *   a default managed model
     * @return the instance
     */
    private MyAasIvmlMapper getInstance(boolean adapt) {
        Configuration cfg = ConfigurationManager.getVilConfiguration();
        Assert.assertNotNull("No configuration available", cfg);
        return new MyAasIvmlMapper(() -> cfg, new ServiceMeshGraphMapper(), adapt); 
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
        ElementsAccess sel = sm.getSubmodelElementCollection("ServiceBase");
        Assert.assertNotNull(sel); // 2 variables of type service shall exist in the model
        // Accessing the net as intended
        sel = sm.getSubmodelElementCollection("Application");
        Assert.assertNotNull(sel); // 1 variables of type Application shall exist in the model
        ElementsAccess sec = sel.getSubmodelElementList(appName);
        Assert.assertNotNull(sec); // this application shall be there
        sel = sec.getSubmodelElementList("services");
        Assert.assertNotNull(sel);
        ElementsAccess varSmc = sel.getSubmodelElementCollection("var_" + netIndex);
        Assert.assertNotNull(varSmc);
        Property prop = varSmc.getProperty("varValue");
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
        InstantiationConfigurer configurer = new NonCleaningInstantiationConfigurer(MODEL_NAME, 
            ivmlFolder, FileUtils.getTempDirectory());
        ConfigurationLifecycleDescriptor lcd = startEasyValidate(configurer);
        AasIvmlMapper mapper = getInstance(false);
        
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
        InstantiationConfigurer configurer = new NonCleaningInstantiationConfigurer(MODEL_NAME, 
            ivmlFolder, FileUtils.getTempDirectory());
        ConfigurationLifecycleDescriptor lcd = startEasyValidate(configurer);
        MyAasIvmlMapper mapper = getInstance(false);
        
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
        
        assertIvmlFileChange(MODEL_NAME, false, "instDir", "javaExe", "deviceIdProvider");
        mapper.assertChangesCount(values.size());
        for (String name : values.keySet()) {
            mapper.assertChange(n -> n.equals(name), ConfigurationChangeType.MODIFIED);
        }
        mapper.clearChanges();

        stopEasy(lcd);
        setupIvmlFiles(); // revert changes
    }
    
    // checkstyle: stop method length check

    /**
     * Tests the set/delete graph functions.
     * 
     * @throws IOException if copying/resetting files fails
     * @throws ExecutionException if setting graphs fails
     */
    @Test
    public void testChangeGraph() throws IOException, ExecutionException {
        InstantiationConfigurer configurer = new NonCleaningInstantiationConfigurer(MODEL_NAME, 
            ivmlFolder, FileUtils.getTempDirectory());
        ConfigurationLifecycleDescriptor lcd = startEasyValidate(configurer);
        DrawflowGraphFormat drawflowFormat = new DrawflowGraphFormat();
        
        DefaultGraph graph = new DefaultGraph(null);
        DefaultNode n1 = new DefaultNode(null);
        n1.setName("graphSrc");
        graph.addNode(n1);
        DefaultNode n2 = new DefaultNode(null);
        n2.setName("Sink");
        n2.setImpl("graphSnk");
        graph.addNode(n2);
        DefaultEdge e1 = new DefaultEdge(null, n1, n2);
        n1.addEdge(e1);
        n2.addEdge(e1);

        MyAasIvmlMapper mapper = getInstance(true);
        mapper.addGraphFormat(drawflowFormat);
        
        mapper.createVariable("graphRec1", "RecordType", false, "{}");
        String valueEx = "{"
            + "id=\"SimpleGraphSource\"," 
            + "name=\"Simple Graph Data Source\","
            + "description=\"\","
            + "ver=\"0.1.0\","
            + "deployable=true,"
            + "asynchronous=true,"
            + "class=\"de.iip_ecosphere.platform.test.apps.serviceImpl.SimpleSourceImpl\","
            + "artifact=\"de.iip-ecosphere.platform:apps.ServiceImpl:0.4.0\","
            + "kind=ServiceKind::SOURCE_SERVICE,"
            + "output={{type=graphRec1}}"
            + "}";
        mapper.createVariable("graphSrc", "JavaService", false, valueEx);
        valueEx = "{"
            + "id=\"SimpleGraphSink\"," 
            + "name=\"Simple Graph Data Sink\","
            + "description=\"\","
            + "ver=\"0.1.0\","
            + "deployable=true,"
            + "asynchronous=true,"
            + "class=\"de.iip_ecosphere.platform.test.apps.serviceImpl.SimpleSinkImpl\","
            + "artifact=\"de.iip-ecosphere.platform:apps.ServiceImpl:0.4.0\","
            + "kind=ServiceKind::SINK_SERVICE,"
            + "input={{type=graphRec1}}"
            + "}";
        mapper.createVariable("graphSnk", "JavaService", false, valueEx);
        
        valueEx = "{"
            + "id=\"myTestApp\","
            + "name=\"name\","
            + "description=\"\","
            + "ver=\"0.1.0\""
            + "}";
        mapper.setGraph("myTestApp", valueEx, "myTestMesh", drawflowFormat.getName(), drawflowFormat.toString(graph));

        assertIvmlFileChange("AllTypes", false, "graphRec1");
        assertIvmlFileChange("AllServices", false, "graphSrc", "graphSnk");
        assertIvmlFileChange("meshes/ServiceMeshPartMyTestMesh", false, "myTestMesh", "graphSrc", "Sink");
        assertIvmlFileChange("apps/ApplicationPartMyTestApp", false, "myTestApp");
        mapper.assertChanges(ConfigurationChangeType.CREATED, "node_Sink", "graphSrc", "myTestMesh", "node_graphSrc", 
            "graphSnk", "conn_0", "graphRec1", "myTestApp");
        mapper.clearChanges();

        mapper.deleteGraph("myTestApp", "myTestMesh"); // delete mesh without app
        Assert.assertFalse(resolveIvmlFile("meshes/ServiceMeshPartMyAppMyTestMesh").exists());
        assertIvmlFileChange("apps/ApplicationPartMyTestApp", false, "myTestApp");
        mapper.assertChanges(ConfigurationChangeType.DELETED, "node_Sink", "conn_0", "node_graphSrc", "myTestMesh");
        mapper.assertChanges(ConfigurationChangeType.MODIFIED, "myTestApp"); // delete mesh without app
        mapper.clearChanges();

        mapper.deleteGraph("myTestApp", null); // delete remaining app
        Assert.assertFalse(resolveIvmlFile("meshes/ServiceMeshPartMyTestAppMyMesh").exists());
        Assert.assertFalse(resolveIvmlFile("apps/ApplicationPartMyTestApp").exists());
        mapper.assertChanges(ConfigurationChangeType.DELETED, "myTestApp"); // delete mesh without app
        mapper.assertChangesCount(1);
        mapper.clearChanges();
        
        stopEasy(lcd);
        setupIvmlFiles(); // revert changes
    }
    
    // checkstyle: resume method length number check

    /**
     * Tests the create/delete variable function.
     * 
     * @throws IOException if copying/resetting files fails
     * @throws ExecutionException if creating/deleting the variable fails
     */
    @Test
    public void testCreateDeleteVariable() throws IOException, ExecutionException {
        InstantiationConfigurer configurer = new NonCleaningInstantiationConfigurer(MODEL_NAME, 
            ivmlFolder, FileUtils.getTempDirectory());
        ConfigurationLifecycleDescriptor lcd = startEasyValidate(configurer);
        MyAasIvmlMapper mapper = getInstance(false);

        mapper.createVariable("rec1", "RecordType", false, "{}");
        mapper.assertChangeAndClear(1, n -> n.endsWith("rec1"), ConfigurationChangeType.CREATED);

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
        
        mapper.createVariable("test1", "JavaService", false, valueEx);
        assertIvmlFileChange("AllTypes", false, "rec1");
        assertIvmlFileChange("AllServices", false, "test1");
        mapper.assertChangeAndClear(1, n -> n.endsWith("test1"), ConfigurationChangeType.CREATED);
        
        mapper.deleteVariable("test1");
        assertIvmlFileChange("AllTypes", false, "rec1");
        assertIvmlFileChange("AllServices", true, "test1");
        mapper.assertChangeAndClear(1, n -> n.endsWith("test1"), ConfigurationChangeType.DELETED);

        mapper.createVariable("test2", "setOf(Integer)", false, "{25, 27}");
        assertIvmlFileChange("AllConstants", false, "test2");
        mapper.assertChangeAndClear(1, n -> n.endsWith("test2"), ConfigurationChangeType.CREATED);
        mapper.deleteVariable("test2");
        assertIvmlFileChange("AllConstants", true, "test2");
        mapper.assertChangeAndClear(1, n -> n.endsWith("test2"), ConfigurationChangeType.DELETED);
        
        stopEasy(lcd);
        setupIvmlFiles(); // revert changes
    }
    
    /**
     * Tests {@link AasIvmlMapper#getTemplates()}.
     * 
     * @throws IOException if copying/resetting files fails
     * @throws ExecutionException if the operation fails
     */
    @Test
    public void testGetTemplates() throws IOException, ExecutionException {
        InstantiationConfigurer configurer = new NonCleaningInstantiationConfigurer(MODEL_NAME, 
            ivmlFolder, FileUtils.getTempDirectory());
        ConfigurationLifecycleDescriptor lcd = startEasyValidate(configurer);
        AasIvmlMapper mapper = getInstance(false);
        
        String templates = mapper.getTemplates();
        Assert.assertNotNull(templates);
        java.util.List<String> templatesList = JsonUtils.listFromJson(templates, String.class);
        Assert.assertNotNull(templatesList);
        Assert.assertTrue(templatesList.contains("tplApp"));

        stopEasy(lcd);
        setupIvmlFiles(); // revert changes
    }

    /**
     * Tests renaming a variable using {@link AasIvmlMapper#renameVariable(String, String)}.
     * 
     * @throws IOException if copying/resetting files fails
     * @throws ExecutionException if the operation fails
     * @throws ModelQueryException if accessing IVML parts fails
     */
    @Test
    public void testRenameVariable() throws IOException, ExecutionException, ModelQueryException {
        InstantiationConfigurer configurer = new NonCleaningInstantiationConfigurer(MODEL_NAME, 
            ivmlFolder, FileUtils.getTempDirectory());
        ConfigurationLifecycleDescriptor lcd = startEasyValidate(configurer);
        MyAasIvmlMapper mapper = getInstance(false);

        mapper.renameVariable("myMesh", "myMesh1");

        net.ssehub.easy.varModel.confModel.Configuration cfg = ConfigurationManager.getIvmlConfiguration();
        Project root = cfg.getProject();
        AbstractVariable var = IvmlModelQuery.findVariable(root, "myMesh", null);
        Assert.assertNull(var); // was renamed, shall not be there
        var = IvmlModelQuery.findVariable(root, "myMesh1", null);
        Assert.assertNotNull(var); // shall be there
        Assert.assertEquals("ServiceMeshPartMyMesh", var.getProject().getName()); // shall still be there
        mapper.assertChangeAndClear(1, n -> n.endsWith("myMesh1"), ConfigurationChangeType.MODIFIED);

        stopEasy(lcd);
        setupIvmlFiles(); // revert changes
    }

    /**
     * Tests instantiating a template {@link AasIvmlMapper#getOpenTemplateVariables(String)}.
     * 
     * @throws IOException if copying/resetting files fails
     * @throws ExecutionException if the operation fails
     * @throws ModelQueryException if accessing IVML parts fails
     */
    @Test
    public void testInstantiateInterfaces() throws IOException, ExecutionException, ModelQueryException {
        File gen = FileUtils.createTmpFolder("okto-instantiate");
        File arts = FileUtils.createTmpFolder("okto-artifacts");
        File artsOrig = ConfigurationSetup.getSetup().getArtifactsFolder();
        ConfigurationSetup.getSetup().setArtifactsFolder(arts);
        InstantiationConfigurer configurer = new NonCleaningInstantiationConfigurer(MODEL_NAME, 
            ivmlFolder, gen);
        ConfigurationLifecycleDescriptor lcd = startEasyValidate(configurer);
        AasIvmlMapper mapper = getInstance(false);

        Object result = mapper.instantiate(InstantiationMode.INTERFACES, null, null); // result does not care here
        Assert.assertTrue(FileUtils.getFolderSize(gen) > 1);
        Assert.assertNotNull(result);
        List<String> resList = Json.listFromJsonDflt(result, String.class);
        Assert.assertTrue(resList.size() > 0);
        
        stopEasy(lcd);
        setupIvmlFiles(); // revert changes
        FileUtils.deleteQuietly(gen);
        FileUtils.deleteQuietly(arts);
        ConfigurationSetup.getSetup().setArtifactsFolder(artsOrig);
    }
    
    /**
     * Tests instantiating a template {@link AasIvmlMapper#getOpenTemplateVariables(String)}.
     * 
     * @throws IOException if copying/resetting files fails
     * @throws ExecutionException if the operation fails
     * @throws ModelQueryException if accessing IVML parts fails
     */
    @Test
    public void testInstantiateTemplate() throws IOException, ExecutionException, ModelQueryException {
        InstantiationConfigurer configurer = new NonCleaningInstantiationConfigurer(MODEL_NAME, 
            ivmlFolder, FileUtils.getTempDirectory());
        ConfigurationLifecycleDescriptor lcd = startEasyValidate(configurer);
        AasIvmlMapper mapper = getInstance(false);

        // run getOpenTemplateVariables before, we assume fixed values here
        Map<String, String> adjustments = new HashMap<>();
        adjustments.put("tplApp.name", "myTestingTemplateApp");
        final String appVarName = "testTemplateApp";
        String res = mapper.instantiateTemplate("tplApp", appVarName, adjustments);
        Assert.assertNotNull(res);
        Assert.assertEquals(appVarName, res);

        stopEasy(lcd);
        setupIvmlFiles(); // revert changes
    }

    /**
     * Tests instantiating a template {@link AasIvmlMapper#getOpenTemplateVariables(String)}.
     * 
     * @throws IOException if copying/resetting files fails
     * @throws ExecutionException if the operation fails
     * @throws ModelQueryException if accessing IVML parts fails
     */
    @Test
    public void getOpenTemplateVariables() throws IOException, ExecutionException, ModelQueryException {
        InstantiationConfigurer configurer = new NonCleaningInstantiationConfigurer(MODEL_NAME, 
            ivmlFolder, FileUtils.getTempDirectory());
        ConfigurationLifecycleDescriptor lcd = startEasyValidate(configurer);
        AasIvmlMapper mapper = getInstance(false);

        String openVars = mapper.getOpenTemplateVariables("tplApp");
        Assert.assertNotNull(openVars);
        java.util.List<String> openVarsList = JsonUtils.listFromJson(openVars, String.class);
        Assert.assertNotNull(mapper);
        Assert.assertTrue("tplApp.name expected to be open", openVarsList.contains("tplApp.name"));

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
     * Composes an (expected) IVML file name.
     * 
     * @param file the file name, may contain paths
     * @return the file object
     */
    private File resolveIvmlFile(String file) {
        if (!file.endsWith(".ivml")) {
            file = file + ".ivml";
        }
        return new File(ivmlFolder, file);
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
            String contents = FileUtils.readFileToString(resolveIvmlFile(file), 
                Charset.defaultCharset());
            for (String e : expected) {
                boolean found = contents.contains(e);
                if (invert) {
                    found = !found;
                }
                Assert.assertTrue("Not found " + Arrays.toString(expected) + " in: " + contents, found);
            }
        } catch (IOException e) {
            Assert.fail("Cannot read " + file + ": " + e.getMessage());
        }
    }
    
    /**
     * Tests {@link AasIvmlMapper#getVariableName(String, String, String)}.
     */
    @Test
    public void testGetVariableName() {
        varNames.clear();
        InstantiationConfigurer configurer = new NonCleaningInstantiationConfigurer(MODEL_NAME, 
            ivmlFolder, FileUtils.getTempDirectory());
        ConfigurationLifecycleDescriptor lcd = startEasyValidate(configurer);
        
        AasIvmlMapper mapper = getInstance(false);
        assertVariableName(mapper.getVariableName("", "", ""));
        assertVariableName(mapper.getVariableName("String", "test my String", "1.01"));
        assertVariableName(mapper.getVariableName("String", "test my String", "1.01"));
        assertVariableName(mapper.getVariableName("String", "test my String", "1.01"));

        stopEasy(lcd);
        varNames.clear();
    }
    
    /**
     * Asserts the validity of a variable name, stores it internally and tries to figure out, whether repeated ones
     * were created.
     * 
     * @param variableName the variable name to assert
     */
    private static void assertVariableName(String variableName) {
        Assert.assertNotNull(variableName);
        Assert.assertTrue(variableName.length() > 0);
        boolean hasValidChars = true;
        for (int i = 0; i < variableName.length(); i++) {
            hasValidChars &= Character.isJavaIdentifierPart(variableName.charAt(i)); 
        }
        Assert.assertTrue(hasValidChars);
        Assert.assertTrue(Character.isJavaIdentifierStart(variableName.charAt(0)));
        Assert.assertFalse(varNames.contains(variableName));
        varNames.add(variableName);
    }

    /**
     * Tests {@link AasIvmlMapper#getUnusedProjects()}, {@link AasIvmlMapper#addImports(String)} and 
     * {@link AasIvmlMapper#removeImports(String)}.
     * 
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testImports() throws ExecutionException {
        varNames.clear();
        InstantiationConfigurer configurer = new NonCleaningInstantiationConfigurer(MODEL_NAME, 
            ivmlFolder, FileUtils.getTempDirectory());
        ConfigurationLifecycleDescriptor lcd = startEasyValidate(configurer);
        
        MyAasIvmlMapper mapper = getInstance(false);
        String result = mapper.getUnusedProjects();
        List<String> unused = Json.listFromJsonDflt(result, String.class);
        Assert.assertTrue(unused.size() > 0); // currently Vdma40001 and PhoenixContactEem
        mapper.assertChangesCount(0);
        
        mapper.addImports("[\"PhoenixContactEem\"]");
        mapper.assertChangesCount(c -> c > 0);
        mapper.clearChanges();
        
        mapper.removeImports("[\"PhoenixContactEem\"]");
        mapper.assertChangesCount(c -> c > 0);
        mapper.clearChanges();

        stopEasy(lcd);
        varNames.clear();
    }

}
