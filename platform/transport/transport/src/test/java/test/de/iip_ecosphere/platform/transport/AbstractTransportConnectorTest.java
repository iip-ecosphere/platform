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
package test.de.iip_ecosphere.platform.transport;

import java.io.IOException;

import org.junit.Assert;

import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.connectors.AbstractReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter.TransportParameterBuilder;
import de.iip_ecosphere.platform.transport.serialization.Serializer;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;

/**
 * Reusable test steps without referring to specific protocols.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractTransportConnectorTest {

    /**
     * Implements a simple reception callback.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class Callback extends AbstractReceptionCallback<Product> {

        private Product data;

        /**
         * Creates the callback instance.
         */
        protected Callback() {
            super(Product.class);
        }

        @Override
        public void received(Product data) {
            this.data = data;
        }

    }
    
    /**
     * Allows to configure the transport parameters.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface TransportParameterConfigurer {

        /**
         * Further setup/configuration of the builder.
         * 
         * @param builder the builder
         */
        public void configure(TransportParameterBuilder builder);
        
    }

    /**
     * Implements the test using the {@link TransportFactory}.
     * 
     * @param addr the server address
     * @param serializerType the serializer type to use
     * @throws IOException in case that connection/communication fails
     */
    public static void doTest(ServerAddress addr, Class<? extends Serializer<Product>> serializerType) 
        throws IOException {
        doTest(addr, serializerType, null);
    }
    
    /**
     * Implements the test using the {@link TransportFactory}.
     * 
     * @param addr the server address
     * @param serializerType the serializer type to use
     * @param configurer the optional transport parameter configurer (may be <b>null</b>)
     * @throws IOException in case that connection/communication fails
     */
    public static void doTest(ServerAddress addr, Class<? extends Serializer<Product>> serializerType, 
        TransportParameterConfigurer configurer) throws IOException {
        Product data1 = new Product("prod1", 10.2);
        Product data2 = new Product("prod2", 5.1);

        System.out.println("Using serializer: " + serializerType.getSimpleName());
        SerializerRegistry.registerSerializer(serializerType);
        TransportParameterBuilder tpb1 = TransportParameterBuilder.newBuilder(addr.getHost(), addr.getPort())
            .setApplicationId("cl1");
        if (null != configurer) {
            configurer.configure(tpb1);
        }
        TransportParameter param1 = tpb1.build();
        TransportConnector cl1 = TransportFactory.createConnector();
        Assert.assertTrue(cl1.getName().length() > 0);
        System.out.println("Connecting connector 1 to " + addr.toUri());
        cl1.connect(param1);
        final String stream1 = cl1.composeStreamName("", "stream1");
        final String stream2 = cl1.composeStreamName("", "stream2");
        final Callback cb1 = new Callback();
        cl1.setReceptionCallback(stream2, cb1);

        TransportParameterBuilder tpb2 = TransportParameterBuilder.newBuilder(addr).setApplicationId("cl2");
        if (null != configurer) {
            configurer.configure(tpb2);
        }
        TransportParameter param2 = tpb2.build();
        TransportConnector cl2 = TransportFactory.createConnector();
        Assert.assertTrue(cl2.getName().length() > 0);
        System.out.println("Connecting connector 2 to " + addr.toUri());
        cl2.connect(param2);
        final Callback cb2 = new Callback();
        cl2.setReceptionCallback(stream1, cb2);

        System.out.println("Sending/Receiving");
        cl1.syncSend(stream1, data1);
        cl2.syncSend(stream2, data2);
        TimeUtils.sleep(2000);
        assertProduct(data1, cb2);
        assertProduct(data2, cb1);

        System.out.println("Cleaning up");
        cl1.disconnect();
        cl2.disconnect();
        SerializerRegistry.unregisterSerializer(Product.class);
    }

    /**
     * Asserts that {@code expected} and the received value in {@code callback}
     * contain the same values.
     * 
     * @param expected expected value
     * @param received received value
     */
    private static void assertProduct(Product expected, Callback received) {
        int count = 0;
        while (received.data == null && count < 10) {
            TimeUtils.sleep(100);
            count++;
        }
        Assert.assertNotNull(received.data);
        Assert.assertEquals(expected.getDescription(), received.data.getDescription());
        Assert.assertEquals(expected.getPrice(), received.data.getPrice(), 0.01);
        received.data = null;
    }
    
}
