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
import de.iip_ecosphere.platform.support.aas.AssetKind;
import de.iip_ecosphere.platform.support.aas.PersistenceRecipe;
import de.iip_ecosphere.platform.support.aas.Type;

/**
 * Tests {@link BaSyxPersistenceRecipe}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PersistenceTest {

    /**
     * Tests writing/reading.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testWriteRead() throws IOException {
        AasFactory factory = AasFactory.getInstance();
        
        List<Aas> aas = new ArrayList<Aas>();

        // we create this here with assets as reading back AAS via BaSyx requires assets from 0.0.1 and asset 
        // references from 0.1.0
        AasBuilder aasB = factory.createAasBuilder("MyAas", "urn:::AAS:::myAAs#");
        aasB.createAssetBuilder("asset", "urn:::AAS:::myAAsasset#", AssetKind.INSTANCE).build();
        SubmodelBuilder smB = aasB.createSubmodelBuilder("MySubModel", "urn:::AAS:::myAAsMySubModel#");
        smB.createPropertyBuilder("MyP").setValue(Type.BOOLEAN, true).build();
        smB.build();
        aasB.createSubmodelBuilder("MySubModel1", null).build(); // no URN, custom here
        aas.add(aasB.build());
        
        aasB = factory.createAasBuilder("MyAas1", "urn:::AAS:::myAAs#1");
        aasB.createAssetBuilder("asset1", "urn:::AAS:::myAAs#1asset#1", AssetKind.INSTANCE);
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
        Assert.assertEquals(2, aasIn.get(0).getSubmodelCount());
        Assert.assertNotNull(aasIn.get(0).getSubmodel("MySubModel"));
        Assert.assertNotNull(aasIn.get(0).getSubmodel("MySubModel").getProperty("MyP"));
        Assert.assertNotNull(aasIn.get(0).getSubmodel("MySubModel1"));

        if (!justFirst) {
            Assert.assertEquals("MyAas1", aasIn.get(1).getIdShort());
        }
    }
    
}
