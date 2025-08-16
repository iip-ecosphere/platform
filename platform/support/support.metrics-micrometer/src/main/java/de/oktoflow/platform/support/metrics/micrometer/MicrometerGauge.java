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

import de.iip_ecosphere.platform.support.metrics.Gauge;
import de.iip_ecosphere.platform.support.metrics.MeterRegistry;

/**
 * A wrapping micrometer gauge.
 * 
 * @author Holger Eichelberger, SSE
 */
class MicrometerGauge extends AbstractMeter<io.micrometer.core.instrument.Gauge> implements Gauge {
    
    /**
     * Creates a wrapping gauge.
     * 
     * @param gauge the micrometer gauge
     */
    MicrometerGauge(io.micrometer.core.instrument.Gauge gauge) {
        super(gauge);
    }
    
    /**
     * A wrapping gauge builder.
     * 
     * @param <T> the type of the state object from which the gauge value is extracted.
     * @author Holger Eichelberger, SSE
     */
    static class MicrometerGaugeBuilder<T> implements GaugeBuilder<T> {
        
        private io.micrometer.core.instrument.Gauge.Builder<T> builder;

        /**
         * Creates a wrapping gauge builder.
         * 
         * @param builder the builder
         */
        MicrometerGaugeBuilder(io.micrometer.core.instrument.Gauge.Builder<T> builder) {
            this.builder = builder;
        }

        @Override
        public GaugeBuilder<T> tags(String... tags) {
            builder.tags(tags);
            return this;
        }

        @Override
        public GaugeBuilder<T> description(String description) {
            builder.description(description);
            return this;
        }

        @Override
        public GaugeBuilder<T> baseUnit(String unit) {
            builder.baseUnit(unit);
            return this;
        }

        @Override
        public Gauge register(MeterRegistry registry) {
            return new MicrometerGauge(builder.register(((MicrometerMeterRegistry) registry).getRegistry()));
        }
        
    }
    
    @Override
    public double value() {
        return getMeter().value();
    }

}
