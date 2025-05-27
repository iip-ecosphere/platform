package de.iip_ecosphere.platform.support.aas.basyx1_0;

import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;

import de.iip_ecosphere.platform.support.aas.AasServer;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxAbstractAasServer;
import de.iip_ecosphere.platform.support.aas.basyx.DeploymentSpec;
import de.iip_ecosphere.platform.support.aas.basyx.VersionAdjustment;
import de.iip_ecosphere.platform.support.aas.basyx.Tools;

/**
 * A registry-based AAS server.
 * 
 * @author Holger Eichelberger, SSE
 */
class BaSyxRegistryDeploymentAasServer extends BaSyxAbstractAasServer {

    private AASServerComponent server; 
    
    // checkstyle: stop parameter number check
    
    /**
     * Creates a new BaSyx AAS server.
     * 
     * @param deploymentSpec the deployment set instance for runtime deployments
     * @param regUrl the registryUR
     * @param backend the AAS server backend to use
     * @param options for server creation
     */
    BaSyxRegistryDeploymentAasServer(DeploymentSpec deploymentSpec, String regUrl, AASServerBackend backend, 
        String... options) {
        super(deploymentSpec);
        AASServerBackend back = Tools.getOption(options, backend, AASServerBackend.class);
        BaSyxAASServerConfiguration cfg = new BaSyxAASServerConfiguration(back, "", regUrl);
        VersionAdjustment.setupBaSyxAASServerConfiguration(cfg);
        // may require source via options
        server = new AASServerComponent(deploymentSpec.getContextConfiguration(), cfg);
    }

    // checkstyle: resume parameter number check

    @Override
    public AasServer start() {
        server.startComponent();
        return this;
    }

    @Override
    public void stop(boolean dispose) {
        server.stopComponent();
        super.stop(dispose); // if not disposable, schedule for deletion at JVM end
    }

}