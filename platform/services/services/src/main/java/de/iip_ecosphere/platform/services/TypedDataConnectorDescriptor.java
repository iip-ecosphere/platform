/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services;

/**
 * A typed data connector, i.e., points where data flows into and out from a service. We introduced this interface as 
 * channels may exhibit in the future also their connection details, e.g., for display in the service AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface TypedDataConnectorDescriptor extends TypedDataDescriptor {
    
    /**
     * Returns the id of the connector. Depending on the implementation, id may be the same as {@link #getName()} or
     * differ, e.g, if {@link #getName()} points to a technical channel name.
     * 
     * @return the id
     */
    public String getId();
    
    /**
     * Returns the id of the service this connector is pointing to/coming from.
     * 
     * @return the service id, may be <b>null</b> or empty for none
     */
    public String getService();
    
    /**
     * Returns the function at the containing service associated with this relation.
     * 
     * @return the function name (may be <b>null</b> or empty for implicit functions that shall not be exibited)
     */
    public String getFunction();

}
