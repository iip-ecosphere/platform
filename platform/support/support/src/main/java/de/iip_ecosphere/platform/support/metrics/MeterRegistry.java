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

import de.iip_ecosphere.platform.support.metrics.Meter.Id;

/**
 * A meter registry akin to micrometer.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface MeterRegistry {

    /**
     * Represents the configuration of the registry.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface Config {
        
        /**
         * Adds a meter filter to the registry. Filters are applied in the order in which they are added.
         *
         * @param filter The filter to add to the registry.
         * @return This configuration instance.
         */
        public Config meterFilter(MeterFilter filter);
        
        /**
         * Returns the clock.
         * 
         * @return the clock used to measure durations of timers and long task timers (and sometimes
         * influences publishing behavior).
         */
        public Clock clock();
        
    }
    
    /**
     * Returns the registry configuration.
     * 
     * @return A configuration object used to change the behavior of this registry.
     */
    public Config config();
    
    /**
     * Remove a {@link Meter} from this {@link MeterRegistry registry}. This is expected to be a {@link Meter} with
     * the same {@link Id} returned when registering a meter - which will have {@link MeterFilter}s applied to it.
     *
     * @param meter The meter to remove
     * @return The removed meter (may not be the same instance as registered before), or <b>null</b> if the provided 
     *     meter is not currently registered.
     */
    public Meter remove(Meter meter);

    /**
     * Remove a {@link Meter} from this {@link MeterRegistry registry}. This is expected to be a {@link Meter} with
     * the same {@link Id} returned when registering a meter - which will have {@link MeterFilter}s applied to it.
     * May affect multiple meters as tags not specified here.
     *
     * @param name the name of the meter to remove
     * @return The removed meter (may not be the same instance as registered before), or <b>null</b> if the provided 
     *     meter is not currently registered.
     */
    public Meter remove(String name);

    /**
     * Remove a {@link Meter} from this {@link MeterRegistry registry} based the given {@link Id} as-is. The registry's
     * {@link MeterFilter}s will not be applied to it. You can use the {@link Id} of the {@link Meter} returned
     * when registering a meter, since that will have {@link MeterFilter}s already applied to it.
     *
     * @param id The id of the meter to remove
     * @return The removed meter, or null if no meter matched the provided id
     */
    public Meter remove(Id id);
    
    /**
     * Returns the meter of the specified name.
     * 
     * @param name the name
     * @return the meter, may not be the same instance as returned before, may be <b>null</b> for none
     */
    public Meter getMeter(String name);

    /**
     * Returns the meter of the specified name matching the given tags.
     * 
     * @param name the name
     * @return the meter, may not be the same instance as returned before, may be <b>null</b> for none
     */
    public Meter getMeter(String name, Iterable<Tag> tags);

    /**
     * Returns the gauge of the specified name.
     * 
     * @param name the name
     * @return the gauge, may be <b>null</b> for none
     */
    public Gauge getGauge(String name);

    /**
     * Returns the counter of the specified name.
     * 
     * @param name the name
     * @return the counter, may be <b>null</b> for none
     */
    public Counter getCounter(String name);

    /**
     * Returns the timer of the specified name.
     * 
     * @param name the name
     * @return the timer, may be <b>null</b> for none
     */
    public Timer getTimer(String name);

    /**
     * Register a gauge that reports the value of the {@link Number}.
     *
     * @param name   Name of the gauge being registered.
     * @param number Thread-safe implementation of {@link Number} used to access the value.
     * @param <T>    The type of the state object from which the gauge value is extracted.
     * @return The number that was passed in so the registration can be done as part of an assignment
     * statement.
     */
    public <T extends Number> T gauge(String name, T number);

    /**
     * Tracks a monotonically increasing value.
     *
     * @param name The base metric name
     * @param tags MUST be an even number of arguments representing key/value pairs of tags.
     * @return A new or existing counter.
     */
    public Counter counter(String name, String... tags);

    /**
     * Measures the time taken for short tasks and the count of these tasks.
     *
     * @param name The base metric name
     * @param tags MUST be an even number of arguments representing key/value pairs of tags.
     * @return A new or existing timer.
     */
    public Timer timer(String name, String... tags);

    /**
     * Returns the registered meters.
     * 
     * @return the registered meters.
     */
    public List<Meter> getMeters();
    
    
}
