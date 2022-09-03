/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration.ivml;

import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraph;

/**
 * Represents a graph format, e.g., a specific JSON structure that can be applied on UI level.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface GraphFormat {

    /**
     * Returns the name to address/access this format.
     * 
     * @return the unique name
     */
    public String getName();

    /**
     * Turns {@code graph} into this format.
     * 
     * @param graph the graph
     * @return the formatted graph
     * @throws ExecutionException if the translation fails
     */
    public String toString(IvmlGraph graph) throws ExecutionException;
  
    /**
     * Parses {@code graph} from this format into an IVML graph structure.
     * 
     * @param graph the graph
     * @return the IVML graph structure
     * @throws ExecutionException if the translation fails
     */
    public IvmlGraph fromString(String graph) throws ExecutionException;
    
}
