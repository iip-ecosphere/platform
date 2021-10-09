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
package de.iip_ecosphere.platform.transport.connectors.rabbitmq;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;

import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.SslUtils;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.impl.AbstractTransportConnector;

/**
 * Implements an AMQP transport connector based on RabbitMQ.
 * 
 * @author Holger Eichelberger, SSE
 */
public class RabbitMqAmqpTransportConnector extends AbstractTransportConnector {

    public static final String NAME = "AMQP";
    
    private Connection connection;
    private Channel channel;

    @Override
    public void syncSend(String stream, Object data) throws IOException {
        send(stream, data, true);
    }

    @Override
    public void asyncSend(String stream, Object data) throws IOException {
        send(stream, data, false);
    }
    
    /**
     * Sends data to {@code stream}.
     * 
     * @param stream the stream to send to
     * @param data the data to send to {@code stream}
     * @param block shall this be a blocking call (ignored)
     * @throws IOException in cases that sending fails
     */
    private void send(String stream, Object data, boolean block) throws IOException {
        if (!isStreamKnown(stream)) {
            channel.queueDeclare(stream, false, false, true, null);
            registerStream(stream);
        }
        // if not known
        byte[] payload = serialize(stream, data);
        channel.basicPublish("", stream, null, payload);
    }

    @Override
    public void setReceptionCallback(String stream, ReceptionCallback<?> callback) throws IOException {
        if (!isStreamKnown(stream)) {
            channel.queueDeclare(stream, false, false, true, null);
        }
        super.setReceptionCallback(stream, callback);
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            notifyCallback(delivery.getEnvelope().getRoutingKey(), delivery.getBody());            
        };
        channel.basicConsume(stream, true, deliverCallback, consumerTag -> { });
    }

    @Override
    public String composeStreamName(String parent, String name) {
        // no real semantics in AMQP
        String streamName = parent != null && parent.length() > 0 ? parent + "-" + name : name;
        // https://www.rabbitmq.com/queues.html
        // Queue names may be up to 255 bytes of UTF-8 characters.
        if (streamName.length() > 256) {
            streamName = streamName.substring(0, 254);
            throw new IllegalArgumentException("stream name length > 256");
        }
        return streamName;
    }

    @Override
    public void connect(TransportParameter params) throws IOException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(params.getHost());
        factory.setPort(params.getPort());
        factory.setAutomaticRecoveryEnabled(true);
        if (null != params.getUser() && null != params.getPassword()) {
            factory.setUsername(params.getUser());
            factory.setPassword(params.getPassword());
        }
        try {                
            File keystore = params.getKeystore();
            SSLContext ctx = SslUtils.createTlsContext(keystore, params.getKeystorePassword());
            if (null != ctx) {
                factory.useSslProtocol(ctx);
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error(
                "AMQP: Loading keystore " + e.getMessage() + ". Trying with no TLS.");
        }
        configureFactory(factory);
        try {
            LoggerFactory.getLogger(getClass()).info(
                "AMQP: Connecting to " + params.getHost() + " " + params.getPort());
            connection = factory.newConnection();
            channel = connection.createChannel();
        } catch (TimeoutException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * Allows further factory configuration.
     * 
     * @param factory the factory to configured
     */
    protected void configureFactory(ConnectionFactory factory) {
    }

    @Override
    public void disconnect() throws IOException {
        try {
            channel.close();
        } catch (TimeoutException e) {
            // nothing for now
        }
        connection.close();
    }

    @Override
    public String getName() {
        return NAME;
    }

}
