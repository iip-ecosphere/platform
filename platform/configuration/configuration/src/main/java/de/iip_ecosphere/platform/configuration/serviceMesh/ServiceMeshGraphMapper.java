package de.iip_ecosphere.platform.configuration.serviceMesh;

import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.configuration.ivml.GraphFactory;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper;
import de.iip_ecosphere.platform.configuration.ivml.IvmlUtils;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * Maps a graph in IIP style. [public for testing]
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServiceMeshGraphMapper implements IvmlGraphMapper {

    @Override
    public IvmlGraph getGraphFor(IDecisionVariable var) throws ExecutionException {
        ServiceMeshGraph result = null;
        if (IvmlUtils.isOfCompoundType(var, "ServiceMesh")) {
            result = new ServiceMeshGraphWalker(var).getResult();
        }
        return result;
    }

    @Override
    public GraphFactory getGraphFactory() {
        return ServiceMeshGraphFactory.INSTANCE;
    }
    
}