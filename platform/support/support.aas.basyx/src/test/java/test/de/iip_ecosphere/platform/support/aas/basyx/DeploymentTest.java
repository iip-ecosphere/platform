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
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasServer;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.Type;

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
        final int port = NetUtils.getEphemeralPort();
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
    
    /**
     * Tests a changed attribute value on a dynamically deployed sub-model elements collection. Does not work,
     * similarly when creating the connectors component AAS.
     * 
     * @throws IOException shall not occur
     * @throws ExecutionException shall not occur
     */
    @Ignore("Fails setting value with ResourceNotFoundException although there")
    @Test
    public void localDynamicSubmodelElementsCollectionPropertyDeployment() throws IOException, ExecutionException {
        final String urn = "urn:::AAS:::testMachines#";
        final String host = "localhost";
        final int port = NetUtils.getEphemeralPort();
        final String registryPath = "registry";
        
        AasFactory factory = AasFactory.getInstance();
        AasBuilder aasB = factory.createAasBuilder("myAas", urn);
        SubmodelBuilder smB = aasB.createSubmodelBuilder("sub");
        smB.build();
        Aas aas = aasB.build();
        
        AasServer server = factory.createDeploymentRecipe(host, port)
            .addInMemoryRegistry(registryPath)
            .deploy(aas)
            .createServer();
        server.start(2000);

        aas = factory.retrieveAas(host, port, registryPath, urn);
        Submodel sub = aas.getSubmodel("sub");
        Assert.assertNotNull(sub);
        SubmodelElementCollectionBuilder smcB = sub.addSubmodelElementCollection("coll", false, false);
        smcB.createPropertyBuilder("prop").setValue(Type.BOOLEAN, true).build();
        smcB.build();

        aas = factory.retrieveAas(host, port, registryPath, urn);
        sub = aas.getSubmodel("sub");
        Assert.assertNotNull(sub);
        SubmodelElementCollection coll = sub.getSubmodelElementCollection("coll");
        Assert.assertNotNull(coll);
        Property prop = coll.getProperty("prop");
        Assert.assertNotNull(prop);
        Assert.assertEquals(true, prop.getValue());

        prop.setValue(false);
        Assert.assertEquals(false, prop.getValue());

        aas = factory.retrieveAas(host, port, registryPath, urn);
        sub = aas.getSubmodel("sub");
        Assert.assertNotNull(sub);
        coll = sub.getSubmodelElementCollection("coll");
        Assert.assertNotNull(coll);
        prop = coll.getProperty("prop");
        Assert.assertNotNull(prop);
        Assert.assertEquals(false, prop.getValue());

        server.stop();

    }

}
