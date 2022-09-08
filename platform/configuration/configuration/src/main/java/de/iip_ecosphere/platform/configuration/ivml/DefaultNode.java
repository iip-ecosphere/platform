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

import java.util.LinkedList;
import java.util.List;

import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphEdge;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphNode;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * Default graph node implementation. {@link #getName()} is bound against the nested variable 
 * {@code name}. Positions/sizes are ignored by default.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DefaultNode extends DefaultGraphElement implements IvmlGraphNode {

    private List<IvmlGraphEdge> inEdges = new LinkedList<>();
    private List<IvmlGraphEdge> outEdges = new LinkedList<>();
    
    /**
     * Creates a graph node.
     * 
     * @param var the underlying variable
     */
    public DefaultNode(IDecisionVariable var) {
        super(var);
    }

    @Override
    public int getXPos() {
        return INVALID_POSITION;
    }

    @Override
    public int getYPos() {
        return INVALID_POSITION;
    }

    @Override
    public int getWidth() {
        return INVALID_SIZE;
    }

    @Override
    public int getHeight() {
        return INVALID_SIZE;
    }

    @Override
    public void setXPos(int xPos) {
    }

    @Override
    public void setYPos(int yPos) {
    }

    @Override
    public void setWidth(int width) {
    }

    @Override
    public void setHeight(int height) {
    }

    @Override
    public Iterable<IvmlGraphEdge> inEdges() {
        return inEdges;
    }

    @Override
    public Iterable<IvmlGraphEdge> outEdges() {
        return outEdges;
    }


    @Override
    public int getInEdgesCount() {
        return inEdges.size();
    }

    @Override
    public int getOutEdgesCount() {
        return outEdges.size();
    }

    /**
     * Adds an outgoing edge to this node.
     * 
     * @param edge the edge
     */
    public void addEdge(IvmlGraphEdge edge) {
        if (edge.getStart() == this) {
            outEdges.add(edge);
        } else if (edge.getEnd() == this) {
            inEdges.add(edge);
        }
    }

}
