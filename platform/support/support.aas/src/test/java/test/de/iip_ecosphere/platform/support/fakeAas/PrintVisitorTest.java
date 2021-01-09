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

package test.de.iip_ecosphere.platform.support.fakeAas;

import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasPrintVisitor;
import de.iip_ecosphere.platform.support.aas.AssetKind;
import de.iip_ecosphere.platform.support.aas.BasicAasVisitor;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;

/**
 * Testing the print visitor against the fake AAS implementation.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PrintVisitorTest {
    
    /**
     * Tests the print visitor.
     */
    @Test
    public void testVisitor() {
        AasFactory factory = AasFactory.getInstance();
        AasBuilder aasB = factory.createAasBuilder("aas", "");
        aasB.createAssetBuilder("asset", null, AssetKind.INSTANCE).build();
        SubmodelBuilder smB =  aasB.createSubmodelBuilder("sub", "");
        smB.createPropertyBuilder("prop").build();
        smB.createOperationBuilder("op").build();
        SubmodelElementCollectionBuilder nB = smB.createSubmodelElementCollectionBuilder("nested", false, false);
        nB.createPropertyBuilder("nested").build();
        nB.createReferenceElementBuilder("parent").setValue(smB.createReference()).build();
        SubmodelElementCollectionBuilder nB2 = nB.createSubmodelElementCollectionBuilder("nested2", false, true);
        nB2.createPropertyBuilder("nested2").build();
        nB2.build();
        nB.build();
        Submodel mySubmodel = smB.build();
        Aas myAas = aasB.build();
        
        AasPrintVisitor vis = new AasPrintVisitor();
        myAas.accept(vis);
        System.out.println("------------");
        mySubmodel.accept(vis);
        // no asserts here... if you want to test the output
        
        myAas.accept(new BasicAasVisitor());
    }

}
