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
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.function.IOConsumer;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;
import de.iip_ecosphere.platform.transport.status.ActionType;
import de.iip_ecosphere.platform.transport.status.Alert;
import de.iip_ecosphere.platform.transport.status.ComponentTypes;
import de.iip_ecosphere.platform.transport.status.StatusMessage;
import de.iip_ecosphere.platform.transport.status.TraceRecord;

/**
 * Preliminary, for name change of "Monitor". At startup of the platform, sending messages and creating a connector
 * may happen in parallel. So far, there is no strategy to clean up the queue, i.e., {@link #createConnector()}
 * or one of the send methods in this class are expected to be called in reasonable manner. 
 * 
 * @author Holger Eichelberger, SSE
 */
public class Transport {

    private static Supplier<TransportSetup> transportSupplier;
    private static TransportConnector connector;
    private static boolean stayOffline = false;
    private static Queue<IOConsumer<TransportConnector>> queue = new ConcurrentLinkedDeque<>();

    /**
     * Sends a service status message. Uses {@link Id#getDeviceId()}. Calls {@link #createConnector()} to obtain
     * a connector instance on demand.
     * 
     * @param action the action on the service
     * @param serviceId the service Id
     * @param aliasIds optional alias ids for the service
     */
    public static void sendServiceStatus(ActionType action, String serviceId, String... aliasIds) {
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
    public static void sendServiceArtifactStatus(ActionType action, String artifactId, String... aliasIds) {
        sendStatus(new StatusMessage(ComponentTypes.SERVICE_ARTIFACT, action, artifactId, Id.getDeviceId(), aliasIds));
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
        sendStatus(new StatusMessage(ComponentTypes.CONTAINER, action, containerId, Id.getDeviceId(), aliasIds));
    }

    /**
     * Sends a resource status message for this resource. Calls {@link #createConnector()} to obtain
     * a connector instance on demand.
     * 
     * @param action the action on the container
     * @param aliasIds optional alias ids for the resource 
     */
    public static void sendResourceStatus(ActionType action, String... aliasIds) {
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
    public static void sendResourceStatus(ActionType action, String deviceId, String... aliasIds) {
        sendStatus(new StatusMessage(action, null == deviceId ? Id.getDeviceId() : deviceId, aliasIds));
    }
    
    /**
     * Sends a message of a certain kind and cares fore queuing.
     * 
     * @param sender the sender including the message
     * @param kind the kind of the message for logging
     */
    private static void send(IOConsumer<TransportConnector> sender, String kind) {
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
     * Sends a trace record. Calls {@link #createConnector()} to obtain
     * a connector instance on demand. Caches messages if no connector is available.
     * 
     * @param record the record to be sent
     */
    public static void sendTraceRecord(TraceRecord record) {
        send(c -> record.send(c), "trace"); 
    }

    /**
     * Sends a status message. Calls {@link #createConnector()} to obtain
     * a connector instance on demand. Caches messages if no connector is available.
     * 
     * @param msg the message to be sent
     */
    public static void sendStatus(StatusMessage msg) {
        send(c -> msg.send(c), "status"); 
    }

    /**
     * Sends an alert message. Calls {@link #createConnector()} to obtain
     * a connector instance on demand. Caches messages if no connector is available.
     * 
     * @param alert the alert to be sent
     */
    public static void sendAlert(Alert alert) {
        send(c -> alert.send(c), "alert"); 
    }
    
    /**
     * Sets up the transport information.
     * 
     * @param supplier the transport supplier
     */
    public static void setTransportSetup(Supplier<TransportSetup> supplier) {
        transportSupplier = supplier;
    }

    /**
     * Tries creating a connector. If successful, {@link #connector} will be initialized for caching. The instance is 
     * cached. The transport information must be set up before {@link #setTransportSetup(Supplier)}. After successfully 
     * creating a connector, queued messages are sent and removed from the queue. However, there is no guarantee that 
     * a connector can be created.
     * 
     * @return the (cached) connector, may be <b>null</b> 
     * 
     * @see #setTransportSetup(Supplier)
     * @see #releaseConnector()
     * @see #releaseConnector(boolean)
     */
    public static TransportConnector createConnector() {
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
    public static void releaseConnector() {
        releaseConnector(true);
    }
    
    /**
     * Releases an existing connector.
     * 
     * @param stayOff whether a call to {@link #createConnector()} shall create a new connector or prevent sending 
     *     further messages.
     */
    public static void releaseConnector(boolean stayOff) {
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
     * @see #createConnector(TransportSetup)
     */
    public static TransportConnector getConnector() {
        return connector;
    }

}
