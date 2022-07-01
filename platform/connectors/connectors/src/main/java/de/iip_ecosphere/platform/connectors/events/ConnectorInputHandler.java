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

import de.iip_ecosphere.platform.connectors.Connector;

/**
 * Code fragment to handle input. {@link Connector} intentionally does not declare methods for
 * the input handler as this interface shall be utilized in generated connector code.
 * 
 * @param <T> the data type to handle, may be {@code I} or an asynchronously delivered type
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ConnectorInputHandler<T> {
    
    /**
     * Called to inform that data to be handled has been received. Calling {@link EventHandlingConnector#trigger()} 
     * or {@link EventHandlingConnector#trigger(ConnectorTriggerQuery)} to asynchronously ingest a next data item by 
     * the {@code connector}.
     * 
     * @param data the data
     * @param connector the connector
     */
    public void received(T data, EventHandlingConnector connector);

}
