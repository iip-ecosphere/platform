/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.iip_aas;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasPrintVisitor;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.LocalNetworkManagerWithParentAas;
import de.iip_ecosphere.platform.support.iip_aas.NetworkManagerAas;
import de.iip_ecosphere.platform.support.iip_aas.NetworkManagerAasClient;
import test.de.iip_ecosphere.platform.support.net.NetworkManagerTest;

/**
 * A test for {@link NetworkManagerAas} and {@link NetworkManagerAasClient}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class NetworkManagerAasTest {

    /**
     * Tests the {@link NetworkManagerAas} by creating the complete platform AAS via the {@link AasPartRegistry}
     * including the {@link NetworkManagerAas} and querying it using the network manager tests through 
     * the {@link NetworkManagerAasClient}.
     * 
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testAas() throws ExecutionException, IOException {
        Assert.assertTrue(AasPartRegistry.contributorClasses().contains(NetworkManagerAas.class));
        AasPartRegistry.AasBuildResult res = AasPartRegistry.build();
        // active AAS require two server instances and a deployment
        Server implServer = res.getProtocolServerBuilder().build();
        implServer.start();
        Server aasServer = AasPartRegistry.deploy(res.getAas()); 
        aasServer.start();
        Aas aas = AasPartRegistry.retrieveIipAas();
        Assert.assertNotNull(aas);
        
        aas.accept(new AasPrintVisitor());
        
        Submodel nwm = aas.getSubmodel(NetworkManagerAas.NAME_SUBMODEL);
        Assert.assertNotNull(nwm);
        NetworkManagerAasClient clientNwm = new NetworkManagerAasClient(nwm);
        NetworkManagerTest.testNetworkManager(clientNwm, "");

        LocalNetworkManagerWithParentAas desc = new LocalNetworkManagerWithParentAas();
        NetworkManagerTest.testNetworkManager(desc.createInstance(), "-desc");
        
        aasServer.stop(true);
        implServer.stop(true);
    }
}
