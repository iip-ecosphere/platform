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

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Statistic;

/**
 * This class aims to provide a prototypical implementation of the Gauge
 * Interface from Micrometer-API, allowing a client process to access the Gauge
 * values collected from a Service in a uniform way.<br>
 * Even though the methods are functional and will indeed offer an accurate
 * implementation of the metric we are simulating with this prototype, it is
 * highly recommended that an instance of this class is used solely to access
 * data and not to modify it, as the changes will not be registered in any sort
 * of registry under normal circumstances, and this will alter the read values
 * of the actual metrics. <br>
 * A gauge tracks a value that may go up or down. The value that is published
 * for gauges is an instantaneous sample of the gauge at publishing time.<br>
 * The JsonObject representing a Gauge will have the following format: <br>
 * 
 * <pre>
{
    "name": "sample.name",
    "description": "sample description, can be null",
    "baseUnit": "sample's.baseUnit",
    "measurements": [
        {
            "statistic": "VALUE",
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
 * <li>Parse a JsonObject into a Gauge</li>
 * <li>Check the gauge's value</li>
 * </ul>
 * 
 * @see Gauge
 * 
 * @author Miguel Gomez
 */
public class GaugeRepresentation extends MeterRepresentation implements Gauge {

    private double value;
    private List<Measurement> measurements;

    /**
     * Initializes a new GaugeRepresentation.
     * 
     * @param object JsonObject representing the Gauge
     * @param tags   tags that the counter has
     * @throws IllegalArgumentException if the object is {@code null}, or if the
     *                                  JsonObject doesn't represent a valid Gauge.
     */
    private GaugeRepresentation(JsonObject object, String... tags) {
        super(object, Type.GAUGE, tags);

        JsonArray ja = object.getJsonArray("measurements");
        JsonObject jo = ja.getJsonObject(0);

        if (ja.size() != 1 || Statistic.valueOf(jo.getString("statistic")) != Statistic.VALUE) {
            throw new IllegalArgumentException("This JsonObject does not represent a valid Gauge!");
        }

        value = jo.getJsonNumber("value").doubleValue();

        measurements = new ArrayList<Measurement>();
        measurements.add(new Measurement(() -> value(), Statistic.VALUE));
    }

    /**
     * Initializes a new GaugeRepresentation.
     * 
     * @param name URN of the gauge
     * @throws IllegalArgumentException if the name is null or empty
     */
    private GaugeRepresentation(String name) {
        super(name, Type.GAUGE);

        value = 0;

        measurements = new ArrayList<Measurement>();
        measurements.add(new Measurement(() -> value, Statistic.VALUE));
    }
    
    /**
     * Parses a new gauge from a JsonObject.<br>
     * See the class documentation to see the format of a JsonObject representing a
     * Gauge is expecting to have.
     * 
     * @param object JsonObject representing the Gauge we wish to parse
     * @param tags   tags that the gauge has following the format {@code key:value}
     * @return a Gauge representation of the JsonObject
     * @throws IllegalArgumentException if the object is {@code null}, or if the
     *                                  JsonObject doesn't represent a valid Gauge.
     */
    public static Gauge parseGauge(JsonObject object, String... tags) {
        return new GaugeRepresentation(object, tags);
    }

    /**
     * Creates a new gauge with no value.
     * 
     * @param name URN of the gauge
     * @return a new Gauge ready to be used
     * @throws IllegalArgumentException if the name is null or empty
     */
    public static Gauge createNewGauge(String name) {
        return new GaugeRepresentation(name);
    }

    @Override
    public double value() {
        return value;
    }

    @Override
    public Iterable<Measurement> measure() {
        return measurements;
    }

    /**
     * Provides an updater for the Gauge.<br>
     * The update information for a Gauge is simply the name an the value of the
     * Gauge.This information is packed into a JSON Object with the following
     * format:
     * 
     * <pre>
     * {
     *     "name":"customgauge",
     *     "value":123.4
     * }
     * </pre>
     * 
     * @see MeterRepresentation#getUpdater()
     */
    @Override
    public JsonObject getUpdater() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("name", getId().getName());
        job.add("value", value);

        return job.build();
    }

    /**
     * Changes the value of this gauge to the requested value.
     * 
     * @param value new value we want this Gauge to have
     */
    public void setValue(double value) {
        this.value = value;
    }

}
