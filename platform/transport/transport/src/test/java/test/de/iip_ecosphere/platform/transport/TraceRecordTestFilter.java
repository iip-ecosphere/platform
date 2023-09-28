/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.transport;

import de.iip_ecosphere.platform.transport.status.TraceRecordSerializer;

/**
 * Tests the record test filter.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TraceRecordTestFilter implements de.iip_ecosphere.platform.transport.status.TraceRecordFilter {

    private int countFilterCalls;
    private int countInitializeCalls;
    
    /**
     * Filters/transforms the payload.
     * 
     * @param payload the payload to filter.
     * 
     * @return the filtered payload, may be <b>null</b> for none; by default {@code payload}
     */
    public Object filterPayload(Object payload) {
        countFilterCalls++;
        return payload;
    }
    
    /**
     * Initialization actions, e.g., {@link TraceRecordSerializer#ignoreField(Class, String)}.
     * The default implementation does nothing.
     */
    public void initialize() {
        countInitializeCalls++;
    }

    /**
     * Clears the filter call count.
     */
    void clear() {
        countFilterCalls = 0;
    }

    /**
     * Returns the filter call count.
     * 
     * @return the count
     */
    int getCountFilterCalls() {
        return countFilterCalls;
    }
    
    /**
     * Returns the initialize call count.
     * 
     * @return the count
     */
    int getCountInitializeCalls() {
        return countInitializeCalls;
    }

}
