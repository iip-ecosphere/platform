package test.de.iip_ecosphere.platform.services.spring;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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

import de.iip_ecosphere.platform.services.spring.SpringCloudServiceSetup;
import de.iip_ecosphere.platform.services.spring.StartupApplicationListener;
import de.iip_ecosphere.platform.services.spring.YamlPropertySource;
import de.iip_ecosphere.platform.support.setup.AbstractSetup;

@SpringBootTest(classes = SetupTest.Config.class)
@TestPropertySource(locations = "classpath:" + AbstractSetup.DEFAULT_FNAME)
@ContextConfiguration(initializers = SetupTest.Initializer.class)
@Import(SpringCloudServiceSetup.class)
@RunWith(SpringRunner.class)
public class SetupTest {

    @Autowired
    private SpringCloudServiceSetup config;

    /**
     * Tests the setup. 
     */
    @Test
    public void testSetup() {        
        Assert.assertNotNull(config);
        // test some properties that shall be there
        Assert.assertEquals("target/plugins", config.getPluginsFolder());
        Assert.assertEquals(30000, config.getWaitingTime());
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
            applicationContext.getEnvironment().getPropertySources().addLast(new YamlPropertySource());
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
