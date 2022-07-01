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

package de.iip_ecosphere.platform.connectors;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.events.EventHandlingConnector;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;

/**
 * The interface of a platform/machine connector. A connector shall define a {@link ConnectorDescriptor} as top-level 
 * inner class and register the descriptor as service. By default, polling shall be enabled but in certain cases this
 * may not fit into the lifecycle of services in a service environment. Then it is possible to explicitly 
 * {@link #enablePolling(boolean) enable/disable} polling.
 * 
 * @param <O> the output type from the underlying machine/platform
 * @param <I> the input type to the underlying machine/platform
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 *
 * @author Holger Eichelberger, SSE
 */
public interface Connector <O, I, CO, CI> extends EventHandlingConnector {
    
    /**
     * Connects the connector to the underlying machine/platform.
     * 
     * @param params connection parameter
     * @throws IOException in case that connecting fails
     */
    public void connect(ConnectorParameter params) throws IOException;
    
    /**
     * Explicitly requests reading data from the underlying machine. This is typically done by polling or
     * events, but, in seldom cases, may be needed manually.
     * 
     * @param sendToCallback whether {@link #setReceptionCallback(ReceptionCallback) the reception callback} shall 
     * be informed about new data
     * @return the data from the machine, <b>null</b> for none, i.e., also no call to 
     *   {@link #setReceptionCallback(ReceptionCallback) the reception callback}
     * @throws IOException in case that reading fails
     */
    public CO request(boolean sendToCallback) throws IOException;
    
    /**
     * Writes the given {@code data} to the underlying machine/platform.
     * 
     * @param data the data to send to {@code stream}
     * @throws IOException in case that problems during the connection happens
     */
    public void write(CI data) throws IOException;
    
    /**
     * Attaches a reception {@code callback} to this connector. The {@code callback}
     * is called upon a reception.
     * 
     * @param callback the callback to attach
     * @throws IOException in case that problems during registering the callback
     *                     (e.g., during subscription) happens
     */
    public void setReceptionCallback(ReceptionCallback<CO> callback) throws IOException;

    /**
     * Disconnects the connector from the underlying machine/platform.
     * 
     * @throws IOException in case that connecting fails
     */
    public void disconnect() throws IOException;
    
    /**
     * Final cleanup when platform shuts down, e.g., for shared resources.
     */
    public void dispose();
    
    /**
     * Returns the input type to the protocol.
     * 
     * @return the input type (may be <b>null</b> in case of generic types, but shall not be <b>null</b>)
     */
    public Class<? extends I> getProtocolInputType();

    /**
     * Returns the input type from the IIP-Ecosphere platform.
     * 
     * @return the input type (may be <b>null</b> in case of generic types, but shall not be <b>null</b>)
     */
    public Class<? extends CI> getConnectorInputType();
    
    /**
     * Returns the output type of the protocol.
     * 
     * @return the output type (may be <b>null</b> in case of generic types, but shall not be <b>null</b>)
     */
    public Class<? extends O> getProtocolOutputType();
    
    /**
     * Returns the output type to the IIP-Ecosphere platform.
     * 
     * @return the output type (may be <b>null</b> in case of generic types, but shall not be <b>null</b>)
     */
    public Class<? extends CO> getConnectorOutputType();
  
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

    /**
     * Enables/disables notifications/polling at all.
     * 
     * @param enableNotifications enable or disable notifications
     */
    public void enableNotifications(boolean enableNotifications);

}
