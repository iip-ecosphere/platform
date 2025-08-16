/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.metrics;

/**
 * Represents a meter filter.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface MeterFilter {
    
    public enum MeterFilterReply {
        DENY, NEUTRAL, ACCEPT
    }
    
    /**
     * Accepts a meter id for filtering.
     * 
     * @param id Id with transformations applied
     * @return after all transformations, should a real meter be registered for this id, or should it be no-op'd.
     */
    public MeterFilterReply accept(Meter.Id id);
    
}