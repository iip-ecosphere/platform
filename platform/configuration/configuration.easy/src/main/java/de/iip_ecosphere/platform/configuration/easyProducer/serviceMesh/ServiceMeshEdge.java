package de.iip_ecosphere.platform.configuration.easyProducer.serviceMesh;

import de.iip_ecosphere.platform.configuration.easyProducer.ivml.DefaultEdge;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.IvmlGraphMapper.IvmlGraphNode;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * Represents a graph edge. Prepared for refinement.
 * 
 * @author Holger Eichelberger, SSE
 */
class ServiceMeshEdge extends DefaultEdge {

    /**
     * Creates an edge.
     * 
     * @param var the underlying variable
     * @param start the start node
     * @param end the end node
     */
    public ServiceMeshEdge(IDecisionVariable var, IvmlGraphNode start, IvmlGraphNode end) {
        super(var, start, end);
    }

}