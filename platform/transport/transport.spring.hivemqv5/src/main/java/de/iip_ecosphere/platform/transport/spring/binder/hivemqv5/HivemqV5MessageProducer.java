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

package de.iip_ecosphere.platform.transport.spring.binder.hivemqv5;

import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

/**
 * Implements a HiveMq message producer.
 * 
 * @author Holger Eichelberger, SSE
 */
public class HivemqV5MessageProducer extends MessageProducerSupport {

    private final ConsumerDestination destination;
    private HivemqV5Client client;
    
    /**
     * Creates a message producer instance.
     * 
     * @param destination the consumer destination
     * @param client the client instance
     */
    public HivemqV5MessageProducer(ConsumerDestination destination, HivemqV5Client client) {
        this.destination = destination;
        this.client = client;
    }
    
    /**
     * The topic-specific reception callback.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class Callback implements HivemqV5Client.ArrivedCallback {

        @Override
        public void messageArrived(String topic, byte[] payload) {
            Message<byte[]> msg = MessageBuilder.withPayload(payload).build();
            sendMessage(msg);
        }

    }

    @Override
    public void doStart() {
        client.subscribeTo(destination.getName(), new Callback());
    }

    @Override
    protected void doStop() {
        client.unsubscribeFrom(destination.getName());
    }

}
