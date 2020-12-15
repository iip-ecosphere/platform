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

package de.iip_ecosphere.platform.support.aas;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A receipe to read/write AAS from/to files.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface PersistenceRecipe {

    /**
     * Writs the given AAS to {@code file}.
     * 
     * @param aas the AAS to write
     * @param file the file to write to
     * @throws IOException in case of I/O problems
     */
    public void writeTo(List<Aas> aas, File file) throws IOException;
    
    /**
     * Reads AAS from the given {@code file}.
     * 
     * @param file the file to read from
     * @return the read AAS
     * @throws IOException in case of I/O problems
     */
    public List<Aas> readFrom(File file) throws IOException;

}
