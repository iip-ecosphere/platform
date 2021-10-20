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

import java.io.IOException;
import java.util.Iterator;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.ConnectorRegistry;
import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorInputTypeTranslator;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;
import de.iip_ecosphere.platform.connectors.types.ChannelTranslatingProtocolAdapter;
import de.iip_ecosphere.platform.connectors.types.ConnectorInputTypeAdapter;
import de.iip_ecosphere.platform.connectors.types.ConnectorInputTypeTranslator;
import de.iip_ecosphere.platform.connectors.types.ConnectorOutputTypeAdapter;
import de.iip_ecosphere.platform.connectors.types.ConnectorOutputTypeTranslator;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import test.de.iip_ecosphere.platform.support.fakeAas.FactoryTest;
import test.de.iip_ecosphere.platform.transport.Command;
import test.de.iip_ecosphere.platform.transport.CommandJsonSerializer;
import test.de.iip_ecosphere.platform.transport.Product;
import test.de.iip_ecosphere.platform.transport.ProductJsonSerializer;

/**
 * Tests the defined classes through fake connectors.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConnectorTest {

    private static AasFactory original;
    
    /**
     * Initializes this test.
     */
    @BeforeClass
    public static void init() {
        original = AasFactory.setInstance(FactoryTest.createDisabledFactory()); // otherwise service loader is consulted
    }
    
    /**
     * Shuts down this test.
     */
    @AfterClass
    public static void shutdown() {
        AasFactory.setInstance(original); // restore for follow-up tests
    }
    
    /**
     * Asserts the registration of {@code cls}.
     * 
     * @param cls the descriptor class to look for in {@link ConnectorRegistry}
     */
    public static void assertDescriptorRegistration(Class<? extends ConnectorDescriptor> cls) {
        ConnectorDescriptor found = null;
        Iterator<ConnectorDescriptor> iter = ConnectorRegistry.getRegisteredConnectorDescriptors();
        while (iter.hasNext() && null == found) {
            ConnectorDescriptor desc = iter.next();
            if (desc.getClass().equals(cls)) {
                found = desc;
            }
        }
        Assert.assertNotNull("Descriptor " + cls.getName() + " not auto-registered in ConnectorRegistry", found);
        Assert.assertTrue(found.getName().length() > 0);
        Assert.assertNotNull(found.getType());
    }
    
    /**
     * Checks the {@link ConnectorRegistry} whether {@code connector} is {@code registered} or not.
     * 
     * @param connector the connector to search for
     * @param registered if {@code true}, fails if {@code connector} is not registered; if {@code false}, fails 
     *   if {@code connector} is registered,   
     */
    public static void assertInstance(Connector<?, ?, ?, ?> connector, boolean registered) {
        Iterator<Connector<?, ?, ?, ?>> iter = ConnectorRegistry.getRegisteredConnectorInstances();
        boolean found = false;
        while (iter.hasNext()) {
            found = iter.next() == connector;
        }
        if (registered) {
            Assert.assertTrue(found);
        } else {
            Assert.assertFalse(found);
        }
    }
    
    /**
     * Tests the connector instance itself.
     * 
     * @param connector the connector instance
     */
    public static void assertConnectorProperties(Connector<?, ?, ?, ?> connector) {
        Assert.assertTrue(connector.getName().length() > 0);
        // may be null but shall not in tests
        Assert.assertNotNull(connector.getConnectorInputType());
        Assert.assertNotNull(connector.getConnectorOutputType());
        Assert.assertNotNull(connector.getProtocolOutputType());
        Assert.assertNotNull(connector.getProtocolInputType());
    }

    /**
     * Tests the model connector with event-based ingestion.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testModelConnectorWithEvents() throws IOException {
        testModelConnector(true);
    }

    /**
     * Tests the model connector with polling.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testModelConnectorWithPolling() throws IOException {
        testModelConnector(false);
    }
    
    /**
     * A simple struct for model-based connector tests.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class MyStruct {
        
        private String data;

        /**
         * Creats an instance.
         * 
         * @param data some data
         */
        private MyStruct(String data) {
            this.data = data;
        }
    }

    /**
     * Implements an input type translator for information-based tests.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ModelInputTranslator 
        extends AbstractConnectorInputTypeTranslator<Command, Object> {

        @Override
        public Object from(Command data) throws IOException {
            // the actual job - translate the command
            ModelAccess acc = getModelAccess();
            acc.set("sProp", data.getCommand());
            // and some testing
            Assert.assertEquals(10, acc.get("iProp"));
            Assert.assertEquals(data.getCommand(), acc.get("sProp"));
            Assert.assertEquals(2.3, (double) acc.get("dProp"), 0.01);
            MyStruct s = acc.getStruct("struct", MyStruct.class);
            Assert.assertNotNull(s);
            Assert.assertEquals("xmas", s.data);
            return new Object(); // irrelevant but to receive something in the test
        }

        @Override
        public Class<? extends Object> getSourceType() {
            return Object.class;
        }

        @Override
        public Class<? extends Command> getTargetType() {
            return Command.class;
        }

    }
    
    /**
     * Implements an output type translator for information-based tests.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ModelOutputTranslator 
        extends AbstractConnectorOutputTypeTranslator<Object, Product> {

        private boolean withEvents;
        
        /**
         * Creates an instance.
         * 
         * @param withEvents for tests with event-based ingestion or for polling ({@code false})
         */
        private ModelOutputTranslator(boolean withEvents) {
            this.withEvents = withEvents;
        }
        
        @Override
        public void initializeModelAccess() throws IOException {
            ModelAccess acc = getModelAccess();
            // for test - populate the model
            acc.set("iProp", 10);
            acc.set("sProp", "HERE");
            acc.set("dProp", 2.3);
            acc.setStruct("struct", new MyStruct("xmas"));
            acc.useNotifications(withEvents);
        }

        @Override
        public Product to(Object source) throws IOException {
            ModelAccess acc = getModelAccess();
            // some testing
            Assert.assertTrue(acc.getQSeparator().length() > 0);
            Assert.assertEquals("", acc.qName());
            Assert.assertEquals("", acc.qName(""));
            Assert.assertEquals("a" + acc.getQSeparator() + "b" + acc.getQSeparator() + "c", 
                acc.qName("a", "b", "c"));
            Assert.assertEquals("a" + acc.getQSeparator() + "b" + acc.getQSeparator() + "c", 
                acc.qName("", "a", "b", "c"));
            Assert.assertEquals("a" + acc.getQSeparator() + "b" + acc.getQSeparator() + "c", 
                acc.qName("a", "", "b", "c"));
            // topQName is empty, see test below
            Assert.assertEquals("a" + acc.getQSeparator() + "b", acc.iqName("a", "", "b"));
            Assert.assertEquals("", acc.iqName());
            Assert.assertEquals("", acc.iqName(""));
            Assert.assertEquals(10, acc.get("iProp"));
            MyStruct s = acc.getStruct("struct", MyStruct.class);
            Assert.assertNotNull(s);
            Assert.assertEquals("xmas", s.data);
            // and the actual job
            return new Product((String) acc.get("sProp"), (double) acc.get("dProp"));
        }

        @Override
        public Class<? extends Object> getSourceType() {
            return Object.class;
        }

        @Override
        public Class<? extends Product> getTargetType() {
            return Product.class;
        }

    }
    
    /**
     * Tests {@link ModelAccess#iqName(String...)} with a non-empty {@link ModelAccess#topInstancesQName()}.
     */
    @Test
    public void testTopQName() {
        ModelAccess acc = new AbstractModelAccess(null) {
            
            @Override
            public String topInstancesQName() {
                return "TOP";
            }
            
            @Override
            public void setStruct(String qName, Object value) throws IOException {
            }
            
            @Override
            public void set(String qName, Object value) throws IOException {
            }
            
            @Override
            public void registerCustomType(Class<?> cls) throws IOException {
            }
            
            @Override
            public <T> T getStruct(String qName, Class<T> type) throws IOException {
                return null;
            }
            
            @Override
            public String getQSeparator() {
                return "/";
            }
            
            @Override
            public Object get(String qName) throws IOException {
                return null;
            }
            
            @Override
            public Object call(String qName, Object... args) throws IOException {
                return null;
            }

            @Override
            public void monitor(int notificationInterval, String... qNames) throws IOException {
            }

            @Override
            public void monitorModelChanges(int notificationInterval) throws IOException {
            }

            @Override
            protected ConnectorParameter getConnectorParameter() {
                return null; // unused
            }
        };

        Assert.assertEquals(acc.topInstancesQName() + acc.getQSeparator() + "a" + acc.getQSeparator() + "b", 
            acc.iqName("a", "", "b"));
        Assert.assertEquals("", acc.iqName());
        Assert.assertEquals("", acc.iqName(""));
    }

    /**
     * Tests the model connector.
     * 
     * @param withEvents use events or polling
     * @throws IOException shall not occur
     */
    private void testModelConnector(boolean withEvents) throws IOException {
        assertDescriptorRegistration(MyModelConnector.Descriptor.class);
        ConnectorParameter params = ConnectorParameterBuilder.newBuilder("", 1234).build();
        ConnectorInputTypeTranslator<Command, Object> in = new ModelInputTranslator(); 
        ConnectorOutputTypeTranslator<Object, Product> out = new ModelOutputTranslator(withEvents);

        Command inData = new Command("def");
        TranslatingProtocolAdapter<Object, Object, Product, Command> adapter 
            = new TranslatingProtocolAdapter<>(out, in);
        MyModelConnector<Product, Command> instance = new MyModelConnector<>(adapter);
        assertConnectorProperties(instance);
        assertInstance(instance, false);
        instance.connect(params);
        assertInstance(instance, true);
        instance.setReceptionCallback(new ReceptionCallback<Product>() {

            @Override
            public void received(Product data) {
                try {
                    instance.write(inData);
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }
            }

            @Override
            public Class<Product> getType() {
                return Product.class;
            }
        });
        instance.trigger();
        Object received = null;
        // trigger?
        int count = 30; // polling
        do {
            received = instance.pollReceived();
            TimeUtils.sleep(100);
            count--;
        } while (count > 0 && null == received);
        Assert.assertNotNull("nothing received", received);
        
        assertInstance(instance, true);
        instance.disconnect();
        assertInstance(instance, false);
        instance.dispose();
    }

    /**
     * Tests failing construction of connectors.
     */
    @Test
    public void testConnectorParams() {
        try {
            new MyModelConnector<Product, Command>();
            Assert.fail("No exception thrown");
        } catch (IllegalArgumentException e) {
            // this is ok
        }
        try {
            new MyModelConnector<Product, Command>((ProtocolAdapter<Object, Object, Product, Command>[]) null);
            Assert.fail("No exception thrown");
        } catch (IllegalArgumentException e) {
            // this is ok
        }
        try {
            ConnectorInputTypeTranslator<Command, Object> in = new ModelInputTranslator(); 
            ConnectorOutputTypeTranslator<Object, Product> out = new ModelOutputTranslator(false);
            TranslatingProtocolAdapter<Object, Object, Product, Command> adapter 
                = new TranslatingProtocolAdapter<>(out, in);
            new MyModelConnector<Product, Command>(adapter, null);
            Assert.fail("No exception thrown");
        } catch (IllegalArgumentException e) {
            // this is ok
        }
    }

    /**
     * Tests the channel connector while reusing serializers.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testChannelConnector() throws IOException {
        assertDescriptorRegistration(MyChannelConnector.Descriptor.class);
        ProductJsonSerializer outSer = new ProductJsonSerializer();
        CommandJsonSerializer inSer = new CommandJsonSerializer();
        ConnectorParameter params = ConnectorParameterBuilder.newBuilder("", 1234).build();
        ChannelTranslatingProtocolAdapter<byte[], byte[], Product, Command> adapter 
            = new ChannelTranslatingProtocolAdapter<>(
                "out", new ConnectorOutputTypeAdapter<Product>(outSer), 
                "in", new ConnectorInputTypeAdapter<Command>(inSer));
        
        Product outData = new Product("abc", 2.3);
        Command inData = new Command("def");
        MyChannelConnector<Product, Command> instance = new MyChannelConnector<>(adapter);
        Assert.assertNull(instance.enabledEncryption());
        Assert.assertNull(instance.supportedEncryption());
        assertConnectorProperties(instance);
        assertInstance(instance, false);
        instance.connect(params);
        assertInstance(instance, true);
        instance.setReceptionCallback(new ReceptionCallback<Product>() {
            
            @Override
            public void received(Product data) {
                Assert.assertEquals(outData.getDescription(), data.getDescription());
                Assert.assertEquals(outData.getPrice(), data.getPrice(), 0.01);
                try {
                    instance.write(inData);
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }
            }
            
            @Override
            public Class<Product> getType() {
                return Product.class;
            }
        });
        instance.offer(outSer.to(outData));
        Command received = null;
        int count = 30; // polling
        do {
            byte[] data = instance.pollReceived();
            if (null != data) {
                received = inSer.from(data);
            }
            TimeUtils.sleep(100);
            count--;
        } while (count > 0 && null == received);
        Assert.assertNotNull("nothing received", received);
        Assert.assertEquals(inData.getCommand(), received.getCommand());
        assertInstance(instance, true);
        instance.disconnect();
        assertInstance(instance, false);
        instance.dispose();
    }

}
