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

import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphEdge;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphNode;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * Default graph edge implementation. {@link #getName()} is bound against the nested variable 
 * {@code name}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DefaultEdge extends DefaultGraphElement implements IvmlGraphEdge {

    private IvmlGraphNode start;
    private IvmlGraphNode end;

    /**
     * Creates an edge.
     * 
     * @param var the underlying variable
     * @param start the start node
     * @param end the end node
     */
    public DefaultEdge(IDecisionVariable var, IvmlGraphNode start, IvmlGraphNode end) {
        super(var);
        this.start = start;
        this.end = end;
    }

    @Override
    public IvmlGraphNode getStart() {
        return start;
    }

    @Override
    public IvmlGraphNode getEnd() {
        return end;
    }

}
