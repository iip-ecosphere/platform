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

import java.util.List;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

import de.iip_ecosphere.platform.support.metrics.Counter.CounterBuilder;
import de.iip_ecosphere.platform.support.metrics.Gauge.GaugeBuilder;
import de.iip_ecosphere.platform.support.metrics.Meter.Id;
import de.iip_ecosphere.platform.support.metrics.Timer.Sample;
import de.iip_ecosphere.platform.support.metrics.Timer.DefaultSample;
import de.iip_ecosphere.platform.support.metrics.Timer.TimerBuilder;
import de.iip_ecosphere.platform.support.plugins.PluginManager;

/**
 * Generic access to Metering/Metrics. Requires an implementing plugin of type {@link MetricsFactory} or an active 
 * {@link MetricsFactoryProviderDescriptor}. Simplified interface akin to micrometer.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class MetricsFactory {
    
    private static MetricsFactory instance; 

    static {
        instance = PluginManager.getPluginInstance(MetricsFactory.class, MetricsFactoryProviderDescriptor.class);
    }

    /**
     * Returns the Rest instance.
     * 
     * @return the instance
     */
    public static MetricsFactory getInstance() {
        return instance;
    }
    
    /**
     * Manually sets the instance. Shall not be needed, but may be required in some tests.
     * 
     * @param rest the Rest instance
     */
    public static void setInstance(MetricsFactory rest) {
        if (null != rest) {
            instance = rest;
        }
    }
    
    /**
     * Creates a default meter registry.
     * 
     * @return the registry
     */
    public MeterRegistry createRegistry() {
        return createRegistry(null, false);
    }

    /**
     * Creates a meter registry. This method is a bit tricky as it implies a border crossing between abstraction
     * and implementation. The actual type of {@code registry} may not fit to the registry type of the implementation,
     * which we actually don't know here and, thus, cannot use. In some cases it is required to use this plugin as
     * maven component and to exclude the contained implementation libray to make the types fit.
     * 
     * @param registry something that the implementation shall use to create a registry from, e.g., 
     *     a micrometer registry, may be <b>null</b>
     * @return the registry
     */
    public MeterRegistry createRegistry(Object registry) {
        return createRegistry(registry, true);
    }

    /**
     * Creates a meter registry. This method is a bit tricky as it implies a border crossing between abstraction
     * and implementation. The actual type of {@code registry} may not fit to the registry type of the implementation,
     * which we actually don't know here and, thus, cannot use. In some cases it is required to use this plugin as
     * maven component and to exclude the contained implementation libray to make the types fit.
     * 
     * @param registry something that the implementation shall use to create a registry from, e.g., 
     *     a micrometer registry, may be <b>null</b>
     * @param warn warn if the provided type does not match the implementation and a default is created instead
     * @return the registry
     */
    protected abstract MeterRegistry createRegistry(Object registry, boolean warn);

    /**
     * Starts building a counter by returning a counter builder.
     * 
     * @param name the name of the counter
     * @return the counter builder
     */
    public abstract CounterBuilder createCounter(String name);

    /**
     * Starts building a timer by returning a timer builder.
     * 
     * @param name the name of the timer
     * @return the timer builder
     */
    public abstract TimerBuilder createTimer(String name);

    /**
     * Starts building a gauge by returning a gauge builder.
     * 
     * @param <T> the type of object
     * @param name the name of the gauge
     * @param obj the object providing the value
     * @param supplier a value supplier turning {@code obj} into a gauge value
     * @return the gauge builder
     */
    public abstract <T> GaugeBuilder<T> createGauge(String name, T obj, ToDoubleFunction<T> supplier);
    
    /**
     * A convenience method for building a gauge from a supplying function, holding a strong
     * reference to this function.
     *
     * @param name the gauge's name
     * @param supplier a function that yields a double value for the gauge
     * @return the gauge builder
     */
    public abstract GaugeBuilder<Supplier<Number>> createGauge(String name, Supplier<Number> supplier);
    
    /**
     * Creates a filter for meters that start with the provided name prefix should NOT be present in published metrics.
     *
     * @param prefix when a meter name starts with the prefix, guarantee its exclusion in published metrics
     * @return a filter that guarantees the exclusion of matching meters
     */
    public abstract MeterFilter createFilterDenyNameStartsWith(String prefix);

    /**
     * Creates a filter for meters that start with the provided name should be present in published metrics.
     *
     * @param prefix When a meter name starts with the prefix, guarantee its inclusion in published metrics
     * @return a filter that guarantees the inclusion of matching meters
     */
    public abstract MeterFilter createFilterAcceptNameStartsWith(String prefix);

    /**
     * Creates a filter excluding all meter in published metrics. 
     *
     * @return A filter that guarantees the exclusion of all meters.
     */
    public abstract MeterFilter createFilterDeny();

    /**
     * Creates a tag.
     * 
     * @param key the key
     * @param value the value
     * @return the tag
     */
    public abstract Tag createTag(String key, String value);

    /**
     * Creates an immutable tag.
     * 
     * @param key the key
     * @param value the value
     * @return the tag
     */
    public abstract Tag createImmutableTag(String key, String value);

    /**
     * Creates an id.
     * 
     * @param name the name
     * @param tags the optional tags, may be <b>null</b>
     * @param baseUnit the optional base unit, may be <b>null</b>
     * @param description an optional description, may be <b>null</b>
     * @param type an optional metrics type, may be <b>null</b>
     * @return the created id
     */
    public abstract Id createId(String name, List<Tag> tags, String baseUnit, String description, Meter.Type type);

    /**
     * Creates a timer start sample.
     * 
     * @return the timer sample
     */
    public Sample createTimerStart() {
        return new DefaultSample(getSystemClock());
    }

    /**
     * Returns the system clock (representation).
     * 
     * @return the system clock
     */
    public abstract Clock getSystemClock();
    
    /**
     * Creates a measurement.
     * 
     * @param valueFunction the (dynamic/static) value function
     * @param statistic the statistic type
     * @return the measurement
     */
    public abstract Measurement createMeasurement(Supplier<Double> valueFunction, Statistic statistic);

    /**
     * Starts building a counter by returning a counter builder.
     * 
     * @param name the name of the counter
     * @return the counter builder
     */
    public static CounterBuilder buildCounter(String name) {
        return getInstance().createCounter(name);
    }

    /**
     * Starts building a timer by returning a timer builder.
     * 
     * @param name the name of the timer
     * @return the timer builder
     */
    public static TimerBuilder buildTimer(String name) {
        return getInstance().createTimer(name);
    }

    /**
     * Starts building a gauge by returning a gauge builder.
     * 
     * @param <T> the type of object
     * @param name the name of the gauge
     * @param obj the object providing the value
     * @param supplier a value supplier turning {@code obj} into a gauge value
     * @return the gauge builder
     */
    public static <T> GaugeBuilder<T> buildGauge(String name, T obj, ToDoubleFunction<T> supplier) {
        return getInstance().createGauge(name, obj, supplier);
    }
    
    /**
     * Starts building a gauge by returning a gauge builder.
     * 
     * @param name the name of the gauge
     * @param supplier a value supplier turning {@code obj} into a gauge value
     * @return the gauge builder
     */
    public static GaugeBuilder<Supplier<Number>> buildGauge(String name, Supplier<Number> supplier) {
        return getInstance().createGauge(name, supplier);
    }
    
    /**
     * Creates a filter for meters that start with the provided name prefix should NOT be present in published metrics.
     *
     * @param prefix when a meter name starts with the prefix, guarantee its exclusion in published metrics
     * @return a filter that guarantees the exclusion of matching meters
     */
    public static MeterFilter denyNameStartsWith(String prefix) {
        return getInstance().createFilterDenyNameStartsWith(prefix);
    }
    
    /**
     * Creates a filter for meters that start with the provided name should be present in published metrics.
     *
     * @param prefix When a meter name starts with the prefix, guarantee its inclusion in published metrics
     * @return a filter that guarantees the inclusion of matching meters
     */
    public static MeterFilter acceptNameStartsWith(String prefix) {
        return getInstance().createFilterAcceptNameStartsWith(prefix);
    }
    
    /**
     * Creates a filter excluding all meter in published metrics. 
     *
     * @return A filter that guarantees the exclusion of all meters.
     */
    public static MeterFilter deny() {
        return getInstance().createFilterDeny();
    }
    
    /**
     * Creates a tag.
     * 
     * @param key the key
     * @param value the value
     * @return the tag
     */
    public static Tag buildTag(String key, String value) {
        return getInstance().createTag(key, value);
    }

    /**
     * Creates an immutable tag.
     * 
     * @param key the key
     * @param value the value
     * @return the tag
     */
    public static Tag buildImmutableTag(String key, String value) {
        return getInstance().createImmutableTag(key, value);
    }
    
    /**
     * Creates an id.
     * 
     * @param name the name
     * @param tags the optional tags, may be <b>null</b>
     * @param baseUnit the optional base unit, may be <b>null</b>
     * @param description an optional description, may be <b>null</b>
     * @param type an optional metrics type, may be <b>null</b>
     * @return the created id
     */
    public static Id buildId(String name, List<Tag> tags, String baseUnit, String description, Meter.Type type) {
        return getInstance().createId(name, tags, baseUnit, description, type);
    }
    
    /**
     * Creates a measurement.
     * 
     * @param valueFunction the (dynamic/static) value function
     * @param statistic the statistic type
     * @return the measurement
     */
    public static Measurement buildMeasurement(Supplier<Double> valueFunction, Statistic statistic) {
        return getInstance().createMeasurement(valueFunction, statistic);
    }


}
