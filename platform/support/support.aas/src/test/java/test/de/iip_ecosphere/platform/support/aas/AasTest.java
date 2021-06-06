/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.aas;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasPrintVisitor;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.Type;

import org.junit.Assert;

/**
 * Tests the AAS abstraction with server and client on the "same machine".
 * 
 * @author Monika Staciwa, SSE
 * @author Holger Eichelberger, SSE
 */
public class AasTest {
    
    public static final String QNAME_VAR_LOTSIZE;
    public static final String QNAME_VAR_VENDOR;
    public static final String QNAME_VAR_POWCONSUMPTION;
    public static final String QNAME_OP_STARTMACHINE;
    public static final String QNAME_OP_RECONFIGURE;
    public static final String QNAME_OP_STOPMACHINE;

    public static final String NAME_AAS = "aasTest";
    public static final String NAME_SUBMODEL = "machine";
    private static final String NAME_SUBMODELC_OUTER = "outer";
    private static final String NAME_VAR_SUBMODELC_OUTER_VAR = "outerVar";
    private static final String NAME_VAR_SUBMODELC_OUTER_REF = "outerRef";
    private static final String NAME_SUBMODELC_INNER = "inner";
    private static final String NAME_VAR_SUBMODELC_INNER_VAR = "innerVar";
    private static final String NAME_VAR_SUBMODELC_INNER_REF = "innerRef";
    private static final String NAME_VAR_LOTSIZE = "lotSize";
    private static final String NAME_VAR_VENDOR = "vendor";
    private static final String NAME_VAR_POWCONSUMPTION = "powerConsumption";
    private static final String NAME_OP_STARTMACHINE = "startMachine";
    private static final String NAME_OP_RECONFIGURE = "setLotSize";
    private static final String NAME_OP_STOPMACHINE = "stopMachine";

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(AasTest.class);
    private static final ServerAddress AAS_SERVER = new ServerAddress(Schema.HTTP); // localhost, ephemeral
    private static final Endpoint AAS_SERVER_BASE = new Endpoint(AAS_SERVER, "");
    private static final Endpoint AAS_SERVER_REGISTRY = new Endpoint(AAS_SERVER, "registry");
    private static final ServerAddress VAB_SERVER = new ServerAddress(Schema.HTTP); // localhost, ephemeral
    private static final String URN_AAS = "urn:::AAS:::testMachines#";
    
    static {
        QNAME_VAR_LOTSIZE = NAME_SUBMODEL + "/" + NAME_VAR_LOTSIZE;
        QNAME_VAR_VENDOR = NAME_SUBMODEL + "/" + NAME_VAR_VENDOR;
        QNAME_VAR_POWCONSUMPTION = NAME_SUBMODEL + "/" + NAME_VAR_POWCONSUMPTION;
        QNAME_OP_STARTMACHINE = NAME_SUBMODEL + "/" + NAME_OP_STARTMACHINE;
        QNAME_OP_RECONFIGURE = NAME_SUBMODEL + "/" + NAME_OP_RECONFIGURE;
        QNAME_OP_STOPMACHINE = NAME_SUBMODEL + "/" + NAME_OP_STOPMACHINE;
    }
    
    /**
     * Creates the operations server for the given machine instance and for the operations in 
     * {@link #createAasOperationsElements(SubmodelElementContainerBuilder, String, int)}..
     * 
     * @param port the server communication port
     * @param machine the machine
     * @return the protocol server
     */
    public static Server createOperationsServer(int port, TestMachine machine) {
        AasFactory factory = AasFactory.getInstance();
        ProtocolServerBuilder builder = factory.createProtocolServerBuilder(AasFactory.DEFAULT_PROTOCOL, port);
        builder.defineProperty(NAME_VAR_LOTSIZE, () -> {
            return machine.getLotSize(); 
        }, (param) -> {
                machine.setLotSize((int) param); 
            });
        builder.defineProperty(NAME_VAR_VENDOR, () -> {
            return machine.getVendor(); 
        }, (param) -> { // whether meaningful or not
                machine.setVendor((String) param); 
            });
        builder.defineProperty(NAME_VAR_POWCONSUMPTION, () -> {
            return machine.getPowerConsumption(); 
        }, null);
        builder.defineOperation(NAME_OP_STARTMACHINE, (params) -> {
            machine.start();
            return null;
        });
        builder.defineOperation(NAME_OP_RECONFIGURE, (params) 
            -> machine.reconfigure((int) params[0]));
        builder.defineOperation(NAME_OP_STOPMACHINE, (params) -> {
            machine.stop();
            return null;
        });
        Assert.assertNull(builder.createPayloadCodec());
        return builder.build();
    }

    /**
     * Creates the corresponding AAS elements for {@link #createOperationsServer(int, TestMachine)}.
     * 
     * @param subModelBuilder the sub model container builder to add the elements to
     * @param addr the server address (schema ignored)
     */
    public static void createAasOperationsElements(SubmodelElementContainerBuilder subModelBuilder, 
        ServerAddress addr) {
        createAasOperationsElements(subModelBuilder, addr.getHost(), addr.getPort());
    }

    /**
     * Creates the corresponding AAS elements for {@link #createOperationsServer(int, TestMachine)}.
     * 
     * @param subModelBuilder the sub model container builder to add the elements to
     * @param host the protocol host 
     * @param port the protocol port
     */
    public static void createAasOperationsElements(SubmodelElementContainerBuilder subModelBuilder, 
        String host, int port) {
        AasFactory factory = AasFactory.getInstance();
        InvocablesCreator invC = factory.createInvocablesCreator(AasFactory.DEFAULT_PROTOCOL, host, port);
        subModelBuilder.createPropertyBuilder(NAME_VAR_LOTSIZE)
            .setType(Type.INTEGER)
            .bind(invC.createGetter(NAME_VAR_LOTSIZE), invC.createSetter(NAME_VAR_LOTSIZE))
            .build();
        subModelBuilder.createPropertyBuilder(NAME_VAR_VENDOR)
            .setType(Type.STRING)
            .bind(invC.createGetter(NAME_VAR_VENDOR), invC.createSetter(NAME_VAR_VENDOR))
            .build();
        subModelBuilder.createPropertyBuilder(NAME_VAR_POWCONSUMPTION)
            .setType(Type.DOUBLE)
            .setSemanticId("irdi:0173-1#02-AAV232#002") // id taken from BaSyX -> temperature ???
            .bind(invC.createGetter(NAME_VAR_POWCONSUMPTION), InvocablesCreator.READ_ONLY)
            .build();
        subModelBuilder.createOperationBuilder(NAME_OP_STARTMACHINE)
            .setInvocable(invC.createInvocable(NAME_OP_STARTMACHINE))
            .build();
        subModelBuilder.createOperationBuilder(NAME_OP_RECONFIGURE)
            .addInputVariable(NAME_VAR_LOTSIZE, Type.INTEGER)
            .setInvocable(invC.createInvocable(NAME_OP_RECONFIGURE))
            .build();
        subModelBuilder.createOperationBuilder(NAME_OP_STOPMACHINE)
            .setInvocable(invC.createInvocable(NAME_OP_STOPMACHINE))
            .build();
    }
    
    /**
     * Tests creating/reading an AAS.
     * 
     * @throws SocketException shall not occur if the test works
     * @throws UnknownHostException shall not occur if the test works
     * @throws ExecutionException shall not occur if the test works
     * @throws IOException shall not occur if the test works
     */
    @Test
    public void testVabQuery() throws SocketException, UnknownHostException, ExecutionException, IOException {
        TestMachine machine = new TestMachine();

        Server ccServer = createOperationsServer(VAB_SERVER.getPort(), machine);
        ccServer.start(); // required here by basyx-0.1.0-SNAPSHOT
        Aas aas = createAas(machine);
        
        Server httpServer = AasFactory.getInstance()
            .createDeploymentRecipe(AAS_SERVER_BASE)
            .addInMemoryRegistry(AAS_SERVER_REGISTRY.getEndpoint())
            .deploy(aas)
            .createServer()
            .start();
        
        queryAas(machine);
        httpServer.stop(true);
        ccServer.stop(true);
    }
    
    /**
     * This method creates a test Asset Administration Shell.
     * 
     * @param machine the test machine instance
     * @return the created AAS
     * @throws SocketException if the port to be used for the AAS is occupied
     * @throws UnknownHostException shall not occur
     */
    private static Aas createAas(TestMachine machine) throws SocketException, UnknownHostException {
        AasFactory factory = AasFactory.getInstance();
        AasBuilder aasBuilder = factory.createAasBuilder(NAME_AAS, URN_AAS);
        SubmodelBuilder subModelBuilder = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, null);
        Assert.assertTrue(subModelBuilder.isNew());
        createAasOperationsElements(subModelBuilder, VAB_SERVER);
        Reference subModelBuilderRef = subModelBuilder.createReference();
        Assert.assertNotNull(aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, null)); // for modification
        
        SubmodelElementCollectionBuilder smcBuilderOuter = subModelBuilder.createSubmodelElementCollectionBuilder(
            NAME_SUBMODELC_OUTER, false, true);
        SubmodelElementCollectionBuilder smcBuilderInner = smcBuilderOuter.createSubmodelElementCollectionBuilder(
            NAME_SUBMODELC_INNER, false, true);
        smcBuilderInner.createPropertyBuilder(NAME_VAR_SUBMODELC_INNER_VAR).setType(Type.INTEGER).build();
        ReferenceElement re = smcBuilderInner.createReferenceElementBuilder(NAME_VAR_SUBMODELC_INNER_REF)
            .setValue(subModelBuilderRef).build();
        Assert.assertNotNull(re.getValue());
        Assert.assertTrue(re.getValue().hasReference());
        
        Reference smcBuilder1Ref = smcBuilderInner.createReference();
        smcBuilderInner.build();
        smcBuilderOuter.createPropertyBuilder(NAME_VAR_SUBMODELC_OUTER_VAR).setType(Type.STRING).build();
        smcBuilderOuter.createReferenceElementBuilder(NAME_VAR_SUBMODELC_OUTER_REF).setValue(smcBuilder1Ref).build();
        SubmodelElementCollection smcOuter = smcBuilderOuter.build();
        assertSize(3, smcOuter.elements());
        Assert.assertEquals(3, smcOuter.getElementsCount());
        Assert.assertNotNull(smcOuter.getDataElement(NAME_VAR_SUBMODELC_OUTER_VAR));
        Assert.assertNotNull(smcOuter.getElement(NAME_SUBMODELC_INNER));
        Assert.assertNotNull(subModelBuilder.createSubmodelElementCollectionBuilder(
            NAME_SUBMODELC_OUTER, false, true)); // for modification
        
        Submodel submodel = subModelBuilder.build();
        assertSize(3, submodel.operations());
        assertSize(0, submodel.dataElements());
        assertSize(3, submodel.properties());
        assertSize(7, submodel.submodelElements());
        Assert.assertNotNull(submodel.getOperation(NAME_OP_RECONFIGURE));
        Assert.assertEquals(7, submodel.getSubmodelElementsCount());
        Assert.assertNull(submodel.getReferenceElement("myRef"));
        Aas aas = aasBuilder.build();
        
        // adding on local models
        Submodel subAdd = aas.createSubmodelBuilder("sub_add", null).build();
        Assert.assertNotNull(aas.getSubmodel("sub_add"));
        subAdd.createSubmodelElementCollectionBuilder("sub_coll", true, true).build();
        Assert.assertNotNull(aas.getSubmodel("sub_add").getSubmodelElementCollection("sub_coll"));
        submodel.createSubmodelElementCollectionBuilder("sub_coll2", false, false).build();
        Assert.assertNotNull(submodel.getSubmodelElementCollection("sub_coll2"));

        aas.accept(new AasPrintVisitor()); // assert the accepts

        return aas;
    }
    
    /**
     * Queries the created AAS.
     * 
     * @param machine the test machine as reference
     * @throws ExecutionException if operation invocations fail
     * @throws IOException if retrieving the AAS fails
     */
    private static void queryAas(TestMachine machine) throws ExecutionException, IOException {
        AasFactory factory = AasFactory.getInstance();
        Aas aas = factory.obtainRegistry(AAS_SERVER_REGISTRY).retrieveAas(URN_AAS);
        Assert.assertEquals(NAME_AAS, aas.getIdShort());
        Assert.assertEquals(2, aas.getSubmodelCount());
        Submodel subm = aas.submodels().iterator().next();
        Assert.assertNotNull(subm);
        Assert.assertEquals(3, subm.getPropertiesCount());
        Property lotSize = subm.getProperty(NAME_VAR_LOTSIZE);
        Assert.assertNotNull(lotSize);
        Assert.assertEquals(machine.getLotSize(), lotSize.getValue());
        Property powConsumption = subm.getProperty(NAME_VAR_POWCONSUMPTION);
        Assert.assertNotNull(powConsumption);
        Assert.assertEquals(machine.getPowerConsumption(), powConsumption.getValue());

        Assert.assertEquals(3, subm.getOperationsCount());
        Operation op = subm.getOperation(NAME_OP_STARTMACHINE);
        Assert.assertNotNull(op);
        op.invoke();
        Assert.assertEquals(machine.getLotSize(), lotSize.getValue());
        Assert.assertEquals(machine.getPowerConsumption(), powConsumption.getValue());
        op = subm.getOperation(NAME_OP_RECONFIGURE);
        Assert.assertNotNull(op);
        op.invoke(5);
        Assert.assertEquals(machine.getLotSize(), lotSize.getValue());
        Assert.assertEquals(machine.getPowerConsumption(), powConsumption.getValue());
        op = subm.getOperation(NAME_OP_STOPMACHINE);
        Assert.assertNotNull(op);
        op.invoke();
        Assert.assertEquals(machine.getLotSize(), lotSize.getValue());
        Assert.assertEquals(machine.getPowerConsumption(), powConsumption.getValue());
        
        SubmodelElement se = subm.getSubmodelElement(NAME_SUBMODELC_OUTER);
        Assert.assertNotNull(se);
        Assert.assertTrue(se instanceof SubmodelElementCollection);
        SubmodelElementCollection secOuter = subm.getSubmodelElementCollection(NAME_SUBMODELC_OUTER);
        Assert.assertNotNull(secOuter);
        Assert.assertTrue(se == secOuter);
        Assert.assertNotNull(secOuter.getProperty(NAME_VAR_SUBMODELC_OUTER_VAR));
        Assert.assertNotNull(secOuter.getReferenceElement(NAME_VAR_SUBMODELC_OUTER_REF));
        
        SubmodelElementCollection secInner = secOuter.getSubmodelElementCollection(NAME_SUBMODELC_INNER);
        Assert.assertNotNull(secInner);
        Assert.assertNotNull(secInner.getProperty(NAME_VAR_SUBMODELC_INNER_VAR));
        Assert.assertNotNull(secInner.getReferenceElement(NAME_VAR_SUBMODELC_INNER_REF));

        // the lately added sub-models/elements
        Assert.assertNotNull(aas.getSubmodel("sub_add"));
        Assert.assertNotNull(aas.getSubmodel("sub_add").getSubmodelElementCollection("sub_coll"));
        Assert.assertNotNull(subm.getSubmodelElementCollection("sub_coll2"));

        // adding on connected models
        Submodel subAdd = aas.createSubmodelBuilder("conn_add", null).build();
        Assert.assertNotNull(aas.getSubmodel("conn_add"));
        subAdd.createSubmodelElementCollectionBuilder("conn_coll", true, true).build();
        Assert.assertNotNull(aas.getSubmodel("conn_add").getSubmodelElementCollection("conn_coll"));
        subm.createSubmodelElementCollectionBuilder("conn_coll2", false, false).build();
        Assert.assertNotNull(subm.getSubmodelElementCollection("conn_coll2"));
        SubmodelElementCollectionBuilder cc3 = subm.createSubmodelElementCollectionBuilder("conn_coll3", false, false);
        cc3.createSubmodelElementCollectionBuilder("cc3_1", false, false).build();
        cc3.build();
        Assert.assertNotNull(subm.getSubmodelElementCollection("conn_coll3"));
        Assert.assertNotNull(subm.getSubmodelElementCollection("conn_coll3").getSubmodelElementCollection("cc3_1"));
        
        subm.getSubmodelElementCollection("conn_coll3").deleteElement("cc3_1");
        Assert.assertNull(subm.getSubmodelElementCollection("conn_coll3").getSubmodelElementCollection("cc3_1"));
        
        aas.accept(new AasPrintVisitor()); // assert the accepts
    }

    /**
     * Asserts an iterator contents (just) by counting the number of elements.
     * 
     * @param <T> the element type
     * @param expectedSize the expected size
     * @param iter the iterator to assert
     */
    private static <T> void assertSize(int expectedSize, Iterable<T> iter) {
        Assert.assertEquals(expectedSize, CollectionUtils.toList(iter.iterator()).size());
    }
    
    
    /**
     * Tests the factory.
     */
    @Test
    public void testFactory() {
        Assert.assertTrue(AasFactory.getInstance().getName().length() > 0);
    }

    /**
     * Tests for illegal short ids. Seems to be valid for all AAs.
     */
    @Test
    public void testIllegalShortId() {
        AasFactory factory = AasFactory.getInstance();
        AasBuilder aasBuilder = factory.createAasBuilder(NAME_AAS, URN_AAS);
        SubmodelBuilder subModelBuilder = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, null);
        try {
            subModelBuilder.createPropertyBuilder("value").setValue(1).build();
            Assert.fail("No exception due to illegal name");
        } catch (IllegalArgumentException e) {
        }
        try {
            subModelBuilder.createPropertyBuilder("1234").setValue(1).build();
            Assert.fail("No exception due to illegal name");
        } catch (IllegalArgumentException e) {
        }
        try {
            subModelBuilder.createPropertyBuilder("java.lang.String").setValue(1).build();
            Assert.fail("No exception due to illegal name");
        } catch (IllegalArgumentException e) {
        }
    }
    
    /**
     * Tests {@link AasFactory#fixId(String)} for BaSyX.
     */
    @Test
    public void testFixId() {
        AasFactory instance = AasFactory.getInstance();
        Assert.assertNull(instance.fixId(null));
        Assert.assertEquals("", instance.fixId(""));
        Assert.assertEquals("id", instance.fixId("id"));
        Assert.assertEquals("a1id", instance.fixId("1id"));
        Assert.assertEquals("a_id", instance.fixId("a_id"));
        Assert.assertEquals("a_id", instance.fixId("a id"));
        Assert.assertEquals("test_log", instance.fixId("test-log"));

        Assert.assertEquals("de_uni_hildesheim_sse_Test_TEst", instance.fixId("de.uni-hildesheim.sse.Test$TEst"));
        Assert.assertEquals("a1de_uni_hildesheim_sse_Test", instance.fixId("1de.uni-hildesheim.sse.Test"));
        Assert.assertEquals("a1de_uni_hildesheim_sse_Test_TEst", instance.fixId("1de.uni-hildesheim.sse.Test$TEst"));
        Assert.assertEquals("jenkins_2_localhost", instance.fixId("jenkins-2@localhost"));
    }

}
