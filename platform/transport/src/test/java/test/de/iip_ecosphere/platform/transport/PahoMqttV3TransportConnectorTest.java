package test.de.iip_ecosphere.platform.transport;

import java.io.IOException;

import org.junit.Test;

import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.TransportFactory.TransportFactoryImplementation;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.impl.PahoMqttV3TransportConnector;

/**
 * Tests the {@link PahoMqttV3TransportConnector}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PahoMqttV3TransportConnectorTest {

    /**
     * Tests the connector through explicitly setting/resetting the factory
     * implementation. Builds up a {@link TestHiveMqServer} so that the test is
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
                        return new PahoMqttV3TransportConnector();
                    }
                });

        final int port = 8883;
        TestHiveMqServer server = new TestHiveMqServer();
        server.start("localhost", port);
        AbstractTransportConnectorTest.doTest("localhost", port, new ProductJsonSerializer());
        AbstractTransportConnectorTest.doTest("localhost", port, new ProductProtobufSerializer());
        server.stop();
        TransportFactory.setFactoryImplementation(old);
    }

}
