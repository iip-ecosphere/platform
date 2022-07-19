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

package de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.json.Json;
import javax.json.JsonObject;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.status.ActionTypes;
import de.iip_ecosphere.platform.transport.status.StatusMessage;
import de.iip_ecosphere.platform.transport.streams.StreamNames;

/**
 * Implements the basis of a device heartbeat watcher.
 * 
 * @author Holger Eichelberger, SSE
 */
public class HeartbeatWatcher {

    private long timeout = 4000; // ms
    private Map<String, Long> received = Collections.synchronizedMap(new HashMap<String, Long>());
    private ReceptionCallback<?> metricsCallback;
    private ReceptionCallback<?> statusCallback;
    
    /**
     * Creates a reception callback to be used with {@link StreamNames#SERVICE_METRICS} or 
     * {@link StreamNames#RESOURCE_METRICS}. You may also directly call {@link #notifyRecordReceived(String)}.
     * 
     * @return the reception callback
     */
    public ReceptionCallback<?> createMetricsReceptionCallback() {
        return new ReceptionCallback<String>() {
    
            @Override
            public void received(String data) {
                JsonObject obj = Json.createReader(new StringReader(data)).readObject();
                notifyRecordReceived(obj.getString("id"));
            }

            @Override
            public Class<String> getType() {
                return String.class;
            }
            
        };
    }
    
    /**
     * Creates a reception callback to be used with {@link StreamNames#STATUS_STREAM}. You may also directly call 
     * {@link #notifyRecordReceived(String)}.
     * 
     * @return the reception callback
     */
    public ReceptionCallback<?> createStatusReceptionCallback() {
        return new ReceptionCallback<StatusMessage>() {
            
            @Override
            public void received(StatusMessage msg) {
                if (ActionTypes.REMOVED == msg.getAction()) {
                    notifyRecordDeleted(msg.getDeviceId());
                } else {
                    notifyRecordReceived(msg.getDeviceId());
                }
            }

            @Override
            public Class<StatusMessage> getType() {
                return StatusMessage.class;
            }
            
        };
    }
    
    /**
     * Creates two reception callbacks and installs them into {@code connector}.
     * Stores the callbacks in this class.
     * 
     * @param connector the connector to install the callbacks into
     * @throws IOException if installation fails
     */
    public void installInto(TransportConnector connector) throws IOException {
        if (null == connector) {
            throw new IOException("No transport connector given");
        }
        metricsCallback = createMetricsReceptionCallback();
        statusCallback = createStatusReceptionCallback();
        connector.setReceptionCallback(StreamNames.SERVICE_METRICS, metricsCallback);
        connector.setReceptionCallback(StreamNames.RESOURCE_METRICS, metricsCallback);
        connector.setReceptionCallback(StreamNames.STATUS_STREAM, statusCallback);
        LoggerFactory.getLogger(HeartbeatWatcher.class).info("Installed watcher on {}, {} and {}", 
            StreamNames.SERVICE_METRICS, StreamNames.RESOURCE_METRICS, StreamNames.STATUS_STREAM);
    }
    
    /**
     * Uninstalls the two stored callbacks from {@code connector}.
     * Deletes the callbacks from this class.
     * 
     * @param connector the connector to uninstall the callbacks from
     * @throws IOException if uninstallation fails
     */
    public void uninstallFrom(TransportConnector connector) throws IOException {
        if (null == connector) {
            throw new IOException("No transport connector given");
        }
        if (null != metricsCallback) {
            connector.detachReceptionCallback(StreamNames.SERVICE_METRICS, metricsCallback);
            connector.detachReceptionCallback(StreamNames.RESOURCE_METRICS, metricsCallback);
            LoggerFactory.getLogger(HeartbeatWatcher.class).info("Uninstalled watcher from {} and {}", 
                StreamNames.SERVICE_METRICS, StreamNames.RESOURCE_METRICS);
        }
        if (null != statusCallback) {
            connector.detachReceptionCallback(StreamNames.STATUS_STREAM, statusCallback);
            LoggerFactory.getLogger(HeartbeatWatcher.class).info("Uninstalled watcher from {}", 
                StreamNames.STATUS_STREAM);
        }
        metricsCallback = null;
        statusCallback = null;
    }
    
    /**
     * Called to notify that we received a signal from {@code deviceId}.
     * 
     * @param deviceId the deviceId we received a signal/event from
     */
    public void notifyRecordReceived(String deviceId) {
        if (null != deviceId) {
            received.put(deviceId, System.currentTimeMillis());
        }
    }

    /**
     * Called to notify that we received a deletion signal from {@code deviceId}.
     * 
     * @param deviceId the deviceId we received a signal/event from
     */
    public void notifyRecordDeleted(String deviceId) {
        if (null != deviceId) {
            received.remove(deviceId);
        }
    }

    /**
     * Deletes outdated entries and informs {@code outdatedHandler} about the removed entry.
     * To be used in an existing timer. Uses {@link #timeout} as timeout for entries.
     * 
     * @param outdatedHandler the outdated handler
     */
    public void deleteOutdated(Consumer<String> outdatedHandler) {
        deleteOutdated(timeout, outdatedHandler);
    }

    /**
     * Deletes outdated entries and informs {@code outdatedHandler} about the removed entry.
     * To be used in an existing timer.
     * 
     * @param timeout for deletion in ms
     * @param outdatedHandler the outdated handler
     */
    public void deleteOutdated(long timeout, Consumer<String> outdatedHandler) {
        long now = System.currentTimeMillis();
        List<Map.Entry<String, Long>> tmp = new ArrayList<>();
        tmp.addAll(received.entrySet());
        Collections.sort(tmp, (e1, e2) -> Long.compare(e1.getValue(), e2.getValue()));
        for (int i = 0; i < tmp.size(); i++) {
            Map.Entry<String, Long> entry = tmp.get(i);
            long lastNotified = entry.getValue();
            if (now - lastNotified > timeout) {
                String deviceId = entry.getKey();
                received.remove(deviceId);
                outdatedHandler.accept(deviceId);
            } else {
                break;
            }
        }
    }
    
    /**
     * Returns the number of devices known to this class.
     * 
     * @return the number of devices
     */
    public int getDeviceCount() {
        return received.size();
    }

    /**
     * Clears all devices/entries.
     */
    public void clear() {
        received.clear();
    }
    
    /**
     * Changes the timeout.
     * 
     * @param tout the timeout in ms
     * @return the timeout set before calling this method
     */
    public long setTimeout(long tout) {
        long orig = timeout;
        timeout = tout;
        return orig;
    }
    
}
