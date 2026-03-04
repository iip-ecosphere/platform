/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.connectors.model;

/**
 * Specific wrapper for float indexes.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LongIndex {

    private long value;
    
    /**
     * Creates a float index.
     * 
     * @param value the value
     */
    public LongIndex(long value) {
        this.value = value;
    }
    
    /**
     * Creates a float index.
     * 
     * @param value the value
     */
    public LongIndex(Long value) {
        this.value = value.longValue();
    }
    
    /**
     * Returns the wrapped value.
     * 
     * @return the value
     */
    public long getValue() {
        return value;
    }

}
