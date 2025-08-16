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
 * Represents a measurement.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Measurement {

    /**
     * Returns the value.
     * 
     * @return value for the measurement.
     */
    public double getValue();

    /**
     * Returns the contained statistic as string.
     * 
     * @return the statistic as string
     */
    public default String getStatisticAsString() {
        return getStatistic().toString();
    }

    /**
     * Returns the contained statistic.
     * 
     * @return the statistic
     */
    public Statistic getStatistic();

}
