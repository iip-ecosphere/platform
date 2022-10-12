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

package de.iip_ecosphere.platform.connectors.events;

import de.iip_ecosphere.platform.connectors.AbstractConnector;

/**
 * Basic connector interface providing access to event-relevant functionality.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface EventHandlingConnector {
    
    /**
     * Returns a descriptive name of the connector/the connected protocol.
     * 
     * @return a descriptive name of the connected protocol
     */
    public String getName();

    /**
     * Enable/disable polling (does not influence the polling timer).
     * 
     * @param enablePolling whether polling shall enabled
     * @see AbstractConnector#enableNotifications(boolean)
     */
    public void enablePolling(boolean enablePolling);

    /**
     * Trigger the ingestion of a next data item.
     */
    public void trigger();

    /**
     * Trigger the ingestion of a next data item.
     * 
     * @param query specification what to ingest; capabilities depend on connector
     */
    public void trigger(ConnectorTriggerQuery query);

}
