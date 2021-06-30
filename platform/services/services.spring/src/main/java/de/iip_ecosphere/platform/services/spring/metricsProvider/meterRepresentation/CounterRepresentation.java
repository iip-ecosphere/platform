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

package de.iip_ecosphere.platform.services.spring.metricsProvider.meterRepresentation;

import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Statistic;

/**
 * This class aims to provide a prototypical implementation of the Counter
 * Interface from Micrometer-API, allowing a client process to access the
 * Counter values collected from a Service in a uniform way.<br>
 * Even though the methods are functional and will indeed offer an accurate
 * implementation of the metric we are simulating with this prototype, it is
 * highly recommended that an instance of this class is used solely to access
 * data and not to modify it, as the changes will not be registered in any sort
 * of registry under normal circumstances, and this will alter the read values
 * of the actual metrics. <br>
 * Counters monitor monotonically increasing values. Counters may never be reset
 * to a lesser value. The JsonObject representing a Counter will have the
 * following format: <br>
 * 
 * <pre>
{
    "name": "sample.name",
    "description": "sample description, can be null",
    "baseUnit": "sample's.baseUnit",
    "measurements": [
        {
            "statistic": "COUNT",
            "value": 123.123
        }
    ],
    "availableTags": []
}
 * </pre>
 * 
 * Special attention is required in the keys for the JsonValues as well as the
 * type of Statistic and number of measurements.<br>
 * The services offered by this class are:
 * <ul>
 * <li>Parse a JsonObject into a Counter</li>
 * <li>Increment a counter</li>
 * <li>Check the counter's value</li>
 * </ul>
 * 
 * @see Counter
 * 
 * @author Miguel Gomez
 */
public class CounterRepresentation extends MeterRepresentation implements Counter {

    private double count;
    private double update;
    private List<Measurement> measurements;

    /**
     * Initializes a new CounterRepresentation.
     * 
     * @param object JsonObject representing the Counter
     * @param tags   tags that the counter has
     * @throws IllegalArgumentException if the object is {@code null}, or if the
     *                                  JsonObject doesn't represent a valid
     *                                  Counter.
     */
    private CounterRepresentation(JsonObject object, String... tags) {
        super(object, Type.COUNTER, tags);

        JsonArray ja = object.getJsonArray("measurements");
        JsonObject jo = ja.getJsonObject(0);

        if (ja.size() > 1 || Statistic.valueOf(jo.getString("statistic")) != Statistic.COUNT) {
            throw new IllegalArgumentException("This object does not map a valid Counter!");
        }

        count = jo.getJsonNumber("value").doubleValue();
        update = 0.0;

        measurements = new ArrayList<Measurement>();
        measurements.add(new Measurement(() -> count(), Statistic.COUNT));
    }

    /**
     * Initializes a new CounterRepresentation.
     * 
     * @param name URN of the counter
     * @throws IllegalArgumentException if the name is null or empty
     */
    private CounterRepresentation(String name) {
        super(name, Type.COUNTER);

        count = 0;
        update = 0;

        measurements = new ArrayList<Measurement>();
        measurements.add(new Measurement(() -> count, Statistic.COUNT));
    }
    
    /**
     * Parses a new counter from a JsonObject.<br>
     * See the class documentation to see the format a JsonObject representing a
     * counter is expected to have.
     * 
     * @param object JsonObject representing the Counter we wish to parse
     * @param tags   tags that the counter has following the format
     *               {@code key:value}
     * @return a Counter representation of the JsonObject
     * @throws IllegalArgumentException if the object is {@code null}, or if the
     *                                  JsonObject doesn't represent a valid
     *                                  Counter.
     */
    public static Counter parseCounter(JsonObject object, String... tags) {
        return new CounterRepresentation(object, tags);
    }

    /**
     * Creates a new counter with no count.
     * 
     * @param name URN of the counter
     * @return a new Counter ready to be used
     * @throws IllegalArgumentException if the name is null or empty
     */
    public static Counter createNewCounter(String name) {
        return new CounterRepresentation(name);
    }

    @Override
    public void increment(double amount) {
        if (amount < 0.0) {
            throw new IllegalArgumentException("Counters may never be reset to a lesser value!");
        }
        count += amount;
        update += amount;

    }

    @Override
    public double count() {
        return count;
    }

    @Override
    public Iterable<Measurement> measure() {
        return measurements;
    }

    /**
     * Provides an updater for the Counter.<br>
     * The information includes the name and the amount that this counter was
     * incremented since its instantiation. This information is packed into a JSON
     * Object with the following format:
     * 
     * <pre>
     * {
     *     "name":"customcounter",
     *     "increment":2.5
     * }
     * </pre>
     * 
     * @see MeterRepresentation#getUpdater()
     */
    @Override
    public JsonObject getUpdater() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("name", getId().getName());
        job.add("increment", update);

        return job.build();
    }

}
