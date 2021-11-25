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

package test.de.iip_ecosphere.platform.support.fakeAas;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AssetKind;
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.PersistenceRecipe;
import de.iip_ecosphere.platform.support.aas.ProtocolDescriptor;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.LocalPersistenceType;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.Type;

/**
 * Tests the factory/descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FactoryTest {
    
    /**
     * Creates a factory instance that does nothing. The instance delegates to {@link AasFactory#DUMMY} to keep
     * this instance in the testing loop.
     * 
     * @return the factory instance
     */
    public static AasFactory createDisabledFactory() {
        return new AasFactory() {
            
            @Override
            public String getName() {
                return DUMMY.getName();
            }

            @Override
            public SubmodelBuilder createSubmodelBuilder(String idShort, String urn) {
                return DUMMY.createSubmodelBuilder(idShort, urn);
            }
            
            @Override
            protected ServerRecipe createDefaultServerRecipe() {
                return new FakeServerReceipe();
            }

            @Override
            public Registry obtainRegistry(Endpoint regEndpoint) throws IOException {
                return DUMMY.obtainRegistry(regEndpoint);
            }

            @Override
            public Registry obtainRegistry(Endpoint regEndpoint, Schema aasSchema) throws IOException {
                return DUMMY.obtainRegistry(regEndpoint, aasSchema);
            }

            @Override
            public DeploymentRecipe createDeploymentRecipe(Endpoint endpoint) {
                return DUMMY.createDeploymentRecipe(endpoint);
            }
            
            @Override
            public DeploymentRecipe createDeploymentRecipe(Endpoint endpoint, File keyPath, String keyPass) {
                return DUMMY.createDeploymentRecipe(endpoint, keyPath, keyPass);
            }
            
            @Override
            public AasBuilder createAasBuilder(String idShort, String urn) {
                return DUMMY.createAasBuilder(idShort, urn);
            }

            @Override
            public PersistenceRecipe createPersistenceRecipe() {
                return DUMMY.createPersistenceRecipe();
            }

            @Override
            public String[] getProtocols() {
                return DUMMY.getProtocols();
            }

            @Override
            public InvocablesCreator createInvocablesCreator(String protocol, String host, int port) {
                return DUMMY.createInvocablesCreator(protocol, host, port);
            }

            @Override
            public ProtocolServerBuilder createProtocolServerBuilder(String protocol, int port) {
                return DUMMY.createProtocolServerBuilder(protocol, port);
            }
            
            @Override
            public String fixId(String id) {
                return DUMMY.fixId(id);
            }
            
            @Override
            protected boolean accept(ProtocolDescriptor creator) {
                return true; // allow the fake test protocol creator for testing
            }

        };
        
    }
    
    /**
     * Tests that there is a fake factory through Java Service loader.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testFakeFactory() throws IOException {
        AasFactory instance = AasFactory.getInstance();
        Assert.assertNotNull(instance);

        // it's just a fake
        Assert.assertEquals("fake", instance.getName());
        Assert.assertNotNull(instance.createAasBuilder("", ""));
        Assert.assertNotNull(instance.createSubmodelBuilder("", ""));

        Endpoint ep = new Endpoint(Schema.HTTP, "", 1234, "");
        ServerRecipe serverRecipe = instance.createServerRecipe();
        Assert.assertNotNull(serverRecipe);
        Endpoint regEp = new Endpoint(ep, "/registry");
        Assert.assertNull(serverRecipe.createRegistryServer(regEp, LocalPersistenceType.INMEMORY, ""));
        Assert.assertNull(serverRecipe.createAasServer(new Endpoint(ep, "/aas"), LocalPersistenceType.INMEMORY, regEp));
        Assert.assertNull(instance.obtainRegistry(ep));
        Assert.assertNull(instance.obtainRegistry(ep, Schema.HTTPS));
        Assert.assertNull(instance.createDeploymentRecipe(ep));

        Assert.assertNull(instance.createPersistenceRecipe());
        
        Assert.assertNotNull(instance.getProtocols());
        Assert.assertTrue(instance.getProtocols().length > 0);
        Assert.assertNotNull(instance.createInvocablesCreator(AasFactory.DEFAULT_PROTOCOL, "localhost", 123));
        Assert.assertNotNull(instance.createProtocolServerBuilder(AasFactory.DEFAULT_PROTOCOL, 123));
        
        Assert.assertEquals("id", instance.fixId("id"));
        
        Assert.assertEquals(LocalPersistenceType.INMEMORY, serverRecipe.toPersistenceType("")); // fallback
        Assert.assertEquals(LocalPersistenceType.INMEMORY, 
            serverRecipe.toPersistenceType(LocalPersistenceType.INMEMORY.name()));
    }

    /**
     * Tests explicitly setting the dummy factory.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testDummyFactory() throws IOException {
        AasFactory old = AasFactory.setInstance(createDisabledFactory()); // otherwise service loader is consulted

        AasFactory instance = AasFactory.getInstance();
        Assert.assertNotNull(instance);
        Assert.assertEquals(AasFactory.DUMMY.getName(), instance.getName());

        Assert.assertNull(instance.createAasBuilder("", ""));
        Assert.assertNull(instance.createSubmodelBuilder("", ""));

        Endpoint ep = new Endpoint(Schema.HTTP, "", 1234, "");
        ServerRecipe serverRecipe = instance.createServerRecipe();
        Assert.assertNotNull(serverRecipe);
        Endpoint regEp = new Endpoint(ep, "/registry");
        Assert.assertNull(serverRecipe.createRegistryServer(regEp, LocalPersistenceType.INMEMORY, ""));
        Assert.assertNull(serverRecipe.createAasServer(new Endpoint(ep, "/aas"), LocalPersistenceType.INMEMORY, regEp));
        assertRegistry(instance.obtainRegistry(ep));
        Assert.assertNull(instance.createDeploymentRecipe(ep));
        Assert.assertNull(instance.createDeploymentRecipe(ep, new File("."), "xxx"));

        Assert.assertNull(instance.createPersistenceRecipe());

        Assert.assertNotNull(instance.getProtocols());
        Assert.assertTrue(instance.getProtocols().length > 0);
        Assert.assertNotNull(instance.createInvocablesCreator(AasFactory.DEFAULT_PROTOCOL, "localhost", 123));
        Assert.assertNotNull(instance.createProtocolServerBuilder(AasFactory.DEFAULT_PROTOCOL, 123));

        AasFactory.setInstance(old);
    }
    
    /**
     * Asserts a default/dummy registry instance.
     * 
     * @param reg the registry instance
     * @throws IOException shall not occur
     */
    private static void assertRegistry(Registry reg) throws IOException {
        Assert.assertNotNull(reg);
        Assert.assertNull(reg.retrieveAas(""));
        reg.createAas(null, "");
        reg.createSubmodel(null, null);
        reg.register(null, null, "");
        Assert.assertNull(reg.retrieveSubmodel("", ""));
    }
    
    /**
     * Asserting plain constants is ridiculous. Real function happens in implementation components.
     */
    @Test
    public void assertConstants() {
        for (Type t : Type.values()) {
            Assert.assertNotNull(t);
        }
        for (AssetKind k : AssetKind.values()) {
            Assert.assertNotNull(k);
        }
    }

}
