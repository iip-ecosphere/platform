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

import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraph;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphEdge;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphNode;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * A factory for graph elements, e.g., used when parsing back graphs.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface GraphFactory {

    /**
     * Creates a graph instance.
     * 
     * @param var the underlying variable
     * @return the graph instance
     */
    public IvmlGraph createGraph(IDecisionVariable var);

    /**
     * Creates an edge.
     * 
     * @param var the underlying variable
     * @param start the start node
     * @param end the end node
     * @return the edge instance
     */
    public IvmlGraphEdge createEdge(IDecisionVariable var, IvmlGraphNode start, IvmlGraphNode end);

    /**
     * Creates a graph node.
     * 
     * @param var the underlying variable
     * @return the node instance
     */
    public IvmlGraphNode createNode(IDecisionVariable var);

}
