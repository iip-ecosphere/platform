package test.de.iip_ecosphere.platform.transport;

import java.io.IOException;

import org.junit.Test;

import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.TransportFactory.TransportFactoryImplementation;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;

public class DirectMemoryTransportConnectorTest {

    /**
     * Tests the connector through explicitly setting/resetting the factory
     * implementation. Builds up a {@link TestHiveMqServer} so that the test is
     * self-contained.
     * 
     * @throws IOException in case that connection/communication fails
     */
    @Test
    public void testMemoryConnector() throws IOException {
        // just for the test as it is based on the factory
        TransportFactoryImplementation old = TransportFactory
            .setFactoryImplementation(new TransportFactoryImplementation() {

                @Override
                public TransportConnector createConnector() {
                    return TransportFactory.createDirectMemoryConnector();
                }
            });
        
        AbstractTransportConnectorTest.doTest("", 0, new ProductJsonSerializer());
        AbstractTransportConnectorTest.doTest("", 0, new ProductProtobufSerializer());
        
        TransportFactory.setFactoryImplementation(old);
    }

}
