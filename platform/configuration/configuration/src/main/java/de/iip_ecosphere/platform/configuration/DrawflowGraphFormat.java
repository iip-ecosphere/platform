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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
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
import de.iip_ecosphere.platform.configuration.ivml.IvmlUtils;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.IvmlDatatypeVisitor;

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
        private Map<IvmlGraphNode, Integer> inputCounts = new HashMap<>();
        private Map<IvmlGraphNode, Integer> outputCounts = new HashMap<>();
        
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
         * Turns service information into data for the editor.
         * 
         * @param service the IVML variable representing the service, may be <b>null</b>
         * @param data the data object to be modified
         */
        @SuppressWarnings("unchecked")
        private void serviceToData(IDecisionVariable service, JSONObject data) {
            if (null != service) {
                service = Configuration.dereference(service); // if this is as often a refTo(Service)
                String type = IvmlDatatypeVisitor.getUnqualifiedType(service.getValue().getType()); // dynamic type
                data.put("type", type);
                String kind = IvmlUtils.toName(IvmlUtils.getEnumValue(IvmlUtils.getNestedSafe(service, "kind")), "?");
                data.put("kind", kind);
                String sId = IvmlUtils.getStringValue(IvmlUtils.getNestedSafe(service, "id"), "");
                data.put("id", sId);
                processBackward(service.getNestedElement("input"), data, "bus-receive");
                processBackward(service.getNestedElement("output"), data, "bus-send");
                final JSONObject serviceStates = new JSONObject();
                if (sId.length() > 0) {
                    StatusCache.getServiceStates(sId, state -> {
                        serviceStates.put(state.getDeviceId(), state.getState());
                    });
                }
                data.put("states", serviceStates);
            }
        }
        
        /**
         * Processes backward flows by adding their type names as String array into a JSON field called {@code name}.
         * 
         * @param set denotes the type set (input/output) to be analyzed
         * @param data the data object to be modified
         * @param name the name of the field to write the backward flows into
         */
        @SuppressWarnings("unchecked")
        private void processBackward(IDecisionVariable set, JSONObject data, String name) {
            JSONArray array = new JSONArray();
            collectTypes(set, fwd -> !fwd, type -> array.add(type));
            data.put(name, array);
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
                    serviceToData(IvmlUtils.getNestedSafe(node.getVariable(), "impl"), data); 
                    jNode.put("data", data);
                    //jNode.put("class", node.getType());
                    jNode.put("html", "<div>" + node.getName() + "</div>"); // preliminary
                    jNode.put("typenode", false);
                    jNode.put("inputs", writeEdges(node, node.inEdges(), true));
                    jNode.put("outputs", writeEdges(node, node.outEdges(), false));
                    jNode.put("pos_x", node.getXPos());
                    jNode.put("pos_y", node.getYPos());
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
         * @param node the start/end node defining the {@code edges}
         * @param edges the edges to write 
         * @param inputEdges are we writing input or output edges
         * @return the edges as JSON object
         */
        @SuppressWarnings("unchecked")
        private JSONObject writeEdges(IvmlGraphNode node, Iterable<? extends IvmlGraphEdge> edges, 
            boolean inputEdges) {
            JSONObject result = new JSONObject();
            String prefix = inputEdges ? "input_" : "output_";
            int count = 1;
            for (IvmlGraphEdge edge : edges) {
                JSONObject inOutput = new JSONObject();
                JSONObject jEdge = new JSONObject();
                JSONArray conns = new JSONArray();
                JSONObject data = new JSONObject();
                IvmlGraphNode other;
                if (inputEdges) {
                    other = edge.getStart();
                    jEdge.put("input", "output_" + nextCount(other, outputCounts));
                    collectForwardTypes(other, node, edge, data);
                } else {
                    other = edge.getEnd();
                    jEdge.put("output", "input_" + nextCount(other, inputCounts));
                    collectForwardTypes(node, other, edge, data);
                }
                jEdge.put("node", node2id.get(other));
                conns.add(jEdge);
                inOutput.put("connections", conns);
                inOutput.put("data", data);
                result.put(prefix + count, inOutput);
                count++;
            }
            return result;
        }

        /**
         * Collects common forward types between the nodes involved in {@code edge} and adds the results as 
         * "type"-array to {@code data}.
         * 
         * @param start the start node
         * @param end the end node
         * @param edge the edge (for future information)
         * @param data the data object to add a "types" field
         */
        @SuppressWarnings("unchecked")
        private void collectForwardTypes(IvmlGraphNode start, IvmlGraphNode end, IvmlGraphEdge edge, JSONObject data) {
            if (start.getVariable() != null && end.getVariable() != null) {
                SortedSet<String> types = new TreeSet<String>();
                collectTypes(start.getVariable().getNestedElement("output"), fwd -> fwd, s -> types.add(s));
                SortedSet<String> types2 = new TreeSet<String>();
                collectTypes(end.getVariable().getNestedElement("input"), fwd -> fwd, s -> types2.add(s));
                types.retainAll(types2);
                JSONArray array = new JSONArray();
                for (String type: types) {
                    array.add(type);
                }
                data.put("types", array);
            }
        }
        
        /**
         * Collects IIP-Ecosphere data types.
         * 
         * @param set the set of decisions to process
         * @param direction a directional predicate over forward/backward types
         * @param consumer processes identified type names
         */
        private void collectTypes(IDecisionVariable set, Predicate<Boolean> direction, Consumer<String> consumer) {
            for (int n = 0; n < set.getNestedElementsCount(); n++) {
                IDecisionVariable type = set.getNestedElement(n);
                if (direction.test(IvmlUtils.getBooleanValue(type.getNestedElement("forward"), true))) {
                    IDecisionVariable dataType = Configuration.dereference(type.getNestedElement("type"));
                    if (null != dataType) {
                        String typeName = IvmlUtils.getStringValue(dataType.getNestedElement("name"), "");
                        if (typeName.length() > 0) {
                            consumer.accept(typeName);
                        }
                    }
                }
            }
        }
        
        /**
         * Returns the next input/output count for {@code node} with respect to the counting map {@code counts}.
         * A call will modify {@code counts} as a side effect on the entry for node, either setting it to 1 or 
         * incrementing it by one.
         * 
         * @param node the note to return the count for
         * @param counts the counting map to use
         * @return the counter
         */
        private int nextCount(IvmlGraphNode node, Map<IvmlGraphNode, Integer> counts) {
            Integer result = counts.get(node);
            if (null == result) {
                result = 1;
            } else {
                result++;
            }
            counts.put(node, result);
            return result;
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
         * Returns {@code optional} if this is an object with contents, else {@code base}.
         * 
         * @param base the base object to be returned if {@code optional} is no valid object
         * @param optional the optional object, may also be <b>null</b>
         * @return {@code base} or {@code optional}
         */
        private JSONObject optional(JSONObject base, JSONObject optional) {
            return (null == optional || optional.isEmpty()) 
                && !(base.size() == 1 && base.containsKey("data")) // last selection, leads to base otherwise
                ? base : optional;
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
                // input format differs from output :(
                JSONObject drawflow = optional(top, getJsonObject(top, "drawflow"));
                JSONObject home = optional(drawflow, getJsonObject(drawflow, "Home"));
                JSONObject data = optional(home, getJsonObject(home, "data"));
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
