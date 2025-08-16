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
 * Represents a clock.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Clock {

    /**
     * Current wall time in milliseconds since the epoch. Typically equivalent to
     * System.currentTimeMillis. Should not be used to determine durations. Used
     * for timestamping metrics being pushed to a monitoring system or for determination
     * of step boundaries.
     *
     * @return Wall time in milliseconds
     */
    long wallTime();

    /**
     * Current time from a monotonic clock source. The value is only meaningful when compared with
     * another snapshot to determine the elapsed time for an operation. The difference between two
     * samples will have a unit of nanoseconds. The returned value is typically equivalent to
     * System.nanoTime.
     *
     * @return Monotonic time in nanoseconds
     */
    long monotonicTime();

}
