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
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.iip_aas.ClassUtility;
import de.iip_ecosphere.platform.support.iip_aas.Skip;

/**
 * Tests {@link ClassUtility}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ClassUtilityTest {

    private static class Simple {
        @SuppressWarnings("unused")
        private int value;
        
        @Skip
        private int secret;
        
    }
    
    private static class Base {
        @SuppressWarnings("unused")
        private String unknown;

        @SuppressWarnings("unused")
        private int[] values;
    }
    
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
        
        Submodel sm = aas.getSubModel(ClassUtility.getSubmodelName(Simple.class));
        Assert.assertNotNull(sm);
        Assert.assertNotNull(sm.getProperty("value"));
        Assert.assertEquals("int", sm.getProperty("value").getValue());
        Assert.assertNull(sm.getProperty("secret"));

        sm = aas.getSubModel(ClassUtility.getSubmodelName(Complex.class));
        Assert.assertNotNull(sm);
        Assert.assertNotNull(sm.getProperty("unknown"));
        Assert.assertEquals("String", sm.getProperty("unknown").getValue());
        Assert.assertNotNull(sm.getProperty("values"));
        Assert.assertEquals("int[]", sm.getProperty("values").getValue());
        Assert.assertNotNull(sm.getProperty("otherValue"));
        Assert.assertEquals("int", sm.getProperty("otherValue").getValue());
        Assert.assertNotNull(sm.getReferenceElement("simple"));
        Assert.assertNotNull(sm.getReferenceElement("simple").getValue());
    }

}
