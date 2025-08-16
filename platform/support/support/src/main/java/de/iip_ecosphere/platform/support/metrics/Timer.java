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

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Represents a timer.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Timer extends Meter {
    
    /**
     * Builds a timer.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface TimerBuilder extends MetricsBuilder<Timer, TimerBuilder> {
        
    }
    
    /**
     * Maintains state on the clock's start position for a latency sample. Complete the timing
     * by calling {@link Sample#stop(Timer)}. Note how the {@link Timer} isn't provided until the
     * sample is stopped, allowing you to determine the timer's tags at the last minute.
     */
    public interface Sample {

        /**
         * Records the duration of the operation.
         *
         * @param timer The timer to record the sample to.
         * @return The total duration of the sample in nanoseconds
         */
        public long stop(Timer timer);
    
    }
    
    /**
     * Executes the runnable {@code func} and records the time taken.
     *
     * @param func function to execute and measure the execution time
     */
    public void record(Runnable func);
    
    /**
     * Updates the statistics kept by the timer with the specified amount.
     *
     * @param amount Duration of a single event being measured by this timer. If the amount is less than 0
     *               the value will be dropped
     * @param unit   Time unit for the amount being recorded
     */
    public void record(long amount, TimeUnit unit);

    /**
     * Executes the Supplier {@code supplier} and records the time taken.
     *
     * @param supplier   Function to execute and measure the execution time.
     * @param <T> The return type of the {@link supplier}.
     * @return The return value of {@code supplier}.
     */
    public <T> T record(Supplier<T> supplier);

    // checkstyle: stop exception type check
    
    /**
     * Executes the callable {@code function} and records the time taken.
     *
     * @param <T> The return type of the {@link Callable}.
     * @param function   Function to execute and measure the execution time.
     * @return The return value of {@code function}.
     * @throws Exception Any exception bubbling up from the callable.
     */
    public <T> T recordCallable(Callable<T> function) throws Exception;

    // checkstyle: resume exception type check

    /**
     * Returns the number of times that stop has been called on this timer.
     * 
     * @return the number of times
     */
    public long count();
    
    /**
     * Returns the base time unit.
     * 
     * @return the base time unit of the timer to which all published metrics will be scaled
     */
    public TimeUnit baseTimeUnit();

    /**
     * Returns the maximum time of a single event.
     * 
     * @param unit The base unit of time to scale the max to
     * @return The maximum time of a single event
     */
    public double max(TimeUnit unit);

    /**
     * Returns the total time of recorded events.
     * 
     * @param unit The base unit of time to scale the total to
     * @return The total time of recorded events
     */
    public double totalTime(TimeUnit unit);

    /**
     * Returns the mean/average for all recorded events.
     * 
     * @param unit The base unit of time to scale the mean to
     * @return the distribution average for all recorded events
     */
    public double mean(TimeUnit unit);

}
