package de.iip_ecosphere.platform.support.aas.basyx;

import java.io.IOException;

import org.eclipse.basyx.aas.metamodel.map.descriptor.SubmodelDescriptor;
import org.eclipse.basyx.submodel.restapi.SubModelProvider;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasServer;
import de.iip_ecosphere.platform.support.aas.Submodel;

/**
 * Basic implementation of the the {@link AasServer} interface based on a {@link DeploymentSpec}.
 * 
 * @author Holger Eichelberger, SSE
 */
abstract class BaSyxAbstractAasServer implements AasServer {

    private DeploymentSpec deploymentSpec;
    
    /**
     * Creates a new BaSyx AAS server.
     * 
     * @param deploymentSpec the deployment set instance for runtime deployments
     */
    BaSyxAbstractAasServer(DeploymentSpec deploymentSpec) {
        this.deploymentSpec = deploymentSpec;
    }
    
    @Override
    public void deploy(Aas aas) throws IOException {
        BaSyxDeploymentRecipe.deploy(deploymentSpec, aas);
    }
    
    @Override
    public void deploy(Aas aas, Submodel submodel) {
        if (!(submodel instanceof BaSyxSubmodel)) {
            throw new IllegalArgumentException("The subModel must be of instance BaSyxSubModel, i.e., created "
                + "through the AasFactory.");
        }
        BaSyxAasDescriptor desc = deploymentSpec.getDescriptor(aas.getIdShort());
        if (null == desc) {
            throw new IllegalArgumentException("The AAS " + aas.getIdShort() + " is unknown on this server "
                + "instance.");
        }
        
        BaSyxSubmodel sm = (BaSyxSubmodel) submodel;
        SubModelProvider subModelProvider = new SubModelProvider(sm.getSubmodel());
        desc.getFullProvider().addSubmodel(subModelProvider);
        desc.getAasDescriptor().addSubmodelDescriptor(new SubmodelDescriptor(sm.getSubmodel(), 
            AbstractSubmodel.getSubmodelEndpoint(deploymentSpec.getEndpoint(), aas, submodel)));
    }
    
    @Override
    public void stop(boolean dispose) {
        if (dispose) {
            Tools.disposeTomcatWorkingDir(null, deploymentSpec.getEndpoint().getPort());
        }
    }

}
