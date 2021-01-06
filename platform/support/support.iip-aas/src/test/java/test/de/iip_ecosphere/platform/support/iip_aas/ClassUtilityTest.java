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

package test.de.iip_ecosphere.platform.support.iip_aas;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasPrintVisitor;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.iip_aas.ClassUtility;
import de.iip_ecosphere.platform.support.iip_aas.Skip;

/**
 * Tests {@link ClassUtility}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ClassUtilityTest {

    private static final String NAME_AAS = "test";
    private static final String URN_AAS = "urn:::AAS:::types#";
    private static final String NAME_TEST_SUBMODEL = "test";
    private static final String NAME_TEST_VAR_PRIMITIVE = "prim"; // TODO array
    private static final String NAME_TEST_VAR_SIMPLE = "input";
    private static final String NAME_TEST_VAR_COMPLEX = "input1";
    
    /**
     * A simple self-contained test class.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class Simple {
        @SuppressWarnings("unused")
        private int value;
        
        @Skip
        private int secret;
        
    }

    /**
     * A simple base test class.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class Base {
        @SuppressWarnings("unused")
        private String unknown;

        @SuppressWarnings("unused")
        private int[] values;
    }

    /**
     * A simple extending test class.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class Complex extends Base {
        @SuppressWarnings("unused")
        private int otherValue;
        
        @SuppressWarnings("unused")
        private Simple simple;
    }
    
    /**
     * Tests adding a type to an AAS at AAS creation time.
     * 
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testAddTypeToClass() throws ExecutionException {
        AasFactory factory = AasFactory.getInstance();
        AasBuilder aasBuilder = factory.createAasBuilder(NAME_AAS, URN_AAS);
        
        // and use the types 
        SubmodelBuilder smBuilder = aasBuilder.createSubmodelBuilder(NAME_TEST_SUBMODEL, null);
        populateModel(smBuilder);
        
        smBuilder.build();
        Aas aas = aasBuilder.build();
        aas.accept(new AasPrintVisitor());
        assertTypeSubmodel(aas);
    }

    /**
     * Populates the model. The types are created implicitly.
     * 
     * @param smBuilder the submodel builder to add the types to 
     */
    private void populateModel(SubmodelBuilder smBuilder) {
        ClassUtility.addTypeSubModelElement(smBuilder, NAME_TEST_VAR_PRIMITIVE, Integer.class);
        ClassUtility.addTypeSubModelElement(smBuilder, NAME_TEST_VAR_SIMPLE, Simple.class);
        ClassUtility.addTypeSubModelElement(smBuilder, NAME_TEST_VAR_COMPLEX, Complex.class);
    }

    /**
     * Asserts the type sub-model.
     * 
     * @param aas the AAS to assert the sub-model for
     * @throws ExecutionException shall not occur
     */
    private void assertTypeSubmodel(Aas aas) throws ExecutionException {
        Submodel smType = aas.getSubmodel(ClassUtility.NAME_TYPE_SUBMODEL);
        SubmodelElementCollection typeC = smType.getSubmodelElementCollection(ClassUtility.getName(Simple.class));
        Assert.assertNotNull(typeC);
        Assert.assertNotNull(typeC.getProperty(ClassUtility.ATTRIBUTE_PREFIX + "value"));
        Assert.assertEquals("int", typeC.getProperty(ClassUtility.ATTRIBUTE_PREFIX + "value").getValue());
        Assert.assertNull(typeC.getProperty(ClassUtility.ATTRIBUTE_PREFIX + "secret"));

        typeC = smType.getSubmodelElementCollection(ClassUtility.getName(Complex.class));
        Assert.assertNotNull(typeC);
        Assert.assertNotNull(typeC.getProperty(ClassUtility.ATTRIBUTE_PREFIX + "unknown"));
        Assert.assertEquals("String", typeC.getProperty(ClassUtility.ATTRIBUTE_PREFIX + "unknown").getValue());
        Assert.assertNotNull(typeC.getProperty(ClassUtility.ATTRIBUTE_PREFIX + "values"));
        Assert.assertEquals("int[]", typeC.getProperty(ClassUtility.ATTRIBUTE_PREFIX + "values").getValue());
        Assert.assertNotNull(typeC.getProperty(ClassUtility.ATTRIBUTE_PREFIX + "otherValue"));
        Assert.assertEquals("int", typeC.getProperty(ClassUtility.ATTRIBUTE_PREFIX + "otherValue").getValue());
        Assert.assertNotNull(typeC.getReferenceElement(ClassUtility.ATTRIBUTE_PREFIX + "simple"));
        Assert.assertNotNull(typeC.getReferenceElement(ClassUtility.ATTRIBUTE_PREFIX + "simple").getValue());
        
        Submodel smTest = aas.getSubmodel(NAME_TEST_SUBMODEL);
        Property primitive = smTest.getProperty(NAME_TEST_VAR_PRIMITIVE);
        Assert.assertNotNull(primitive);
        Assert.assertEquals("int", primitive.getValue());
        ReferenceElement ref = smTest.getReferenceElement(NAME_TEST_VAR_SIMPLE);
        Assert.assertNotNull(ref);
        Assert.assertNotNull(ref.getValue().hasReference());
        ref = smTest.getReferenceElement(NAME_TEST_VAR_COMPLEX);
        Assert.assertNotNull(ref);
        Assert.assertNotNull(ref.getValue().hasReference());
    }
    
    /**
     * Tests the {@link ClassUtility#getId(String, Object)} method.
     */
    @Test
    public void testGetId() {
        final String prefix = "prefix_";
        Object o = new Object();
        String id1 = ClassUtility.getId("", o);
        Assert.assertNotNull(id1);
        Assert.assertTrue(id1.length() > 0);
        String id2 = ClassUtility.getId(prefix, o);
        Assert.assertNotNull(id2);
        Assert.assertTrue(id2.length() > 0);
        Assert.assertTrue(id2.indexOf(id1) == prefix.length());
    }
    
    /**
     * Tests adding a type to a deployed AAS. We pre-deploy the sub-models to keep the scenario simple.
     * 
     * @throws IOException shall not occur
     * @throws ExecutionException shall not occur
     */
    @Test
    public void addTestTypeToClassWithDeployment() throws IOException, ExecutionException {
        AasFactory factory = AasFactory.getInstance();
        AasBuilder aasBuilder = factory.createAasBuilder(NAME_AAS, URN_AAS);
        aasBuilder.createSubmodelBuilder(ClassUtility.NAME_TYPE_SUBMODEL, null).build();
        aasBuilder.createSubmodelBuilder(NAME_TEST_SUBMODEL, null).build();
        
        // deploy the AAS
        ServerAddress serverAdr = new ServerAddress(Schema.HTTP);
        Endpoint regEp = new Endpoint(serverAdr, "registry");
        Server httpServer = AasFactory.getInstance()
            .createDeploymentRecipe(new Endpoint(serverAdr, ""))
            .addInMemoryRegistry(regEp.getEndpoint())
            .deploy(aasBuilder.build())
            .createServer()
            .start();
        
        
        // read back the AAS
        factory = AasFactory.getInstance();
        Aas aas = factory.obtainRegistry(regEp).retrieveAas(URN_AAS);
        SubmodelBuilder smBuilder = aas.createSubmodelBuilder(NAME_TEST_SUBMODEL, null); // regardless whether it exists
        populateModel(smBuilder);
        smBuilder.build();
        aas.accept(new AasPrintVisitor());
        assertTypeSubmodel(aas);

        // and assert again, e.g., different process
        factory = AasFactory.getInstance();
        aas = factory.obtainRegistry(regEp).retrieveAas(URN_AAS);
        aas.accept(new AasPrintVisitor());
        assertTypeSubmodel(aas);

        httpServer.stop(true);
    }

}
