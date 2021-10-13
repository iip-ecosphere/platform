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
package de.iip_ecosphere.platform.transport.connectors;

import java.io.IOException;

/**
 * Defines the interface of a transport connector. The connector can handle
 * multiple different homogeneous streams.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface TransportConnector {

    /**
     * Empty stream parent.
     */
    public static final String EMPTY_PARENT = "";

    /**
     * Sends the given {@code data} on {@code stream} in synchronized manner, e.g.,
     * by blocking this call until the {@code data} is sent.
     * 
     * @param stream the stream to send to
     * @param data   the data to send to {@code stream}
     * @throws IOException in case that problems during the connection happens
     */
    public void syncSend(String stream, Object data) throws IOException;

    /**
     * Sends the given {@code data} on {@code stream} in asnychronous manner, e.g.,
     * by not blocking this call until the {@code data} is sent, i.e., sending may
     * not be completed when this method returns. If not possible for this
     * connector, the underlying implementation may resort to synchronized sending.
     * 
     * @param stream the stream to send to
     * @param data   the data to send to {@code stream}
     * @throws IOException in case that problems during the connection happens
     */
    public void asyncSend(String stream, Object data) throws IOException;

    /**
     * Attaches a reception {@code callback} to {@code stream}. The {@code callback}
     * is called upon a reception.
     * 
     * @param stream   the stream to attach the reception to
     * @param callback the callback to attach
     * @throws IOException in case that problems during registering the callback
     *                     (e.g., during subscription) happens
     */
    public void setReceptionCallback(String stream, ReceptionCallback<?> callback) throws IOException;

    /**
     * Composes a hierarchical stream name (in the syntax/semantics of the
     * connector).
     * 
     * @param parent the parent name (may be {@link #EMPTY_PARENT} for top-level streams)
     * @param name   the name of the stream
     * @return the composed name
     * @throws IllegalArgumentException in case that the stream name is (structurally) illegal
     */
    public String composeStreamName(String parent, String name);

    /**
     * Connects the underlying connections.
     * 
     * @param params the parameters to start the underlying connection
     * @throws IOException in case that problems during the connection happens
     */
    public void connect(TransportParameter params) throws IOException;

    /**
     * Disconnects the underlying connections.
     * 
     * @throws IOException in case that problems during the disconnect happens
     */
    public void disconnect() throws IOException;

    /**
     * Returns a descriptive name of the transport protocol being implemented.
     * 
     * @return the name of the protocol
     */
    public String getName();
    
    /**
     * Returns the supported encryption mechanisms.
     * 
     * @return the supported encryption mechanisms (comma-separated), may be <b>null</b> or empty
     */
    public String supportedEncryption();

    /**
     * Returns the actually enabled encryption mechanisms on this instance. The result may change when connecting 
     * the connector.
     * 
     * @return the enabled encryption mechanisms (comma-separated), may be <b>null</b> or empty
     */
    public String enabledEncryption();
    
}
