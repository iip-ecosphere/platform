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

package test.de.iip_ecosphere.platform.services.spring;

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.local.LocalAppDeployer;
import org.springframework.cloud.deployer.spi.local.LocalDeployerProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import de.iip_ecosphere.platform.services.ArtifactDescriptor;
import de.iip_ecosphere.platform.services.ServiceDescriptor;
import de.iip_ecosphere.platform.services.ServiceFactory;
import de.iip_ecosphere.platform.services.ServiceManager;
import de.iip_ecosphere.platform.services.ServiceState;
import de.iip_ecosphere.platform.services.ServicesAas;
import de.iip_ecosphere.platform.services.spring.SpringCloudServiceConfiguration;
import de.iip_ecosphere.platform.services.spring.SpringCloudServiceManager;
import de.iip_ecosphere.platform.services.spring.StartupApplicationListener;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import test.de.iip_ecosphere.platform.transport.mqttv5.TestHiveMqServer;

/**
 * Tests {@ink SpringCloudServiceManager}.
 * 
 * @author Holger Eichelberger, SSE
 */
@SpringBootTest(classes = TestServiceManager.Config.class)
@TestPropertySource(locations = "classpath:application.yml")
@ContextConfiguration(initializers = TestServiceManager.Initializer.class)
@Import(SpringCloudServiceConfiguration.class)
@RunWith(SpringRunner.class)
public class TestServiceManager {

    private static final ServerAddress BROKER = new ServerAddress(Schema.IGNORE); // localhost, ephemeral

    private static TestHiveMqServer server;
    private static NotificationMode oldM;
    private static Endpoint oldEp;
    private static ServerAddress oldImpl;
    private static Server implServer;
    private static Server aasServer;
    @Autowired
    private SpringCloudServiceConfiguration config;

    /**
     * Initializes the test by starting an embedded MQTT server. Requires the HiveMq configuration xml/extensions 
     * folder in src/test.
     */
    @BeforeClass
    public static void init() {
        server = new TestHiveMqServer(BROKER);
        server.start();
        
        oldM = ActiveAasBase.setNotificationMode(NotificationMode.SYNCHRONOUS);
        Assert.assertTrue(AasPartRegistry.contributorClasses().contains(ServicesAas.class));
        oldEp = AasPartRegistry.setAasEndpoint(new Endpoint(Schema.HTTP, AasPartRegistry.DEFAULT_ENDPOINT));
        oldImpl = AasPartRegistry.setProtocolAddress(new ServerAddress(Schema.TCP));
        AasPartRegistry.AasBuildResult res = AasPartRegistry.build(c -> c instanceof ServicesAas);
        
        implServer = res.getProtocolServerBuilder().build();
        implServer.start();
        aasServer = AasPartRegistry.deploy(res.getAas()); 
        aasServer.start();
    }
    
    /**
     * Shuts down client and test server.
     */
    @AfterClass
    public static void shutdown() {
        server.stop(false);
        aasServer.stop(true);
        implServer.stop(true);
        AasPartRegistry.setAasEndpoint(oldEp);
        AasPartRegistry.setProtocolAddress(oldImpl);
        ActiveAasBase.setNotificationMode(oldM);
    }
    
    /**
     * Tests a simple start-stop cycle of the {@ink SpringCloudServiceManager} with two processes. This test requires 
     * an actual version of {@code test.simpleStream.spring} in {@code target/jars} - Maven downloads the artifact 
     * in the compile phase.
     * 
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testSimpleStartStop() throws ExecutionException {
        doTestStartStop("deployment.yml", new ArtifactAsserter() {

            /*@Override
            public void testDescriptor(ArtifactDescriptor aDesc) {
                // more specific tests may go here
            }*/

        });
    }

    /**
     * Tests a simple start-stop cycle of the {@ink SpringCloudServiceManager} in one process as an ensemble. As 
     * {@link #testSimpleStartStop()}, this test requires an actual version of {@code test.simpleStream.spring}.
     * 
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testEnsembleStartStop() throws ExecutionException {
        doTestStartStop("deployment1.yml", new ArtifactAsserter() {

            /*@Override
            public void testDescriptor(ArtifactDescriptor aDesc) {
                // more specific tests may go here
            }*/

        });
    }

    /**
     * Artifact asserter interface.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class ArtifactAsserter {

        /**
         * Performs specific tests for the given descriptor.
         * 
         * @param desc the descriptor
         */
        public void testDescriptor(ArtifactDescriptor desc) {
        }

        /**
         * Specific tests for the given deployment, i.e., the running services.
         * 
         * @param desc the descriptor
         */
        public void testDeployment(ArtifactDescriptor desc) {
        }

    }
    
    /**
     * Implements the tests for a start-stop scenario with different descriptors/related asserters.
     * 
     * @param descriptorName the descriptor name
     * @param asserter the asserter related to descriptor-specific properties
     * @throws ExecutionException
     */
    private void doTestStartStop(String descriptorName, ArtifactAsserter asserter) throws ExecutionException {
        config.setDescriptorName(descriptorName);
        ServiceManager mgr = ServiceFactory.getServiceManager();
        Assert.assertTrue(mgr instanceof SpringCloudServiceManager);
        
        File f = new File("./target/jars/simpleStream.spring.jar");
        Assert.assertTrue("Test cannot be executed as " + f 
            + " does not exist. Was it downloaded by Maven?", f.exists());
        
        String aId = mgr.addArtifact(f.toURI());
        Assert.assertNotNull(aId);
        Assert.assertTrue(aId.length() > 0);
        ArtifactDescriptor aDesc = mgr.getArtifact(aId);
        Assert.assertNotNull(aDesc);
        Assert.assertTrue(mgr.getArtifactIds().contains(aId));
        Assert.assertTrue(mgr.getArtifacts().contains(aDesc));

        // commonalities for both descriptors
        Assert.assertTrue(aDesc.getServiceIds().size() == 2);
        Assert.assertTrue(aDesc.getServiceIds().contains("simpleStream-create"));
        Assert.assertTrue(aDesc.getServiceIds().contains("simpleStream-log"));
        Assert.assertTrue(aDesc.getServices().size() == 2);
        ServiceDescriptor inputService = aDesc.getService("simpleStream-create");
        ServiceDescriptor outputService = aDesc.getService("simpleStream-log");
        Assert.assertTrue(aDesc.getServices().contains(inputService));
        Assert.assertTrue(aDesc.getServices().contains(outputService));
        
        asserter.testDescriptor(aDesc);
        for (ServiceDescriptor sDesc : aDesc.getServices()) {
            Assert.assertNotNull(sDesc.getId());
            Assert.assertTrue(sDesc.getId().length() > 0);
            Assert.assertNotNull(sDesc.getVersion());
            Assert.assertNotNull(sDesc.getName());
            Assert.assertTrue(sDesc.getName().length() > 0);
            Assert.assertEquals(ServiceState.AVAILABLE, sDesc.getState());
        }

        String[] ids = new String[aDesc.getServices().size()];
        aDesc.getServiceIds().toArray(ids);
        mgr.startService(ids);

        for (ServiceDescriptor sDesc : aDesc.getServices()) {
            Assert.assertEquals("Service " + sDesc.getId() + " " + sDesc.getName() + " not running: "
                + sDesc.getState(), ServiceState.RUNNING, sDesc.getState());
        }
        
        TimeUtils.sleep(2000);

        mgr.stopService(ids);

        for (ServiceDescriptor sDesc : aDesc.getServices()) {
            Assert.assertEquals("Service " + sDesc.getId() + " " + sDesc.getName() + " not stopped: " 
                + sDesc.getState(), ServiceState.STOPPED, sDesc.getState());
        }

        asserter.testDeployment(aDesc);
        
        mgr.removeArtifact(aId);
        Assert.assertFalse(mgr.getArtifactIds().contains(aId));
        Assert.assertFalse(mgr.getArtifacts().contains(aDesc));
        Assert.assertNull(mgr.getArtifact(aId));
    }
    
    @Import(SpringCloudServiceConfiguration.class)
    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues
                .of("service-mgr.brokerPort=" + BROKER.getPort())
                .applyTo(applicationContext);
        }
        
    }
    
    /**
     * Configures the context, in particular through instances that are not loaded automatically by Spring in tests.
     * 
     * @author Holger Eichelberger, SSE
     */
    @Configuration
    @EnableConfigurationProperties(LocalDeployerProperties.class)
    public static class Config {

        /**
         * In tests, forces the {@link AppDeployer} to have an implementation.
         * 
         * @param properties the deployer properties needed to initialize the deployer instance
         * @return the deployer instance to use
         */
        @Bean
        public AppDeployer appDeployer(LocalDeployerProperties properties) {
            return new LocalAppDeployer(properties);
        }
        
        @Component
        class Startup extends StartupApplicationListener {
        }
        
    }
    
}
