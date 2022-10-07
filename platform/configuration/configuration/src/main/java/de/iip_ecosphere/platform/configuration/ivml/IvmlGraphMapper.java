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

import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * Maps a decision variable into a graph structure that can be processed further.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface IvmlGraphMapper {

    /**
     * Basic interface for all graph elements.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface IvmlGraphElement {

        /**
         * Returns the name of the element.
         * 
         * @return the name
         */
        public String getName();

        /**
         * Changes the name of the element.
         * 
         * @param name the new name
         */
        public void setName(String name);
        
        /**
         * Returns the underlying IVML variable.
         * 
         * @return the variable
         */
        public IDecisionVariable getVariable();
        
    }

    /**
     * Represents an IVML graph.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface IvmlGraph extends IvmlGraphElement {
        
        /**
         * Adds a node to this graph.
         * 
         * @param node the node
         */
        public void addNode(IvmlGraphNode node);
        
        /**
         * Returns the nodes in the graph.
         * 
         * @return the nodes
         */
        public Iterable<? extends IvmlGraphNode> nodes();
        
        /**
         * Returns the number of nodes of this graph.
         * 
         * @return the number of nodes
         */
        public int getNodeCount();
        
    }
    
    /**
     * Represents a graph node.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface IvmlGraphNode extends IvmlGraphElement {
        
        /**
         * Returns the left position of the node. May be ignored by the graph reader/writer.
         * 
         * @return the left position, invalid if negative
         */
        public int getXPos();

        /**
         * Returns the top position of the node. May be ignored by the graph reader/writer.
         * 
         * @return the top position, invalid if negative
         */
        public int getYPos();
        
        /**
         * Returns the width of the node. May be ignored by the graph reader/writer.
         * 
         * @return the width, invalid if negative
         */
        public int getWidth();

        /**
         * Returns the height of the node. May be ignored by the graph reader/writer.
         * 
         * @return the height, invalid if negative
         */
        public int getHeight();
        
        /**
         * Changes the left position of the node. May be ignored by the graph reader/writer.
         * 
         * @param xPos the left position, invalid if negative
         */
        public void setXPos(int xPos);

        /**
         * Changes the top position of the node. May be ignored by the graph reader/writer.
         * 
         * @param yPos the left position, invalid if negative
         */
        public void setYPos(int yPos);

        /**
         * Changes the width of the node. May be ignored by the graph reader/writer.
         * 
         * @param width the width of the node, invalid if negative
         */
        public void setWidth(int width);
        
        /**
         * Changes the height of the node. May be ignored by the graph reader/writer.
         * 
         * @param height the height of the node, invalid if negative
         */
        public void setHeight(int height);

        /**
         * Adds an incoming/outgoing edge to this node.
         * 
         * @param edge the edge
         */
        public void addEdge(IvmlGraphEdge edge);
        
        /**
         * Returns the edges ending at this node.
         * 
         * @return the edges
         */
        public Iterable<? extends IvmlGraphEdge> inEdges();
        
        /**
         * Returns the number of edges ending at this node.
         * 
         * @return the number of in-edges
         */
        public int getInEdgesCount();

        /**
         * Returns the edges starting at this node.
         * 
         * @return the edges
         */
        public Iterable<? extends IvmlGraphEdge> outEdges();

        /**
         * Returns the number of edges starting at this node.
         * 
         * @return the number of out-edges
         */
        public int getOutEdgesCount();

    }

    /**
     * Represents an IVML graph edge.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface IvmlGraphEdge extends IvmlGraphElement {
        
        /**
         * Returns the start node.
         * 
         * @return the start node
         */
        public IvmlGraphNode getStart();

        /**
         * Returns the end node.
         * 
         * @return the end node
         */
        public IvmlGraphNode getEnd();
        
    }

    /**
     * Tries to turn {@code var} into an application-specific graph.
     * 
     * @param var the variable to turn into a graph
     * @return the graph structure representation
     * @throws ExecutionException if {@code var} cannot be turned into a graph
     */
    public IvmlGraph getGraphFor(IDecisionVariable var) throws ExecutionException;

    /**
     * Tries to write back an application-specific graph {@code graph} into {@code var}.
     * 
     * @param var the variable to turn into a graph
     * @param graph the graph structure representation
     * @throws ExecutionException if {@code graph} cannot be written back
     */
    //public void synchronize(IDecisionVariable var, IvmlGraph graph) throws ExecutionException;

    /**
     * Returns the factory to use to crate graphs.
     * 
     * @return the factory
     */
    public GraphFactory getGraphFactory();
    
}
