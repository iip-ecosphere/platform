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

package test.de.iip_ecosphere.platform.support.aas;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe.RegistryDeploymentRecipe;
import de.iip_ecosphere.platform.support.aas.IdentityStoreAuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasPrintVisitor;
import de.iip_ecosphere.platform.support.aas.AasServer;
import de.iip_ecosphere.platform.support.aas.AssetKind;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.BasicSetupSpec;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.Invokable.GetterInvokable;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.LocalPersistenceType;
import de.iip_ecosphere.platform.support.aas.SetupSpec;
import de.iip_ecosphere.platform.support.aas.SetupSpec.AasComponent;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;

/**
 * Tests deployment scenarios.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DeploymentTest {
    
    /**
     * Tests a local dynamic sub-model deployment.
     * 
     * @throws IOException shall not occur if the test works
     * @throws ExecutionException shall not occur
     */
    @Test
    public void localSubmodelDynamicDeployment() throws IOException, ExecutionException {
        final String urn = "urn:::AAS:::testMachines#";
        
        AasFactory factory = AasFactory.getInstance();
        AasBuilder aasB = factory.createAasBuilder("myAas", urn);
        aasB.createSubmodelBuilder("initial", null).defer();
        Aas aas = aasB.build();
        
        ServerAddress serverAdr = new ServerAddress(Schema.HTTP);
        Endpoint regEp = createDependentEndpoint(serverAdr, "registry");
        BasicSetupSpec spec = new BasicSetupSpec(regEp, serverAdr);
        System.out.println(spec);
        AasServer server = factory.createDeploymentRecipe(spec)
            .forRegistry()
            .deploy(aas)
            .createServer()
            .start();
        Assert.assertTrue(factory.isAvailable(spec, AasComponent.AAS_REPOSITORY));
        Registry reg = factory.obtainRegistry(spec);
        Assert.assertTrue(factory.isAvailable(spec, AasComponent.AAS_REGISTRY));
        aas = reg.retrieveAas(urn);
        Submodel sub = aas.createSubmodelBuilder("dynamic", null).build();
        server.deploy(aas, sub);

        aas = reg.retrieveAas(urn);
        Assert.assertNotNull(aas.getSubmodel("dynamic"));
        
        SubmodelBuilder smb = aas.createSubmodelBuilder("dynamic", null);
        smb.createPropertyBuilder("prop").setValue(Type.STRING, "bb").build();
        smb.defer(); // this defer/buildDeferred is useless here, but tests the mechanism on AAS level
        aas.buildDeferred();

        aas = reg.retrieveAas(urn);
        sub = aas.getSubmodel("dynamic");
        Assert.assertNotNull(sub);
        Property prop = sub.getProperty("prop");
        Assert.assertNotNull(prop);
        Assert.assertEquals("bb", prop.getValue());

        server.stop(true);
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
        Endpoint regEp = createDependentEndpoint(serverAddress, "registry");
        BasicSetupSpec spec = new BasicSetupSpec(regEp, serverAddress);
        System.out.println(spec);

        AasFactory factory = AasFactory.getInstance();
        AasBuilder aasB = factory.createAasBuilder("myAas", urn);
        aasB.createSubmodelBuilder("sub", null).build();
        Aas aas = aasB.build();
        
        AasServer server = factory.createDeploymentRecipe(spec)
            .forRegistry()
            .deploy(aas)
            .createServer()
            .start();

        Registry reg = factory.obtainRegistry(spec);
        aas = reg.retrieveAas(urn, true);
        Submodel sub = aas.getSubmodel("sub");
        Assert.assertNotNull(sub);
        SubmodelElementCollectionBuilder smcB = sub.createSubmodelElementCollectionBuilder("coll", false, true);
        smcB.createPropertyBuilder("prop").setValue(Type.BOOLEAN, true).build();
        smcB.build();

        aas = reg.retrieveAas(urn, true);
        Aas aas1 = reg.retrieveAas(urn); // snapshot
        
        sub = aas.getSubmodel("sub");
        Assert.assertNotNull(sub);
        SubmodelElementCollection coll = sub.getSubmodelElementCollection("coll");
        Assert.assertNotNull(coll);

        SubmodelElementCollection coll1 = aas1.getSubmodel("sub").getSubmodelElementCollection("coll");
        Assert.assertNull(coll1.getProperty("prop1")); // does not exist, not yet created (here, forces init)
        smcB = sub.createSubmodelElementCollectionBuilder("coll", false, true);
        smcB.createPropertyBuilder("prop1").setValue(Type.BOOLEAN, true).build();
        smcB.build();
        Assert.assertNull(coll1.getProperty("prop1")); // exists in other instance, e.g., other process
        coll1.update(); // force update
        Assert.assertNotNull(coll1.getProperty("prop1")); // there it is

        sub.deleteElement(coll);

        aas = reg.retrieveAas(urn);
        sub = aas.getSubmodel("sub");
        Assert.assertNotNull(sub);
        coll = sub.getSubmodelElementCollection("coll");
        Assert.assertNull(coll);

        server.stop(true);
    }
    
    /**
     * Tests a changed attribute value on a dynamically deployed sub-model elements collection. Does not work,
     * similarly when creating the connectors component AAS.
     * 
     * @throws IOException shall not occur
     * @throws ExecutionException shall not occur
     */
    @Test
    public void localDynamicSubmodelElementsCollectionPropertyDeployment() throws IOException, ExecutionException {
        final String urn = "urn:::AAS:::testMachines#";
        ServerAddress serverAddress = new ServerAddress(Schema.HTTP);
        Endpoint regEp = createDependentEndpoint(serverAddress, "registry");
        BasicSetupSpec spec = new BasicSetupSpec(regEp, serverAddress);
        System.out.println(spec);
        
        AasFactory factory = AasFactory.getInstance();
        AasBuilder aasB = factory.createAasBuilder("myAas", urn);
        aasB.createSubmodelBuilder("sub", null).build();
        Aas aas = aasB.build();
        
        AasServer server = factory.createDeploymentRecipe(spec)
            .forRegistry()
            .deploy(aas)
            .createServer()
            .start();

        Registry reg = factory.obtainRegistry(spec);
        aas = reg.retrieveAas(urn);
        Submodel sub = aas.getSubmodel("sub");
        Assert.assertNotNull(sub);
        SubmodelElementCollectionBuilder smcB = sub.createSubmodelElementCollectionBuilder("coll", false, false);
        Assert.assertTrue(smcB.isNew());
        smcB.createPropertyBuilder("prop").setValue(Type.BOOLEAN, true).build();
        Utils.setValue(smcB.createPropertyBuilder("prop2").setType(Type.INTEGER), 5,
            (GetterInvokable & Serializable) () -> 5, InvocablesCreator.READ_ONLY).build();
        smcB.build();

        aas = reg.retrieveAas(urn);
        sub = aas.getSubmodel("sub");
        Assert.assertNotNull(sub);
        SubmodelElementCollection coll = sub.getSubmodelElementCollection("coll");
        Assert.assertNotNull(coll);
        assertProp(coll);
        assertProp2(coll);

        // add dynamically, no sub-model builder available, call buildDeferred at the end
        smcB = sub.createSubmodelElementCollectionBuilder("coll", false, false);
        smcB.createPropertyBuilder("prop2").setValue(Type.STRING, "aa").build();
        SubmodelElementCollectionBuilder smcBI 
            = smcB.createSubmodelElementCollectionBuilder("coll_inner", false, false);
        smcBI.createPropertyBuilder("prop2I").setValue(Type.STRING, "ab").build();
        smcBI.defer();
        smcB.defer();
        smcB = sub.createSubmodelElementCollectionBuilder("coll", false, false);
        smcB.createPropertyBuilder("prop3").setValue(Type.STRING, "bb").build();
        smcBI = smcB.createSubmodelElementCollectionBuilder("coll_inner", false, false);
        smcBI.createPropertyBuilder("prop3I").setValue(Type.STRING, "ac").build();
        smcBI.defer();
        smcB.buildDeferred();
        aas.accept(new AasPrintVisitor());
        
        aas = reg.retrieveAas(urn);
        sub = aas.getSubmodel("sub");
        Assert.assertNotNull(sub);
        coll = sub.getSubmodelElementCollection("coll");
        Assert.assertNotNull(coll);
        Property prop = coll.getProperty("prop");
        Assert.assertNotNull(prop);
        Assert.assertEquals(false, prop.getValue());

        prop = coll.getProperty("prop2");
        Assert.assertNotNull(prop);
        Assert.assertEquals("aa", prop.getValue());
        prop = coll.getProperty("prop3");
        Assert.assertNotNull(prop);
        Assert.assertEquals("bb", prop.getValue());

        SubmodelElementCollection collInner = coll.getSubmodelElementCollection("coll_inner");
        Assert.assertNotNull(collInner);
        prop = collInner.getProperty("prop2I");
        Assert.assertNotNull(prop);
        Assert.assertEquals("ab", prop.getValue());
        prop = collInner.getProperty("prop3I");
        Assert.assertNotNull(prop);
        Assert.assertEquals("ac", prop.getValue());
        
        server.stop(true);
    }

    /**
     * Asserts the expected value of a boolean property before/after change.
     * 
     * @param coll the parent collection
     * @throws ExecutionException if access fails
     */
    private void assertProp(SubmodelElementCollection coll) throws ExecutionException {
        Property prop = coll.getProperty("prop");
        Assert.assertNotNull(prop);
        Assert.assertEquals(true, prop.getValue());
        prop.setValue(false);
        Assert.assertEquals(false, prop.getValue());
    }

    /**
     * Asserts the expected value of an umodifialbe integer property before/after change.
     * 
     * @param coll the parent collection
     * @throws ExecutionException if access fails
     */
    private void assertProp2(SubmodelElementCollection coll) throws ExecutionException {
        Property prop2 = coll.getProperty("prop2");
        Assert.assertNotNull(prop2);
        Assert.assertEquals(5, prop2.getValue());
        prop2.setValue(8);
        Utils.assertIfPropertyFunction(prop2, 5, 8); // if: cannot overwrite as assigned getter
    }

    /**
     * Tests a remote AAS HTTP deployment.
     * 
     * @throws IOException shall not occur if the test works
     */
    @Test
    public void remoteAasDeploymentTest() throws IOException {
        remoteAasDeploymentTestImpl(Schema.HTTP, null, null);
    }
    
    /**
     * Tests a remote AAS HTTPS deployment, without authentication.
     * 
     * @throws IOException shall not occur if the test works
     */
    @Test
    public void remoteAasSslDeploymentTest() throws IOException {
        remoteAasSslDeploymentTest(null);
    }

    /**
     * Tests a remote AAS HTTPS deployment, without authentication.
     * 
     * @throws IOException shall not occur if the test works
     */
    @Test
    public void remoteAasSslDeploymentAuthTest() throws IOException {
        if (AasFactory.getInstance().supportsAuthentication()) {
            remoteAasSslDeploymentTest(new IdentityStoreAuthenticationDescriptor());
        }
    }
    
    /**
     * Tests a remote AAS HTTPS deployment.
     * 
     * @param aDesc the optional authentication descriptor, may be <b>null</b> for none
     * 
     * @throws IOException shall not occur if the test works
     */
    private void remoteAasSslDeploymentTest(AuthenticationDescriptor aDesc) throws IOException {
        File keyPath = new File("./src/test/resources/keystore.jks");
        remoteAasDeploymentTestImpl(Schema.HTTPS, new KeyStoreDescriptor(keyPath, "a1234567", "tomcat"), null);
    }
    
    /**
     * Adapts the registry schema if needed.
     * 
     * @param schema the schema
     * @return {@code schema}
     */
    protected Schema adaptRegistrySchema(Schema schema) {
        return schema;
    }
    
    /**
     * Creates a dependent endpoint. May have to be overridden for more recent implementations.
     * 
     * @param address the address
     * @param endpoint the endpoint
     * @return the created enpoint
     */
    protected Endpoint createDependentEndpoint(ServerAddress address, String endpoint) {
        return new Endpoint(address, endpoint);
    }
    
    /**
     * Tests a remote AAS deployment.
     * 
     * @param schema the schema for the servers
     * @param kstore the key store descriptor, ignored if <b>null</b>
     * @param aDesc the authentication descriptor, ignored if <b>null</b>
     * 
     * @throws IOException shall not occur if the test works
     */
    private void remoteAasDeploymentTestImpl(Schema schema, KeyStoreDescriptor kstore, AuthenticationDescriptor aDesc)
        throws IOException {
        // adapted from org.eclipse.basyx.examples.scenarios.cloudedgedeployment.CloudEdgeDeploymentScenario
        AasFactory factory = AasFactory.getInstance();

        ServerRecipe srcp = factory.createServerRecipe();
        
        Endpoint serverEp = new Endpoint(schema, "cloud");
        Endpoint regEp = new Endpoint(adaptRegistrySchema(schema), "registry");
        BasicSetupSpec spec = new BasicSetupSpec(regEp, serverEp, kstore, aDesc);
        System.out.println(spec);
        // start a registry server
        Server regServer = srcp.createRegistryServer(spec, LocalPersistenceType.INMEMORY).start();
        // Start target deployment server and connect to the registry
        RegistryDeploymentRecipe regD = factory.createDeploymentRecipe(spec)
            .forRegistry(regEp);
        Registry reg = regD.obtainRegistry();
        AasServer cloudServer = regD.createServer().start();

        // Create/Push the AAS to the cloud server
        final String aasUrn = "urn:::AAS:::oven#";
        AasBuilder aasB = factory.createAasBuilder("oven", aasUrn);
        aasB.createAssetBuilder("OvenAsset", "urn:::AAS:::ovenAsset#", AssetKind.INSTANCE).build();
        Aas aas = aasB.build();
        reg.createAas(aas, serverEp.toUri());

        // Create/Push the docuSubmodel to the cloud
        // first variant: factory-based standalone submodel assigned to AAS
        final String smUrn = "urn:::AAS:::ovenDoc#";
        SubmodelBuilder smB = factory.createSubmodelBuilder("oven_doc", smUrn);
        smB.createPropertyBuilder("max_temp").setValue(1000).build();
        reg.createSubmodel(aas, smB.build());
        
        // second variant: AAS-based submodel, registry is known through AAS
        final String smUrn2 = "urn:::AAS:::ovenDoc2#";
        smB = aas.createSubmodelBuilder("oven_doc2", smUrn2);
        smB.createPropertyBuilder("max_temp").setValue(1000).build();
        smB.build();
        
        assertRemoteAas(spec, aasUrn, "oven_doc", smUrn, serverEp);
        assertRemoteAas(spec, aasUrn, "oven_doc2", smUrn2, serverEp);

        cloudServer.stop(true);
        regServer.stop(true);
    }
    
    /**
     * Asserts the remote AAS created by {@link #remoteAasDeploymentTest()}.
     * 
     * @param spec the setup specification
     * @param aasUrn the AAS URN
     * @param submName the name of the submodel to assert for
     * @param smUrn the submodel URN
     * @param aasEp the endpoint of the AAS server
     * @throws IOException in case that obtaining the registry/receiving the AAS fails
     */
    private void assertRemoteAas(SetupSpec spec, String aasUrn, String submName, String smUrn, Endpoint aasEp) 
        throws IOException {
        // could use reg from above, "simulate" access from other location
        Registry reg = AasFactory.getInstance().obtainRegistry(spec, aasEp.getSchema());
        Aas aas = reg.retrieveAas(aasUrn);
        Assert.assertNotNull(aas);
        Assert.assertEquals("oven", aas.getIdShort());
        Submodel sm = reg.retrieveSubmodel(aasUrn, smUrn);
        Assert.assertNotNull(sm);
        Assert.assertEquals(submName, sm.getIdShort());
        Assert.assertNotNull(sm.getProperty("max_temp"));
    }

}
