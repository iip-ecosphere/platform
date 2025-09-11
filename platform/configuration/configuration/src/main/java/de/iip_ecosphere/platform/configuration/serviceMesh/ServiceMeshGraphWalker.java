package de.iip_ecosphere.platform.configuration.serviceMesh;

import java.util.HashMap;
import java.util.Map;

import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * Traverses a graph.
 * 
 * @author Holger Eichelberger, SSE
 */
class ServiceMeshGraphWalker {

    private ServiceMeshGraph result;
    private Map<IDecisionVariable, ServiceMeshNode> nodes = new HashMap<>();

    /**
     * Creates a new instance.
     * 
     * @param var the variable to start the traversal
     */
    public ServiceMeshGraphWalker(IDecisionVariable var) {
        result =  new ServiceMeshGraph(var);
        traverseNodes(var.getNestedElement("sources"));
    }
    
    /**
     * Returns the created graph.
     * 
     * @return the graph
     */
    public ServiceMeshGraph getResult() {
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
                ServiceMeshNode node = nodes.get(elt);
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
    private void traverseConnectors(IDecisionVariable var, ServiceMeshNode from) {
        if (null != var) {
            for (int n = 0; n < var.getNestedElementsCount(); n++) {
                IDecisionVariable conn = Configuration.dereference(var.getNestedElement(n));
                IDecisionVariable next = Configuration.dereference(conn.getNestedElement("next"));
                ServiceMeshNode node = nodes.get(next);
                if (null == node) {
                    node = createNode(next);
                    traverseConnectors(next.getNestedElement("next"), node);
                }
                from.addEdge(new ServiceMeshEdge(conn, from, node));
                node.addEdge(new ServiceMeshEdge(conn, from, node));
            }
        }
    }

    /**
     * Creates a node and registers it in the resulting graph.
     * 
     * @param elt the variable representing the node
     * @return the created node
     */
    private ServiceMeshNode createNode(IDecisionVariable elt) {
        ServiceMeshNode node = new ServiceMeshNode(elt);
        nodes.put(elt, node);
        result.addNode(node);
        return node;
    }

}