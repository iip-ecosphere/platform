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
package test.de.iip_ecosphere.platform.transport.mqttv5;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.TransportFactory.ConnectorCreator;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.mqttv5.PahoMqttV5TransportConnector;
import test.de.iip_ecosphere.platform.transport.AbstractTransportConnectorTest;
import test.de.iip_ecosphere.platform.transport.ProductJsonSerializer;
import test.de.iip_ecosphere.platform.transport.ProductProtobufSerializer;

/**
 * Tests the {@link PahoMqttV5TransportConnector}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PahoMqttV5TransportConnectorTest {

    /**
     * Tests the connector through explicitly setting/resetting the factory
     * implementation. Builds up a {@link TestHiveMqServer} so that the test is
     * self-contained.
     * 
     * @throws IOException in case that connection/communication fails
     */
    @Test
    public void testPahoConnector() throws IOException {
        ConnectorCreator old = TransportFactory.setMainImplementation(new ConnectorCreator() {

            @Override
            public TransportConnector createConnector() {
                return new PahoMqttV5TransportConnector();
            }

            @Override
            public String getName() {
                return PahoMqttV5TransportConnector.NAME;
            }
            
        });

        Assert.assertEquals(PahoMqttV5TransportConnector.NAME, TransportFactory.getConnectorName());
        final int port = 8883;
        TestHiveMqServer server = new TestHiveMqServer();
        server.start("localhost", port);
        AbstractTransportConnectorTest.doTest("localhost", port, ProductJsonSerializer.class);
        AbstractTransportConnectorTest.doTest("localhost", port, ProductProtobufSerializer.class);
        server.stop();
        TransportFactory.setMainImplementation(old);
    }

}
