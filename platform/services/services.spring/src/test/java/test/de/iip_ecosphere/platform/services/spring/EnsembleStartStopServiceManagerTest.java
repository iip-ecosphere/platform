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

import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
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

import de.iip_ecosphere.platform.services.spring.SpringCloudServiceSetup;
import de.iip_ecosphere.platform.services.spring.SpringCloudServiceManager;
import de.iip_ecosphere.platform.services.spring.StartupApplicationListener;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;

/**
 * Tests {@link SpringCloudServiceManager}. We assume that the test artifacts are prepared for MQTT v3.
 * 
 * @author Holger Eichelberger, SSE
 */
@SpringBootTest(classes = EnsembleStartStopServiceManagerTest.Config.class)
@TestPropertySource(locations = "classpath:" + AbstractSetup.DEFAULT_FNAME)
@ContextConfiguration(initializers = EnsembleStartStopServiceManagerTest.Initializer.class)
@Import(SpringCloudServiceSetup.class)
@RunWith(SpringRunner.class)
public class EnsembleStartStopServiceManagerTest extends AbstractTestServiceManager {

    private static final ServerAddress BROKER = new ServerAddress(Schema.IGNORE); // localhost, ephemeral
    
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
     * Tests a simple start-stop cycle of the {@link SpringCloudServiceManager} in one process as an ensemble. As 
     * {@link #testSimpleStartStop()}, this test requires an actual version of {@code test.simpleStream.spring}.
     * 
     * @throws ExecutionException shall not occur for successful test
     * @throws IOException shall not occur for successful test
     */
    @Test
    public void testEnsembleStartStop() throws ExecutionException, IOException {
        assumeTrue(SystemUtils.IS_OS_WINDOWS); // unclear failures on Jenkins
        doTestStartStop("deployment1.yml", new ArtifactAsserter() {

            /*@Override
            public void testDescriptor(ArtifactDescriptor aDesc) {
                // more specific tests may go here
            }*/

        }, false);
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

}
