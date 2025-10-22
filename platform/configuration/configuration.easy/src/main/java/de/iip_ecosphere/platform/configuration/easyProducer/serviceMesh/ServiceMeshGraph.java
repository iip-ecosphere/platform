package de.iip_ecosphere.platform.configuration.easyProducer.serviceMesh;

import de.iip_ecosphere.platform.configuration.easyProducer.ivml.DefaultGraph;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.IvmlUtils;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * Represents a service mesh as graph.
 * 
 * @author Holger Eichelberger, SSE
 */
class ServiceMeshGraph extends DefaultGraph {

    /**
     * Creates a graph instance.
     * 
     * @param var the underlying variable
     */
    ServiceMeshGraph(IDecisionVariable var) {
        super(var);
    }
    
    @Override
    public String getName() {
        return IvmlUtils.getStringValue(getVariable().getNestedElement("description"), ""); // preliminary
    }

}