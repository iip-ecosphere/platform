package de.iip_ecosphere.platform.configuration.easyProducer.serviceMesh;

import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.configuration.easyProducer.ivml.GraphFactory;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.IvmlGraphMapper;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.IvmlUtils;
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