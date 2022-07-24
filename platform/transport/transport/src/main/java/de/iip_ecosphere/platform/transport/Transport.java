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

package de.iip_ecosphere.platform.transport;

import java.io.IOException;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.function.IOConsumer;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;
import de.iip_ecosphere.platform.transport.status.ActionType;
import de.iip_ecosphere.platform.transport.status.ActionTypes;
import de.iip_ecosphere.platform.transport.status.Alert;
import de.iip_ecosphere.platform.transport.status.ComponentTypes;
import de.iip_ecosphere.platform.transport.status.StatusMessage;
import de.iip_ecosphere.platform.transport.status.TraceRecord;

/**
 * Global and local transport support. At startup of the platform, sending messages and creating a connector
 * may happen in parallel. So far, there is no strategy to clean up the queue, i.e., {@link #createConnector()}
 * or one of the send methods in this class are expected to be called in reasonable manner. 
 * 
 * This class supports two internal transport instances, the default global transport instance (for a global
 * transport broker, initially the same as the local broker), and the local broker for local communication
 * without using the external network (to be activated by {@link #setLocalSetup(Supplier)}).
 * 
 * @author Holger Eichelberger, SSE
 */
public class Transport {

    private static Predicate<TraceRecord> traceFilter;
    private static TransportInstance globalTransport = new TransportInstance();
    private static TransportInstance localTransport = globalTransport;
    private static Set<String> globalRoutingKeys = new HashSet<>();
    
    /**
     * An instance of the transport.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class TransportInstance {
    
        private TransportConnector connector;
        private boolean stayOffline = false;
        private Queue<IOConsumer<TransportConnector>> queue = new ConcurrentLinkedDeque<>();
        private Supplier<TransportSetup> transportSupplier;

        /**
         * Creates a transport instance without transport setup information (for deferred setup). 
         * Call {@link #setTransportSetup(Supplier)} afterwards.
         */
        public TransportInstance() {
        }

        /**
         * Creates a transport information instance and sets the transport information.
         * 
         * @param supplier the transport supplier
         * @see #setTransportSetup(Supplier)
         */
        public TransportInstance(Supplier<TransportSetup> supplier) {
            setTransportSetup(supplier);
        }
        
        /**
         * Sets up the transport information.
         * 
         * @param supplier the transport supplier
         */
        public void setTransportSetup(Supplier<TransportSetup> supplier) {
            transportSupplier = supplier;
        }
        
        /**
         * Sends a service status message. Uses {@link Id#getDeviceId()}. Calls {@link #createConnector()} to obtain
         * a connector instance on demand.
         * 
         * @param action the action on the service
         * @param serviceId the service Id
         * @param aliasIds optional alias ids for the service
         */
        public void sendServiceStatus(ActionType action, String serviceId, String... aliasIds) {
            sendStatus(new StatusMessage(ComponentTypes.SERVICE, action, serviceId, Id.getDeviceId(), aliasIds));
        }

        /**
         * Sends a service artifact status message. Uses {@link Id#getDeviceId()}. Calls {@link #createConnector()} to 
         * obtain a connector instance on demand.
         * 
         * @param action the action on the artifact
         * @param artifactId the artifact Id
         * @param aliasIds optional alias ids for the artifact 
         */
        public void sendServiceArtifactStatus(ActionType action, String artifactId, String... aliasIds) {
            sendStatus(new StatusMessage(ComponentTypes.SERVICE_ARTIFACT, action, artifactId, Id.getDeviceId(), 
                aliasIds));
        }

        /**
         * Sends a container status message. Uses {@link Id#getDeviceId()}. Calls {@link #createConnector()} to obtain
         * a connector instance on demand.
         * 
         * @param action the action on the container
         * @param containerId the container Id
         * @param aliasIds optional alias ids for the container
         */
        public void sendContainerStatus(ActionType action, String containerId, String... aliasIds) {
            sendStatus(new StatusMessage(ComponentTypes.CONTAINER, action, containerId, Id.getDeviceId(), aliasIds));
        }

        /**
         * Sends a resource status message for this resource. Calls {@link #createConnector()} to obtain
         * a connector instance on demand.
         * 
         * @param action the action on the container
         * @param aliasIds optional alias ids for the resource 
         */
        public void sendResourceStatus(ActionType action, String... aliasIds) {
            sendResourceStatus(action, null, aliasIds);
        }

        /**
         * Sends a resource status message.  Calls {@link #createConnector()} to obtain
         * a connector instance on demand.
         * 
         * @param action the action on the container
         * @param deviceId the device id, may be <b>null</b> then {@link Id#getDeviceId()} is used
         * @param aliasIds optional alias ids for the resource 
         */
        public void sendResourceStatus(ActionType action, String deviceId, String... aliasIds) {
            sendStatus(new StatusMessage(action, null == deviceId ? Id.getDeviceId() : deviceId, aliasIds));
        }
        
        /**
         * Sends a message of a certain {@code kind} and cares for queuing.
         * 
         * @param sender the sender including the message
         * @param kind the kind of the message for logging
         */
        public void send(IOConsumer<TransportConnector> sender, String kind) {
            createConnector();
            if (null != connector) {
                try {
                    sender.accept(connector);
                } catch (IOException e) {
                    LoggerFactory.getLogger(Transport.class).error(
                        "Cannot sent {} message: {}", kind, e.getMessage());
                } catch (NullPointerException e) { // preliminary, may occur if the connector is not yet connected
                    LoggerFactory.getLogger(Transport.class).error(
                        "Cannot sent {} message: Connector not yet connected (NPE)");
                }
            } else {
                queue.add(sender);
                LoggerFactory.getLogger(Transport.class).error(
                    "Cannot sent {} message now. Queued message until connector becomes available.", kind);
            }
        }

        /**
         * Defines a trace filter.
         * 
         * @param filter the filter, <b>null</b> for no filter
         */
        public void setTraceFilter(Predicate<TraceRecord> filter) {
            traceFilter = filter;
        }
        
        /**
         * Sends a trace record. Calls {@link #createConnector()} to obtain
         * a connector instance on demand. Caches messages if no connector is available.
         * 
         * @param record the record to be sent
         */
        public void sendTraceRecord(TraceRecord record) {
            if (null == traceFilter || traceFilter.test(record)) {
                send(c -> record.send(c), "trace"); 
            }
        }
        
        /**
         * Sends information about a processing status.
         * 
         * @param componentId the component id
         * @param step the step [0; max]
         * @param max the maximum step
         * @param description the description of the task
         */
        public void sendProcessStatus(String componentId, int step, int max, String description) {
            sendProcessStatus(componentId, step, max, description, null);
        }

        /**
         * Sends information about a processing status.
         * 
         * @param componentId the component id
         * @param step the step [0; max]
         * @param max the maximum step
         * @param description the description of the task
         * @param subDescription the description of an optional sub-task within the actual task, may be 
         *     <b>null</b> or empty
         */
        public void sendProcessStatus(String componentId, int step, int max, String description, 
            String subDescription) {
            StatusMessage msg = new StatusMessage(ActionTypes.PROCESS, componentId, Id.getDeviceId())
                .withDescription(description)
                .withSubDescription(subDescription);
            send(c -> msg.send(c), "progress status");
        }

        /**
         * Sends a status message. Calls {@link #createConnector()} to obtain
         * a connector instance on demand. Caches messages if no connector is available.
         * 
         * @param msg the message to be sent
         */
        public void sendStatus(StatusMessage msg) {
            send(c -> msg.send(c), "status"); 
        }

        /**
         * Sends an alert message. Calls {@link #createConnector()} to obtain
         * a connector instance on demand. Caches messages if no connector is available.
         * 
         * @param alert the alert to be sent
         */
        public void sendAlert(Alert alert) {
            send(c -> alert.send(c), "alert"); 
        }

        /**
         * Tries creating a connector. If successful, {@link #connector} will be initialized for caching. The instance 
         * is cached. The transport information must be set up before {@link #setTransportSetup(Supplier)}. After 
         * successfully creating a connector, queued messages are sent and removed from the queue. However, there is no 
         * guarantee that a connector can be created.
         * 
         * @return the (cached) connector, may be <b>null</b> 
         * 
         * @see #setTransportSetup(Supplier)
         * @see #releaseConnector()
         * @see #releaseConnector(boolean)
         */
        public TransportConnector createConnector() {
            if (null == connector && !stayOffline) {
                if (null != transportSupplier) {
                    TransportParameter params = transportSupplier.get().createParameter();
                    try {
                        TransportConnector con = TransportFactory.createConnector();
                        con.connect(params);
                        connector = con;
                        if (!queue.isEmpty()) {
                            new Thread(() -> {
                                while (queue.isEmpty()) {
                                    try {
                                        queue.remove().accept(connector);
                                    } catch (IOException e) {
                                        LoggerFactory.getLogger(Transport.class).error(
                                            "Cannot sent deferred status message: " + e.getMessage() + ". Dropping.");
                                    }
                                }
                            }).start();
                        }
                    } catch (IOException e) {
                        LoggerFactory.getLogger(Transport.class).error(
                            "Cannot create transport connector: " + e.getMessage());
                        connector = null;
                    }
                }
            }
            return connector;
        }

        /**
         * Releases an existing connector and stays offline.
         */
        public void releaseConnector() {
            releaseConnector(true);
        }
        
        /**
         * Releases an existing connector.
         * 
         * @param stayOff whether a call to {@link #createConnector()} shall create a new connector or prevent sending 
         *     further messages.
         */
        public void releaseConnector(boolean stayOff) {
            if (null != connector) {
                try {
                    connector.disconnect();
                    connector = null;
                } catch (IOException e) {
                    LoggerFactory.getLogger(Transport.class).error(
                        "Cannot disconnect transport connector: " + e.getMessage());
                }
            }
            stayOffline = stayOff;
            if (stayOffline) {
                LoggerFactory.getLogger(Transport.class).error(
                     "Staying offline with status/monitoring messages from now on");
            }
        }
        
        /**
         * Returns the transport connector.
         * 
         * @return the transport connector, may be <b>null</b>
         * @see #createConnector()
         */
        public TransportConnector getConnector() {
            return connector;
        }
        
    }

    /**
     * Prevents external creation.
     */
    private Transport() {
    }

    /**
     * Sends a service status message. Uses {@link Id#getDeviceId()}. Calls {@link #createConnector()} to obtain
     * a connector instance on demand.
     * 
     * @param action the action on the service
     * @param serviceId the service Id
     * @param aliasIds optional alias ids for the service
     */
    public static void sendServiceStatus(ActionType action, String serviceId, String... aliasIds) {
        globalTransport.sendServiceStatus(action, serviceId, aliasIds);
    }

    /**
     * Sends a service artifact status message. Uses {@link Id#getDeviceId()}. Calls {@link #createConnector()} to 
     * obtain a connector instance on demand.
     * 
     * @param action the action on the artifact
     * @param artifactId the artifact Id
     * @param aliasIds optional alias ids for the artifact 
     */
    public static void sendServiceArtifactStatus(ActionType action, String artifactId, String... aliasIds) {
        globalTransport.sendServiceArtifactStatus(action, artifactId, aliasIds);
    }

    /**
     * Sends a container status message. Uses {@link Id#getDeviceId()}. Calls {@link #createConnector()} to obtain
     * a connector instance on demand.
     * 
     * @param action the action on the container
     * @param containerId the container Id
     * @param aliasIds optional alias ids for the container
     */
    public static void sendContainerStatus(ActionType action, String containerId, String... aliasIds) {
        globalTransport.sendContainerStatus(action, containerId, aliasIds);
    }

    /**
     * Sends a resource status message for this resource. Calls {@link #createConnector()} to obtain
     * a connector instance on demand.
     * 
     * @param action the action on the container
     * @param aliasIds optional alias ids for the resource 
     */
    public static void sendResourceStatus(ActionType action, String... aliasIds) {
        globalTransport.sendResourceStatus(action, aliasIds);
    }

    /**
     * Sends a resource status message.  Calls {@link #createConnector()} to obtain
     * a connector instance on demand.
     * 
     * @param action the action on the container
     * @param deviceId the device id, may be <b>null</b> then {@link Id#getDeviceId()} is used
     * @param aliasIds optional alias ids for the resource 
     */
    public static void sendResourceStatus(ActionType action, String deviceId, String... aliasIds) {
        globalTransport.sendResourceStatus(action, deviceId, aliasIds);
    }
    
    /**
     * Sends a message of a certain {@code kind} with no/global routing and cares fore queuing.
     * 
     * @param sender the sender including the message
     * @param kind the kind of the message for logging
     */
    public static void send(IOConsumer<TransportConnector> sender, String kind) {
        globalTransport.send(sender, kind);
    }
    
    /**
     * Sends a message of a certain {@code kind} and cares fore queuing.
     * 
     * @param sender the sender including the message
     * @param kind the kind of the message for logging
     * @param routingKeys if <b>null</b>, empty or in {@link #globalRoutingKeys} then use the {@link #globalTransport}
     *     instance, else the {@link #localTransport} instance; may use both transport instances
     * @see #addGlobalRoutingKey(String)
     */
    public static void send(IOConsumer<TransportConnector> sender, String kind, String... routingKeys) {
        boolean global = false;
        boolean local = false;
        if (null == routingKeys || routingKeys.length == 0) {
            global = true;
        } else {
            for (int k = 0; k < routingKeys.length; k++) {
                if (globalRoutingKeys.contains(routingKeys[k])) {
                    global = true;
                } else {
                    local = true;
                }
            }
        }
        if (global) {
            globalTransport.send(sender, kind);
        }
        if (local && globalTransport != localTransport) {
            localTransport.send(sender, kind);
        }
    }

    /**
     * Adds a global routing key.
     * 
     * @param routingKey the routing key, may be any string, <b>null</b> or empty is ignored
     */
    public static void addGlobalRoutingKey(String routingKey) {
        if (null != routingKey && routingKey.length() > 0) {
            globalRoutingKeys.add(routingKey);
        }
    }

    /**
     * Defines a (global, local) trace filter.
     * 
     * @param filter the filter, <b>null</b> for no filter
     */
    public static void setTraceFilter(Predicate<TraceRecord> filter) {
        traceFilter = filter;
    }
    
    /**
     * Sends a trace record (global). Calls {@link #createConnector()} to obtain
     * a connector instance on demand. Caches messages if no connector is available.
     * 
     * @param record the record to be sent
     */
    public static void sendTraceRecord(TraceRecord record) {
        globalTransport.sendTraceRecord(record);
    }
    
    /**
     * Sends information about a processing status (global).
     * 
     * @param componentId the component id
     * @param step the step [0; max]
     * @param max the maximum step
     * @param description the description of the task
     */
    public static void sendProcessStatus(String componentId, int step, int max, String description) {
        globalTransport.sendProcessStatus(componentId, step, max, description);
    }

    /**
     * Sends information about a processing status (global).
     * 
     * @param componentId the component id
     * @param step the step [0; max]
     * @param max the maximum step
     * @param description the description of the task
     * @param subDescription the description of an optional sub-task within the actual task, may be <b>null</b> or empty
     */
    public static void sendProcessStatus(String componentId, int step, int max, String description, 
        String subDescription) {
        globalTransport.sendProcessStatus(componentId, step, max, description, subDescription);
    }

    /**
     * Sends a status message (global). Calls {@link #createConnector()} to obtain
     * a connector instance on demand. Caches messages if no connector is available.
     * 
     * @param msg the message to be sent
     */
    public static void sendStatus(StatusMessage msg) {
        globalTransport.sendStatus(msg); 
    }

    /**
     * Sends an alert message (global). Calls {@link #createConnector()} to obtain
     * a connector instance on demand. Caches messages if no connector is available.
     * 
     * @param alert the alert to be sent
     */
    public static void sendAlert(Alert alert) {
        globalTransport.sendAlert(alert); 
    }
    
    /**
     * Sets up the (global) transport information.
     * 
     * @param supplier the transport supplier
     */
    public static void setTransportSetup(Supplier<TransportSetup> supplier) {
        globalTransport.setTransportSetup(supplier);
    }

    /**
     * Sets up the (local) transport information and enforces a local transport instance.
     * 
     * @param supplier the transport supplier
     */
    public static void setLocalSetup(Supplier<TransportSetup> supplier) {
        if (localTransport == globalTransport) {
            localTransport = new TransportInstance();
        }
        localTransport.setTransportSetup(supplier);
    }

    /**
     * Tries creating a (global) connector. If successful, {@link #connector} will be initialized for caching. The 
     * instance is cached. The transport information must be set up before {@link #setTransportSetup(Supplier)}. After 
     * successfully creating a connector, queued messages are sent and removed from the queue. However, there is no 
     * guarantee that a connector can be created.
     * 
     * @return the (cached) connector, may be <b>null</b> 
     * 
     * @see #setTransportSetup(Supplier)
     * @see #releaseConnector()
     * @see #releaseConnector(boolean)
     */
    public static TransportConnector createConnector() {
        return globalTransport.createConnector();
    }

    /**
     * Releases an existing (global) connector and stays offline.
     */
    public static void releaseConnector() {
        globalTransport.releaseConnector();
    }
    
    /**
     * Releases an existing (global) connector.
     * 
     * @param stayOff whether a call to {@link #createConnector()} shall create a new connector or prevent sending 
     *     further messages.
     */
    public static void releaseConnector(boolean stayOff) {
        globalTransport.releaseConnector(stayOff);
    }
    
    /**
     * Returns the (global) transport connector.
     * 
     * @return the transport connector, may be <b>null</b>
     * @see #createConnector()
     */
    public static TransportConnector getConnector() {
        return globalTransport.getConnector();
    }

    /**
     * Returns the (global) transport instance.
     * 
     * @return the transport instance, may be uninitialized
     */
    public static TransportInstance getGlobalTransport() {
        return globalTransport;
    }

    /**
     * Returns the (local) transport connector.
     * 
     * @return the transport connector, may be the same as the global connector, may be <b>null</b>
     * @see #createConnector()
     */
    public static TransportConnector getLocalConnector() {
        return localTransport.getConnector();
    }

    /**
     * Returns the (local) transport instance.
     * 
     * @return the transport instance, may be uninitialized, may be the same as {@link #getGlobalTransport()}
     */
    public static TransportInstance getLocalTransport() {
        return localTransport;
    }

}
