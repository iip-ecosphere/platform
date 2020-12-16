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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.PersistenceRecipe;
import de.iip_ecosphere.platform.support.aas.Type;

/**
 * Tests {@link BaSyxPersistenceRecipe}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxPersistenceTest {

    /**
     * Tests writing/reading.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testWriteRead() throws IOException {
        AasFactory factory = AasFactory.getInstance();
        
        List<Aas> aas = new ArrayList<Aas>();
        
        AasBuilder aasB = factory.createAasBuilder("MyAas", "urn:::AAS:::myAAs#");
        SubmodelBuilder smB = aasB.createSubmodelBuilder("MySubModel");
        smB.createPropertyBuilder("MyP").setValue(Type.BOOLEAN, true).build();
        smB.build();
        aas.add(aasB.build());
        
        aasB = factory.createAasBuilder("MyAas1", "urn:::AAS:::myAAs#1");
        aas.add(aasB.build());
        
        File xml = new File(FileUtils.getTempDirectory(), "myAAS.xml");
        xml.delete();
        File aasx = new File(FileUtils.getTempDirectory(), "myAAS.aasx");
        aasx.delete();
        
        PersistenceRecipe recipe = factory.createPersistenceRecipe();
        // Basyx just considers the first AAS and ignores the remaining
        List<Aas> aasxList = new ArrayList<Aas>();
        aasxList.add(aas.get(0));
        recipe.writeTo(aasxList, aasx);
        recipe.writeTo(aas, xml);
        
        assertAas(recipe.readFrom(aasx), true);
        assertAas(recipe.readFrom(xml), false);

        xml.delete();
        aasx.delete();
    }
  
    /**
     * Asserts the expected contents in read AAS.
     * 
     * @param aasIn the input/read AAS
     * @param justFirst consider only the first AAS
     */
    private static void assertAas(List<Aas> aasIn, boolean justFirst) {
        Assert.assertEquals(justFirst ? 1 : 2, aasIn.size());
        
        Assert.assertEquals("MyAas", aasIn.get(0).getIdShort());
        Assert.assertEquals(1, aasIn.get(0).getSubmodelCount());
        Assert.assertNotNull(aasIn.get(0).getSubmodel("MySubModel"));
        Assert.assertNotNull(aasIn.get(0).getSubmodel("MySubModel").getProperty("MyP"));

        if (!justFirst) {
            Assert.assertEquals("MyAas1", aasIn.get(1).getIdShort());
        }
    }
    
}
