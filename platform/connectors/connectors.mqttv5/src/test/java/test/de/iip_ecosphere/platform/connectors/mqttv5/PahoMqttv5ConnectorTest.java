/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.connectors.mqttv5;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.mqttv5.PahoMqttv5Connector;
import de.iip_ecosphere.platform.connectors.types.ChannelTranslatingProtocolAdapter;
import de.iip_ecosphere.platform.connectors.types.ConnectorInputTypeAdapter;
import de.iip_ecosphere.platform.connectors.types.ConnectorOutputTypeAdapter;
import de.iip_ecosphere.platform.transport.Utils;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter.TransportParameterBuilder;
import de.iip_ecosphere.platform.transport.mqttv5.PahoMqttV5TransportConnector;
import de.iip_ecosphere.platform.transport.serialization.Serializer;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;
import org.junit.Assert;
import test.de.iip_ecosphere.platform.transport.Product;
import test.de.iip_ecosphere.platform.transport.ProductJsonSerializer;
import test.de.iip_ecosphere.platform.connectors.ConnectorTest;
import test.de.iip_ecosphere.platform.transport.Command;
import test.de.iip_ecosphere.platform.transport.CommandJsonSerializer;
import test.de.iip_ecosphere.platform.transport.mqttv5.TestHiveMqServer;

/**
 * Implements a test for {@link PahoMqttv5Connector}. Data is sent via the test server from a transport connector
 * to the machine connector which mirrors the data and sends it back in another format.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PahoMqttv5ConnectorTest {

    private static final String CMD_CHANNEL = "cmd";
    private static final String PROD_CHANNEL = "prod";
    
    /**
     * Tests the connector through explicitly setting/resetting the factory
     * implementation. Builds up a {@link TestHiveMqServer} so that the test is
     * self-contained.
     * 
     * @throws IOException in case that connection/communication fails
     */
    @Test
    public void testPahoConnector() throws IOException {
        final int port = 8883;
        TestHiveMqServer server = new TestHiveMqServer();
        server.start("localhost", port);
        doTest("localhost", port);
        server.stop();
    }
        
    /**
     * Implements a MQTT connector test.
     * 
     * @param host the host to use (usually "localhost")
     * @param port the TCP port to use
     * @throws IOException in case that connection/communication fails
     */
    public static void doTest(String host, int port) throws IOException {
        Product prod1 = new Product("prod1", 10.2);
        Product prod2 = new Product("prod2", 5.1);

        System.out.println("Using JSON serializers");

        ConnectorParameter cParams = ConnectorParameterBuilder
            .newBuilder(host, port).setApplicationInformation("m1", "").build();
        TransportParameter tParams = TransportParameterBuilder.newBuilder(host, port).setApplicationId("cl1").build();

        Serializer<Product> outSer = new ProductJsonSerializer();
        SerializerRegistry.registerSerializer(outSer);
        Serializer<Command> inSer = new CommandJsonSerializer();
        SerializerRegistry.registerSerializer(inSer);
        
        PahoMqttv5Connector<Product, Command> mConnector = new PahoMqttv5Connector<>(
            new ChannelTranslatingProtocolAdapter<byte[], byte[], Product, Command, Object>(
                PROD_CHANNEL, new ConnectorOutputTypeAdapter<Product, Object>(outSer), 
                CMD_CHANNEL, new ConnectorInputTypeAdapter<Command, Object>(inSer)));
        ConnectorTest.assertInstance(mConnector, false);
        ConnectorTest.assertConnectorProperties(mConnector);
        mConnector.connect(cParams);
        ConnectorTest.assertInstance(mConnector, true);
        mConnector.setReceptionCallback(new ReceptionCallback<Product>() {
            
            @Override
            public void received(Product data) {
                try {
                    mConnector.write(new Command(data.getDescription()));
                } catch (IOException e) {
                    System.out.println("ERROR WHILE SENDING: " + e.getMessage());
                }
            }
            
            @Override
            public Class<Product> getType() {
                return Product.class;
            }
        });
        
        PahoMqttV5TransportConnector tConnector = new PahoMqttV5TransportConnector();
        tConnector.connect(tParams);

        List<Command> received = new ArrayList<Command>();
        tConnector.setReceptionCallback(CMD_CHANNEL, new ReceptionCallback<Command>() {

            @Override
            public void received(Command cmd) {
                received.add(cmd);
            }

            @Override
            public Class<Command> getType() {
                return Command.class;
            }
        });
        
        tConnector.syncSend(PROD_CHANNEL, prod1);
        tConnector.syncSend(PROD_CHANNEL, prod2);

        int count = 20;
        while (received.size() < 2 && count > 0) {
            Utils.sleep(100);
            count--;
        }

        System.out.println("Cleaning up");
        ConnectorTest.assertInstance(mConnector, true);
        mConnector.disconnect();
        tConnector.disconnect();
        ConnectorTest.assertInstance(mConnector, false);
        
        SerializerRegistry.unregisterSerializer(outSer);
        SerializerRegistry.unregisterSerializer(inSer);
        
        Assert.assertEquals(2, received.size());
        Assert.assertEquals(prod1.getDescription(), received.get(0).getCommand());
        Assert.assertEquals(prod2.getDescription(), received.get(1).getCommand());
    }

}
