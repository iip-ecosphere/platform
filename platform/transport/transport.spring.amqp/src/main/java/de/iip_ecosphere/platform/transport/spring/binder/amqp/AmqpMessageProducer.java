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

package de.iip_ecosphere.platform.transport.spring.binder.amqp;

import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

/**
 * Implements a AMQP message producer.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AmqpMessageProducer extends MessageProducerSupport {

    private final ConsumerDestination destination;
    
    /**
     * Creates a message producer instance.
     * 
     * @param destination the consumer destination
     */
    public AmqpMessageProducer(ConsumerDestination destination) {
        this.destination = destination;
    }
    
    /**
     * The topic-specific reception callback.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class Callback implements AmqpClient.ArrivedCallback {

        @Override
        public void messageArrived(String topic, byte[] payload) {
            Message<byte[]> msg = MessageBuilder.withPayload(payload).build();
            sendMessage(msg);
        }

    }

    @Override
    public void doStart() {
        AmqpClient.subscribeTo(destination.getName(), new Callback());
    }

    @Override
    protected void doStop() {
        AmqpClient.unsubscribeFrom(destination.getName());
    }

}
