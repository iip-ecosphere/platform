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

import de.iip_ecosphere.platform.support.metrics.Meter;
import de.oktoflow.platform.support.metrics.micrometer.AbstractMeter.MicrometerId;
import io.micrometer.core.instrument.config.MeterFilter;

/**
 * A wrapped meter filter.
 * 
 * @author Holger Eichelberger, SSE
 */
class MicrometerMeterFilter implements de.iip_ecosphere.platform.support.metrics.MeterFilter {

    private MeterFilter filter;
    
    /**
     * Creates a wrapped meter filter.
     * 
     * @param filter the filter
     */
    MicrometerMeterFilter(MeterFilter filter) {
        this.filter = filter;
    }
    
    /**
     * Returns the implementing filter.
     * 
     * @return the implementing filter
     */
    MeterFilter getFilter() {
        return filter;
    }

    @Override
    public MeterFilterReply accept(Meter.Id id) {
        MeterFilterReply result = MeterFilterReply.NEUTRAL;
        if (id instanceof MicrometerId) {
            io.micrometer.core.instrument.config.MeterFilterReply reply = filter.accept(((MicrometerId) id).getId());
            result = MicrometerUtils.value(MeterFilterReply.class, reply, MeterFilterReply.NEUTRAL);
        }
        return result;
    }

    @Override
    public int hashCode() {
        return filter.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || filter.equals(
            other instanceof MicrometerMeterFilter ? ((MicrometerMeterFilter) other).filter : other);
    }

}
