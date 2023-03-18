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

package de.iip_ecosphere.platform.configuration;

import java.io.IOException;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.status.ActionTypes;
import de.iip_ecosphere.platform.transport.status.ComponentTypes;
import de.iip_ecosphere.platform.transport.status.StatusMessage;

/**
 * Simple pre-processing cache of {@link StatusMessage} for UI display.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StatusCache {
    
    private static Map<String, Map<String, String>> serviceStates = Collections.synchronizedMap(new HashMap<>());
    private static ReceptionCallback<StatusMessage> statusCallback = new ReceptionCallback<StatusMessage>() {

        @Override
        public void received(StatusMessage data) {
            if (ComponentTypes.SERVICE == data.getComponentType()) {
                handleServiceStateChange(data);
            }
        }

        @Override
        public Class<StatusMessage> getType() {
            return StatusMessage.class;
        }
        
    };

    /**
     * Ensures the existence of a device-state map for the service message {@code message}.
     * 
     * @param message the message
     * @return the device-state map
     */
    private static Map<String, String>  ensureServicePerDevice(StatusMessage message) {
        Map<String, String> servicePerDevice = serviceStates.get(message.getId());
        if (null == servicePerDevice) {
            servicePerDevice = new HashMap<>();
            serviceStates.put(message.getId(), servicePerDevice);
        }
        return servicePerDevice;
    }

    /**
     * Handles a service state change.
     * 
     * @param message the message indicating the service status change
     */
    private static void handleServiceStateChange(StatusMessage message) {
        if (ActionTypes.ADDED == message.getAction()) {
            ensureServicePerDevice(message).put(message.getDeviceId(), message.getDescription());
        } else if (ActionTypes.CHANGED == message.getAction()) {
            ensureServicePerDevice(message).put(message.getDeviceId(), message.getDescription());
        } else if (ActionTypes.REMOVED == message.getAction()) {
            Map<String, String> servicePerDevice = serviceStates.get(message.getId());
            if (null != servicePerDevice) {
                servicePerDevice.remove(message.getDeviceId());
                if (servicePerDevice.isEmpty()) {
                    serviceStates.remove(message.getId());
                }
            }
        }
    }
    
    /**
     * Denotes the state of a service on a certain device.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class ServiceDeviceState {
        
        private String deviceId;
        private String state;

        /**
         * Creates an instance.
         * 
         * @param deviceId the device id
         * @param state the service state
         */
        private ServiceDeviceState(String deviceId, String state) {
            this.deviceId = deviceId;
            this.state = state;
        }

        /**
         * Returns the device id.
         * 
         * @return the device id
         */
        public String getDeviceId() {
            return deviceId;
        }
 
        /**
         * Returns the service state.
         * 
         * @return the service state
         */
        public String getState() {
            return state;
        }
        
    }
    
    /**
     * Returns the states of service {@code sId} on all known devices. If a device is not mentioned,
     * the service is not running there.
     * 
     * @param sId the service id
     * @param consumer a consumer receiving the data
     */
    public static void getServiceStates(String sId, Consumer<ServiceDeviceState> consumer) {
        Map<String, String> servicePerDevice = serviceStates.get(sId);
        if (null != servicePerDevice) {
            try {
                for (Map.Entry<String, String> e: servicePerDevice.entrySet()) {
                    consumer.accept(new ServiceDeviceState(e.getKey(), e.getValue()));
                }
            } catch (ConcurrentModificationException e) {
                // safe side, information is just descriptive
            }
        }
    }

    /**
     * Starts the status cache.
     */
    public static void start() {
        TransportConnector conn = Transport.getConnector();
        if (null != conn) {
            try {
                conn.setReceptionCallback(StatusMessage.STATUS_STREAM, statusCallback);
            } catch (IOException e) {
                LoggerFactory.getLogger(StatusCache.class).warn("While attaching StatusCache: {}", e.getMessage());
            }
        } else {
            LoggerFactory.getLogger(StatusCache.class).warn("No transport connector. Cannot attach StatusCache. No "
                + "status updates for UI");
        }
    }

    /**
     * Stops the status cache.
     */
    public static void stop() {
        TransportConnector conn = Transport.getConnector();
        if (null != conn) {
            try {
                conn.detachReceptionCallback(StatusMessage.STATUS_STREAM, statusCallback);
            } catch (IOException e) {
                LoggerFactory.getLogger(StatusCache.class).warn("While detaching StatusCache: {}", e.getMessage());
            }
        }
    }

}
