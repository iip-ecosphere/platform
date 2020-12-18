/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.aas.basyx;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasServer;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;

/**
 * Tests deployment scenarios.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DeploymentTest {
    
    /**
     * Tests a local dynamic sub-model deployment.
     */
    @Test
    public void localSubmodelDynamicDeployment() throws IOException {
        final String urn = "urn:::AAS:::testMachines#";
        final String host = "localhost";
        final int port = 4050;
        final String registryPath = "registry";
        
        AasFactory factory = AasFactory.getInstance();
        AasBuilder aasB = factory.createAasBuilder("myAas", urn);
        SubmodelBuilder smB = aasB.createSubmodelBuilder("initial");
        smB.build();
        Aas aas = aasB.build();
        
        AasServer server = factory.createDeploymentRecipe(host, port)
            .addInMemoryRegistry(registryPath)
            .deploy(aas)
            .createServer();
        server.start(2000);
        aas = factory.retrieveAas(host, port, registryPath, urn);
        Submodel sub = aas.addSubmodel("dynamic").build();

        server.deploy(aas, sub);

        aas = factory.retrieveAas(host, port, registryPath, urn);
        Assert.assertNotNull(aas.getSubmodel("dynamic"));

        server.stop();
    }

}
