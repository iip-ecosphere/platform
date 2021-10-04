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

package de.iip_ecosphere.platform.transport.spring.binder.mqttv3;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

/**
 * Implements a MQTT v3 message producer.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MqttV3MessageProducer extends MessageProducerSupport {

    private final ConsumerDestination destination;
    private MqttClient client;
    
    /**
     * Creates a message producer instance.
     * 
     * @param destination the consumer destination
     * @param client the client instance
     */
    public MqttV3MessageProducer(ConsumerDestination destination, MqttClient client) {
        this.destination = destination;
        this.client = client;
    }
    
    /**
     * The topic-specific reception callback.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class Callback implements MqttClient.ArrivedCallback {

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            byte[] payload = message.getPayload();
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
