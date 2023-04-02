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
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.switching.ServiceBase;
import de.iip_ecosphere.platform.services.spring.SpringCloudServiceSetup;
import de.iip_ecosphere.platform.services.spring.SpringCloudServiceDescriptor;
import de.iip_ecosphere.platform.services.spring.SpringCloudServiceManager;
import de.iip_ecosphere.platform.services.spring.StartupApplicationListener;
import de.iip_ecosphere.platform.services.spring.descriptor.ProcessSpec;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;

/**
 * Tests {@link SpringCloudServiceManager}. We assume that the test artifacts are prepared for MQTT v3.
 * 
 * @author Holger Eichelberger, SSE
 */
@SpringBootTest(classes = SimpleStartStopServiceManagerTest.Config.class)
@TestPropertySource(locations = "classpath:" + AbstractSetup.DEFAULT_FNAME)
@ContextConfiguration(initializers = SimpleStartStopServiceManagerTest.Initializer.class)
@Import(SpringCloudServiceSetup.class)
@RunWith(SpringRunner.class)
public class SimpleStartStopServiceManagerTest extends AbstractTestServiceManager {

    private static final ServerAddress BROKER = new ServerAddress(Schema.IGNORE); // localhost, ephemeral
    private static final Function<String, String> APPID_ADAPTER = id -> ServiceBase.composeId(id, "app", "1");
    
    /**
     * Initializes the test by starting an embedded AMQP server. Requires the Qpid configuration file in src/test.
     * We do not rely on MQTT here, because Moquette is not stable enough and Hivemq requires JDK 11.
     */
    @BeforeClass
    public static void init() {
        init(BROKER);
    }
    
    /**
     * Shuts down client and test server.
     */
    @AfterClass
    public static void shutdown() {
        AbstractTestServiceManager.shutdown();
    }
    
    /**
     * A re-usable artifact asserter.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class MyArtifactAsserter extends ArtifactAsserter {

        private File homePath;
        private String createServiceId;

        /**
         * Creates an instance.
         * 
         * @param createServiceId the id of the create service to assert for
         */
        private MyArtifactAsserter(String createServiceId) {
            this.createServiceId = createServiceId;
        }

        @Override
        public void testDeployment(ArtifactDescriptor aDesc) {
            ServiceDescriptor sDesc = aDesc.getService(createServiceId);
            Assert.assertTrue(sDesc instanceof SpringCloudServiceDescriptor);
            ProcessSpec pspec = ((SpringCloudServiceDescriptor) sDesc).getSvc().getProcess();
            Assert.assertNotNull(pspec);
            Assert.assertTrue(pspec.isStarted());
            Assert.assertNotNull(pspec.getArtifacts());
            Assert.assertEquals(2, pspec.getArtifacts().size());

            homePath = pspec.getHomePath();
            Assert.assertNotNull(homePath);
            Assert.assertTrue(homePath.toString().indexOf("${tmp}") < 0); // has been substituted
            Assert.assertNotNull(pspec.getExecutablePath());
            Assert.assertTrue(pspec.getExecutablePath().toString().indexOf("${tmp}") < 0); // has been substituted
            assertFileExists(new File(homePath, "test.txt")); // extracted from artifacts
            assertFileExists(new File(homePath, "test2.txt"));
            
            sDesc = aDesc.getServer("java-server");
            Assert.assertTrue(sDesc instanceof SpringCloudServiceDescriptor);
            Assert.assertEquals(ServiceState.RUNNING, sDesc.getState());
        }
        
        @Override
        public void cleanup(ArtifactDescriptor aDesc) {
            FileUtils.deleteQuietly(homePath); // for next test

            ServiceDescriptor sDesc = aDesc.getServer("java-server");
            Assert.assertTrue(sDesc instanceof SpringCloudServiceDescriptor);
            Assert.assertEquals(ServiceState.STOPPED, sDesc.getState());
            ProcessSpec ps = ((SpringCloudServiceDescriptor) sDesc).getSvc().getProcess();
            if (null != ps && null != ps.getHomePath()) {
                FileUtils.deleteQuietly(ps.getHomePath()); // for next test
            }
        }

    }

    /**
     * Tests a simple start-stop cycle of the {@link SpringCloudServiceManager} with two processes. This test requires 
     * an actual version of {@code test.simpleStream.spring} in {@code target/jars}. Maven downloads the artifact 
     * in the compile phase.
     * 
     * @throws ExecutionException shall not occur for successful test
     * @throws IOException shall not occur for successful test
     */
    @Test
    public void testSimpleStartStop() throws ExecutionException, IOException {
        doTestStartStop("deployment.yml", new MyArtifactAsserter("simpleStream-create"), false);
    }

    /**
     * Tests a simple start-stop cycle of the {@link SpringCloudServiceManager} with two processes and a given 
     * app/instance id. This test requires an actual version of {@code test.simpleStream.spring} in {@code target/jars}.
     * Maven downloads the artifact in the compile phase.
     * 
     * @throws ExecutionException shall not occur for successful test
     * @throws IOException shall not occur for successful test
     */
    @Test
    public void testSimpleStartStopAppId() throws ExecutionException, IOException {
        doTestStartStop("deployment.yml", new MyArtifactAsserter(APPID_ADAPTER.apply("simpleStream-create")), 
            false, APPID_ADAPTER);
    }

    /**
     * Initializes/modifies the spring setup.
     * 
     * @author Holger Eichelberger, SSE
     */
    @Import(SpringCloudServiceSetup.class)
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
    
    /**
     * Tests known/default values in setup.
     */
    @Test
    public void testSetup() {
        Assert.assertNotNull(getConfig().getJavaOpts());
        Assert.assertTrue(getConfig().getJavaOpts().size() > 0);
    }

}
