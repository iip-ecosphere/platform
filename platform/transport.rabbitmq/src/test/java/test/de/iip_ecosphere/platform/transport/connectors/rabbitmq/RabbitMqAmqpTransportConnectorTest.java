package test.de.iip_ecosphere.platform.transport.connectors.rabbitmq;

import java.io.IOException;

import org.junit.Test;

import com.rabbitmq.client.ConnectionFactory;

import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.TransportFactory.TransportFactoryImplementation;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.rabbitmq.RabbitMqAmqpTransportConnector;
import test.de.iip_ecosphere.platform.transport.AbstractTransportConnectorTest;
import test.de.iip_ecosphere.platform.transport.ProductJsonSerializer;
import test.de.iip_ecosphere.platform.transport.ProductProtobufSerializer;

/**
 * Tests the {@link RabbitMqAmqpTransportConnector}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class RabbitMqAmqpTransportConnectorTest {

    /**
     * An extended AMQP connector with fixed plaintext authentication (see src/test/config.json).
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class FakeAuthConnector extends RabbitMqAmqpTransportConnector {

        @Override
        protected void configureFactory(ConnectionFactory factory) {
            factory.setUsername("user");
            factory.setPassword("pwd");
        }

    }
    
    /**
     * Tests the connector through explicitly setting/resetting the factory
     * implementation. Builds up a {@link TestQpidServer} so that the test is
     * self-contained.
     * 
     * @throws IOException in case that connection/communication fails
     */
    @Test
    public void testPahoConnector() throws IOException {
        TransportFactoryImplementation old = TransportFactory
            .setFactoryImplementation(new TransportFactoryImplementation() {

                @Override
                public TransportConnector createConnector() {
                    return new FakeAuthConnector();
                }
            });

        final int port = 8883;
        TestQpidServer server = new TestQpidServer();
        server.start("localhost", port);
        AbstractTransportConnectorTest.doTest("localhost", port, new ProductJsonSerializer());
        AbstractTransportConnectorTest.doTest("localhost", port, new ProductProtobufSerializer());
        server.stop();
        TransportFactory.setFactoryImplementation(old);        
    }
    
}
