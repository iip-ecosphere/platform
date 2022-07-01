/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.connectors.opcuav1;

import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

import de.iip_ecosphere.platform.connectors.events.ConnectorTriggerQuery;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.ConnectorOutputTypeTranslator;

/**
 * Represents a monitored, changed data item send to the {@link ConnectorOutputTypeTranslator} for detailed change
 * data, but only if enabled through {@link ModelAccess#setDetailNotifiedItem(boolean)}. 
 * 
 * @author Holger Eichelberger, SSE
 */
public class DataItem implements ConnectorTriggerQuery {
    
    private ConnectorTriggerQuery query;
    private Variant data;
    private Object identifier;

    /**
     * Creates a data item.
     * 
     * @param query the data trigger query
     */
    public DataItem(ConnectorTriggerQuery query) {
        this.query = query;
    }

    /**
     * Creates a data item.
     * 
     * @param identifier the data item identifier in the model, usually a string but who knows in Milo
     * @param data the new data
     */
    public DataItem(Object identifier, Variant data) {
        this.identifier = identifier;
        this.data = data;
    }

    /**
     * Returns the changed data.
     * 
     * @return the changed data (may be <b>null</b> if {@link #getQuery()} is specified)
     */
    public Variant getData() {
        return data;
    }

    /**
     * Returns the identifier in the model.
     * 
     * @return the data item identifier in the model, usually a string but who knows in Milo (may be <b>null</b> 
     *     if {@link #getQuery()} is specified)
     */
    public Object getIdentifier() {
        return identifier;
    }
    
    /**
     * Returns the query.
     * 
     * @return the query (may be <b>null</b> if {@link #getData()}/{@link #getIdentifier()} are specified)
     */
    public ConnectorTriggerQuery getQuery() {
        return query;
    }

}
