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

package de.iip_ecosphere.platform.support.aas.basyx;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.iip_ecosphere.platform.support.FileFormat;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.PersistenceRecipe;

/**
 * A persistence recipe for BaSyx AAS. This implementation is internally based on short ids. 
 * 
 * Limitations:
 * <ul>
 *   <li>This class does not consider concept descriptions or assets when reading directly from XML.</li>
 *   <li>Might be this is not enough for reading models uniquely back directly from XML.</li>
 *   <li>Reading of AASX supports only 1 AAS per AASX (BaSyx). Therefore, two classes from BaSyx taken over
 *       into IIP-Ecosphere code as they are not sufficiently usable/extensible.</li>
 *   <li>Translating AAS shortIds to file names is limited to whitespace replacement, i.e., no Umlauts etc. for 
 *       now.</li>
 *   <li>Written management XML elements in AAS may contain empty xmlns attributes.</li>
 * </ul>
 * 
 * @author Holger Eichelberger, SSE
 */
class BaSyxPersistenceRecipe implements PersistenceRecipe {
    
    private static final Map<FileFormat, PersistenceRecipe> RECIPES;
    
    static {
        Map<FileFormat, PersistenceRecipe> recipes = new HashMap<>();

        // ASSUMPTION - no double registration here
        PersistenceRecipe[] rcps = new PersistenceRecipe[] {
            new XmlPersistenceRecipe(),
            new AasxPersistenceRecipe()
        };
        
        for (PersistenceRecipe r : rcps) {
            for (FileFormat f : r.getSupportedFormats()) {
                recipes.put(f, r);
            }
        }
        RECIPES = Collections.unmodifiableMap(recipes);
    }

    /**
     * Finds a matching {@link PersistenceRecipe} against the known recipes and their file formats.
     * 
     * @param file the file to look for
     * @return the persistence recipe
     * @throws IllegalArgumentException if there is no matching file format/recipe
     */
    private PersistenceRecipe findMatching(File file) {
        PersistenceRecipe result = null;
        for (FileFormat ff : RECIPES.keySet()) {
            if (ff.matches(file)) {
                result = RECIPES.get(ff);
            }
        }
        if (null == result) {
            throw new IllegalArgumentException("Unrecognized file format for " + file);
        }
        return result;
    }
    
    @Override
    public void writeTo(List<Aas> aas, File file) throws IOException {
        findMatching(file).writeTo(aas, file);
    }

    @Override
    public List<Aas> readFrom(File file) throws IOException {
        return findMatching(file).readFrom(file);
    }

    @Override
    public Collection<FileFormat> getSupportedFormats() {
        return RECIPES.keySet(); // this shall be unmodifiable
    }

}
