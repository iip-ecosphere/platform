/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.environment.metricsProvider.meterRepresentation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;

/**
 * This class aims to provide a prototypical implementation of the Timer
 * Interface from Micrometer-API, allowing a client process to access the Timer
 * values collected from a Service in a uniform way.<br>
 * Even though the methods are functional and will indeed offer an accurate
 * implementation of the metric we are simulating with this prototype, it is
 * highly recommended that an instance of this class is used solely to access
 * data and not to modify it, as the changes will not be registered in any sort
 * of registry under normal circumstances, and this will alter the read values
 * of the actual metrics. <br>
 * Timer intended to track of a large number of short running events.<br>
 * The JsonObject representing a Timer will have the following format: <br>
 * 
 * <pre>
{
    "name": "sample.name",
    "description": "sample description, can be null",
    "baseUnit": "ourTimeUnit",
    "measurements": [
        {
            "statistic": "COUNT",
            "value": 123
        },
        {
            "statistic": "TOTAL_TIME",
            "value": 123.123
        },
        {
            "statistic": "MAX",
            "value": 123.123
        }
    ],
    "availableTags": []
}
 * </pre>
 * 
 * Special attention is required in the keys for the JsonValues as well as the
 * type of Statistic and number of measurements. For the time valid time unit
 * values, see {@link TimeUnit}<br>
 * The services offered by this class are:
 * <ul>
 * <li>Parse a JsonObject into a Timer</li>
 * <li>Take a Histogram Snapshot</li>
 * <li>Record an amount of time with this timer</li>
 * <li>Check the maximum and total time recorded by the timer</li>
 * <li>Check the amount of times the timer has been called</li>
 * <li>Check the basic time unit for the timer</li>
 * </ul>
 * 
 * @see Timer
 * @see TimeUnit
 * 
 * @author Miguel Gomez
 */
public class TimerRepresentation extends MeterRepresentation implements Timer {

    private long count;
    private TimeUnit baseTimeUnit;
    private List<Measurement> measurements;

    // Due to the implementation of TimeUnit, these values are stored as Nanoseconds
    private long totalTime;
    private long maxTime;

    private List<Long> updates;

    /**
     * Initializes a new TimerRepresentation.
     * 
     * @param object JsonObject representing the Timer
     * @param tags   tags that the Timer has
     * @throws IllegalArgumentException if the object is {@code null}, or if the
     *                                  JsonObject doesn't represent a valid timer.
     */
    private TimerRepresentation(JsonObject object, String... tags) {
        super(object, Type.TIMER, tags);

        try {
            baseTimeUnit = TimeUnit.valueOf(object.getString("baseUnit").toUpperCase());
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("This object does not map a valid Timer!", iae);
        }

        long conv = baseTimeUnit.toNanos(1);
        JsonArray ja = object.getJsonArray("measurements");

        if (ja.size() != 3) {
            throw new IllegalArgumentException("This object does not map a valid Timer!");
        }

        measurements = new ArrayList<Measurement>();

        boolean cFlag = false;
        boolean ttFlag = false;
        boolean mtFlag = false;
        JsonObject jo;

        for (int i = 0; i < 3; i++) {
            final double aux;
            jo = ja.getJsonObject(i);
            switch (Statistic.valueOf(jo.getString("statistic"))) {
            case COUNT:
                if (cFlag) {
                    throw new IllegalArgumentException("This object does not map a valid Timer!");
                }
                count = jo.getJsonNumber("value").longValue();
                measurements.add(new Measurement(() -> (double) count, Statistic.COUNT));
                cFlag = true;
                break;
            case TOTAL_TIME:
                if (ttFlag) {
                    throw new IllegalArgumentException("This object does not map a valid Timer!");
                }
                aux = jo.getJsonNumber("value").doubleValue();
                totalTime = (long) (aux * conv);
                measurements.add(new Measurement(() -> totalTime(baseTimeUnit), Statistic.TOTAL_TIME));
                ttFlag = true;
                break;
            case MAX:
                if (mtFlag) {
                    throw new IllegalArgumentException("This object does not map a valid Timer!");
                }
                aux = jo.getJsonNumber("value").doubleValue();
                maxTime = (long) (aux * conv);
                measurements.add(new Measurement(() -> max(baseTimeUnit), Statistic.MAX));
                mtFlag = true;
                break;
            default:
                throw new IllegalArgumentException("This object does not map a valid Timer!");
            }

        }

        updates = new ArrayList<Long>();
    }

    /**
     * Initializes a new TimerRepresentation.
     * 
     * @param name URN of the timer
     * @throws IllegalArgumentException if the name is null or empty
     */
    private TimerRepresentation(String name) {
        super(name, Type.TIMER);

        count = 0;
        baseTimeUnit = TimeUnit.SECONDS;
        totalTime = 0;
        maxTime = 0;
        updates = new ArrayList<Long>();

        measurements = new ArrayList<Measurement>();
        measurements.add(new Measurement(() -> (double) count, Statistic.COUNT));
        measurements.add(new Measurement(() -> (double) totalTime, Statistic.TOTAL_TIME));
        measurements.add(new Measurement(() -> (double) maxTime, Statistic.MAX));
    }
    
    /**
     * Parses a new timer from a JsonObject.<br>
     * See the class documentation to see the format of a JsonObject representing a
     * Timer is expecting to have.
     * 
     * @param object JsonObject representing the Timer we wish to parse
     * @param tags   tags that the counter has following the format
     *               {@code key:value}
     * @return a Timer representation of the JsonObject
     * @throws IllegalArgumentException if the object is {@code null}, or if the
     *                                  JsonObject doesn't represent a valid timer.
     */
    public static Timer parseTimer(JsonObject object, String... tags) {
        return new TimerRepresentation(object, tags);
    }

    /**
     * Creates a new Timer with no measurements in it.
     * 
     * @param name URN of the timer
     * @return a new Timer ready to be used
     * @throws IllegalArgumentException if the name is null or empty
     */
    public static Timer createNewTimer(String name) {
        return new TimerRepresentation(name);
    }

    @Override
    public HistogramSnapshot takeSnapshot() {
        return new HistogramSnapshot(count, totalTime, maxTime, null, null, null);
    }

    @Override
    public void record(long amount, TimeUnit unit) {
        long aux = amount * unit.toNanos(1);

        if (maxTime < aux) {
            maxTime = aux;
        }

        count++;
        totalTime += aux;
        updates.add(aux);
    }

    @Override
    public <T> T record(Supplier<T> supplier) {
        T supply;
        Sample timer = Timer.start();
        supply = supplier.get();
        timer.stop(this);

        return supply;
    }

    @Override
    public <T> T recordCallable(Callable<T> callable) throws Exception {
        T supply;
        Sample timer = Timer.start();
        supply = callable.call();
        timer.stop(this);

        return supply;
    }

    @Override
    public void record(Runnable runnable) {
        Sample timer = Timer.start();
        runnable.run();
        timer.stop(this);
    }

    @Override
    public long count() {
        return count;
    }

    @Override
    public double totalTime(TimeUnit unit) {
        return (double) totalTime / (double) unit.toNanos(1);
    }

    @Override
    public double max(TimeUnit unit) {
        return (double) maxTime / (double) unit.toNanos(1);
    }

    @Override
    public TimeUnit baseTimeUnit() {
        return baseTimeUnit;
    }

    @Override
    public Iterable<Measurement> measure() {
        return measurements;
    }

    /**
     * Provides an updater for the Timer. <br>
     * The information includes the name and the different recordings carried out by
     * this timer since its instantiation. This information is packed into a JSON
     * Object with the following format:
     * 
     * <pre>
     * {
     *     "name":"customtimer",
     *     "recordings":[
     *         100000000,
     *         300000000,
     *         200000000
     *     ]
     * }
     * </pre>
     * 
     * @see MeterRepresentation#getUpdater()
     */
    @Override
    public JsonObject getUpdater() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        JsonArrayBuilder jab = Json.createArrayBuilder();

        job.add("name", getId().getName());

        for (long update : updates) {
            jab.add(update);
        }

        job.add("recordings", jab.build());

        return job.build();
    }

}
