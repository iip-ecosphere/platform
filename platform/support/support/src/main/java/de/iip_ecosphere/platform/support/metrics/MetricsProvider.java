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

    /**
     * Creates and registers a default service received counter.
     * 
     * @param serviceName the static name/id of the service
     * @param serviceId the (complete) id of the service
     * @param appId the id of the application
     * @param appInstanceId the id of the application instance
     * @return the registered counter instance
     * @see #createServiceSentReceivedCounter(boolean, String, String, String, String)
     */
    public default Counter createServiceReceivedCounter(String serviceName, String serviceId, 
        String appId, String appInstanceId) {
        return createServiceSentReceivedCounter(true, serviceName, serviceId, appId, appInstanceId);
    }
    
    /**
     * Creates and registers a default service sent counter.
     * 
     * @param serviceName the static name/id of the service
     * @param serviceId the (complete) id of the service
     * @param appId the id of the application
     * @param appInstanceId the id of the application instance
     * @return the registered counter instance
     * @see #createServiceSentReceivedCounter(boolean, String, String, String, String)
     */
    public default Counter createServiceSentCounter(String serviceName, String serviceId, 
        String appId, String appInstanceId) {
        return createServiceSentReceivedCounter(false, serviceName, serviceId, appId, appInstanceId);
    }

    /**
     * Creates and registers a default service send/receive counter.
     * 
     * @param receive is it for receiving ({@code true}) or sending
     * @param serviceName the static name/id of the service
     * @param serviceId the (complete) id of the service
     * @param appId the id of the application
     * @param appInstanceId the id of the application instance
     * @return the registered counter instance
     */
    public Counter createServiceSentReceivedCounter(boolean receive, String serviceName, String serviceId, 
        String appId, String appInstanceId);
    
    /**
     * Creates and registers a default service processing timer.
     * 
     * @param serviceName the static name/id of the service
     * @param serviceId the (complete) id of the service
     * @param appId the id of the application
     * @param appInstanceId the id of the application instance
     * @return the registered counter instance
     */
    public Timer createServiceProcessingTimer(String serviceName, String serviceId, String appId, 
        String appInstanceId);    
    
    /**
     * Helper method to increase/increment the given {@code counter} by the specified {@code value} if {@code data}
     * is not <b>null</b>. Does nothing if {@code counter} is <b>null</b>.
     * 
     * @param counter the counter
     * @param value the value
     * @param data the data to consider
     * @return {@code data}
     */
    public static <T> T increaseCounterBy(Counter counter, double value, T data) {
        if (null != data) {
            increaseCounterBy(counter, value);
        }
        return data;
    }
    
    /**
     * Helper method to increase/increment the given {@code counter} by the specified {@code value}.
     * Does nothing if {@code counter} is <b>null</b>.
     * 
     * @param counter the counter
     * @param value the value
     */
    public static void increaseCounterBy(Counter counter, double value) {
        if (null != counter) {
            counter.increment(value);
        }
    }
    
    /**
     * Adds a service to be updated regularly regarding its monitoring data. Services are 
     * not added multiple times.
     * 
     * @param service the service, ignored if <b>null</b>
     */
    public void addService(MonitoringService service);    
    
}
