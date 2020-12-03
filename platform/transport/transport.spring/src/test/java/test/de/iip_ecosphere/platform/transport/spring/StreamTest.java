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

package test.de.iip_ecosphere.platform.transport.spring;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MimeType;

import de.iip_ecosphere.platform.transport.spring.SerializerMessageConverter;

/**
 * Brings up a spring cloud stream that uses the string serializer. This test requires setting the default 
 * contentType for the streams so that the serializer can be applied.
 * 
 * @author Holger Eichelberger, SSE
 */
@SpringBootTest(classes = StreamTest.class)
@RunWith(SpringRunner.class)
@ComponentScan(basePackages = "de.iip_ecosphere.platform.transport.spring")
public class StreamTest {

    /**
     * Tests the stream application.
     */
    @Test
    public void testStream() {
        TestCounters.reset();
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
            TestChannelBinderConfiguration.getCompleteConfiguration(
                MyProcessor.class)).run()) {
            InputDestination source = context.getBean(InputDestination.class);
            // OutputDestination target = context.getBean(OutputDestination.class);
            source.send(new GenericMessage<byte[]>("DMG-1".getBytes()));
            // the real result value is a bit timing dependent, but not really required here
            //System.out.println(new String(target.receive().getPayload()));
            Assert.assertEquals(1, TestCounters.getFromCount());
            Assert.assertEquals(1, TestCounters.getToCount());
            Assert.assertEquals(0, TestCounters.getCloneCount());
        }        
    }
    
    /**
     * A simple processor class.
     * 
     * @author Holger Eichelberger, SSE
     */
    @SpringBootApplication
    @EnableBinding(Processor.class)
    public static class MyProcessor {
        
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
