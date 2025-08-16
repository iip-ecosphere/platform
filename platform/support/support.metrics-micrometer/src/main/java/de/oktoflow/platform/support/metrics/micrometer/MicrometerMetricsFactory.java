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

import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.metrics.Counter.CounterBuilder;
import de.iip_ecosphere.platform.support.metrics.Gauge.GaugeBuilder;
import de.iip_ecosphere.platform.support.metrics.Measurement;
import de.iip_ecosphere.platform.support.metrics.Meter.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

import de.iip_ecosphere.platform.support.metrics.MeterFilter;
import de.iip_ecosphere.platform.support.metrics.MeterRegistry;
import de.iip_ecosphere.platform.support.metrics.MetricsFactory;
import de.iip_ecosphere.platform.support.metrics.Statistic;
import de.iip_ecosphere.platform.support.metrics.Tag;
import de.iip_ecosphere.platform.support.metrics.Timer;
import de.iip_ecosphere.platform.support.metrics.Timer.Sample;
import de.iip_ecosphere.platform.support.metrics.Timer.TimerBuilder;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Implements the metrics factory for micrometer.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MicrometerMetricsFactory extends MetricsFactory {

    @Override
    public MeterRegistry createRegistry(Object registry, boolean warn) {
        if (registry instanceof io.micrometer.core.instrument.MeterRegistry) {
            return new MicrometerMeterRegistry((io.micrometer.core.instrument.MeterRegistry) registry);
        } else {
            if (warn) {
                LoggerFactory.getLogger(this).warn("Cannot cast registry of type {} to {}. Using default "
                    + "implementation instead.", registry == null ? null : registry.getClass().getName(), 
                    io.micrometer.core.instrument.MeterRegistry.class.getName());
            }
            return new MicrometerMeterRegistry(new SimpleMeterRegistry());
        }
    }

    @Override
    public CounterBuilder createCounter(String name) {
        return new MicrometerCounter.MicrometerCounterBuilder(name);
    }

    @Override
    public TimerBuilder createTimer(String name) {
        return new MicrometerTimer.MicrometerTimerBuilder(name);
    }

    @Override
    public Sample createTimerStart() {
        return new Sample() {
            
            private io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start();
            
            @Override
            public long stop(Timer timer) {
                return sample.stop(((MicrometerTimer) timer).getMeter());
            }
            
        };
    }

    @Override
    public <T> GaugeBuilder<T> createGauge(String name, T obj, ToDoubleFunction<T> supplier) {
        return new MicrometerGauge.MicrometerGaugeBuilder<T>(
            io.micrometer.core.instrument.Gauge.builder(name, obj, supplier)
        );
    }

    @Override
    public GaugeBuilder<Supplier<Number>> createGauge(String name, Supplier<Number> supplier) {
        return new MicrometerGauge.MicrometerGaugeBuilder<Supplier<Number>>(
            io.micrometer.core.instrument.Gauge.builder(name, supplier)
        );
    }

    @Override
    public MeterFilter createFilterDenyNameStartsWith(String prefix) {
        return new MicrometerMeterFilter(io.micrometer.core.instrument.config.MeterFilter.denyNameStartsWith(prefix));
    }
    
    @Override
    public MeterFilter createFilterAcceptNameStartsWith(String prefix) {
        return new MicrometerMeterFilter(io.micrometer.core.instrument.config.MeterFilter.acceptNameStartsWith(prefix));
    }
    
    @Override
    public MeterFilter createFilterDeny() {
        return new MicrometerMeterFilter(io.micrometer.core.instrument.config.MeterFilter.deny());
    }
    
    @Override
    public Tag createTag(String key, String value) {
        return new MicrometerTag(io.micrometer.core.instrument.Tag.of(key, value));
    }

    @Override
    public Tag createImmutableTag(String key, String value) {
        return new MicrometerTag(new ImmutableTag(key, value));
    }

    @Override
    public Id createId(String name, List<Tag> tags, String baseUnit, String description, 
        de.iip_ecosphere.platform.support.metrics.Meter.Type type) {
        Tags tmp = null;
        if (tags != null) {
            List<io.micrometer.core.instrument.Tag> t = new ArrayList<>(tags.size());
            for (int i = 0; i < tags.size(); i++) {
                t.add(((MicrometerTag) tags.get(i)).getTag());
            }
            tmp = Tags.of(t);
        }
        return new AbstractMeter.MicrometerId(new io.micrometer.core.instrument.Meter.Id(name, tmp, baseUnit, 
            description, MicrometerUtils.mmTypeValue(type)));
    }
    
    /**
     * Creates a measurement.
     * 
     * @param valueFunction the (dynamic/static) value function
     * @param statistic the statistic type
     * @return the measurement
     */
    public Measurement createMeasurement(Supplier<Double> valueFunction, Statistic statistic) {
        return new MicrometerMeasurement(new io.micrometer.core.instrument.Measurement(
            valueFunction, MicrometerUtils.statisticValue(statistic)));
    }

}
