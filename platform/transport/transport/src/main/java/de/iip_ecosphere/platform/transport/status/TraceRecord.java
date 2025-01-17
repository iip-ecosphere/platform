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
 * Optional generic tracing of platform actions. [preliminary]
 * 
 * @author Holger Eichelberger, SSE
 */
public class TraceRecord {

    public static final String TRACE_STREAM = StreamNames.TRACE_STREAM;
    
    /**
     * A data item was received. Payload is the data item.
     */
    public static final String ACTION_RECEIVING = "receiving";

    /**
     * A data item was processed and send further. Payload is the data item.
     */
    public static final String ACTION_SENDING = "sending";
    
    /**
     * A parameter was changed.
     */
    public static final String ACTION_PARAMETER_CHANGE = "paramChange";

    /**
     * A family starts switching a service. Payload is the id of the service to switch to.
     */
    public static final String ACTION_SWITCHING_SERVICE = "switchingService";

    /**
     * A family completed switching a service. Payload is the id of the actual service (may be the old one).
     */
    public static final String ACTION_SWITCHED_SERVICE = "switchedService";

    private String source;
    private long timestamp;
    private String action;
    private Object payload;
    
    /**
     * Creates a trace record. [serialization]
     */
    TraceRecord() {
    }

    /**
     * Creates a trace record with the current time as timestamp.
     * 
     * @param source the source of the record
     * @param action the action
     * @param payload arbitrary payload
     */
    public TraceRecord(String source, String action, Object payload) {
        this(source, System.currentTimeMillis(), action, payload);
    }

    /**
     * Creates a trace record.
     * 
     * @param source the source of the record
     * @param timestamp the timestamp indicating the creation of the record
     * @param action the action
     * @param payload arbitrary payload
     */
    public TraceRecord(String source, long timestamp, String action, Object payload) {
        this.source = source;
        this.timestamp = timestamp;
        this.action = action;
        this.payload = payload;
    }
    
    /**
     * Returns the source of the record.
     * 
     * @return the source, may be a service id, an AAS URL, ...
     */
    public String getSource() {
        return source;
    }
    
    /**
     * Defines the source of the record.
     * 
     * @param source the source, may be a service id, an AAS URL, ...
     */    
    void setSource(String source) {
        this.source = source;
    }

    /**
     * Returns the timestamp of record creation.
     * 
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Changes the timestamp of record creation.
     * 
     * @param timestamp the timestamp
     */
    void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Characterizes the action causing the creation of the record.
     * 
     * @return the action, e.g., classification; may be empty
     */    
    public String getAction() {
        return action;
    }

    /**
     * Changes the action causing the creation of the record.
     * 
     * @param action the action, e.g., classification; may be empty
     */    
    void setAction(String action) {
        this.action = action;
    }
    
    /**
     * Returns the (arbitrary) payload.
     * 
     * @return the payload, may be <b>null</b>
     */
    public Object getPayload() {
        return payload;
    }

    /**
     * Changes the (arbitrary) payload.
     * 
     * @param payload the payload, may be <b>null</b>
     */
    void setPayload(Object payload) {
        this.payload = payload;
    }

    /**
     * Sends this message to the given connector on {@code #TRACE_STREAM}. [convenience]
     * 
     * @param conn the connector
     * @throws IOException if sending fails
     */
    public void send(TransportConnector conn) throws IOException {
        conn.asyncSend(TRACE_STREAM, this);
    }

    /**
     * Registers a type to be ignored when serializing trace record (payloads). [convenience]
     * 
     * @param cls the class representing the type to be ignored
     */
    public static void ignoreClass(Class<?> cls) {
        TraceRecordSerializer.ignoreClass(cls);
    }

    /**
     * Registers a field to be ignored when serializing trace record (payloads). [convenience]
     * 
     * @param cls the class representing the type containing the field
     * @param field the field to be ignored
     */
    public static void ignoreField(Class<?> cls, String field) {
        TraceRecordSerializer.ignoreField(cls, field);
    }

    /**
     * Registers fields to be ignored when serializing trace record (payloads). [convenience]
     * 
     * @param cls the class representing the type containing the field
     * @param fields the fields to be ignored
     */
    public static void ignoreFields(Class<?> cls, String... fields) {
        TraceRecordSerializer.ignoreFields(cls, fields);
    }

    /**
     * Clears all ignored types and fields. [convenience]
     */
    public static void clearIgnores() {
        TraceRecordSerializer.clearIgnores();
    }
    
}
