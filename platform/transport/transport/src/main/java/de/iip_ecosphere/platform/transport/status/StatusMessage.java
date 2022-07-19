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

import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.streams.StreamNames;

/**
 * Represents a status message for a component. A component is denoted by the device id (of the ECS runtime) the 
 * component is running on, the component id (in the context of a device) and optional alias ids, e.g., introduced
 * by a specific device management approach. If the component to notify about is a device, the device id and the 
 * component id shall be equal.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StatusMessage {

    public static final String STATUS_STREAM = StreamNames.STATUS_STREAM;
    
    private ComponentType componentType;
    private ActionType action;
    private String id;
    private String[] aliasIds;
    private String deviceId;
    private int progress = -1;
    private String description = "";
    private String subDescription = "";

    /**
     * Creates an empty status message. [deserialization]
     */
    StatusMessage() {
    }

    /**
     * Creates a new status message for devices, i.e., {@link ComponentTypes#DEVICE}. 
     * 
     * @param action the action
     * @param id the id of the component
     * @param aliasIds optional alias ids
     */
    public StatusMessage(ActionType action, String id, String... aliasIds) {
        this(ComponentTypes.DEVICE, action, id, id, aliasIds);
    }
    
    /**
     * Creates a new status message.
     * 
     * @param componentType the component type
     * @param action the action
     * @param id the id of the component
     * @param deviceId the id of device providing the context, shall be equal to {@code id} if {@code componentType}
     *     is {@link ComponentTypes#DEVICE}.  
     * @param aliasIds optional alias ids
     */
    public StatusMessage(ComponentType componentType, ActionType action, String id, String deviceId, 
        String... aliasIds) {
        this.componentType = componentType;
        this.action = action;
        this.id = id;
        this.deviceId = deviceId;
        this.aliasIds = aliasIds;
    }

    /**
     * Returns the component type.
     * 
     * @return the component type
     */
    public ComponentType getComponentType() {
        return componentType;
    }

    /**
     * Changes the component type. [deserialization]
     * 
     * @param componentType the new component type
     */
    void setComponentType(ComponentType componentType) {
        this.componentType = componentType;
    }

    /**
     * Returns the action type causing this message.
     * 
     * @return the action type causing this message
     */
    public ActionType getAction() {
        return action;
    }

    /**
     * Changes the action type. [deserialization]
     * 
     * @param action the new action type
     */
    void setAction(ActionType action) {
        this.action = action;
    }

    /**
     * Returns the primary id of the component.
     * 
     * @return the primary id, may be equal to {@link #getDeviceId()} if 
     *    {@link #getComponentType()} is {@link ComponentTypes#DEVICE}.
     */
    public String getId() {
        return id;
    }

    /**
     * Changes the primary id of the component. [deserialization]
     * 
     * @param id the primary id
     */
    void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the device (context) id of the component.
     * 
     * @return the device id, shall be equal to {@link #getId()} if {@link #getComponentType()} 
     *    is {@link ComponentTypes#DEVICE}.
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Changes the device (context) id of the component. [deserialization]
     * 
     * @param deviceId the device id
     */
    void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Returns optional alias ids of the component.
     * 
     * @return the alias ids, may be empty
     */
    public String[] getAliasIds() {
        return aliasIds;
    }

    /**
     * Changes the alias ids of the component. [deserialization]
     * 
     * @param aliasIds the primary id
     */
    void setAliasIds(String[] aliasIds) {
        this.aliasIds = aliasIds;
    }

    /**
     * Sends this message to the given connector on {@code #STATUS_STREAM}. [convenience]
     * 
     * @param conn the connector
     * @throws IOException if sending fails
     */
    public void send(TransportConnector conn) throws IOException {
        conn.asyncSend(STATUS_STREAM, this);
    }
    
    /**
     * Defines the progress for {@link ActionTypes#PROCESS}.
     * 
     * @param progress the progress in [0;100]; if 100 longer running process is assumed to be completed
     * @return <b>this</b>
     */
    public StatusMessage withProgress(int progress) {
        this.progress = progress;
        return this;
    }
    
    /**
     * Returns the progress for {@link ActionTypes#PROCESS}.
     * 
     * @return the progress in percent: if negative, no progress will be reported. If positive, further messages shall 
     * follow until 100.
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Sets the description for {@link #getProgress()}, {@link ActionTypes#PROCESS}.
     * 
     * @param description the description, may be <b>null</b> or ""
     * @return <b>this</b>
     */
    public StatusMessage withDescription(String description) {
        this.description = null == description ? "" : description;
        return this;
    }
    
    /**
     * Returns the description, {@link ActionTypes#PROCESS}.
     * 
     * @return the description, may be empty but not <b>null</b>
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the sub(-task) description for {@link #getProgress()}, {@link ActionTypes#PROCESS}.
     * 
     * @param subDescription the sub(-task) description, may be <b>null</b> or ""
     * @return <b>this</b>
     */
    public StatusMessage withSubDescription(String subDescription) {
        this.subDescription = null == subDescription ? "" : subDescription;
        return this;
    }
    
    /**
     * Returns the sub(-task) description, {@link ActionTypes#PROCESS}.
     * 
     * @return the sub(-task) description, may be empty but not <b>null</b>
     */
    public String getSubDescription() {
        return subDescription;
    }

}
