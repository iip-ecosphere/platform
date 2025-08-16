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

package test.de.iip_ecosphere.platform.support.metrics;

import java.util.List;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

import de.iip_ecosphere.platform.support.metrics.Counter.CounterBuilder;
import de.iip_ecosphere.platform.support.metrics.Gauge.GaugeBuilder;
import de.iip_ecosphere.platform.support.metrics.Measurement;
import de.iip_ecosphere.platform.support.metrics.Meter.Id;
import de.iip_ecosphere.platform.support.metrics.Meter.Type;
import de.iip_ecosphere.platform.support.metrics.MeterFilter;
import de.iip_ecosphere.platform.support.metrics.MeterRegistry;
import de.iip_ecosphere.platform.support.metrics.MetricsFactory;
import de.iip_ecosphere.platform.support.metrics.Statistic;
import de.iip_ecosphere.platform.support.metrics.Tag;
import de.iip_ecosphere.platform.support.metrics.Timer.Sample;
import de.iip_ecosphere.platform.support.metrics.Timer.TimerBuilder;

/**
 * Implements an empty metrics factory interface for simple testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestMetrics extends MetricsFactory {

    @Override
    protected MeterRegistry createRegistry(Object registry, boolean warn) {
        return null;
    }

    @Override
    public CounterBuilder createCounter(String name) {
        return null;
    }

    @Override
    public TimerBuilder createTimer(String name) {
        return null;
    }

    @Override
    public <T> GaugeBuilder<T> createGauge(String name, T obj, ToDoubleFunction<T> supplier) {
        return null;
    }

    @Override
    public GaugeBuilder<Supplier<Number>> createGauge(String name, Supplier<Number> supplier) {
        return null;
    }

    @Override
    public MeterFilter createFilterDenyNameStartsWith(String prefix) {
        return null;
    }

    @Override
    public MeterFilter createFilterAcceptNameStartsWith(String prefix) {
        return null;
    }

    @Override
    public MeterFilter createFilterDeny() {
        return null;
    }

    @Override
    public Tag createTag(String key, String value) {
        return null;
    }

    @Override
    public Tag createImmutableTag(String key, String value) {
        return null;
    }

    @Override
    public Id createId(String name, List<Tag> tags, String baseUnit, String description, Type type) {
        return null;
    }

    @Override
    public Sample createTimerStart() {
        return null;
    }

    @Override
    public Measurement createMeasurement(Supplier<Double> valueFunction, Statistic statistic) {
        return null;
    }

}
