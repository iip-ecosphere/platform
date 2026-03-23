package de.iip_ecosphere.platform.support.aas.basyx2.common;

import java.io.IOException;

import de.iip_ecosphere.platform.support.NoOpServer;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasServer;
import de.iip_ecosphere.platform.support.aas.Submodel;

public class NoOpAasServer extends NoOpServer implements AasServer {

    @Override
    public void deploy(Aas aas) throws IOException {
    }

    @Override
    public void deploy(Aas aas, Submodel submodel) throws IOException {
    }
    
    @Override
    public AasServer start() {
        return this;
    }

}
