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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.core.MessageSource;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MimeType;

import com.rabbitmq.client.ConnectionFactory;

import de.iip_ecosphere.platform.transport.Utils;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.rabbitmq.RabbitMqAmqpTransportConnector;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;
import de.iip_ecosphere.platform.transport.spring.SerializerMessageConverter;
import de.iip_ecosphere.platform.transport.spring.binder.amqp.AmqpClient;
import test.de.iip_ecosphere.platform.transport.connectors.rabbitmq.TestQpidServer;
import test.de.iip_ecosphere.platform.transport.spring.StringSerializer;

/**
 * Test class for the message binder. This class uses the application configuration from transport.spring!
 * 
 * @author Holger Eichelberger, SSE
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
public class AmqpMessageBinderTest {

    private static TestQpidServer server;
    private static String received;

    @Autowired
    private TransportParameter params;
    
    /**
     * Initializes the test by starting an embedded AMQP server and by sending back received results on the output
     * stream to the "input2" stream. Requires the application configuration file "test.properties" in the test 
     * classpath with the username/password stated below.
     */
    @BeforeClass
    public static void init() {
        server = new TestQpidServer();
        final String host = "localhost";
        final int port = 8883;
        try {
            server.start(host, port);
        } catch (IOException e) {
            Assert.fail("BROKER PROBLEM " + e.getMessage());
        }
        Utils.sleep(1000);
        SerializerRegistry.registerSerializer(StringSerializer.class);
        final RabbitMqAmqpTransportConnector infra = new RabbitMqAmqpTransportConnector() {
            
            protected void configureFactory(ConnectionFactory factory) {
                factory.setUsername("user");
                factory.setPassword("pwd");
            }
            
        };
        try {
            infra.connect(new TransportParameter(host, port, "infra"));
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
        System.out.println("Started infra client on " + host + " " + port);
        Utils.sleep(1000);
    }
    
    /**
     * Shuts down client and test server.
     */
    @AfterClass
    public static void shutdown() {
        AmqpClient.stopClient();
        server.stop();
        SerializerRegistry.unregisterSerializer(StringSerializer.class);
    }
    
    /**
     * Testing.
     */
    @Test
    public void testMessages() {
        // wait for delivery
        Utils.sleep(2000);
        // and assert composed result
        Assert.assertEquals("Received value on configuration stream does not match", "config DMG-1 world", received);
        
        Assert.assertNotNull("The autowired transport parameters shall not be null", params);
        Assert.assertEquals("localhost", params.getHost());
        Assert.assertEquals(8883, params.getPort());
        Assert.assertEquals("", params.getClientId()); // no client ids here
    }

    /**
     * Defines a processor with additional input for receiving messages from the binder/test broker server.
     * 
     * @author Holger Eichelberger, SSE
     */
    interface Proc extends Processor {

        /**
         * A second input.
         * 
         * @return the input channel
         */
        @Input
        SubscribableChannel input2();

    }
    
    @SpringBootApplication
    @EnableBinding(Proc.class)
    public static class MyProcessor {

        /**
         * Produces the inbound messages.
         * 
         * @return the produced messages
         */
        @Bean
        @InboundChannelAdapter(value = Processor.INPUT, poller = @Poller(fixedDelay = "200", maxMessagesPerPoll = "1"))
        public MessageSource<String> in() {
            return () -> new GenericMessage<String>("DMG-1");
        }
        
        /**
         * Transforms the received input.
         * 
         * @param in the input
         * @return the transformed input
         */
        @Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
        public String transform(String in) {
            return in + " world";
        }
        
        /**
         * Receives the bounced message from the binder.
         * 
         * @param value the value
         */
        @StreamListener("input2")
        public void receiveInput(String value) {
            received = value;
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
