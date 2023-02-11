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
import java.util.function.Supplier;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
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

import de.iip_ecosphere.platform.services.ServiceFactory;
import de.iip_ecosphere.platform.services.ServiceManager;
import de.iip_ecosphere.platform.services.spring.SpringCloudServiceSetup;
import de.iip_ecosphere.platform.services.spring.SpringCloudServiceManager;
import de.iip_ecosphere.platform.services.spring.StartupApplicationListener;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;
import de.iip_ecosphere.platform.support.net.LocalNetworkManagerImpl;
import de.iip_ecosphere.platform.support.net.NetworkManager;

/**
 * Tests {@link SpringCloudServiceManager}. We assume that the test artifacts are prepared for MQTT v3.
 * 
 * @author Holger Eichelberger, SSE
 */
@SpringBootTest(classes = ServerStartStopServiceManagerTest.Config.class)
@TestPropertySource(locations = "classpath:" + AbstractSetup.DEFAULT_FNAME)
@ContextConfiguration(initializers = ServerStartStopServiceManagerTest.Initializer.class)
@Import(SpringCloudServiceSetup.class)
@RunWith(SpringRunner.class)
public class ServerStartStopServiceManagerTest extends AbstractTestServiceManager {

    
    /**
     * Shuts down client and test server.
     */
    @AfterClass
    public static void shutdown() {
        AbstractTestServiceManager.shutdown();
    }
    
    /**
     * Tests a simple start-stop cycle of the {@link SpringCloudServiceManager} with two processes. This test requires 
     * an actual version of {@code test.simpleStream.spring} in {@code target/jars} - Maven downloads the artifact 
     * in the compile phase.
     * 
     * @throws ExecutionException shall not occur for successful test
     * @throws IOException shall not occur for successful test
     */
    @Test
    public void testSimpleStartStop() throws ExecutionException, IOException {
        NotificationMode oldM = ActiveAasBase.setNotificationMode(NotificationMode.NONE);
        getConfig().setDescriptorName("deployment.yml");
        ServiceManager mgr = ServiceFactory.getServiceManager();
        Assert.assertTrue(mgr instanceof SpringCloudServiceManager);
        SpringCloudServiceManager sMgr = (SpringCloudServiceManager) mgr;
        sMgr.clear(); // start test in fair conditions
        Supplier<NetworkManager> oldSupp = sMgr.setNetworkManagerClientSupplier(() -> new LocalNetworkManagerImpl());
        File f = new File("./src/test/resources/serverStartStop.jar");
        Assert.assertTrue("Test cannot be executed as " + f + " does not exist.", f.exists());
        String aId = mgr.addArtifact(f.toURI());
        sMgr.startService(); // shall start servers
        sMgr.stopService(); // shall stop servers
        sMgr.removeArtifact(aId);
        sMgr.setNetworkManagerClientSupplier(oldSupp); 
        ActiveAasBase.setNotificationMode(oldM);
        // used deployment.yml shall cause start/stop of TestServer
        Assert.assertEquals(1, TestServer.getAndResetCreatedCount()); 
        Assert.assertEquals(1, TestServer.getAndResetStoppedCount()); 
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
