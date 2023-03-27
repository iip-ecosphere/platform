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

package test.de.iip_ecosphere.platform.connectors;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.types.ChannelProtocolAdapter;
import de.iip_ecosphere.platform.connectors.types.ChannelTranslatingProtocolAdapter;
import de.iip_ecosphere.platform.connectors.types.ChanneledConnectorOutputTypeAdapter;
import de.iip_ecosphere.platform.connectors.types.ChanneledConnectorOutputTypeAdapter.ChanneledSerializer;
import de.iip_ecosphere.platform.connectors.types.ConnectorInputTypeAdapter;
import de.iip_ecosphere.platform.connectors.types.ConnectorOutputTypeAdapter;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter.TransportParameterBuilder;
import de.iip_ecosphere.platform.transport.serialization.Serializer;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;
import test.de.iip_ecosphere.platform.transport.Command;
import test.de.iip_ecosphere.platform.transport.CommandJsonSerializer;
import test.de.iip_ecosphere.platform.transport.Product;
import test.de.iip_ecosphere.platform.transport.ProductJsonSerializer;
import test.de.iip_ecosphere.platform.transport.AbstractTransportConnectorTest.TransportParameterConfigurer;

/**
 * Generic re-usable test for serializing connectors, i.e., internal {@code byte[]} types.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractSerializingConnectorTest {

    private static final String CMD_CHANNEL = "cmd";
    private static final String PROD_CHANNEL = "prod";
    
    /**
     * Creates the connector to be tested.
     * 
     * @param adapter the protocol adapter(s) used to create the connector
     * @return the connector instance
     */
    @SuppressWarnings("unchecked")
    protected abstract Connector<byte[], byte[], Product, Command> createConnector(
        ChannelProtocolAdapter<byte[], byte[], Product, Command>... adapter);

    /**
     * Returns the connector descriptor for {@link #createConnector(ChannelProtocolAdapter)}.
     * 
     * @return the connector descriptor
     */
    protected abstract Class<? extends ConnectorDescriptor> getConnectorDescriptor();

    /**
     * Creates the test server to test against.
     * 
     * @param addr the server address (schema may be ignored)
     * @param configDir specific configuration directory for this test server, may be <b>null</b> for none
     * @return the server instance
     */
    protected abstract Server createTestServer(ServerAddress addr, File configDir);

    /**
     * Returns the transport connector to test against behind the test server.
     * 
     * @return the transport connector
     */
    protected abstract TransportConnector createTransportConnector();

    /**
     * Configures the transport parameters used to connect the instance from 
     * {@link #createConnector(ChannelProtocolAdapter)}.
     * 
     * @param builder the parameter builder
     * @return {@code builder}
     */
    protected abstract TransportParameterBuilder configureTransportParameter(TransportParameterBuilder builder);

    /**
     * Tests the connector through explicitly setting/resetting the factory
     * implementation. Builds up a {@link Server} based on {@link #createTestServer(ServerAddress, File)} that the 
     * test is self-contained.
     * 
     * @throws IOException in case that connection/communication fails
     */
    @Test
    public void testConnector() throws IOException {
        ConnectorParameterConfigurer configurer = getConfigurer(false);
        ServerAddress addr = new ServerAddress(Schema.IGNORE); // localhost, ephemeral port
        Server server = createTestServer(addr, null);
        server.start();
        doTest(addr, configurer);
        doTestMultiChannel(addr, configurer);
        server.stop(true);
    }
    
    /**
     * Tests the TLS connector through explicitly setting/resetting the factory
     * implementation. Builds up a {@link Server} based on {@link #createTestServer(ServerAddress, File)} that 
     * the test is self-contained.
     * 
     * @throws IOException in case that connection/communication fails
     * @see #getConfigurer(boolean)
     */
    @Test
    public void testTlsConnector() throws IOException {
        ConnectorParameterConfigurer configurer = getConfigurer(true);
        if (null == configurer) {
            System.out.println("No TLS test performed, no connector.");
        } else {
            ServerAddress addr = new ServerAddress(Schema.IGNORE); // localhost, ephemeral port
            Server server = createTestServer(addr, configurer.getConfigDir());
            server.start();
            doTest(addr, configurer);
            doTestMultiChannel(addr, configurer);
            server.stop(true);
        }
    }
    
    /**
     * Returns the test configurer.
     * 
     * @param withTls shall a configurer for TLS tests be created
     * @return the test configurer, may be <b>null</b> for none
     */
    protected abstract ConnectorParameterConfigurer getConfigurer(boolean withTls);

    /**
     * Returns whether the connector implementation supports encryption.
     * 
     * @return {@code true} for supported, {@code false} for not supported
     */
    protected abstract boolean implementsEncryption();
    
    /**
     * Allows to configure the connector parameters. (orthogonal, in particular for TLS)
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface ConnectorParameterConfigurer extends TransportParameterConfigurer {

        /**
         * Further setup/configuration of the builder.
         * 
         * @param builder the builder
         */
        public void configure(ConnectorParameterBuilder builder);

        /**
         * Returns the (specific) server configuration directory.
         * 
         * @return the directory, may be <b>null</b> for none
         */
        public File getConfigDir();
        
        /**
         * Returns whether we test here for encryption.
         * 
         * @return {@code true} for encryption, {@code false} else
         */
        public boolean withEncryption();
        
    }

    /**
     * Creates the coonnector parameter instance.
     * 
     * @param addr the server address (schema is ignored)
     * @param configurer the parameter configurer, may be <b>null</b> for none
     * @return the transport parameter
     */
    protected ConnectorParameter createConnectorParameter(ServerAddress addr, ConnectorParameterConfigurer configurer) {
        ConnectorParameterBuilder cBuilder = ConnectorParameterBuilder
            .newBuilder(addr.getHost(), addr.getPort()).setApplicationInformation("m1", "");
        if (null != configurer) {
            configurer.configure(cBuilder);
        }
        return cBuilder.build();
    }

    /**
     * Creates the transport parameter instance. Uses {@link #configureTransportParameter(TransportParameterBuilder)} 
     * for basic, test-wide configuration.
     * 
     * @param addr the server address (schema is ignored)
     * @param configurer the parameter configurer, may be <b>null</b> for none
     * @return the transport parameter
     */
    protected TransportParameter createTransportParameter(ServerAddress addr, TransportParameterConfigurer configurer) {
        TransportParameterBuilder tBuilder = TransportParameterBuilder.newBuilder(addr.getHost(), addr.getPort());
        if (null != configurer) {
            configurer.configure(tBuilder);
        }
        return configureTransportParameter(tBuilder).build();
    }

    /**
     * Implements a MQTT connector test.
     * 
     * @param addr the server address (schema is ignored)
     * @param configurer the parameter configurer, may be <b>null</b> for none
     * @throws IOException in case that connection/communication fails
     */
    protected void doTest(ServerAddress addr, ConnectorParameterConfigurer configurer) throws IOException {
        ConnectorTest.assertDescriptorRegistration(getConnectorDescriptor());
        Product prod1 = new Product("prod1", 10.2);
        Product prod2 = new Product("prod2", 5.1);
        System.out.println("Using JSON serializers");
        ConnectorParameter cParams = createConnectorParameter(addr, configurer);
        TransportParameter tParams = createTransportParameter(addr, configurer);

        Serializer<Product> outSer = new ProductJsonSerializer();
        SerializerRegistry.registerSerializer(outSer);
        Serializer<Command> inSer = new CommandJsonSerializer();
        SerializerRegistry.registerSerializer(inSer);
        
        @SuppressWarnings("unchecked")
        Connector<byte[], byte[], Product, Command> mConnector = createConnector(
            new ChannelTranslatingProtocolAdapter<byte[], byte[], Product, Command>(
                PROD_CHANNEL, new ConnectorOutputTypeAdapter<Product>(outSer), 
                CMD_CHANNEL, new ConnectorInputTypeAdapter<Command>(inSer)));
        ConnectorTest.assertInstance(mConnector, false);
        ConnectorTest.assertConnectorProperties(mConnector);
        testEnc(mConnector, configurer, false);
        mConnector.connect(cParams);
        testEnc(mConnector, configurer, true);
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
        
        TransportConnector tConnector = createTransportConnector();
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
            TimeUtils.sleep(100);
            count--;
        }

        System.out.println("Cleaning up");
        ConnectorTest.assertInstance(mConnector, true);
        mConnector.disconnect();
        tConnector.disconnect();
        ConnectorTest.assertInstance(mConnector, false);
        SerializerRegistry.unregisterSerializer(outSer);
        SerializerRegistry.unregisterSerializer(inSer);
        mConnector.dispose();
        
        Assert.assertEquals(2, received.size());
        Assert.assertEquals(prod1.getDescription(), received.get(0).getCommand());
        Assert.assertEquals(prod2.getDescription(), received.get(1).getCommand());
    }

    /**
     * Just counts product receptions.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class CountingProductReceptionCallback implements ReceptionCallback<Product> {
        
        private int counter;
        
        @Override
        public void received(Product data) {
            counter++;
        }
        
        @Override
        public Class<Product> getType() {
            return Product.class;
        }
        
        /**
         * Returns the counter value.
         * 
         * @return the number of received products
         */
        public int getCounter() {
            return counter;
        }
    }

    /**
     * Re-used channeled serializer, ignoring the channel here.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ChanneledProductJsonSerializer extends ProductJsonSerializer 
        implements ChanneledSerializer<Product> {

        @Override
        public Product from(String channel, byte[] data) throws IOException {
            Assert.assertNotNull(channel);
            Assert.assertTrue(channel.length() > 0);            
            return super.from(data);
        }
        
    }

    /**
     * Does a multi-channel test.
     * 
     * @param addr the server address (schema is ignored)
     * @param configurer the parameter configurer, may be <b>null</b> for none
     * @throws IOException in case that connection/communication fails
     */
    protected void doTestMultiChannel(ServerAddress addr, ConnectorParameterConfigurer configurer) throws IOException {
        final String prodChannel1 = "data/channel/prod1";
        final String prodChannel2 = "data/channel/prod2";
        final String prodChannelDyn = "data/channel/+";
        Product prod1 = new Product("prod1", 10.2);
        Product prod2 = new Product("prod2", 4.1);

        ChanneledSerializer<Product> outSer = new ChanneledProductJsonSerializer();
        SerializerRegistry.registerSerializer(outSer);
        Serializer<Command> inSer = new CommandJsonSerializer();
        SerializerRegistry.registerSerializer(inSer);
        ConnectorParameter cParams = createConnectorParameter(addr, configurer);
        TransportParameter tParams = createTransportParameter(addr, configurer);

        // multi adapter setup for statically known channels
        @SuppressWarnings("unchecked")
        Connector<byte[], byte[], Product, Command> cConnector = createConnector(
            new ChannelTranslatingProtocolAdapter<byte[], byte[], Product, Command>(
                prodChannel1, new ConnectorOutputTypeAdapter<Product>(outSer), 
                CMD_CHANNEL, new ConnectorInputTypeAdapter<Command>(inSer)),
            new ChannelTranslatingProtocolAdapter<byte[], byte[], Product, Command>(
                prodChannel2, new ConnectorOutputTypeAdapter<Product>(outSer), 
                CMD_CHANNEL, new ConnectorInputTypeAdapter<Command>(inSer)));
        cConnector.connect(cParams);
        CountingProductReceptionCallback cCallback = new CountingProductReceptionCallback();
        cConnector.setReceptionCallback(cCallback);

        // single adapter setup for dynamically resolved wildcard channels
        @SuppressWarnings("unchecked")
        Connector<byte[], byte[], Product, Command> dConnector = createConnector(
            new ChannelTranslatingProtocolAdapter<byte[], byte[], Product, Command>(
                prodChannelDyn, new ChanneledConnectorOutputTypeAdapter<Product>(outSer), 
                CMD_CHANNEL, new ConnectorInputTypeAdapter<Command>(inSer)));
        dConnector.connect(cParams);
        CountingProductReceptionCallback dCallback = new CountingProductReceptionCallback();
        dConnector.setReceptionCallback(dCallback);
        
        TransportConnector tConnector = createTransportConnector();
        tConnector.connect(tParams);
        tConnector.syncSend(prodChannel1, prod1);
        tConnector.syncSend(prodChannel2, prod2);

        cConnector.disconnect();
        tConnector.disconnect();

        SerializerRegistry.unregisterSerializer(outSer);
        SerializerRegistry.unregisterSerializer(inSer);
        
        Assert.assertEquals(2, cCallback.getCounter());
        Assert.assertEquals(2, dCallback.getCounter());
    }

    /**
     * Tests encryption statements of {@code connector}, i.e., {@link Connector#supportedEncryption()} 
     * and {@link Connector#enabledEncryption()}.
     * 
     * @param connector the connector instance to test
     * @param configurer the configurer indicating whether encryption shall be activated if connected
     * @param connected whether the connector is supposed to be connected
     */
    private void testEnc(Connector<?, ?, ?, ?> connector, ConnectorParameterConfigurer configurer, boolean connected) {
        if (implementsEncryption()) {
            Assert.assertTrue(connector.supportedEncryption().length() > 0);
        } else {
            Assert.assertTrue(connector.supportedEncryption() ==  null 
                || connector.supportedEncryption().length() == 0);
        }
        if (null == configurer || !connected) {
            Assert.assertTrue(connector.enabledEncryption() ==  null 
                || connector.enabledEncryption().length() == 0);
        } else {
            if (configurer.withEncryption()) {
                Assert.assertTrue(connector.enabledEncryption().length() > 0);
            }
        }
    }
    
}
