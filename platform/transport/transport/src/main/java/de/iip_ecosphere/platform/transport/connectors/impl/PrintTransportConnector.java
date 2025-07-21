/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.transport.connectors.impl;

import java.io.IOException;

import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.transport.serialization.Serializer;

/**
 * A default print transport consumer for debugging.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PrintTransportConnector extends AbstractTransportConnector {

    private TransportConsumer consumer = (s, d) -> System.out.println(s + ":" + new String(d));

    /**
     * Consumer for transport data.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface TransportConsumer {
        
        /**
         * Accepts a stream and serialized data.
         * 
         * @param stream the stream to send to
         * @param data the serialized data
         */
        public void accept(String stream, byte[] data);
        
    }

    /**
     * Changes the transport/print consumer.
     * 
     * @param consumer the new consumer, ignored if <b>null</b>
     */
    public void setTransportConsumer(TransportConsumer consumer) {
        if (null != consumer) {
            this.consumer = consumer;
        }
    }
    
    @Override
    public void syncSend(String stream, Object data) throws IOException {
        send(stream, data);
    }

    @Override
    public void asyncSend(String stream, Object data) throws IOException {
        send(stream, data);
    }
    
    /**
     * Sends data to {@code stream}.
     * 
     * @param stream the stream to send to
     * @param data the data to send to {@code stream}
     * @throws IOException in cases that sending fails
     */
    private void send(String stream, Object data) throws IOException {
        try {
            byte[] payload = serialize(stream, data);
            consumer.accept(stream, payload);
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("While sending/serializing data: {}", e.getMessage());
        }
    }

    @Override
    protected <T> byte[] serialize(String stream, T data) throws IOException {
        // replicated from base class
        byte[] result;
        @SuppressWarnings("unchecked")
        Class<T> cls = (Class<T>) data.getClass();
        Serializer<T> serializer = getSerializerProvider().getSerializer(cls);
        if (null != serializer) {
            System.out.println("Serializing " + data.getClass() + " through " + getSerializerProvider().getClass() 
                + " by " + serializer.getClass());
            result = serializer.to(data);
        } else {
            System.out.println("Serializing " + data.getClass() + " through " + getSerializerProvider().getClass() 
                + " to default empty array as no serializer is available");
            result = new byte[0];
        }
        return result;
    }

    @Override
    public String composeStreamName(String parent, String name) {
        return parent + "/" + name;
    }

    @Override
    public String getName() {
        return "Print transport connector (DEBUG)";
    }

    @Override
    public String supportedEncryption() {
        return null;
    }

    @Override
    public String enabledEncryption() {
        return null;
    }

}
