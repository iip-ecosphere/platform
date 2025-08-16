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

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.support.metrics.MeterRegistry;
import de.iip_ecosphere.platform.support.metrics.Timer;

/**
 * A wrapping micrometer timer.
 * 
 * @author Holger Eichelberger, SSE
 */
class MicrometerTimer extends AbstractMeter<io.micrometer.core.instrument.Timer> implements Timer {
    
    /**
     * Creates a wrapping timer.
     * 
     * @param timer the micrometer timer
     */
    MicrometerTimer(io.micrometer.core.instrument.Timer timer) {
        super(timer);
    }
    
    /**
     * A wrapping timer builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class MicrometerTimerBuilder implements TimerBuilder {
        
        private io.micrometer.core.instrument.Timer.Builder builder;

        /**
         * Creates a wrapping counter builder.
         * 
         * @param name the name of the counter
         */
        MicrometerTimerBuilder(String name) {
            builder = io.micrometer.core.instrument.Timer.builder(name);
        }
        
        @Override
        public TimerBuilder tags(String... tags) {
            builder.tags(tags);
            return this;
        }

        @Override
        public TimerBuilder description(String description) {
            builder.description(description);
            return this;
        }

        @Override
        public Timer register(MeterRegistry registry) {
            return new MicrometerTimer(builder.register(((MicrometerMeterRegistry) registry).getRegistry()));
        }
        
    }

    @Override
    public void record(Runnable func) {
        getMeter().record(func);
    }
    
    @Override
    public void record(long amount, TimeUnit unit) {
        getMeter().record(amount, unit);
    }
    
    @Override
    public <T> T record(Supplier<T> supplier) {
        return getMeter().record(supplier);
    }

    @Override
    public <T> T recordCallable(Callable<T> function) throws Exception {
        return getMeter().recordCallable(function);
    }
    
    @Override
    public long count() {
        return getMeter().count();
    }
    
    @Override
    public TimeUnit baseTimeUnit() {
        return getMeter().baseTimeUnit();
    }
    
    @Override
    public double max(TimeUnit unit) {
        return getMeter().max(unit);
    }

    @Override
    public double totalTime(TimeUnit unit) {
        return getMeter().totalTime(unit);
    }

    @Override
    public double mean(TimeUnit unit) {
        return getMeter().mean(unit);
    }
    
}
