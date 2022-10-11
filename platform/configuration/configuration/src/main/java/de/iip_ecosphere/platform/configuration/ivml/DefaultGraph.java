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

import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraph;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphNode;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * Default graph edge implementation. {@link #getName()} is bound against the nested variable 
 * {@link #getNameVarName()}. Shall serve for a more generic mapping, to be part of EASY-Producer, thus, customizable.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DefaultGraph extends DefaultGraphElement implements IvmlGraph {

    private List<IvmlGraphNode> nodes = new LinkedList<>();
    
    /**
     * Creates a graph instance.
     * 
     * @param var the underlying variable
     */
    public DefaultGraph(IDecisionVariable var) {
        super(var);
    }
    
    @Override
    public Iterable<IvmlGraphNode> nodes() {
        return nodes;
    }

    @Override
    public void addNode(IvmlGraphNode node) {
        nodes.add(node);
    }

    @Override
    public int getNodeCount() {
        return nodes.size();
    }

}
