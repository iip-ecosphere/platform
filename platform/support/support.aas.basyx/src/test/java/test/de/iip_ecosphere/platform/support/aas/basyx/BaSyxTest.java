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

package test.de.iip_ecosphere.platform.support.aas.basyx;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.DeploymentBuilder;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxDeploymentBuilder;
import de.iip_ecosphere.platform.support.aas.basyx.Invocables;

import org.junit.Assert;

/**
 * Tests the BaSyx abstraction with server and client on the "same machine".
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxTest {

    public static final String QNAME_VAR_LOTSIZE;
    public static final String QNAME_VAR_POWCONSUMPTION;
    public static final String QNAME_OP_STARTMACHINE;
    public static final String QNAME_OP_RECONFIGURE;
    public static final String QNAME_OP_STOPMACHINE;

    private static final String NAME_AAS = "aasTest";
    private static final String NAME_SUBMODEL = "machine";
    private static final String NAME_SUBMODELC_OUTER = "outer";
    private static final String NAME_VAR_SUBMODELC_OUTER_VAR = "outerVar";
    private static final String NAME_VAR_SUBMODELC_OUTER_REF = "outerRef";
    private static final String NAME_SUBMODELC_INNER = "inner";
    private static final String NAME_VAR_SUBMODELC_INNER_VAR = "innerVar";
    private static final String NAME_VAR_SUBMODELC_INNER_REF = "innerRef";
    private static final String NAME_VAR_LOTSIZE = "lotSize";
    private static final String NAME_VAR_POWCONSUMPTION = "powerConsumption";
    private static final String NAME_OP_STARTMACHINE = "startMachine";
    private static final String NAME_OP_RECONFIGURE = "setLotSize";
    private static final String NAME_OP_STOPMACHINE = "stopMachine";

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(BaSyxTest.class);
    private static final String HOST_AAS = "localhost";
    private static final int PORT_AAS = 4000;
    private static final int PORT_VAB = 4001;
    private static final String URN_AAS = "urn:::AAS:::testMachines#";
    private static final String REGISTRY_PATH = "registry";
    
    static {
        QNAME_VAR_LOTSIZE = NAME_SUBMODEL + "/" + NAME_VAR_LOTSIZE;
        QNAME_VAR_POWCONSUMPTION = NAME_SUBMODEL + "/" + NAME_VAR_POWCONSUMPTION;
        QNAME_OP_STARTMACHINE = NAME_SUBMODEL + "/" + NAME_OP_STARTMACHINE;
        QNAME_OP_RECONFIGURE = NAME_SUBMODEL + "/" + NAME_OP_RECONFIGURE;
        QNAME_OP_STOPMACHINE = NAME_SUBMODEL + "/" + NAME_OP_STOPMACHINE;
    }
    
    /**
     * Tests creating/reading an AAS.
     */
    @Test
    public void test() throws SocketException, UnknownHostException, ExecutionException, IOException {
        TestMachine machine = new TestMachine();
        Server ccServer = BaSyxDeploymentBuilder.createControlComponent(new TestControlComponent(machine), PORT_VAB);

        Aas aas = createAas(machine);
        
        DeploymentBuilder dBuilder = AasFactory.getInstance().createDeploymentBuilder(HOST_AAS, PORT_AAS);
        dBuilder.addInMemoryRegistry(REGISTRY_PATH);
        dBuilder.deploy(aas);
        Server httpServer = dBuilder.createServer(3000);

        ccServer.start();
        httpServer.start();
        
        queryAas(machine);
        httpServer.stop();
        ccServer.stop();
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
        SubmodelBuilder subModelBuilder = aasBuilder.createSubModelBuilder(NAME_SUBMODEL);
        subModelBuilder.createPropertyBuilder(NAME_VAR_LOTSIZE)
            .setType(Type.INTEGER)
            .bind(() -> {
                return machine.getLotSize(); 
            }, (param) -> {
                    machine.setLotSize((int) param); 
                })
            .build();
        subModelBuilder.createPropertyBuilder(NAME_VAR_POWCONSUMPTION)
            .setType(Type.DOUBLE)
            .bind(() -> {
                return machine.getPowerConsumption(); 
            }, null)
            .build();
        subModelBuilder.createOperationBuilder(NAME_OP_STARTMACHINE)
            .setInvocable(Invocables.createInvocable(TestControlComponent.OPMODE_STARTING, HOST_AAS, PORT_VAB))
            .build();
        subModelBuilder.createOperationBuilder(NAME_OP_RECONFIGURE)
            .addInputVariable()
            .setInvocable(Invocables.createInvocable(TestControlComponent.OPMODE_CONFIGURING, HOST_AAS, PORT_VAB))
            .build();
        subModelBuilder.createOperationBuilder(NAME_OP_STOPMACHINE)
            .setInvocable(Invocables.createInvocable(TestControlComponent.OPMODE_STOPPING, HOST_AAS, PORT_VAB))
            .build();
        Reference subModelBuilderRef = subModelBuilder.createReference();
        
        SubmodelElementCollectionBuilder smcBuilderOuter = subModelBuilder.createSubmodelElementCollectionBuilder(
            NAME_SUBMODELC_OUTER, false, true);
        SubmodelElementCollectionBuilder smcBuilderInner = smcBuilderOuter.createSubmodelElementCollectionBuilder(
            NAME_SUBMODELC_INNER, false, true);
        smcBuilderInner.createPropertyBuilder(NAME_VAR_SUBMODELC_INNER_VAR).setType(Type.INTEGER).build();
        smcBuilderInner.createReferenceElementBuilder(NAME_VAR_SUBMODELC_INNER_REF).setValue(subModelBuilderRef)
            .build();
        Reference smcBuilder1Ref = smcBuilderInner.createReference();
        smcBuilderInner.build();
        smcBuilderOuter.createPropertyBuilder(NAME_VAR_SUBMODELC_OUTER_VAR).setType(Type.STRING).build();
        smcBuilderOuter.createReferenceElementBuilder(NAME_VAR_SUBMODELC_OUTER_REF).setValue(smcBuilder1Ref).build();
        smcBuilderOuter.build();
        
        subModelBuilder.build();
        return aasBuilder.build();
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
        Aas aas = factory.retrieveAas(HOST_AAS, PORT_AAS, REGISTRY_PATH, URN_AAS);
        Assert.assertEquals(NAME_AAS, aas.getIdShort());
        Assert.assertEquals(1, aas.getSubmodelCount());
        Submodel subModel = aas.submodels().iterator().next();
        Assert.assertNotNull(subModel);
        Assert.assertEquals(2, subModel.getDataElementsCount());
        Property lotSize = subModel.getProperty(NAME_VAR_LOTSIZE);
        Assert.assertNotNull(lotSize);
        Assert.assertEquals(machine.getLotSize(), lotSize.getValue());
        Property powConsumption = subModel.getProperty(NAME_VAR_POWCONSUMPTION);
        Assert.assertNotNull(powConsumption);
        Assert.assertEquals(machine.getPowerConsumption(), powConsumption.getValue());

        Assert.assertEquals(3, subModel.getOperationsCount());
        Operation op = subModel.getOperation(NAME_OP_STARTMACHINE, 0, 0, 0);
        Assert.assertNotNull(op);
        op.invoke();
        Assert.assertEquals(machine.getLotSize(), lotSize.getValue());
        Assert.assertEquals(machine.getPowerConsumption(), powConsumption.getValue());
        op = subModel.getOperation(NAME_OP_RECONFIGURE, 1, 0, 0);
        Assert.assertNotNull(op);
        op.invoke(5);
        Assert.assertEquals(machine.getLotSize(), lotSize.getValue());
        Assert.assertEquals(machine.getPowerConsumption(), powConsumption.getValue());
        op = subModel.getOperation(NAME_OP_STOPMACHINE, 0, 0, 0);
        Assert.assertNotNull(op);
        op.invoke();
        Assert.assertEquals(machine.getLotSize(), lotSize.getValue());
        Assert.assertEquals(machine.getPowerConsumption(), powConsumption.getValue());
        
        SubmodelElement se = subModel.getSubmodelElement(NAME_SUBMODELC_OUTER);
        Assert.assertNotNull(se);
        Assert.assertTrue(se instanceof SubmodelElementCollection);
        SubmodelElementCollection secOuter = subModel.getSubmodelElementCollection(NAME_SUBMODELC_OUTER);
        Assert.assertNotNull(secOuter);
        Assert.assertTrue(se == secOuter);
        Assert.assertNotNull(secOuter.getProperty(NAME_VAR_SUBMODELC_OUTER_VAR));
        Assert.assertNotNull(secOuter.getReferenceElement(NAME_VAR_SUBMODELC_OUTER_REF));
        
        SubmodelElementCollection secInner = secOuter.getSubmodelElementCollection(NAME_SUBMODELC_INNER);
        Assert.assertNotNull(secInner);
        Assert.assertNotNull(secInner.getProperty(NAME_VAR_SUBMODELC_INNER_VAR));
        Assert.assertNotNull(secInner.getReferenceElement(NAME_VAR_SUBMODELC_INNER_REF));
    }
    
}
