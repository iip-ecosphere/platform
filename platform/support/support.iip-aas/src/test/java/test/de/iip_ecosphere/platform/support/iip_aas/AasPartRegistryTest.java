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

package test.de.iip_ecosphere.platform.support.iip_aas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasBasedSetup;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor.Kind;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasMode;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.PlatformAas;
import de.iip_ecosphere.platform.support.iip_aas.SubmodelClient;

/**
 * Tests {@link AasPartRegistry}, {@link ActiveAasBase} and {@link SubmodelClient}. Do not rename, this class is 
 * referenced in {@code META-INF/services}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasPartRegistryTest {
    
    private static final String NAME_MY_AAS = "myAas";

    /**
     * A first contributor, mapped in via {@code META-INF/services}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Contributor1 implements AasContributor {
        
        @Override
        public Aas contributeTo(AasBuilder aasBuilder, InvocablesCreator iCreator) {
            aasBuilder.createSubmodelBuilder("c1", null).build();
            return null;
        }

        @Override
        public void contributeTo(ProtocolServerBuilder sBuilder) {
        }
        
        @Override
        public Kind getKind() {
            return Kind.PASSIVE;
        }
        
        @Override
        public boolean isValid() {
            return true;
        }
        
    }

    /**
     * A second contributor with own AAS, mapped in via {@code META-INF/services}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Contributor2 implements AasContributor {
        
        @Override
        public Aas contributeTo(AasBuilder aasBuilder, InvocablesCreator iCreator) {
            AasBuilder builder = AasFactory.getInstance().createAasBuilder(NAME_MY_AAS, "urn:::AAS:::myAas#");
            SubmodelBuilder smb = builder.createSubmodelBuilder("c2", null);
            smb.createPropertyBuilder("c2prop").setType(Type.STRING).setValue("a").build();
            smb.createOperationBuilder("c2op").build(Type.NONE);
            smb.build();
            return builder.build();
        }

        @Override
        public void contributeTo(ProtocolServerBuilder sBuilder) {
        }

        @Override
        public Kind getKind() {
            return Kind.PASSIVE;
        }
        
        @Override
        public boolean isValid() {
            return true;
        }

    }
    
    /**
     * A simple submodel client.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class MyAasClient extends SubmodelClient {
        
        /**
         * Creates an instance.
         * 
         * @param submodel
         */
        MyAasClient(Submodel submodel) {
            super(submodel);
            Assert.assertEquals(submodel, getSubmodel());
        }
        
        /**
         * Asserts the existence of a property, i.e., via {@link #getProperty(String)}. Usually, this would be 
         * a meaningful operation based on the property.
         * 
         * @throws ExecutionException if the property does not exist
         */
        public void assertProp() throws ExecutionException {
            try {
                getProperty("c2prop1");
                Assert.fail("No exception");
            } catch (ExecutionException e) {
                // ok
            }
            Assert.assertNotNull(getProperty("c2prop"));
            Assert.assertNotNull(getPropertyStringValue("c2prop", null));
        }

        /**
         * Asserts the existence of an operation, i.e., via {@link #getOperation(String)}. Usually, this would be 
         * a meaningful operation based on the operation.
         * 
         * @throws ExecutionException if the operation does not exist
         */
        public void assertOp() throws ExecutionException {
            try {
                getOperation("c2op1");
                Assert.fail("No exception");
            } catch (ExecutionException e) {
                // ok
            }
            Assert.assertNotNull(getOperation("c2op"));
        }
        
    }

    /**
     * Tests the part registry.
     * 
     * @throws IOException shall not occur
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testPartRegistry() throws IOException, ExecutionException {
        Assert.assertTrue(CollectionUtils.toSet(AasPartRegistry.contributors()).size() >= 2);
        Set<Class<? extends AasContributor>> cClasses = AasPartRegistry.contributorClasses();
        Assert.assertTrue(cClasses.contains(Contributor1.class));
        Assert.assertTrue(cClasses.contains(Contributor2.class));
        
        AasPartRegistry.AasBuildResult res = AasPartRegistry.build(
            c -> c.getKind() != Kind.ACTIVE || c instanceof PlatformAas);
        Assert.assertNotNull(res.getAas());
        Assert.assertEquals(2, res.getAas().size());
        Assert.assertNotNull(res.getProtocolServerBuilder());
        
        Map<String, Aas> hashedAas = new HashMap<>();
        for (Aas a : res.getAas()) {
            hashedAas.put(a.getIdShort(), a);
        }
        Assert.assertNotNull(hashedAas.get(AasPartRegistry.NAME_AAS));
        Assert.assertNotNull(hashedAas.get(NAME_MY_AAS));
        
        Assert.assertNotNull(hashedAas.get(AasPartRegistry.NAME_AAS).getSubmodel("c1"));
        Assert.assertNotNull(hashedAas.get(NAME_MY_AAS).getSubmodel("c2"));

        AasSetup oldSetup = AasPartRegistry.setAasSetup(AasSetup.createLocalEphemeralSetup());
        // no impl server here, no real active aas
        Server server = AasPartRegistry.deploy(res.getAas()).start();
        Aas deployedAas = AasPartRegistry.retrieveIipAas();
        Assert.assertNotNull(AasPartRegistry.retrieveIipAas());
        Assert.assertEquals(AasPartRegistry.NAME_AAS, deployedAas.getIdShort());
        Assert.assertNotNull(deployedAas.getSubmodel("c1"));
               
        NotificationMode oldP = ActiveAasBase.setNotificationMode(NotificationMode.SYNCHRONOUS);
        ActiveAasBase.processNotification("c1", (s, a) -> Assert.assertEquals("c1", s.getIdShort()));
        ActiveAasBase.setNotificationMode(NotificationMode.ASYNCHRONOUS);
        AtomicBoolean done = new AtomicBoolean(false);
        ActiveAasBase.processNotification("c1", (s, a) -> { 
            Assert.assertEquals("c1", s.getIdShort()); done.set(true); 
        });
        while (!done.get()) {
            TimeUtils.sleep(200);
        }

        Submodel sub = ActiveAasBase.getSubmodel(PlatformAas.NAME_SUBMODEL);
        Assert.assertNotNull(sub);
        sub = hashedAas.get(NAME_MY_AAS).getSubmodel("c2");
        MyAasClient client = new MyAasClient(sub);
        client.assertProp();
        client.assertOp();
        
        server.stop(true);
        AasPartRegistry.setAasSetup(oldSetup);
        ActiveAasBase.setNotificationMode(oldP);
    }

    /**
     * Tests the checks in {@link SubmodelClient}.
     */
    @Test
    public void testAasClientChecks() {
        SubmodelClient.checkString("ok");
        try {
            SubmodelClient.checkString("");
            Assert.fail("No Exception");
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            SubmodelClient.checkString(null);
            Assert.fail("No Exception");
        } catch (IllegalArgumentException e) {
            // ok
        }
        
        SubmodelClient.checkNotNull("ok");
        try {
            SubmodelClient.checkNotNull(null);
            Assert.fail("No Exception");
        } catch (IllegalArgumentException e) {
            // ok
        }
    }

    /**
     * Tests the {@link AasPartRegistry#getAas(List, String)} helper method.
     */
    @Test
    public void testGet() {
        List<Aas> list = new ArrayList<Aas>();
        Assert.assertNull(AasPartRegistry.getAas(list, ""));
        list.add(AasFactory.getInstance().createAasBuilder("test", "urn:::AAS:::test#").build());
        Assert.assertNull(AasPartRegistry.getAas(list, ""));
        Assert.assertNotNull(AasPartRegistry.getAas(list, "test"));
        list.add(AasFactory.getInstance().createAasBuilder("test2", "urn:::AAS:::test2#").build());
        Assert.assertNull(AasPartRegistry.getAas(list, ""));
        Assert.assertNotNull(AasPartRegistry.getAas(list, "test"));
    }
    
    /**
     * Configuration class for YAML test.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class TestSetup extends AasBasedSetup {

        private String name = "";

        /**
         * Returns the "name" of the configuration.
         * 
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Defines the "name" of the configuration.
         * 
         * @param name the name
         */
        public void setName(String name) {
            this.name = name;
        }
        
    }
    
    /**
     * Tests a setup with {@link AasSetup} using the file "aasPartRegistry.yml".
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testSetup_aasPartRegistry() throws IOException {
        TestSetup config = TestSetup.readFromYaml(TestSetup.class, "/aasPartRegistry.yml");

        Assert.assertNotNull(config);
        Assert.assertEquals("test", config.getName());

        AasSetup setup = config.getAas();
        Assert.assertNotNull(setup);
        Assert.assertNotNull(setup.getServer());
        Assert.assertNotNull(setup.getServerEndpoint());
        Assert.assertNotNull(setup.getRegistry());
        Assert.assertNotNull(setup.getMode());
        Assert.assertNotNull(setup.getRegistryEndpoint());
        Assert.assertEquals("VAB", setup.getImplementationProtocol());
        Assert.assertNotNull(setup.getImplementationServer());
        Assert.assertEquals(AasMode.REGISTER, setup.getMode());

        Assert.assertEquals(Schema.HTTP, setup.getServer().getSchema());
        Assert.assertEquals("here.de", setup.getServer().getHost());
        Assert.assertEquals(9994, setup.getServer().getPort());
        Assert.assertEquals("aas", setup.getServer().getPath());
        Assert.assertEquals(9994, setup.getServer().getServerAddress().getPort()); 
        
        Assert.assertEquals(Schema.HTTP, setup.getRegistry().getSchema());
        Assert.assertEquals("me.de", setup.getRegistry().getHost());
        Assert.assertEquals(9995, setup.getRegistry().getPort());
        Assert.assertEquals("registry", setup.getRegistry().getPath());
        Assert.assertEquals(9995, setup.getRegistry().getServerAddress().getPort()); 
        
        Assert.assertEquals(Schema.TCP, setup.getImplementation().getSchema());
        Assert.assertEquals("localhost", setup.getImplementation().getHost());
        Assert.assertEquals(10220, setup.getImplementation().getPort());
        Assert.assertEquals("VAB", setup.getImplementation().getProtocol());
        Assert.assertEquals(10220, setup.getImplementation().getServerAddress().getPort()); 
    }

    /**
     * Tests a setup for {@link AasSetup} using the file "aasPartRegistry_eph.yml".
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testSetup_aasPartRegistry_eph() throws IOException {
        TestSetup config = TestSetup.readFromYaml(TestSetup.class, "/aasPartRegistry_eph.yml");

        AasSetup setup = config.getAas();
        Assert.assertNotNull(setup);
        Assert.assertEquals(Schema.TCP, setup.getImplementation().getSchema());
        Assert.assertEquals("localhost", setup.getImplementation().getHost());
        Assert.assertTrue(setup.getImplementation().getPort() < 0); // ephemerial
        Assert.assertEquals("VAB-TCP", setup.getImplementation().getProtocol());
        Assert.assertTrue(setup.getImplementation().getServerAddress().getPort() > 0); // something selected
    }
    
}
