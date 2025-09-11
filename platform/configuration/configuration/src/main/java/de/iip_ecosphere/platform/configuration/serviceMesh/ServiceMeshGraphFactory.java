package de.iip_ecosphere.platform.configuration.serviceMesh;

import de.iip_ecosphere.platform.configuration.ivml.GraphFactory;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraph;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphEdge;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphNode;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * Implements a factory for the service mesh graph elements used.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServiceMeshGraphFactory implements GraphFactory {

    public static final GraphFactory INSTANCE = new ServiceMeshGraphFactory();
    
    @Override
    public IvmlGraph createGraph(IDecisionVariable var) {
        return new ServiceMeshGraph(var);
    }

    @Override
    public IvmlGraphEdge createEdge(IDecisionVariable var, IvmlGraphNode start, IvmlGraphNode end) {
        return new ServiceMeshEdge(var, start, end);
    }

    @Override
    public IvmlGraphNode createNode(IDecisionVariable var) {
        return new ServiceMeshNode(var);
    }
    
}