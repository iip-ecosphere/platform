/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.environment.spring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.PreDestroy;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.iip_ecosphere.platform.services.environment.Service;
import de.iip_ecosphere.platform.services.environment.switching.ServiceBase;
import de.iip_ecosphere.platform.support.function.IOConsumer;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;
import de.iip_ecosphere.platform.transport.status.TraceRecord;

/**
 * Basic Spring Service functions for asynchronous forward/backward data flows via the transport layer.
 * 
 * @author Holger Eichelberger, SSE
 */
@Component
public class SpringAsyncServiceBase {

    private List<InstalledCallback> callbacks = Collections.synchronizedList(new ArrayList<>());

    /**
     * Records information about an installed callback.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class InstalledCallback {
        private ReceptionCallback<?> callback;
        private String channel;
    }

    /**
     * Creates a reception callback.
     * 
     * @param <T> the type of data handled by the callback
     * @param channel the channel to listen to
     * @param consumer the consumer function of the service
     * @param cls the type of data handled by the callback
     * @return the callback or <b>null</b> if the callback cannot be created/registered with {@link Transport}
     */
    protected <T> ReceptionCallback<T> createReceptionCallback(String channel, Consumer<T> consumer, Class<T> cls) {
        return createReceptionCallback(channel, consumer, cls, null);
    }
    
    /**
     * Ensures the setup data.
     */
    protected void ensureSetup() {
        Starter.getSetup();
    }

    /**
     * Creates a reception callback. [public for testing]
     * 
     * @param <T> the type of data handled by the callback
     * @param channel the channel to listen to
     * @param consumer the consumer function of the service
     * @param cls the type of data handled by the callback
     * @param routingKey if <b>null</b>, empty or in {@link Transport#addGlobalRoutingKey(String)} then use the 
     *     global transport instance, else the local transport instance from {@link Transport}; may use both 
     *     transport instances
     * @return the callback or <b>null</b> if the callback cannot be created/registered with {@link Transport}
     */
    public <T> ReceptionCallback<T> createReceptionCallback(String channel, Consumer<T> consumer, Class<T> cls, 
        String routingKey) {
        ReceptionCallback<T> result = null;
        ensureSetup();
        TransportConnector conn = Transport.createConnector(routingKey);
        if (null != conn) {
            try {
                result = new ReceptionCallback<T>() {
            
                    @Override
                    public void received(T data) {
                        consumer.accept(data);
                    }
            
                    @Override
                    public Class<T> getType() {
                        return cls;
                    }
            
                };
                conn.setReceptionCallback(channel, result);
                InstalledCallback icb = new InstalledCallback();
                icb.callback = result;
                icb.channel = channel;
                callbacks.add(icb);
                TransportSetup tSetup = Starter.getSetup().getTransport();
                LoggerFactory.getLogger(getClass()).info("Installed transport callback on channel {} for type {} "
                    + "with routingKey {} on {}:{}", channel, cls.getName(), routingKey, tSetup.getHost(), 
                    tSetup.getPort());
            } catch (IOException e) {
                result = null;
                LoggerFactory.getLogger(getClass()).error("No transport setup, will not listen to data on {}. {}", 
                    channel, e.getMessage());
            }
        } else {
            LoggerFactory.getLogger(getClass()).error("No transport setup, will not listen to data on {}.", channel);
        }
        return result;
    }
    
    /**
     * Sends a message of a certain {@code kind} and cares fore queuing.
     * 
     * @param sender the sender including the message
     * @param kind the kind of the message for logging
     */
    protected void send(IOConsumer<TransportConnector> sender, String kind) {
        Transport.send(sender, kind);
    }
    
    /**
     * Sends a message of a certain {@code kind} and cares fore queuing.
     * 
     * @param sender the sender including the message
     * @param kind the kind of the message for logging
     * @param routingKeys if <b>null</b>, empty or in {@link Transport#addGlobalRoutingKey(String)} then use the 
     *     global transport instance, else the local transport instance from {@link Transport}; may use both 
     *     transport instances
     * @see Transport#addGlobalRoutingKey(String)
     */
    public static void send(IOConsumer<TransportConnector> sender, String kind, String... routingKeys) {
        Transport.send(sender, kind, routingKeys);
    }
    
    /**
     * Sends a trace record. Caches messages if no connector is available.
     * 
     * @param record the record to be sent
     */
    public void sendTraceRecord(TraceRecord record) {
        Transport.sendTraceRecord(record);
    }

    /**
     * Detach all callbacks.
     */
    public void detach() {
        TransportConnector conn = Transport.getConnector();
        if (null != conn) { // well, should be there
            for (InstalledCallback icb : callbacks) {
                try {
                    conn.detachReceptionCallback(icb.channel, icb.callback);
                } catch (IOException e) {
                    LoggerFactory.getLogger(getClass()).error("Cannot detach reception callback on {}. {}", 
                        icb.channel, e.getMessage());
                }
            }
        }  else {
            LoggerFactory.getLogger(getClass()).warn("No transport setup, cannot unregister callbacks.");
        }
        callbacks.clear();
    }
    
    /**
     * Called at end of lifecycle to get rid of callbacks.
     */
    @PreDestroy
    public void destroy() {
        detach();
        Transport.releaseConnector();
    }
    
    /**
     * Composes a channel suffix id for a service possibly including the application instance id.
     * 
     * @param service the service instance
     * @param separator the separator string to insert between the ids
     * @return the id suffix, may be empty
     */
    public static String getAppInstIdSuffix(Service service, String separator) {
        String result;
        if (null != service) {
            String sId = Starter.getServiceId(service);
            result = ServiceBase.getApplicationInstanceId(sId);
            if (null == result || result.length() == 0) {
                result = "";
            } else {
                result = separator + result;
            }
        } else {
            result = "";
        }
        return result;
    }

}
