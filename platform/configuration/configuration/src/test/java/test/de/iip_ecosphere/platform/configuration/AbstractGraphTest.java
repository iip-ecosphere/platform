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

package test.de.iip_ecosphere.platform.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;

import de.iip_ecosphere.platform.configuration.ivml.DecisionVariableProvider;
import de.iip_ecosphere.platform.configuration.ivml.DefaultEdge;
import de.iip_ecosphere.platform.configuration.ivml.DefaultGraph;
import de.iip_ecosphere.platform.configuration.ivml.DefaultNode;
import de.iip_ecosphere.platform.configuration.ivml.GraphFactory;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraph;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphEdge;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphNode;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * Basic graph testing facility.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractGraphTest {

    protected static final GraphFactory FACTORY = new GraphFactory() {

        @Override
        public IvmlGraph createGraph(IDecisionVariable var) {
            return new TestGraph();
        }

        @Override
        public IvmlGraphEdge createEdge(IDecisionVariable var, IvmlGraphNode start, IvmlGraphNode end) {
            return new TestEdge(start, end);
        }

        @Override
        public IvmlGraphNode createNode(IDecisionVariable var) {
            return new TestNode();
        }
        
    };
    
    protected static final DecisionVariableProvider VAR_PROVIDER = new DecisionVariableProvider() {

        @Override
        public IDecisionVariable getVariable(String varName) throws ExecutionException {
            return null; // preliminary
        }
        
    };
    
    /**
     * A test graph overriding IVML accesses.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class TestGraph extends DefaultGraph {
        
        private String name;
        
        /**
         * Creates a graph instance.
         */
        public TestGraph() {
            super(null);
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public void setName(String name) {
            this.name = name;
        }
        
    }

    /**
     * A test node overriding IVML accesses.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class TestNode extends DefaultNode {

        private String name;
        private int xPos = INVALID_POSITION;
        private int yPos = INVALID_POSITION;
        private int width = INVALID_SIZE;
        private int height = INVALID_SIZE;
        
        /**
         * Creates a graph node.
         */
        public TestNode() {
            super(null);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        public int getXPos() {
            return xPos;
        }

        @Override
        public int getYPos() {
            return yPos;
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public void setXPos(int xPos) {
            this.xPos = xPos;
        }

        @Override
        public void setYPos(int yPos) {
            this.yPos = yPos;
        }

        @Override
        public void setWidth(int width) {
            this.width = width;
        }

        @Override
        public void setHeight(int height) {
            this.height = height;
        }

    }

    /**
     * A test edge overriding IVML accesses.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class TestEdge extends DefaultEdge {

        private String name;

        /**
         * Creates an edge.
         * 
         * @param start the start node
         * @param end the end node
         */
        public TestEdge(IvmlGraphNode start, IvmlGraphNode end) {
            super(null, start, end);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

    }

    /**
     * Creates a graph for testing.
     * 
     * @return the graph
     */
    public static IvmlGraph createAbcGraph() {
        TestGraph result = new TestGraph();
        result.setName("ABC graph");

        TestNode nodeA = new TestNode();
        nodeA.setName("Node A");
        result.addNode(nodeA);
        TestNode nodeB = new TestNode();
        nodeB.setName("Node B");
        result.addNode(nodeB);
        TestNode nodeC = new TestNode();
        nodeC.setName("Node C");
        result.addNode(nodeC);

        TestEdge edgeAB = new TestEdge(nodeA, nodeB);
        edgeAB.setName("Edge A-B");
        nodeA.addEdge(edgeAB);
        nodeB.addEdge(edgeAB);
        TestEdge edgeBC = new TestEdge(nodeB, nodeC);
        edgeBC.setName("Edge B-C");
        nodeB.addEdge(edgeBC);
        nodeC.addEdge(edgeBC);
        TestEdge edgeAC = new TestEdge(nodeA, nodeC);
        edgeAC.setName("Edge A-C");
        nodeA.addEdge(edgeAC);
        nodeC.addEdge(edgeAC);
        return result;
    }

    /**
     * Creates an empty graph for testing.
     * 
     * @return the graph
     */
    public static IvmlGraph createEmptyGraph() {
        TestGraph result = new TestGraph();
        result.setName("empty");
        return result;
    }

    /**
     * Asserts equality of graphs.
     * 
     * @param expected the expected graph
     * @param actual the actual graph
     */
    public static void assertGraph(IvmlGraph expected, IvmlGraph actual) {
        Assert.assertEquals(expected.getNodeCount(), actual.getNodeCount());
        Map<String, IvmlGraphNode> nodes = new HashMap<>();
        for (IvmlGraphNode node : expected.nodes()) {
            nodes.put(node.getName(), node);
        }
        for (IvmlGraphNode node : actual.nodes()) {
            IvmlGraphNode eNode = nodes.get(node.getName());
            Assert.assertNotNull(eNode);
            Assert.assertEquals(eNode.getYPos(), node.getYPos());
            Assert.assertEquals(eNode.getXPos(), node.getXPos());
            Assert.assertEquals(eNode.getWidth(), node.getWidth());
            Assert.assertEquals(eNode.getHeight(), node.getHeight());
            
            Assert.assertEquals(eNode.getInEdgesCount(), node.getInEdgesCount());
            Assert.assertEquals(eNode.getOutEdgesCount(), node.getOutEdgesCount());
            assertEdges(eNode.inEdges(), node.inEdges());
            assertEdges(eNode.outEdges(), node.outEdges());
            
            nodes.remove(node.getName());
        }
        Assert.assertEquals(0, nodes.size());
    }
    
    /**
     * Asserts edges.
     * 
     * @param expected the expected edges
     * @param actual the actual edges
     */
    private static void assertEdges(Iterable<? extends IvmlGraphEdge> expected, 
        Iterable<? extends IvmlGraphEdge> actual) {
        Map<String, IvmlGraphEdge> edges = new HashMap<>();
        for (IvmlGraphEdge e: expected) {
            String id = e.getStart().getName() + "|" + e.getEnd().getName();
            edges.put(id, e);
        }
        for (IvmlGraphEdge e: actual) {
            String id = e.getStart().getName() + "|" + e.getEnd().getName();
            IvmlGraphEdge eEdge = edges.get(id);
            Assert.assertNotNull(eEdge);
            Assert.assertEquals(e.getStart().getName(), eEdge.getStart().getName());
            Assert.assertEquals(e.getEnd().getName(), eEdge.getEnd().getName());
            edges.remove(id);
        }
        Assert.assertEquals(0, edges.size());
    }

}
