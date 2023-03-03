/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.configuration.ivml.AasIvmlMapper;
import de.iip_ecosphere.platform.configuration.ivml.AasIvmlMapper.AasChange;
import de.iip_ecosphere.platform.configuration.ivml.DefaultEdge;
import de.iip_ecosphere.platform.configuration.ivml.DefaultGraph;
import de.iip_ecosphere.platform.configuration.ivml.DefaultNode;
import de.iip_ecosphere.platform.configuration.ivml.GraphFactory;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraph;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphEdge;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphNode;
import de.iip_ecosphere.platform.configuration.ivml.IvmlUtils;
import de.iip_ecosphere.platform.configuration.ivml.AbstractIvmlModifier.ConfigurationChangeListener;
import de.iip_ecosphere.platform.configuration.ivml.AbstractIvmlModifier.ConfigurationChangeType;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper.OperationCompletedListener;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * Realizes the AAS of the configuration component.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConfigurationAas implements AasContributor, ConfigurationChangeListener, OperationCompletedListener {

    public static final String NAME_SUBMODEL = "Configuration"; 
    private static final GraphFactory GRAPH_FACTORY = new IipGraphFactory();
    
    private AasIvmlMapper mapper;
    private transient List<AasChange> aasChanges = new ArrayList<>();

    /**
     * Implements a factory for the graph elements used.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class IipGraphFactory implements GraphFactory {

        @Override
        public IvmlGraph createGraph(IDecisionVariable var) {
            return new IipGraph(var);
        }

        @Override
        public IvmlGraphEdge createEdge(IDecisionVariable var, IvmlGraphNode start, IvmlGraphNode end) {
            return new IipEdge(var, start, end);
        }

        @Override
        public IvmlGraphNode createNode(IDecisionVariable var) {
            return new IipNode(var);
        }
        
    }
    
    /**
     * Represents a graph.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class IipGraph extends DefaultGraph {

        /**
         * Creates a graph instance.
         * 
         * @param var the underlying variable
         */
        private IipGraph(IDecisionVariable var) {
            super(var);
        }
        
        @Override
        public String getName() {
            return IvmlUtils.getStringValue(getVariable().getNestedElement("description"), ""); // preliminary
        }

    }

    /**
     * Represents a graph node. Prepared for refinement.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class IipNode extends DefaultNode {
                
        /**
         * Creates a graph node.
         * 
         * @param var the underlying variable
         */
        private IipNode(IDecisionVariable var) {
            super(var);
        }
        
        @Override
        public String getName() {
            String result = super.getName();
            if (null == result || result.length() == 0) {
                result = getImpl();
            }
            return result;
        }

        @Override
        protected String getXPosVarName() {
            return "pos_x";
        }

        @Override
        protected String getYPosVarName() {
            return "pos_y";
        }

    }

    /**
     * Represents a graph edge. Prepared for refinement.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class IipEdge extends DefaultEdge {

        /**
         * Creates an edge.
         * 
         * @param var the underlying variable
         * @param start the start node
         * @param end the end node
         */
        public IipEdge(IDecisionVariable var, IvmlGraphNode start, IvmlGraphNode end) {
            super(var, start, end);
        }

    }

    /**
     * Maps a graph in IIP style. [public for testing]
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class IipGraphMapper implements IvmlGraphMapper {

        @Override
        public IvmlGraph getGraphFor(IDecisionVariable var) throws ExecutionException {
            IipGraph result = null;
            if (IvmlUtils.isOfCompoundType(var, "ServiceMesh")) {
                result = new GraphWalker(var).getResult();
            }
            return result;
        }

        @Override
        public GraphFactory getGraphFactory() {
            return GRAPH_FACTORY;
        }
        
    }
    
    /**
     * Traverses a graph.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class GraphWalker {

        private IipGraph result;
        private Map<IDecisionVariable, IipNode> nodes = new HashMap<>();

        /**
         * Creates a new instance.
         * 
         * @param var the variable to start the traversal
         */
        private GraphWalker(IDecisionVariable var) {
            result =  new IipGraph(var);
            traverseNodes(var.getNestedElement("sources"));
        }
        
        /**
         * Returns the created graph.
         * 
         * @return the graph
         */
        private IipGraph getResult() {
            return result;
        }

        /**
         * Traverses the nodes stored as nested elements in {@code var}.
         * 
         * @param var the variable representing the nodes
         */
        private void traverseNodes(IDecisionVariable var) {
            if (null != var) {
                for (int n = 0; n < var.getNestedElementsCount(); n++) {
                    IDecisionVariable elt = Configuration.dereference(var.getNestedElement(n));
                    IipNode node = nodes.get(elt);
                    if (null == node) {
                        node = createNode(elt);
                        traverseConnectors(elt.getNestedElement("next"), node);
                    } // else node is already known
                }
            }
        }

        /**
         * Traverses the connectors stored as nested elements in {@code var}.
         * 
         * @param var the variable representing the nodes
         * @param from the node where this call originates, the start node of edges to be created
         */
        private void traverseConnectors(IDecisionVariable var, IipNode from) {
            if (null != var) {
                for (int n = 0; n < var.getNestedElementsCount(); n++) {
                    IDecisionVariable conn = Configuration.dereference(var.getNestedElement(n));
                    IDecisionVariable next = Configuration.dereference(conn.getNestedElement("next"));
                    IipNode node = nodes.get(next);
                    if (null == node) {
                        node = createNode(next);
                        traverseConnectors(next.getNestedElement("next"), node);
                    }
                    from.addEdge(new IipEdge(conn, from, node));
                    node.addEdge(new IipEdge(conn, from, node));
                }
            }
        }

        /**
         * Creates a node and registers it in the resulting graph.
         * 
         * @param elt the variable representing the node
         * @return the created node
         */
        private IipNode createNode(IDecisionVariable elt) {
            IipNode node = new IipNode(elt);
            nodes.put(elt, node);
            result.addNode(node);
            return node;
        }

    }
    
    @Override
    public Aas contributeTo(AasBuilder aasBuilder, InvocablesCreator iCreator) {
        SubmodelBuilder smB = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, null);
        mapper = new AasIvmlMapper(() -> ConfigurationManager.getVilConfiguration(), new IipGraphMapper(), this, this);
        mapper.mapByType(smB, iCreator);
        mapper.addGraphFormat(new DrawflowGraphFormat());
        smB.build();
        return null;
    }

    @Override
    public void contributeTo(ProtocolServerBuilder sBuilder) {
        mapper.bindOperations(sBuilder);
    }

    @Override
    public Kind getKind() {
        return Kind.ACTIVE;
    }
    
    @Override
    public boolean isValid() {
        return true;
    }
  
    /**
     * Clears all remaining AAS changes.
     */
    private void clearAasChanges() {
        aasChanges.clear();
    }

    @Override
    public void configurationChanged(IDecisionVariable var, ConfigurationChangeType type) {
        aasChanges.add(new AasChange(var, type));
    }

    @Override
    public void operationCompleted() {
        try {
            Aas aas = AasPartRegistry.retrieveIipAas();
            Submodel sm = aas.getSubmodel(NAME_SUBMODEL);
            SubmodelBuilder smB = aas.createSubmodelBuilder(NAME_SUBMODEL, null);
            for (AasChange c : aasChanges) {
                c.apply(mapper, sm, smB);
            }
            smB.build();
        } catch (IOException e) {
            LoggerFactory.getLogger(ConfigurationAas.class).error(
                "While modifying configuration AAS: {}", e.getMessage());
        }
        clearAasChanges();
    }

    @Override
    public void operationFailed() {
        clearAasChanges();
    }

}
