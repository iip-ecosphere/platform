/********************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/
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

                /**
                 * Creates an inter-process connector.
                 * 
                 * @return the created connector instance
                 */
                public TransportConnector createIpcConnector() {
                    return new FakeAuthConnector();
                }

                /**
                 * Creates a direct memory transfer connector instance.
                 * 
                 * @return the direct memory connector instance
                 */
                public TransportConnector createDirectMemoryConnector() {
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
