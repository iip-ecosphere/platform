package test.de.iip_ecosphere.platform.transport.spring;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import de.iip_ecosphere.platform.transport.TransportFactory;

/**
 * Brings up a spring application doing nothing rather than testing the automatic configuration of the transport
 * factory.
 * 
 * @author Holger Eichelberger, SSE
 */
@SpringBootTest(classes = TransportFactoryConfigurationTest.class)
@RunWith(SpringRunner.class)
@ComponentScan(basePackages = "de.iip_ecosphere.platform.transport.spring")
public class TransportFactoryConfigurationTest extends AbstractMockingTest {
    
    /**
     * Tests the automatic configuration of the transport factory.
     */
    @Test
    public void testTransportFactoryConfiguration() {
        SpringApplication.run(TransportFactoryConfigurationTest.class);
        Assert.assertTrue(TransportFactory.createConnector() instanceof FakeTransportConnector1);
        Assert.assertTrue(TransportFactory.createIpcConnector() instanceof FakeTransportConnector2);
        Assert.assertNotNull(TransportFactory.createDirectMemoryConnector()); // someone from before
    }

}