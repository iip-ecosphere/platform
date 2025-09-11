package de.iip_ecosphere.platform.configuration.serviceMesh;

import de.iip_ecosphere.platform.configuration.ivml.DefaultNode;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * Represents a graph node. Prepared for refinement.
 * 
 * @author Holger Eichelberger, SSE
 */
class ServiceMeshNode extends DefaultNode {
            
    /**
     * Creates a graph node.
     * 
     * @param var the underlying variable
     */
    ServiceMeshNode(IDecisionVariable var) {
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