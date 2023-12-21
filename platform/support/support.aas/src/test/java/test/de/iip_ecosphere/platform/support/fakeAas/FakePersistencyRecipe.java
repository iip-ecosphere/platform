/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.fakeAas;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.iip_ecosphere.platform.support.FileFormat;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.PersistenceRecipe;

/**
 * A catch-all persistency receipe.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakePersistencyRecipe implements PersistenceRecipe {

    @Override
    public Collection<FileFormat> getSupportedFormats() {
        Collection<FileFormat> format = new ArrayList<>();
        format.add(new FileFormat("all", "all") {
            
            @Override
            public boolean matches(File file) {
                return true;
            }
        });
        return format;
    }

    @Override
    public void writeTo(List<Aas> aas, File thumbnail, List<FileResource> resources, File file) throws IOException {
    }

    @Override
    public List<Aas> readFrom(File file) throws IOException {
        return new ArrayList<Aas>();
    }

}
