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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.Queue;

import de.iip_ecosphere.platform.support.net.SslUtils;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
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
    private boolean tlsEnabled = false;
    private Map<String, String> tags = Collections.synchronizedMap(new HashMap<>());
    private boolean closing = false;
    private Map<String, String> queueStream = Collections.synchronizedMap(new HashMap<>());

    @Override
    public void syncSend(String stream, Object data) throws IOException {
        send(stream, data, true);
    }

    @Override
    public void asyncSend(String stream, Object data) throws IOException {
        send(stream, data, false);
    }
    
    /**
     * Checks the given {@code stream}.
     * 
     * @param stream the stream
     * @param send sending or receiving
     * @throws IOException if stream operations cannot be carried out
     */
    private void checkStream(String stream, boolean send) throws IOException {
        if (!isStreamKnown(stream)) {
            channel.exchangeDeclare(stream, BuiltinExchangeType.FANOUT, false, true, null);
            registerStream(stream);
        }
        if (!send && queueStream.get(stream) == null) {
            Queue.DeclareOk qRes = channel.queueDeclare();
            queueStream.put(stream, qRes.getQueue()); 
            channel.queueBind(qRes.getQueue(), stream, "");
        }
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
        try {
            checkStream(stream, true);
            // if not known
            byte[] payload = serialize(stream, data);
            channel.basicPublish(stream, "", null, payload);
        } catch (IOException e) {
            if (!closing) {
                throw e;
            }
        } catch (AlreadyClosedException e) {
            // ok, let's forget about that
        }
    }

    @Override
    public void setReceptionCallback(String stream, ReceptionCallback<?> callback) throws IOException {
        checkStream(stream, false);
        super.setReceptionCallback(stream, callback);
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            Envelope env = delivery.getEnvelope();
            String st = env.getExchange(); // new approach
            if (null == st || st.length() == 0) { // legacy
                st = env.getRoutingKey();
            }
            notifyCallback(st, delivery.getBody());
            //channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        String tag = UUID.randomUUID().toString();
        channel.basicConsume(queueStream.get(stream), true, tag, deliverCallback, consumerTag -> { });
        tags.put(stream, tag);
    }

    @Override
    public void unsubscribe(String stream, boolean delete) throws IOException {
        super.unsubscribe(stream, delete);
        String tag = tags.remove(stream);
        if (null != tag) {
            channel.basicCancel(tag);
        }
        if (delete) {
            channel.queueDeleteNoWait(stream, true, false);
        }
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
        super.connect(params);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(params.getHost());
        factory.setPort(params.getPort());
        factory.setAutomaticRecoveryEnabled(true);
        applyAuthenticationKey(params.getAuthenticationKey(), (user, pwd, enc) -> {
            factory.setUsername(user);
            factory.setPassword(pwd);
            return true;
        });
        if (useTls(params)) {
            try {                
                factory.useSslProtocol(createTlsContext(params));
                tlsEnabled = true;
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error(
                    "AMQP: Loading keystore " + e.getMessage() + ". Trying with no TLS.");
            }
        }
        configureFactory(factory);
        try {
            LoggerFactory.getLogger(getClass()).info(
                "AMQP: Connecting to " + params.getHost() + " " + params.getPort());
            connection = factory.newConnection();
            channel = connection.createChannel();
            //channel.basicQos(10); //channel.basicQos(0, 0, true);
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
        if (!closing) {
            closing = true;
            super.disconnect();
            try {
                channel.close();
            } catch (TimeoutException e) {
                // nothing for now
            } catch (AlreadyClosedException e) {
                // ok, fine
            }
            try {
                connection.close();
            }  catch (AlreadyClosedException e) {
                // ok, fine
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
    
    @Override
    public String supportedEncryption() {
        return SslUtils.CONTEXT_ALG_TLS;
    }

    @Override
    public String enabledEncryption() {
        return tlsEnabled ? SslUtils.CONTEXT_ALG_TLS : null;
    }

}
