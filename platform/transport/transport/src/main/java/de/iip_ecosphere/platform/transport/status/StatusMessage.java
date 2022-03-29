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

/**
 * Represents a status message.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StatusMessage {

    public static final String STATUS_STREAM = "ComponentStatus";
    
    private ComponentType type;
    private ActionType action;
    private String id;
    private String[] aliasIds;

    /**
     * Creates an empty status message. [deserialization]
     */
    StatusMessage() {
    }
    
    /**
     * Creates a new status message.
     * 
     * @param type the type
     * @param action the action
     * @param id the id of the component
     * @param aliasIds optional alias ids
     */
    public StatusMessage(ComponentType type, ActionType action, String id, String... aliasIds) {
        this.type = type;
        this.action = action;
        this.id = id;
        this.aliasIds = aliasIds;
    }

    /**
     * Returns the component type.
     * 
     * @return the component type
     */
    public ComponentType getType() {
        return type;
    }

    /**
     * Changes the component type. [deserialization]
     * 
     * @param type the new component type
     */
    void setType(ComponentType type) {
        this.type = type;
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
     * @return the primary id
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

}
