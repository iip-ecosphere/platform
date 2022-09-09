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
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.configuration.ConfigurationAas;
import de.iip_ecosphere.platform.configuration.ConfigurationLifecycleDescriptor;
import de.iip_ecosphere.platform.configuration.ConfigurationManager;
import de.iip_ecosphere.platform.configuration.ConfigurationSetup;
import de.iip_ecosphere.platform.configuration.DrawflowGraphFormat;
import de.iip_ecosphere.platform.configuration.PlatformInstantiator.InstantiationConfigurer;
import de.iip_ecosphere.platform.configuration.ivml.AasIvmlMapper;
import de.iip_ecosphere.platform.configuration.ivml.GraphFormat;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraph;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphEdge;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphNode;
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

/**
 * Tests {@link AasIvmlMapper}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasIvmlMapperTest {

    /**
     * Tests {@link AasIvmlMapper}.
     * 
     * @throws ExecutionException shall not occur in a successful test
     */
    @Test
    public void testAasIvmlMapper() throws ExecutionException {
        InstantiationConfigurer configurer = new InstantiationConfigurer("SimpleMesh", 
            new File("src/test/easy"), FileUtils.getTempDirectory());
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
        ConfigurationSetup setup = ConfigurationSetup.getSetup();
        configurer.configure(setup);
        ConfigurationLifecycleDescriptor lcd = configurer.obtainLifecycleDescriptor();
        lcd.startup(new String[0]); // shall register executor
        ReasoningResult rRes = ConfigurationManager.validateAndPropagate();
        Assert.assertNotNull("No model loaded", rRes);
        Configuration cfg = ConfigurationManager.getVilConfiguration();
        Assert.assertNotNull("No configuration available", cfg);
        AasIvmlMapper mapper = new AasIvmlMapper(() -> cfg, new ConfigurationAas.IipGraphMapper());
        mapper.addGraphFormat(format);        
        
        AasFactory aasFactory = AasFactory.getInstance();
        SubmodelBuilder smb = aasFactory.createSubmodelBuilder(ConfigurationAas.NAME_SUBMODEL, null);
        InvocablesCreator iCreator = aasFactory.createInvocablesCreator(AasFactory.LOCAL_PROTOCOL, "localhost", 0);
        ProtocolServerBuilder psb = aasFactory.createProtocolServerBuilder(AasFactory.LOCAL_PROTOCOL, 0);
        mapper.mapByType(smb, iCreator);
        mapper.bindOperations(psb);
        String res = assertSubmodel(smb.build(), appName, netIndex, servicesAsserter);
        psb.build();
        lcd.shutdown();
        setup.getEasyProducer().reset();
        return format.fromString(res, mapper.getGraphFactory(), mapper);
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

}
