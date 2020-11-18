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

/**
 * Callback to notify a using implementation about the reception of data in a
 * {@link TransportConnector}.
 * 
 * @param <T> the type of data
 * @author Holger Eichelberger, SSE
 */
public interface ReceptionCallback<T> {

    /**
     * Notifies about the reception of a {@code} data value.
     * 
     * @param data the data value
     */
    public void received(T data);

    /**
     * Returns the type of the data.
     * 
     * @return the class representing the type of the data
     */
    public Class<T> getType();

}