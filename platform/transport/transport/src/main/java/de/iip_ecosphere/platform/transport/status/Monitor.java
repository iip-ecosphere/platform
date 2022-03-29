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

package de.iip_ecosphere.platform.transport.status;

import java.io.IOException;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;

/**
 * Basic class to support monitoring activities. Call {@link #setTransportSetup(TransportSetup)} first to allow
 * for automated creation of a transport connector when needed.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Monitor {

    private static TransportSetup transportSetup;
    private static TransportConnector connector;
    private static boolean stayOffline = false;

    /**
     * Sends a service status message. Uses {@link Id#getDeviceId()}.
     * 
     * @param action the action on the service
     * @param serviceId the service Id
     * @param aliasIds optional alias ids for the service 
     */
    public static void sendServiceStatus(ActionType action, String serviceId, String... aliasIds) {
        sendStatus(new StatusMessage(ComponentTypes.SERVICE, action, serviceId, Id.getDeviceId(), aliasIds));
    }

    /**
     * Sends a service artifact status message. Uses {@link Id#getDeviceId()}.
     * 
     * @param action the action on the artifact
     * @param artifactId the artifact Id
     * @param aliasIds optional alias ids for the artifact 
     */
    public static void sendServiceArtifactStatus(ActionType action, String artifactId, String... aliasIds) {
        sendStatus(new StatusMessage(ComponentTypes.SERVICE_ARTIFACT, action, artifactId, Id.getDeviceId(), aliasIds));
    }

    /**
     * Sends a container status message. Uses {@link Id#getDeviceId()}.
     * 
     * @param action the action on the container
     * @param containerId the container Id
     * @param aliasIds optional alias ids for the container
     */
    public static void sendContainerStatus(ActionType action, String containerId, String... aliasIds) {
        sendStatus(new StatusMessage(ComponentTypes.CONTAINER, action, containerId, Id.getDeviceId(), aliasIds));
    }

    /**
     * Sends a resource status message for this resource.
     * 
     * @param action the action on the container
     * @param aliasIds optional alias ids for the resource 
     */
    public static void sendResourceStatus(ActionType action, String... aliasIds) {
        sendResourceStatus(action, null, aliasIds);
    }

    /**
     * Sends a resource status message.
     * 
     * @param action the action on the container
     * @param deviceId the device id, may be <b>null</b> then {@link Id#getDeviceId()} is used
     * @param aliasIds optional alias ids for the resource 
     */
    public static void sendResourceStatus(ActionType action, String deviceId, String... aliasIds) {
        sendStatus(new StatusMessage(action, null == deviceId ? Id.getDeviceId() : deviceId, aliasIds));
    }

    /**
     * Sends a status message.
     * 
     * @param msg the message to be sent
     */
    public static void sendStatus(StatusMessage msg) {
        createConnector();
        if (null != connector) {
            try {
                msg.send(connector);
            } catch (IOException e) {
                LoggerFactory.getLogger(Monitor.class).error(
                    "Cannot sent status message: " + e.getMessage());
            }
        } else {
            LoggerFactory.getLogger(Monitor.class).error(
                "Cannot sent status message: No connector available (see above)");
        }
    }
    
    /**
     * Sets up the transport information.
     * 
     * @param setup the setup instance
     */
    public static void setTransportSetup(TransportSetup setup) {
        transportSetup = setup;
    }

    /**
     * Tries creating a connector. If successful, {@link #connector} will be initialized afterwards. However,
     * there is no guarantee that a connector can be created.
     * 
     * @see #setTransportSetup(TransportSetup)
     */
    public static void createConnector() {
        if (null == connector && !stayOffline) {
            if (null != transportSetup) {
                try {
                    connector = TransportFactory.createConnector();
                    connector.connect(transportSetup.createParameter());
                } catch (IOException e) {
                    LoggerFactory.getLogger(Monitor.class).error(
                        "Cannot create transport connector: " + e.getMessage());
                    connector = null;
                }
            }
        }
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
                LoggerFactory.getLogger(Monitor.class).error(
                    "Cannot disconnect transport connector: " + e.getMessage());
            }
        }
        stayOffline = stayOff;
        if (stayOffline) {
            LoggerFactory.getLogger(Monitor.class).error("Staying offline with status/monitoring messages from now on");
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
