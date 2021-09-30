package de.iip_ecosphere.platform.support.aas.basyx;

import org.eclipse.basyx.vab.protocol.http.server.BaSyxHTTPServer;

import de.iip_ecosphere.platform.support.aas.AasServer;

/**
 * A simple, immediate AAS server implementation.
 * 
 * @author Holger Eichelberger, SSE
 */
class BaSyxImmediateDeploymentAasServer extends BaSyxAbstractAasServer {

    private BaSyxHTTPServer server;
    
    /**
     * Creates a new BaSyx AAS server.
     * 
     * @param deploymentSet the deployment set instance for runtime deployments
     */
    BaSyxImmediateDeploymentAasServer(DeploymentSpec deploymentSet) {
        super(deploymentSet);
        server = new BaSyxHTTPServer(deploymentSet.getContext());
    }
    
    @Override
    public AasServer start() {
        server.start();
        return this;
    }

    @Override
    public void stop(boolean dispose) {
        server.shutdown();
        super.stop(dispose); // if not disposable, schedule for deletion at JVM end
    }

}