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

package test.de.oktoflow.platform.connectors.serial;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.types.ChannelTranslatingProtocolAdapter;
import de.iip_ecosphere.platform.connectors.types.ConnectorInputTypeAdapter;
import de.iip_ecosphere.platform.connectors.types.ConnectorOutputTypeAdapter;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.Serializer;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;
import de.oktoflow.platform.connectors.serial.JSerialCommConnector;
import test.de.iip_ecosphere.platform.transport.Product;
import test.de.iip_ecosphere.platform.transport.ProductJsonSerializer;
import test.de.iip_ecosphere.platform.connectors.ConnectorTest;
import test.de.iip_ecosphere.platform.transport.Command;
import test.de.iip_ecosphere.platform.transport.CommandJsonSerializer;

/**
 * Implements a test for {@link JSerialCommConnector}, app and machine playing ping-ping via two bridged a virtual 
 * serial ports. Requires a virtual port emulator on windows (bridging COM1 and COM2) like HDD Virtual Serial 
 * Port Tools. Testing under Linux requires an installed ``socat``.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JSerialCommConnectorTest {

    private static final String CMD_CHANNEL = "cmd";
    private static final String PROD_CHANNEL = "prod";

    private ConnectorParameter appParams;
    private ConnectorParameter machineParams;
    private Connector<byte[], byte[], Product, Command> app;
    private Connector<byte[], byte[], Command, Product> machine;
    private Process comEmulator;
    
    /**
     * Creates the connector parameters.
     */
    private void createParameters() throws IOException {
        String appPortDescriptor = "COM1";
        String machinePortDescriptor = "COM2";
        if (!SystemUtils.IS_OS_WINDOWS) {
            // https://stackoverflow.com/questions/52187/virtual-serial-port-for-linux
            appPortDescriptor = "/tmp/ttyV0";
            machinePortDescriptor = "/tmp/ttyV1";
            List<String> args = CollectionUtils.toList("socat", "-d", "-d", 
                "pty,raw,echo=0,link=" + appPortDescriptor, 
                "pty,raw,echo=0,link=" + machinePortDescriptor);
            ProcessBuilder pb = new ProcessBuilder(args);
            pb.inheritIO();
            comEmulator = pb.start();
        }
        appParams = createConnectorParameter(appPortDescriptor);
        machineParams = createConnectorParameter(machinePortDescriptor);
    }
    
    /**
     * Tests the connector through explicitly setting/resetting the factory
     * implementation. Builds up a {@link Server} based on {@link #createTestServer(ServerAddress, File)} that the 
     * test is self-contained.
     * 
     * @throws IOException in case that connection/communication fails
     */
    @Test
    public void testConnector() throws IOException {
        ConnectorTest.assertDescriptorRegistration(JSerialCommConnector.Descriptor.class);
        Command comm1 = new Command("abc");
        Command comm2 = new Command("cde");
        System.out.println("Using JSON serializers");
        createParameters();

        Serializer<Product> prodSer = new ProductJsonSerializer();
        SerializerRegistry.registerSerializer(prodSer);
        Serializer<Command> commSer = new CommandJsonSerializer();
        SerializerRegistry.registerSerializer(commSer);
        
        // app: read product, write command
        app = new JSerialCommConnector<>(
            new ChannelTranslatingProtocolAdapter<byte[], byte[], Product, Command>(
                PROD_CHANNEL, new ConnectorOutputTypeAdapter<Product>(prodSer), 
                CMD_CHANNEL, new ConnectorInputTypeAdapter<Command>(commSer)));
        // machine: read command, write product
        machine = new JSerialCommConnector<>(
            new ChannelTranslatingProtocolAdapter<byte[], byte[], Command, Product>(
                CMD_CHANNEL, new ConnectorOutputTypeAdapter<Command>(commSer),
                PROD_CHANNEL, new ConnectorInputTypeAdapter<Product>(prodSer)));
        
        ConnectorTest.assertInstance(app, false);
        ConnectorTest.assertConnectorProperties(app);
        Assert.assertNull(app.supportedEncryption());
        Assert.assertNull(app.enabledEncryption());
        app.connect(appParams);
        ConnectorTest.assertInstance(app, true);
        
        List<Product> received = new ArrayList<Product>();
        machine.connect(machineParams);
        createCallbacks(received);
        
        app.write(new Command("abc"));
        app.write(new Command("cde"));

        int count = 20;
        while (received.size() < 2 && count > 0) {
            TimeUtils.sleep(100);
            count--;
        }

        System.out.println("Cleaning up");
//        ConnectorTest.assertInstance(app, true);
        app.disconnect();
        machine.disconnect();
//        ConnectorTest.assertInstance(app, false);
        SerializerRegistry.unregisterSerializer(prodSer);
        SerializerRegistry.unregisterSerializer(commSer);
        app.dispose();
        
        Assert.assertEquals(2, received.size());
        Assert.assertEquals(comm1.getCommand(), received.get(0).getDescription());
        Assert.assertEquals(comm2.getCommand(), received.get(1).getDescription());
        if (null != comEmulator) {
            comEmulator.destroyForcibly();
        }
    }
    
    /**
     * Creates the linked ping-pong connector callbacks.
     * 
     * @param received the received products, modified as a side effect
     * @throws IOException if registring the callbacks fails
     */
    private void createCallbacks(List<Product> received) throws IOException {
        machine.setReceptionCallback(new ReceptionCallback<Command>() {
            
            @Override
            public void received(Command data) {
                try {
                    machine.write(new Product(data.getCommand(), 1));
                } catch (IOException e) {
                    System.out.println("MACHINE ERROR WHILE SENDING: " + e.getMessage());
                }
            }
            
            @Override
            public Class<Command> getType() {
                return Command.class;
            }
        });
        
        app.setReceptionCallback(new ReceptionCallback<Product>() {
            
            @Override
            public void received(Product data) {
                received.add(data);
            }
            
            @Override
            public Class<Product> getType() {
                return Product.class;
            }
        });
    }
    
    /**
     * Creates the connector parameter instance.
     * 
     * @param portDescriptor the comm port descriptor to use
     * @return the transport parameter
     */
    protected ConnectorParameter createConnectorParameter(String portDescriptor) {
        ConnectorParameterBuilder cBuilder = ConnectorParameterBuilder
            .newBuilder(portDescriptor, 1);
        return cBuilder.build();
    }    
    

}
