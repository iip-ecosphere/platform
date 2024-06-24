/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.connectors.events;

/**
 * Implements a query represented by a string interpreted by the connector.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StringTriggerQuery implements ConnectorTriggerQuery {

    private String query;
    private int delay = 0;

    /**
     * Creates a trigger query.
     * 
     * @param query the query
     */
    public StringTriggerQuery(String query) {
        this(query, 0);
    }

    /**
     * Creates a trigger query.
     * 
     * @param query the query
     * @param delay the fixed delay after each result, in ms, ignored if not positive
     */
    public StringTriggerQuery(String query, int delay) {
        this.query = query;
        this.delay = delay;
    }
    
    /**
     * Returns the query .
     * 
     * @return the query 
     */
    public String getQuery() {
        return query;
    }
    
    @Override
    public int delay() {
        return delay;
    }
    
}
