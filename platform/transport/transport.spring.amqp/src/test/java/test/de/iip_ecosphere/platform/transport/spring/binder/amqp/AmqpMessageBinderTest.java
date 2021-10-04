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

package test.de.iip_ecosphere.platform.transport.spring.binder.amqp;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MimeType;

import com.rabbitmq.client.ConnectionFactory;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter.TransportParameterBuilder;
import de.iip_ecosphere.platform.transport.connectors.rabbitmq.RabbitMqAmqpTransportConnector;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;
import de.iip_ecosphere.platform.transport.spring.SerializerMessageConverter;
import de.iip_ecosphere.platform.transport.spring.binder.amqp.AmqpClient;
import test.de.iip_ecosphere.platform.transport.spring.StringSerializer;
import test.de.iip_ecosphere.platform.test.amqp.qpid.TestQpidServer;

/**
 * Test class for the message binder. This class uses the application configuration from transport.spring!
 * Binder name is explicitly configured in {@code test.properties} as {@code spring.cloud.stream.defaultBinder} in 
 * order to test for a correct binder name.
 * 
 * @author Holger Eichelberger, SSE
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
@ContextConfiguration(initializers = AmqpMessageBinderTest.Initializer.class)
@RunWith(SpringRunner.class)
public class AmqpMessageBinderTest {

    private static final ServerAddress ADDR = new ServerAddress(Schema.IGNORE); // localhost, ephemeral port
    private static TestQpidServer server;
    private static String received;

    @Autowired
    private TransportParameter params;
    
    /**
     * An initializer to override certain configuration values, in particular dynamic ports.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues
                .of("amqp.port=" + ADDR.getPort())
                .applyTo(applicationContext);
        }
        
    }
    
    /**
     * Initializes the test by starting an embedded AMQP server and by sending back received results on the output
     * stream to the "input2" stream. Requires the application configuration file "test.properties" in the test 
     * classpath with the username/password stated below.
     */
    @BeforeClass
    public static void init() {
        server = new TestQpidServer(ADDR);
        server.start();
        TimeUtils.sleep(1000);
        SerializerRegistry.registerSerializer(StringSerializer.class);
        final RabbitMqAmqpTransportConnector infra = new RabbitMqAmqpTransportConnector() {
            
            protected void configureFactory(ConnectionFactory factory) {
                factory.setUsername("user");
                factory.setPassword("pwd");
            }
            
        };
        try {
            infra.connect(TransportParameterBuilder.newBuilder(ADDR).setApplicationId("infra").build());
            infra.setReceptionCallback("amqpBinder", new ReceptionCallback<String>() {
    
                @Override
                public void received(String data) {
                    try {
                        infra.asyncSend("input2", "config " + data);
                    } catch (IOException e) {
                        System.out.println("SEND PROBLEM " + e.getMessage());
                    }
                }
    
                @Override
                public Class<String> getType() {
                    return String.class;
                }
            });
        } catch (IOException e) {
            Assert.fail("CONNECTOR PROBLEM " + e.getMessage());
        }
        System.out.println("Started infra client on " + ADDR.getHost() + " " + ADDR.getPort());
        TimeUtils.sleep(1000);
    }
    
    /**
     * Shuts down client and test server.
     */
    @AfterClass
    public static void shutdown() {
        if (null != AmqpClient.getLastInstance()) {
            AmqpClient.getLastInstance().stopClient();
        }
        server.stop(true);
        SerializerRegistry.unregisterSerializer(StringSerializer.class);
        SerializerRegistry.resetDefaults();
    }
    
    /**
     * Testing.
     */
    @Test
    public void testMessages() {
        // wait for delivery
        TimeUtils.sleep(2000);
        // and assert composed result
        Assert.assertEquals("Received value on configuration stream does not match", "config DMG-1 world", received);
        
        Assert.assertNotNull("The autowired transport parameters shall not be null", params);
        Assert.assertEquals("localhost", params.getHost());
        Assert.assertEquals(ADDR.getPort(), params.getPort());
        Assert.assertEquals("", params.getApplicationId()); // no client ids here
    }
    
    /**
     * A simple test processor.
     * 
     * @author Holger Eichelberger, SSE
     */
    @SpringBootApplication
    public static class MyProcessor {

        /**
         * Produces the inbound messages.
         * 
         * @return supplier for inbound messages
         */
        @Bean
        public Supplier<String> in() {
            return () -> "DMG-1";
        }
        
        /**
         * Transforms the received input.
         * 
         * @return function transforming the input
         */
        @Bean
        public Function<String, String> transform() {
            return in -> in + " world";
        }
        
        /**
         * Receives the bounced message from the binder.
         * 
         * @return consumer instance
         */
        @Bean
        public Consumer<String> receiveInput() {
            return s -> received = s;
        }
        
        /**
         * Creates a custom message converter.
         * 
         * @return the custom message converter
         */
        @Bean
        public MessageConverter customMessageConverter() {
            return new SerializerMessageConverter(new MimeType("application", "ser-string"));
        }
      
    }

}
