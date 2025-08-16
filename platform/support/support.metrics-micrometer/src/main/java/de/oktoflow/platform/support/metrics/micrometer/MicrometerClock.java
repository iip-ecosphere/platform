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

package de.oktoflow.platform.support.metrics.micrometer;

import de.iip_ecosphere.platform.support.metrics.Clock;

/**
 * Wraps a clock.
 * 
 * @author Holger Eichelberger, SSE
 */
class MicrometerClock implements Clock {

    private io.micrometer.core.instrument.Clock clock;
    
    /**
     * Creates a wrapping instance.
     * 
     * @param clock the wrapped clock
     */
    MicrometerClock(io.micrometer.core.instrument.Clock clock) {
        this.clock = clock;
    }
    
    @Override
    public long wallTime() {
        return clock.wallTime();
    }

    @Override
    public long monotonicTime() {
        return clock.monotonicTime();
    }

    @Override
    public int hashCode() {
        return clock.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || clock.equals(
            other instanceof MicrometerClock ? ((MicrometerClock) other).clock : other);
    }

}
