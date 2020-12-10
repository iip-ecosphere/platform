package de.iip_ecosphere.platform.support.iip_aas;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.DeploymentBuilder;

public class AasPartRegistry {

    public static final String ID_SHORT = "IIP-Ecosphere";
    public static final String URN = "urn:::AAS:::iipEcosphere#";
    
    /**
     * Build up all AAS of the currently running platform part. [public for testing]
     * 
     * @return the list of AAS
     */
    public static List<Aas> build() {
        List<Aas> aas = new ArrayList<>();
        AasBuilder aasBuilder = AasFactory.getInstance().createAasBuilder(ID_SHORT, URN);
        Iterator<AasContributor> iter = ServiceLoader.load(AasContributor.class).iterator();
        while (iter.hasNext()) {
            AasContributor contributor = iter.next();
            Aas partAas = contributor.contributeTo(aasBuilder);
            if (null != partAas) {
                aas.add(partAas);
            }
        }
        aas.add(0, aasBuilder.build());
        return aas;
    }
    
    /**
     * Deploy the given AAS to a local server. [preliminary]
     * 
     * @param aas the list of aas, e.g., from {@link #build()}
     * @param host the host to deploy to
     * @param port the TCP port to deploy to 
     * @param regPath the local registry path
     * @return the server instance
     */
    public static Server deployTo(List<Aas> aas, String host, int port, String regPath) {
        DeploymentBuilder dBuilder = AasFactory.getInstance().createDeploymentBuilder(host, port);
        dBuilder.addInMemoryRegistry(regPath);
        for (Aas a: aas) {
            dBuilder.deploy(a);
        }
        return dBuilder.createServer(200);
    }

}
