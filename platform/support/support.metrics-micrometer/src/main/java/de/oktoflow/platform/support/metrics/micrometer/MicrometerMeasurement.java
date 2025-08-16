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

import de.iip_ecosphere.platform.support.metrics.Measurement;
import de.iip_ecosphere.platform.support.metrics.Statistic;

/**
 * Wraps the micrometer measurement.
 * 
 * @author Holger Eichelberger, SSE
 */
class MicrometerMeasurement implements Measurement {

    private io.micrometer.core.instrument.Measurement measurement;
    
    /**
     * Creates a wrapping instance.
     * 
     * @param measurement the wrapped measurement
     */
    public MicrometerMeasurement(io.micrometer.core.instrument.Measurement measurement) {
        this.measurement = measurement;
    }

    @Override
    public double getValue() {
        return measurement.getValue();
    }

    @Override
    public String getStatisticAsString() {
        return measurement.getStatistic().toString();
    }

    @Override
    public Statistic getStatistic() {
        return MicrometerUtils.mmStatisticValue(measurement.getStatistic());
    }

    @Override
    public int hashCode() {
        return measurement.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || measurement.equals(
            other instanceof MicrometerMeasurement ? ((MicrometerMeasurement) other).measurement : other);
    }
    
    /**
     * Returns the wrapped measurement.
     * 
     * @return the wrapped measurement
     */
    io.micrometer.core.instrument.Measurement getMeasurement() {
        return measurement;
    }
    
}
