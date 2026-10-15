/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.metrics;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Simplified metrics provider interface.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface MetricsProvider {
    
    /**
     * Returns the clock used in the underlying meter registry.
     * 
     * @return the clock
     */
    public Clock getClock();

    /**
     * Returns the meter registry.
     * 
     * @return the meter registry
     */
    public MeterRegistry getRegistry();

    /**
     * Records the execution of a runnable using a timer.
     * 
     * @param timerId  identifier of the custom timer
     * @param runnable runnable function that we want to record
     * @throws IllegalArgumentException if the runnable or the identifier are null
     */
    public void recordWithTimer(String timerId, Runnable runnable);
    
    /**
     * Records the execution of a supplier using a timer.
     * 
     * @param <T>      the datatype that the supplier returns
     * @param timerId  identifier of the custom timer
     * @param supplier supplier function that we want to record
     * @throws IllegalArgumentException if the identifier or the supplier are null
     * @return the value returned by the supplier
     */
    public <T> T recordWithTimer(String timerId, Supplier<T> supplier);

    /**
     * Adds time from a specific timer.
     * 
     * @param timerId identifier of the custom timer
     * @param time    amount of time we want to record
     * @param unit    base unit of the time we want to record
     * @throws IllegalArgumentException if the identifier or the supplier are null
     * @throws IllegalArgumentException if the {@code time} is negative
     */
    public void recordWithTimer(String timerId, long time, TimeUnit unit);
    
    /**
     * Adds a gauge value to the MeterRegistry.<br>
     * If the identifier does not correspond to an existing gauge, a new gauge will
     * be created and registered. If it is an existing gauge, the value will simply
     * be modified.
     * 
     * @param gaugeId identifier for the gauge
     * @param value   value we want to set the gauge to
     * @throws IllegalArgumentException if the identifier is null
     */
    public void addGaugeValue(String gaugeId, double value);

    /**
     * Remove a custom gauge from the Meter Registry.
     * 
     * @param gaugeId identifier of the custom gauge
     * @throws IllegalArgumentException if there is no gauge with that identifier
     */
    public void removeGauge(String gaugeId);
    
    /**
     * Increases the counter by a certain value.<br>
     * If the identifier does not correspond to an existing counter, a new counter
     * will be created and registered. If it is an existing counter, the value will
     * simply be incremented by the amount requested.
     * 
     * @param counterId identifier of the custom counter
     * @param value     the amount we want to increases the counter
     * @throws IllegalArgumentException if {@code value} is negative or if
     *                                  {@code counterId} is null
     */
    public void increaseCounterBy(String counterId, double value);
    
}
