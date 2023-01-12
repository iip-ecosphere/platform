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
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;
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
    private static final String NAME_VAR_SUBMODELC_INNER_INT = "innerInt";
    private static final String NAME_VAR_SUBMODELC_INNER_REF = "innerRef";
    private static final String NAME_VAR_LOTSIZE = "lotSize";
    private static final String NAME_VAR_VENDOR = "vendor";
    private static final String NAME_VAR_POWCONSUMPTION = "powerConsumption";
    private static final String NAME_VAR_DESCRIPTION1 = "description1";
    private static final String NAME_VAR_DESCRIPTION2 = "description2";
    private static final String NAME_OP_STARTMACHINE = "startMachine";
    private static final String NAME_OP_RECONFIGURE = "setLotSize";
    private static final String NAME_OP_STOPMACHINE = "stopMachine";

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
     * {@link #createAasOperationsElements(SubmodelElementContainerBuilder, String, int, String)}.
     * 
     * @param port the server communication port
     * @param machine the machine
     * @param protocol the VAB protocol as used in {@link AasFactory}
     * @param kstore the keystore descriptor, ignored if <b>null</b>
     * @return the protocol server
     */
    public static Server createOperationsServer(int port, TestMachine machine, String protocol, 
        KeyStoreDescriptor kstore) {
        AasFactory factory = AasFactory.getInstance();
        ProtocolServerBuilder builder = factory.createProtocolServerBuilder(protocol, port, kstore);
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
        builder.createPayloadCodec(); // there are specific tests for that, we ignore the result here..
        return builder.build();
    }

    /**
     * Creates the corresponding AAS elements for {@link #createOperationsServer(int, TestMachine, 
     * String, KeyStoreDescriptor)}.
     * 
     * @param subModelBuilder the sub model container builder to add the elements to
     * @param addr the server address (schema ignored)
     * @param protocol the VAB protocol as used in {@link AasFactory}
     */
    public void createAasOperationsElements(SubmodelElementContainerBuilder subModelBuilder, 
        ServerAddress addr, String protocol) {
        createAasOperationsElements(subModelBuilder, addr.getHost(), addr.getPort(), protocol);
    }

    /**
     * Creates the corresponding AAS elements for {@link #createOperationsServer(int, TestMachine, String, 
     * KeyStoreDescriptor)}.
     * 
     * @param subModelBuilder the sub model container builder to add the elements to
     * @param host the protocol host 
     * @param port the protocol port
     * @param protocol the VAB protocol as used in {@link AasFactory}
     */
    public void createAasOperationsElements(SubmodelElementContainerBuilder subModelBuilder, 
        String host, int port, String protocol) {
        AasFactory factory = AasFactory.getInstance();
        InvocablesCreator invC = factory.createInvocablesCreator(protocol, host, port, 
            getKeyStoreDescriptor(protocol));
        subModelBuilder.createPropertyBuilder(NAME_VAR_LOTSIZE)
            .setType(Type.INTEGER)
            .bind(invC.createGetter(NAME_VAR_LOTSIZE), invC.createSetter(NAME_VAR_LOTSIZE))
            .build();
        subModelBuilder.createPropertyBuilder(NAME_VAR_VENDOR)
            .setType(Type.STRING)
            .bindLazy(invC.createGetter(NAME_VAR_VENDOR), invC.createSetter(NAME_VAR_VENDOR))
            .build();
        subModelBuilder.createPropertyBuilder(NAME_VAR_POWCONSUMPTION)
            .setType(Type.DOUBLE)
            .setSemanticId("irdi:0173-1#02-AAV232#002") // id taken from BaSyX -> temperature ???
            .bind(invC.createGetter(NAME_VAR_POWCONSUMPTION), InvocablesCreator.READ_ONLY)
            .build();
        subModelBuilder.createOperationBuilder(NAME_OP_STARTMACHINE)
            .setInvocable(invC.createInvocable(NAME_OP_STARTMACHINE))
            .build(Type.NONE);
        subModelBuilder.createOperationBuilder(NAME_OP_RECONFIGURE)
            .addInputVariable(NAME_VAR_LOTSIZE, Type.INTEGER)
            .setInvocableLazy(invC.createInvocable(NAME_OP_RECONFIGURE))
            .build(Type.BOOLEAN);
        subModelBuilder.createOperationBuilder(NAME_OP_STOPMACHINE)
            .setInvocable(invC.createInvocable(NAME_OP_STOPMACHINE))
            .build(Type.NONE);
    }
    
    /**
     * Tests creating/reading an AAS over all protocols of a factory.
     * 
     * @throws SocketException shall not occur if the test works
     * @throws UnknownHostException shall not occur if the test works
     * @throws ExecutionException shall not occur if the test works
     * @throws IOException shall not occur if the test works
     */
    @Test
    public void testVabQuery() throws SocketException, UnknownHostException, ExecutionException, IOException {
        for (String proto : AasFactory.getInstance().getProtocols()) {
            if (!AasFactory.LOCAL_PROTOCOL.equals(proto) && !excludeProtocol(proto)) { // VAB only
                System.out.println("Testing VAB protocol: " + proto);
                testVabQuery(proto);
            }
        }
    }

    /**
     * To be overridden: descriptor for keystore per protocol.
     * 
     * @param protocol the protocol
     * @return the keystore, may be <b>null</b> for none
     */
    protected KeyStoreDescriptor getKeyStoreDescriptor(String protocol) {
        return null;
    }
    
    /**
     * To be overridden: Exclude the given protocol from testing. 
     * 
     * @param protocol the protocol
     * @return {@code true} for exclusion, {@code false} for inclusion
     */
    protected boolean excludeProtocol(String protocol) {
        return false;
    }

    /**
     * Tests creating/reading an AAS.
     *
     * @param protocol the VAB protocol as used in {@link AasFactory}
     * @throws SocketException shall not occur if the test works
     * @throws UnknownHostException shall not occur if the test works
     * @throws ExecutionException shall not occur if the test works
     * @throws IOException shall not occur if the test works
     */
    protected void testVabQuery(String protocol) throws SocketException, UnknownHostException, ExecutionException, 
        IOException {
        TestMachine machine = new TestMachine();
        Server ccServer = createOperationsServer(VAB_SERVER.getPort(), machine, protocol, 
            getKeyStoreDescriptor(protocol));
        ccServer.start(); // required here by basyx-0.1.0-SNAPSHOT
        ProtocolServerBuilder builder = AasFactory.getInstance().createProtocolServerBuilder(protocol, 
            VAB_SERVER.getPort(), getKeyStoreDescriptor(protocol));
        Assert.assertTrue(builder.isAvailable(VAB_SERVER.getHost(), 5000));

        Aas aas = createAas(machine, protocol);
        
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
     * @param protocol the VAB protocol as used in {@link AasFactory}
     * @return the created AAS
     * @throws SocketException if the port to be used for the AAS is occupied
     * @throws UnknownHostException shall not occur
     */
    private Aas createAas(TestMachine machine, String protocol) throws SocketException, UnknownHostException {
        AasFactory factory = AasFactory.getInstance();
        AasBuilder aasBuilder = factory.createAasBuilder(NAME_AAS, URN_AAS);
        SubmodelBuilder subModelBuilder = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, null);
        Assert.assertTrue(subModelBuilder.isNew());
        createAasOperationsElements(subModelBuilder, VAB_SERVER, protocol);
        Reference subModelBuilderRef = subModelBuilder.createReference();
        Assert.assertNotNull(aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, null)); // for modification
        subModelBuilder.createPropertyBuilder(NAME_VAR_DESCRIPTION1)
            .setValue(Type.LANG_STRING, LangString.create("test@de"))
            .build();
        subModelBuilder.createPropertyBuilder(NAME_VAR_DESCRIPTION2)
            .setValue(Type.LANG_STRING, "test2@en")
            .build();
        
        SubmodelElementCollectionBuilder smcBuilderOuter = subModelBuilder.createSubmodelElementCollectionBuilder(
            NAME_SUBMODELC_OUTER, false, true);
        SubmodelElementCollectionBuilder smcBuilderInner = smcBuilderOuter.createSubmodelElementCollectionBuilder(
            NAME_SUBMODELC_INNER, false, true);
        smcBuilderInner.createPropertyBuilder(NAME_VAR_SUBMODELC_INNER_VAR).setType(Type.AAS_INTEGER).build();
        smcBuilderInner.createPropertyBuilder(NAME_VAR_SUBMODELC_INNER_INT).setValue(Type.INTEGER, 1).build();
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
        Assert.assertNotNull(submodel.getIdentification());
        assertSize(3, submodel.operations());
        assertSize(0, submodel.dataElements());
        assertSize(5, submodel.properties());
        assertSize(9, submodel.submodelElements());
        Assert.assertNotNull(submodel.getOperation(NAME_OP_RECONFIGURE));
        Assert.assertEquals(9, submodel.getSubmodelElementsCount());
        Assert.assertNull(submodel.getReferenceElement("myRef"));
        Aas aas = aasBuilder.build();
        Assert.assertNotNull(aas.getIdentification());
        
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
     * Asserts lang string equality.
     * 
     * @param val the value of a property
     * @param str the (composed) lang string to test for
     */
    private static void assertLangString(Object val, String str) {
        Assert.assertNotNull(val);
        Assert.assertTrue(val instanceof LangString);
        LangString ls2 = LangString.create(str);
        Assert.assertEquals(ls2, val);
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
        Registry reg = factory.obtainRegistry(AAS_SERVER_REGISTRY);
        Aas aas = reg.retrieveAas(URN_AAS);
        Assert.assertEquals(NAME_AAS, aas.getIdShort());
        Assert.assertEquals(2, aas.getSubmodelCount());
        Submodel subm = aas.submodels().iterator().next();
        Assert.assertNotNull(subm);
        Assert.assertEquals(5, subm.getPropertiesCount());
        Property lotSize = subm.getProperty(NAME_VAR_LOTSIZE);
        Assert.assertNotNull(lotSize);
        Assert.assertEquals(machine.getLotSize(), lotSize.getValue());
        Property powConsumption = subm.getProperty(NAME_VAR_POWCONSUMPTION);
        Assert.assertNotNull(powConsumption);
        Assert.assertEquals(machine.getPowerConsumption(), powConsumption.getValue());

        assertLangString(subm.getProperty(NAME_VAR_DESCRIPTION1).getValue(), "test@de");
        assertLangString(subm.getProperty(NAME_VAR_DESCRIPTION2).getValue(), "test2@en");

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
        Assert.assertNotNull(secInner.getProperty(NAME_VAR_SUBMODELC_INNER_INT));
        Assert.assertEquals(1, secInner.getProperty(NAME_VAR_SUBMODELC_INNER_INT).getValue());
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
        
        Aas aas2 = reg.retrieveAas(reg.getEndpoint(aas));
        Assert.assertNotNull(aas2);
        Assert.assertEquals(aas2.getIdShort(), aas.getIdShort());
        Assert.assertNull(reg.retrieveAas("http://me.here.de/aas"));
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
