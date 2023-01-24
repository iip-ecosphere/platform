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

package de.iip_ecosphere.platform.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.iip_ecosphere.platform.configuration.ivml.DecisionVariableProvider;
import de.iip_ecosphere.platform.configuration.ivml.GraphFactory;
import de.iip_ecosphere.platform.configuration.ivml.GraphFormat;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraph;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphEdge;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphNode;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * Writes a graph in the <a href="https://github.com/jerosoler/Drawflow">"drawflow"</a> format.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DrawflowGraphFormat implements GraphFormat {

    public static final String NAME = "drawflow";
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getFormatKind() {
        return "JSON";
    }

    @Override
    public String toString(IvmlGraph graph) throws ExecutionException {
        GraphWriter writer = new GraphWriter(graph);
        return writer.writeGraph();
    }
    
    /**
     * Instance to map and write a graph.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class GraphWriter {

        private Map<IvmlGraphNode, String> node2id = new HashMap<>();
        private IvmlGraph graph;
        
        /**
         * Creates a writer for {@code graph}.
         * 
         * @param graph the graph to write
         */
        private GraphWriter(IvmlGraph graph) {
            this.graph = graph;
            mapNodes();
        }

        /**
         * Maps the nodes of {@code graph} to ids.
         */
        private void mapNodes() {
            int id = 1;
            if (null != graph) {
                for (IvmlGraphNode node : graph.nodes()) {
                    String tmp = String.valueOf(id);
                    node2id.put(node, tmp);
                    id++;
                }
            }
        }

        /**
         * Writes the "home" data section.
         * 
         * @return the home data JSON object
         */
        @SuppressWarnings("unchecked")
        private JSONObject writeHomeData() {
            JSONObject homeData = new JSONObject();
            if (null != graph) {
                for (IvmlGraphNode node : graph.nodes()) {
                    JSONObject jNode = new JSONObject();
                    String nodeId = node2id.get(node);
                    jNode.put("class", "node");
                    jNode.put("id", nodeId);
                    jNode.put("name", node.getName());
                    JSONObject data = new JSONObject();
                    data.put("ivmlVar", getId(node, nodeId));
                    jNode.put("data", data);
                    //jNode.put("class", node.getType());
                    jNode.put("html", "<div>" + node.getName() + "</div>"); // preliminary
                    jNode.put("typenode", false);
                    jNode.put("inputs", writeEdges(node.inEdges(), true));
                    jNode.put("outputs", writeEdges(node.outEdges(), false));
                    if (node.getXPos() >= 0 && node.getYPos() >= 0) {
                        jNode.put("pos_x", node.getXPos());
                        jNode.put("pos_y", node.getYPos());
                    }
                    homeData.put(nodeId, jNode);
                }
            }
            return homeData;
        }

        /**
         * Returns the ID of the node in terms of its IVML variable name.
         * 
         * @param node the node
         * @param dflt the default value
         * @return the node ID
         */
        private String getId(IvmlGraphNode node, String dflt) {
            String result;
            if (node.getVariable() != null) {
                result = node.getVariable().getDeclaration().getName();
            } else {
                result = dflt;
            }
            return result;
        }
        
        /**
         * Writes the graph to a JSON string.
         * 
         * @return the JSON string
         */
        @SuppressWarnings("unchecked")
        private String writeGraph() {
            JSONObject otherData = new JSONObject();
            // TODO write other remaining services??
            JSONObject other = new JSONObject();
            other.put("data", otherData);
            
            JSONObject home = new JSONObject();
            home.put("data", writeHomeData());

            JSONObject dataflow = new JSONObject();
            dataflow.put("Home", home);
            dataflow.put("Other", other);

            JSONObject outer = new JSONObject();
            outer.put("drawflow", dataflow);
            return outer.toJSONString();
        }
        
        /**
         * Writes the given edges.
         * 
         * @param edges the edges to write
         * @param inputEdges are we writing input or output edges
         * @return the edges as JSON object
         */
        @SuppressWarnings("unchecked")
        private JSONObject writeEdges(Iterable<? extends IvmlGraphEdge> edges, boolean inputEdges) {
            JSONArray conns = new JSONArray();
            int count = 1;
            for (IvmlGraphEdge edge : edges) {
                JSONObject jEdge = new JSONObject();
                IvmlGraphNode other;
                if (inputEdges) {
                    other = edge.getStart();
                    jEdge.put("input", "output_" + count);
                } else {
                    other = edge.getEnd();
                    jEdge.put("output", "input_" + count);
                }
                jEdge.put("node", node2id.get(other));
                conns.add(jEdge);
                count++;
            }
            JSONObject input = new JSONObject();
            input.put("connections", conns);
            JSONObject inputs = new JSONObject();
            inputs.put(inputEdges ? "input_1" : "output_1", input);
            return inputs;
        }

    }
    
    @Override
    public IvmlGraph fromString(String graph, GraphFactory factory, DecisionVariableProvider varProvider) 
        throws ExecutionException {
        GraphReader reader = new GraphReader(factory, varProvider);
        return reader.read(graph);
    }
        
    /**
     * Obtains a JSON object from {@code object}.
     * 
     * @param <T> the type to be returned
     * @param object the object to read from
     * @param field the field name to read from within {@code object}
     * @param cls the type as class
     * @param dflt the default value if {@code field} does not exist
     * @param converter optional converter if the found value is not compliant with {@code cls}, may be <b>null</b>
     * @return returns the JSON object or, if not found, an empty JSON object
     */
    private static <T> T getJson(JSONObject object, String field, Class<T> cls, Supplier<T> dflt, 
        Function<Object, T> converter) {
        T result;
        Object o = object.get(field);
        if (cls.isInstance(o)) {
            result = cls.cast(o);
        } else if (null != o && null != converter) {
            result = converter.apply(o);
        } else {
            result = dflt.get();
        }
        return result;
    }

    /**
     * Obtains a JSON object from {@code object}.
     * 
     * @param object the object to read from
     * @param field the field name
     * @return returns the JSON object or, if not found, an empty JSON object
     */
    private static JSONObject getJsonObject(JSONObject object, String field) {
        return getJson(object, field, JSONObject.class, () -> new JSONObject(), null);
    }


    /**
     * Obtains a JSON array from {@code object}.
     * 
     * @param object the object to read from
     * @param field the field name
     * @return returns the JSON array, or, if not found, an empty JSON array
     */
    private static JSONArray getJsonArray(JSONObject object, String field) {
        return getJson(object, field, JSONArray.class, () -> new JSONArray(), null);
    }

    /**
     * Obtains a string from {@code object}.
     * 
     * @param object the object to read from
     * @param field the field name
     * @param dflt the default value
     * @return returns the string or, if not found, {@code dflt}
     */
    private static String getString(JSONObject object, String field, String dflt) {
        return getJson(object, field, String.class, () -> dflt, null);
    }

    /**
     * Obtains an int from {@code object}.
     * 
     * @param object the object to read from
     * @param field the field name
     * @param dflt the default value
     * @return returns the int value or, if not found, {@code dflt}
     */
    private static int getInteger(JSONObject object, String field, int dflt) {
        return getJson(object, field, Integer.class, () -> dflt, o -> Integer.parseInt(o.toString()));
    }
    
    /**
     * Implements a graph reader.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class GraphReader {

        private GraphFactory factory;
        private DecisionVariableProvider varProvider;
        private Map<String, IvmlGraphNode> id2Nodes = new HashMap<>();
        private Map<String, IvmlGraphEdge> ids2Edges = new HashMap<>();
        
        /**
         * Creates a graph reader instance.
         * 
         * @param factory the graph factory
         * @param varProvider the variability provider
         */
        private GraphReader(GraphFactory factory, DecisionVariableProvider varProvider) {
            this.factory = factory;
            this.varProvider = varProvider;
        }

        /**
         * Reads a JSON representation and returns an IVML graph.
         * 
         * @param graph the graph to read
         * @return the IVML graph representation
         * @throws ExecutionException if parsing {@code graph} fails
         */
        private IvmlGraph read(String graph) throws ExecutionException {
            IvmlGraph result = null;
            try {
                JSONParser jsonParser = new JSONParser();
                JSONObject top = (JSONObject) jsonParser.parse(graph);
                JSONObject drawflow = getJsonObject(top, "drawflow");
                JSONObject home = getJsonObject(drawflow, "Home");
                JSONObject data = getJsonObject(home, "data");
                result = factory.createGraph(null); // TODO where does the variable come from
                for (Object id : data.keySet()) {
                    JSONObject node = getJsonObject(data, id.toString());
                    String varName = getString(node, "id", "");
                    String name = getString(node, "name", "");
                    IDecisionVariable var = varProvider.getVariable(varName); // null, does not exit
                    if (null == var) {
                        var = null;
                        //var = varProvider.createVariable(name, type);
                    }
                    IvmlGraphNode gNode = factory.createNode(var);
                    result.addNode(gNode);
                    gNode.setXPos(getInteger(node, "pos_x", -1));
                    gNode.setYPos(getInteger(node, "pos_y", -1));
                    gNode.setName(name);
                    id2Nodes.put(id.toString(), gNode);
                }
                for (Object id : data.keySet()) {
                    JSONObject node = getJsonObject(data, id.toString());
                    readEdges(id, node, true);
                    readEdges(id, node, false);
                }
            } catch (ParseException e) {
                throw new ExecutionException("While parsing graph JSON: " + e.getMessage(), e);
            }
            return result;
        }
        
        /**
         * Reads the edges for {@code id}/{@code node}.
         * 
         * @param id the node id
         * @param node the node
         * @param inputEdges are we reading input or output edges
         */
        private void readEdges(Object id, JSONObject node, boolean inputEdges) {
            IvmlGraphNode gNode = id2Nodes.get(id.toString());
            JSONObject edges = getJsonObject(node, inputEdges ? "inputs" : "outputs");
            for (Object eId : edges.keySet()) {
                JSONObject edge = getJsonObject(edges, eId.toString());
                JSONArray connections = getJsonArray(edge, "connections");
                for (int c = 0; c < connections.size(); c++) {
                    JSONObject connection = (JSONObject) connections.get(c);
                    String oId = getString(connection, "node", "");
                    IvmlGraphNode oNode = id2Nodes.get(oId);
                    String edgeId;
                    if (inputEdges) {
                        edgeId = oId + "|" + id;
                    } else {
                        edgeId = id + "|" + oId;
                    }
                    if (!ids2Edges.containsKey(edgeId)) {
                        IvmlGraphEdge gEdge;
                        if (inputEdges) {
                            gEdge = factory.createEdge(null, oNode, gNode); // TODO edge var
                        } else {
                            gEdge = factory.createEdge(null, gNode, oNode); // TODO edge var
                        }
                        gNode.addEdge(gEdge); 
                        oNode.addEdge(gEdge); 
                        ids2Edges.put(edgeId, gEdge);
                    }
                }
            }
        }
        
    }

}
