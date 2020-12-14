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

import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasPrintVisitor;
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
     * Tests adding a type to an AAS.
     * 
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testAddTypeToClass() throws ExecutionException {
        AasFactory factory = AasFactory.getInstance();
        AasBuilder aasBuilder = factory.createAasBuilder("test", "urn:::AAS:::types#");
        ClassUtility.addType(aasBuilder, Simple.class);
        ClassUtility.addType(aasBuilder, Complex.class);
        SubmodelBuilder smBuilder = aasBuilder.createSubModelBuilder("test");
        ClassUtility.addTypeSubModelElement(smBuilder, "input", Simple.class);
        smBuilder.build();
        Aas aas = aasBuilder.build();
        aas.accept(new AasPrintVisitor());
        
        Submodel smType = aas.getSubModel(ClassUtility.NAME_TYPE_SUBMODEL);
        SubmodelElementCollection typeC = smType.getSubmodelElementCollection(ClassUtility.getName(Simple.class));
        Assert.assertNotNull(typeC);
        Assert.assertNotNull(typeC.getProperty("value"));
        Assert.assertEquals("int", typeC.getProperty("value").getValue());
        Assert.assertNull(typeC.getProperty("secret"));

        typeC = smType.getSubmodelElementCollection(ClassUtility.getName(Complex.class));
        Assert.assertNotNull(typeC);
        Assert.assertNotNull(typeC.getProperty("unknown"));
        Assert.assertEquals("String", typeC.getProperty("unknown").getValue());
        Assert.assertNotNull(typeC.getProperty("values"));
        Assert.assertEquals("int[]", typeC.getProperty("values").getValue());
        Assert.assertNotNull(typeC.getProperty("otherValue"));
        Assert.assertEquals("int", typeC.getProperty("otherValue").getValue());
        Assert.assertNotNull(typeC.getReferenceElement("simple"));
        Assert.assertNotNull(typeC.getReferenceElement("simple").getValue());
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

}
