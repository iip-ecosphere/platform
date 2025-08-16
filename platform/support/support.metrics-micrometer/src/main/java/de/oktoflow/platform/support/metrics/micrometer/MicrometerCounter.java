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

import de.iip_ecosphere.platform.support.metrics.Counter;
import de.iip_ecosphere.platform.support.metrics.MeterRegistry;

/**
 * A wrapping micrometer counter.
 * 
 * @author Holger Eichelberger, SSE
 */
class MicrometerCounter extends AbstractMeter<io.micrometer.core.instrument.Counter> implements Counter {
    
    /**
     * Creates a wrapping counter.
     * 
     * @param counter the micrometer counter
     */
    MicrometerCounter(io.micrometer.core.instrument.Counter counter) {
        super(counter);
    }
    
    /**
     * A wrapping counter builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class MicrometerCounterBuilder implements CounterBuilder {
        
        private io.micrometer.core.instrument.Counter.Builder builder;

        /**
         * Creates a wrapping counter builder.
         * 
         * @param name the name of the counter
         */
        MicrometerCounterBuilder(String name) {
            builder = io.micrometer.core.instrument.Counter.builder(name);
        }
        
        @Override
        public CounterBuilder tags(String... tags) {
            builder.tags(tags);
            return this;
        }

        @Override
        public CounterBuilder description(String description) {
            builder.description(description);
            return this;
        }

        @Override
        public CounterBuilder baseUnit(String unit) {
            builder.baseUnit(unit);
            return this;
        }

        @Override
        public Counter register(MeterRegistry registry) {
            return new MicrometerCounter(builder.register(((MicrometerMeterRegistry) registry).getRegistry()));
        }
        
    }

    @Override
    public void increment(double amount) {
        getMeter().increment(amount);
    }
    
    @Override
    public double count() {
        return getMeter().count();
    }

}
