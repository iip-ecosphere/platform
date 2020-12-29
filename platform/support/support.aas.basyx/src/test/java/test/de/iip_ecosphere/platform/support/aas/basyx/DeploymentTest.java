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

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe.RegistryDeploymentRecipe;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasServer;
import de.iip_ecosphere.platform.support.aas.AssetKind;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.basyx.Tomcats;

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
        //final int port = NetUtils.getEphemeralPort();
        final String registryPath = "registry";
        
        AasFactory factory = AasFactory.getInstance();
        AasBuilder aasB = factory.createAasBuilder("myAas", urn);
        aasB.createSubmodelBuilder("initial", null).build();
        Aas aas = aasB.build();
        
        ServerAddress serverAdr = new ServerAddress(Schema.HTTP);
        AasServer server = factory.createDeploymentRecipe(new Endpoint(serverAdr, ""))
            .addInMemoryRegistry(registryPath)
            .deploy(aas)
            .createServer()
            .start();
        Endpoint regEp = new Endpoint(serverAdr, registryPath);
        Registry reg = factory.obtainRegistry(regEp);
        aas = reg.retrieveAas(urn);
        Submodel sub = aas.addSubmodel("dynamic", null).build();

        server.deploy(aas, sub);

        aas = reg.retrieveAas(urn);
        Assert.assertNotNull(aas.getSubmodel("dynamic"));

        server.stop();
    }

    /**
     * Tests a dynamically deployed/removed sub-model elements collection. 
     * 
     * @throws IOException shall not occur
     * @throws ExecutionException shall not occur
     */
    @Test
    public void localDynamicSubmodelElementsCollectionDeployment() throws IOException, ExecutionException {
        final String urn = "urn:::AAS:::testMachines#";
        final ServerAddress serverAddress = new ServerAddress(Schema.HTTP);
        final String registryPath = "registry";
        
        AasFactory factory = AasFactory.getInstance();
        AasBuilder aasB = factory.createAasBuilder("myAas", urn);
        aasB.createSubmodelBuilder("sub", null).build();
        Aas aas = aasB.build();
        
        AasServer server = factory.createDeploymentRecipe(new Endpoint(serverAddress, ""))
            .addInMemoryRegistry(registryPath)
            .deploy(aas)
            .createServer()
            .start();

        Endpoint regEp = new Endpoint(serverAddress, registryPath);
        Registry reg = factory.obtainRegistry(regEp);
        aas = reg.retrieveAas(urn);
        Submodel sub = aas.getSubmodel("sub");
        Assert.assertNotNull(sub);
        SubmodelElementCollectionBuilder smcB = sub.addSubmodelElementCollection("coll", false, true);
        smcB.createPropertyBuilder("prop").setValue(Type.BOOLEAN, true).build();
        smcB.build();

        aas = reg.retrieveAas(urn);
        sub = aas.getSubmodel("sub");
        Assert.assertNotNull(sub);
        SubmodelElementCollection coll = sub.getSubmodelElementCollection("coll");
        Assert.assertNotNull(coll);
        // do not access prop, fails in BaSyx-0.1.0

        sub.delete(coll);

        aas = reg.retrieveAas(urn);
        sub = aas.getSubmodel("sub");
        Assert.assertNotNull(sub);
        coll = sub.getSubmodelElementCollection("coll");
        Assert.assertNull(coll);

        server.stop();
    }
    
    /**
     * Tests a changed attribute value on a dynamically deployed sub-model elements collection. Does not work,
     * similarly when creating the connectors component AAS.
     * 
     * @throws IOException shall not occur
     * @throws ExecutionException shall not occur
     */
    @Ignore("Fails setting value with ResourceNotFoundException -> tries to access ArrayList")
    @Test
    public void localDynamicSubmodelElementsCollectionPropertyDeployment() throws IOException, ExecutionException {
        final String urn = "urn:::AAS:::testMachines#";
        ServerAddress serverAddress = new ServerAddress(Schema.HTTP);
        final String registryPath = "registry";
        
        AasFactory factory = AasFactory.getInstance();
        AasBuilder aasB = factory.createAasBuilder("myAas", urn);
        aasB.createSubmodelBuilder("sub", null).build();
        Aas aas = aasB.build();
        
        AasServer server = factory.createDeploymentRecipe(new Endpoint(serverAddress, ""))
            .addInMemoryRegistry(registryPath)
            .deploy(aas)
            .createServer()
            .start();

        Endpoint regEp = new Endpoint(serverAddress, registryPath);
        Registry reg = factory.obtainRegistry(regEp);
        aas = reg.retrieveAas(urn);
        Submodel sub = aas.getSubmodel("sub");
        Assert.assertNotNull(sub);
        SubmodelElementCollectionBuilder smcB = sub.addSubmodelElementCollection("coll", false, false);
        smcB.createPropertyBuilder("prop").setValue(Type.BOOLEAN, true).build();
        smcB.build();

        aas = reg.retrieveAas(urn);
        sub = aas.getSubmodel("sub");
        Assert.assertNotNull(sub);
        SubmodelElementCollection coll = sub.getSubmodelElementCollection("coll");
        Assert.assertNotNull(coll);
        Property prop = coll.getProperty("prop");
        Assert.assertNotNull(prop);
        Assert.assertEquals(true, prop.getValue());

        prop.setValue(false);
        Assert.assertEquals(false, prop.getValue());

        aas = reg.retrieveAas(urn);
        sub = aas.getSubmodel("sub");
        Assert.assertNotNull(sub);
        coll = sub.getSubmodelElementCollection("coll");
        Assert.assertNotNull(coll);
        prop = coll.getProperty("prop");
        Assert.assertNotNull(prop);
        Assert.assertEquals(false, prop.getValue());

        server.stop();
    }

    /**
     * Tests a remote AAS deployment.
     */
    @Test
    public void remoteAasDeploymentTest() throws IOException {
        // adapted from org.eclipse.basyx.examples.scenarios.cloudedgedeployment.CloudEdgeDeploymentScenario
        AasFactory factory = AasFactory.getInstance();

        // start a registry server
        Endpoint regEp = new Endpoint(Schema.HTTP, "registry");
        Server regServer = factory.createRegistryServer(regEp).start();
        
        // Start target deployment server and connect to the registry
        Endpoint serverEp = new Endpoint(Schema.HTTP, "cloud");
        RegistryDeploymentRecipe regD = factory.createDeploymentRecipe(serverEp)
            .setRegistryUrl(regEp);
        Registry reg = regD.obtainRegistry();
        AasServer cloudServer = regD.createServer().start();

        // Create/Push the AAS to the cloud server
        final String aasUrn = "urn:::AAS:::oven#";
        AasBuilder aasB = factory.createAasBuilder("oven", aasUrn);
        aasB.createAssetBuilder("OvenAsset", "urn:::AAS:::ovenAsset#", AssetKind.INSTANCE).build();
        Aas aas = aasB.build();
        reg.createAas(aas, serverEp.toUri());

        // Create/Push the docuSubmodel to the cloud
        final String smUrn = "urn:::AAS:::ovenDoc#";
        SubmodelBuilder smB = factory.createSubmodelBuilder("oven_doc", smUrn);
        smB.createPropertyBuilder("max_temp").setValue(1000).build();
        reg.createSubmodel(aas, smB.build());

        assertRemoteAas(regEp, aasUrn, smUrn);

        cloudServer.stop();
        regServer.stop();
        Tomcats.clear(); // just that it is done once
    }
    
    /**
     * Asserts the remote AAS created by {@link #remoteAasDeploymentTest()}.
     * 
     * @param regEp the registry enpoint
     * @param aasUrn the AAS URN
     * @param smUrn the submodel URN
     * @throws IOException in case that obtaining the registry/receiving the AAS fails
     */
    private void assertRemoteAas(Endpoint regEp, String aasUrn, String smUrn) throws IOException {
        // could use reg from above, "simulate" access from other location
        Registry reg = AasFactory.getInstance().obtainRegistry(regEp);
        Aas aas = reg.retrieveAas(aasUrn);
        Assert.assertNotNull(aas);
        Assert.assertEquals("oven", aas.getIdShort());
        Submodel sm = reg.retrieveSubmodel(aasUrn, smUrn);
        Assert.assertNotNull(sm);
        Assert.assertEquals("oven_doc", sm.getIdShort());
        Assert.assertNotNull(sm.getProperty("max_temp"));
    }

}
