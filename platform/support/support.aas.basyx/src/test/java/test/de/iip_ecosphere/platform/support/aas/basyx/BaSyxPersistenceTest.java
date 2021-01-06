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
import java.util.ArrayList;
import java.util.List;

import de.iip_ecosphere.platform.support.aas.Aas;
import test.de.iip_ecosphere.platform.support.aas.PersistenceTest;

/**
 * Tests {@link BaSyxPersistenceRecipe}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxPersistenceTest extends PersistenceTest {
    
    private static final File XML = obtainTmpFile("myAAS.xml");
    private static final File AASX = obtainTmpFile("myAAS.aasx");
    private static final File JSON = obtainTmpFile("myAAS.json");
    
    @Override
    protected File[] filesToTest() {
        return new File[] {XML, AASX, JSON};
    }

    @Override
    protected List<Aas> selectedAas(File file, List<Aas> aas) {
        List<Aas> result = aas;
        if (file == AASX) {
            result = new ArrayList<Aas>();
            result.add(aas.get(0));
        }
        return result;
    }
    
    @Override
    protected boolean assertOnlyFirst(File file) {
        return file == AASX; // TODO BaSyx just considers the first AAS and ignores the remaining
    }

    @Override
    protected boolean assertAsset(File file) {
        return file == AASX; // TODO it seems that BaSyx does not read back the assets from AASX
    }

}
