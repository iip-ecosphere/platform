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

import org.springframework.cloud.stream.binder.AbstractMessageChannelBinder;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

/**
 * A MQTT v3 message binder turning messages to be sent into MQTT messages.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MqttV3MessageBinder extends AbstractMessageChannelBinder<ConsumerProperties, ProducerProperties, 
    MqttV3MessageBinderProvisioner> {

    private MqttClient client;
    
    /**
     * Creates a message binder instance.
     * 
     * @param headersToEmbed the headers to embed
     * @param provisioningProvider the provisioning provider including the destination information
     * @param client the client instance
     */
    public MqttV3MessageBinder(String[] headersToEmbed, MqttV3MessageBinderProvisioner provisioningProvider, 
        MqttClient client) {
        super(headersToEmbed, provisioningProvider);
        this.client = client;
    }

    @Override
    protected MessageHandler createProducerMessageHandler(ProducerDestination destination,
            ProducerProperties producerProperties, MessageChannel errorChannel) throws Exception {
        return message -> {
            client.send(destination.getName(), (byte[]) message.getPayload());
        };
    }

    @Override
    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group,
            ConsumerProperties properties) throws Exception {
        return new MqttV3MessageProducer(destination, client);
    }

}
