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

package de.iip_ecosphere.platform.transport.spring;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.util.MimeType;

import de.iip_ecosphere.platform.transport.serialization.Serializer;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;

/**
 * Generic Spring Cloud Stream message converter for {@link SerializerRegistry}. Register your (generated) serializers
 * at startup time of the application. Applied to streams with the correct mime type, e.g., via 
 * {@code spring.cloud.stream.default.contentType=application/ser-string}.
 *  
 * @author Holger Eichelberger, SSE
 */
public class SerializerMessageConverter extends AbstractMessageConverter {

    /**
     * The default mime type for IIP-Ecosphere serialized data types via the transport layer.
     */
    public static final MimeType MIME_TYPE = new MimeType("application", "iip");

    private static final Logger LOGGER = LoggerFactory.getLogger(SerializerMessageConverter.class);

    /**
     * Creates an instance for {@link #MIME_TYPE}.
     */
    public SerializerMessageConverter() {
        super(MIME_TYPE);
    }

    /**
     * Creates an instance for a given mime type. The mime type shall be defined based on the used serializers.
     * 
     * @param mimeType the mime type
     */
    public SerializerMessageConverter(MimeType mimeType) {
        super(mimeType);
    }
    
    @Override
    protected boolean supports(Class<?> clazz) {
        return SerializerRegistry.hasSerializer(clazz);
    }
    
    @Override
    protected Object convertFromInternal(Message<?> message, Class<?> targetClass, Object conversionHint) {
        Object payload = message.getPayload();
        Serializer<?> serializer = SerializerRegistry.getSerializer(targetClass);
        if (null != serializer) {
            try {
                payload = serializer.from((byte[]) payload);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            LOGGER.error("No serializer found for " + payload.getClass().getName() 
                + " although type seems to be supported");
        }
        return payload;
    }
    
    @Override
    protected Object convertToInternal(Object payload, MessageHeaders headers, Object conversionHint) {
        return serialize(payload);
    }
    
    /**
     * Serializes the given payload.
     * 
     * @param <T> the type of the payload
     * @param payload the payload to be serialized
     * @return the serialized payload as {@code byte[]} wire format
     */
    private <T> Object serialize(T payload) {
        Object result;
        @SuppressWarnings("unchecked")
        Serializer<T> serializer = (Serializer<T>) SerializerRegistry.getSerializer(payload.getClass());
        if (null != serializer) {
            try {
                result = serializer.to(payload);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            LOGGER.error("No serializer found for " + payload.getClass().getName() 
                + " although type seems to be supported");
            result = payload;
        }
        return result;
    }

    /**
     * "Manual" serialization and sending over a binding with <code>useNativeEncoding=false</code>.
     * 
     * @param <T> the type of the payload
     * @param streamBridge the stream bridge to send to
     * @param bindingName the binding name to be used as channel
     * @param payload the payload to send
     */
    public static <T> void serializeAndSend(StreamBridge streamBridge, String bindingName, T payload) {
        @SuppressWarnings("unchecked")
        Serializer<T> serializer = (Serializer<T>) SerializerRegistry.getSerializer(payload.getClass());
        if (null != serializer) {
            try {
                streamBridge.send(bindingName, MessageBuilder.withPayload(serializer.to(payload)).build());
            } catch (IOException e) {
                LOGGER.error("Cannot send instance of " + payload.getClass().getName() 
                    + ": " + e.getMessage());
            }
        } else {
            LOGGER.error("No serializer found for " + payload.getClass().getName() 
                + " although type seems to be supported. Cannot send instance.");
        }
    }

}
