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
import java.util.Collections;
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
 * Tests {@link PersistenceRecipe}. This class only implements the basics and must be completed by the 
 * respective AAS abstraction implementation.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class PersistenceTest {

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
        
        aas = Collections.unmodifiableList(aas);
        PersistenceRecipe recipe = factory.createPersistenceRecipe();
        for (File f : filesToTest()) {
            System.out.println("Testing " + f);
            List<Aas> aasxList = selectedAas(f, aas);
            recipe.writeTo(aasxList, f);
            assertAas(recipe.readFrom(f), assertOnlyFirst(f), assertAsset(f));
            f.delete();
        }
    }

    /**
     * Returns the files to test.
     * 
     * @return the files to test
     */
    protected abstract File[] filesToTest();
    
    /**
     * Returns the AAS to test, as a selection of {@code aas}. This may be required if a certain persistence recipe
     * does not take multiple AAS.
     * 
     * @param file the file to write as an indicator of the current test process, one of the files 
     *     from {@link #filesToTest()}
     * @param aas the AAS to select from (unmodifiable)
     * @return the selected AAS
     */
    protected abstract List<Aas> selectedAas(File file, List<Aas> aas);

    /**
     * Returns whether only the first AAS shall be asserted. This may be required if {@link #selectedAas(File, List)}
     * selects AAS.
     * 
     * @param file the file to write as an indicator of the current test process, one of the files 
     *     from {@link #filesToTest()}
     * @return {@code true} for asserting only the first, {@code false} for asserting all
     */
    protected abstract boolean assertOnlyFirst(File file);

    /**
     * Returns whether the asset/properties shall be asserted.
     * 
     * @param file the file to write as an indicator of the current test process, one of the files 
     *     from {@link #filesToTest()}
     * @return {@code true} for asserting the asset/properties, {@code false} for ignoring the asset
     */
    protected abstract boolean assertAsset(File file);
    
    /**
     * Obtains the name of a file in the temporary directory and tries to ensure that it does not exist.
     * We go for file names so that debugging becomes possible (rather than for random names).
     * 
     * @param name the name of the file
     * @return the file
     */
    protected static File obtainTmpFile(String name) {
        File file = new File(FileUtils.getTempDirectory(), name );
        file.delete();
        return file;
    }
  
    /**
     * Asserts the expected contents in read AAS.
     * 
     * @param aasIn the input/read AAS
     * @param justFirst consider only the first AAS
     * @param testAsset consider the asset during testing
     */
    private static void assertAas(List<Aas> aasIn, boolean justFirst, boolean testAsset) {
        Assert.assertEquals(justFirst ? 1 : 2, aasIn.size());
        
        Aas aas = aasIn.get(0);
        Assert.assertEquals("MyAas", aas.getIdShort());
        Assert.assertEquals(2, aas.getSubmodelCount());
        Assert.assertNotNull(aas.getSubmodel("MySubModel"));
        Assert.assertNotNull(aas.getSubmodel("MySubModel").getProperty("MyP"));
        Assert.assertNotNull(aas.getSubmodel("MySubModel1"));
        if (testAsset) {
            Assert.assertNotNull(aas.getAsset());
            Assert.assertEquals("asset", aas.getAsset().getIdShort());
            Assert.assertEquals(AssetKind.INSTANCE, aas.getAsset().getAssetKind());
        }

        if (!justFirst) {
            Assert.assertEquals("MyAas1", aasIn.get(1).getIdShort());
        }
    }
    
}
