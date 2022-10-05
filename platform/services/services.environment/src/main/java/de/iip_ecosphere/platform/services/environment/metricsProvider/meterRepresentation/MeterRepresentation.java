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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.stream.JsonParsingException;

import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

/**
 * This class aims to provide a prototypical implementation of the Meter
 * Interface from Micrometer-API, allowing a client process to access the Meter
 * values collected from a Service in a uniform way.<br>
 * Even though the methods are functional and will indeed offer an accurate
 * implementation of the metric we are simulating with this prototype, it is
 * highly recommended that an instance of this class is used solely to access
 * data and not to modify it, as the changes will not be registered in any sort
 * of registry under normal circumstances, and this will alter the read values
 * of the actual metrics. <br>
 * A meter is a named and dimensioned producer of one or more measurements. This
 * is a generic superclass that is extended by each individual meter. Even
 * though every meter can be represented as a Meter for simplification and
 * Abstraction, it is recommended that the prototype is instanced as a
 * particular type of Meter in stead of a generic type. Please see
 * {@link CounterRepresentation}, {@link GaugeRepresentation} and
 * {@link TimerRepresentation} to instantiate a more specific type of Meter.
 * This representation of a Meter should only be an important detail to
 * know.<br>
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
 * Please see {@link CounterRepresentation}, {@link GaugeRepresentation} and
 * {@link TimerRepresentation} for a more specific and accurate representation
 * of each specific type of meter. If a generic meter is instantiated, special
 * attention is required in the keys of the JsonValues as well as making sure
 * that there is at least one measurement that consists of JsonObject containing
 * a statistic and a value.<br>
 * The services offered by this class are:
 * <ul>
 * <li>Collect the Meter's ID</li>
 * <li>Collect the Meter's measurements</li>
 * <li>Create a JsonObject with the update information</li>
 * </ul>
 * 
 * @see Meter
 * @see CounterRepresentation
 * @see GaugeRepresentation
 * @see TimerRepresentation
 * 
 * @author Miguel Gomez
 */
public abstract class MeterRepresentation implements Meter {

    private static final String NON_VALID_JSON = "JsonObject does not map a valid Meter!";
    private Id id;

    /**
     * Instantiates a new Meter representation.<br>
     * This method should be called by all extending subclasses to provide the
     * attributes required by the Meter superclass that is extended by the interface
     * they represent. The type of Meter to be indicated is {@code COUNTER},
     * {@code GAUGE} or {@code TIMER} depending on the subclass calling. If we don't
     * want/need to specify the type, we can use {@code OTHER}.
     * 
     * @param object JsonObject representing the Meter we wish to parse
     * @param type   type of meter that is being created
     * @param tags   tags that the meter has following the format {@code key:value}
     * @throws IllegalArgumentException if the object is {@code null}, or if the
     *                                  JsonObject doesn't represent a valid Meter.
     */
    protected MeterRepresentation(JsonObject object, Type type, String... tags) {
        if (object == null) {
            throw new IllegalArgumentException("The Object is null!");
        }

        try {
            String baseUnit = object.isNull("baseUnit") ? null : object.getString("baseUnit");
            String description = object.isNull("description") ? null : object.getString("description");

            id = new Id(object.getString("name"), extractTagsMap(tags), baseUnit, description, type);
        } catch (NullPointerException | ClassCastException ex) {
            throw new IllegalArgumentException(NON_VALID_JSON, ex);
        }
    }

    /**
     * Instantiates a brand new Meter representation with just the name.<br>
     * This method should be called by all extending subclasses to provide the
     * attributes required by the Meter superclass that is extended by the interface
     * they represent. The type of Meter to be indicated is {@code COUNTER},
     * {@code GAUGE} or {@code TIMER} depending on the subclass calling. If we don't
     * want/need to specify the type, we can use {@code OTHER}.
     * 
     * @param name URN of the meter
     * @param type type of meter that is being created
     * @throws IllegalArgumentException if the name is null or empty
     */
    protected MeterRepresentation(String name, Type type) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or null");
        }
        id = new Id(name, Tags.of(new ArrayList<Tag>()), null, null, type);
    }

    /**
     * Extracts the tags from a string array and turns them into a Tags object.<br>
     * The tags should be the same ones used for the HTTP request that provided the
     * JsonObject representation of the Meter.<br>
     * Following the same format used for the tags from said request, these tags
     * follow the notation {@code key:value}.
     * 
     * @param tags tags that the meter has
     * @return Tags representation of the set of tags
     */
    private static Tags extractTagsMap(String... tags) {
        List<Tag> tagList = new ArrayList<Tag>();
        String[] aux;
        try {
            for (String tag : tags) {
                aux = tag.split(":");
                if (aux[0].equals("") || aux[1].equals("")) {
                    throw new IllegalArgumentException(NON_VALID_JSON);
                }
                tagList.add(new ImmutableTag(aux[0], aux[1]));

            }
        } catch (ArrayIndexOutOfBoundsException aiobe) {
            throw new IllegalArgumentException(NON_VALID_JSON, aiobe);
        }

        return Tags.of(tagList);

    }

    @Override
    public Id getId() {
        return id;
    }

    @Override
    public abstract Iterable<Measurement> measure();

    /**
     * Creates a JsonObject with the information required to update the meter in the
     * Meter registry in the server-side.
     * 
     * @return a JsonObject representing the changes this meter has experienced
     */
    public abstract JsonObject getUpdater();

    /**
     * Generically parses a meter. Emits a log message if the {@code json} cannot be parsed.
     * 
     * @param json   JSON string representation of the meter
     * @param tags   tags that the counter has following the format
     *               {@code key:value}
     * @return a Meter representation of the JsonObject (<b>null</b> if invalid, unknown)
     */
    public static Meter parseMeter(String json, String... tags) {
        try {
            return parseMeter(Json.createReader(new StringReader(json)).readObject(), tags);
        } catch (JsonParsingException e) {
            LoggerFactory.getLogger(MeterRepresentation.class).info("Cannot parse meter JSON '{}' : {}", 
                json, e.getMessage());
            return null;
        }
    }

    /**
     * Generically parses a meter. Does not emit anything if the {@code json} cannot be parsed.
     * 
     * @param json   JSON string representation of the meter
     * @param tags   tags that the counter has following the format
     *               {@code key:value}
     * @return a Meter representation of the JsonObject (<b>null</b> if invalid, unknown)
     */
    public static Meter parseMeterQuiet(String json, String... tags) {
        try {
            return parseMeter(Json.createReader(new StringReader(json)).readObject(), tags);
        } catch (JsonParsingException e) {
            return null;
        }
    }

    /**
     * Generically parses a meter.
     * 
     * @param object JsonObject representing the Timer we wish to parse
     * @param tags   tags that the counter has following the format
     *               {@code key:value}
     * @return a Meter representation of the JsonObject (<b>null</b> if invalid, unknown)
     */
    public static Meter parseMeter(JsonObject object, String... tags) {
        Meter result = null;
        boolean valueFound = false;
        boolean countFound = false;
        boolean totalTimeFound = false;
        JsonArray measurements = object.getJsonArray("measurements");
        if (null != measurements) {
            for (int i = 0; i < measurements.size(); i++) {
                JsonObject measurement = measurements.getJsonObject(i);
                if (null != measurement) {
                    String statistics = measurement.getString("statistic");
                    valueFound |= Statistic.VALUE.name().equals(statistics);
                    countFound |= Statistic.COUNT.name().equals(statistics);
                    totalTimeFound |= Statistic.TOTAL_TIME.name().equals(statistics);
                }
            }
        }
        JsonArray tgs = object.getJsonArray("availableTags");
        if (null != tgs) {
            String[] tmp = new String[tags.length + tgs.size()];
            int pos = 0;
            for (int i = 0; i < tags.length; i++) {
                tmp[pos++] = tags[i];
            }
            // just add them, may override values from call parameter but given tags shall take precedence
            for (int i = 0; i < tgs.size(); i++) {
                tmp[pos++] = tgs.getString(i);
            }
            tags = tmp;
        }
        if (totalTimeFound && countFound) {
            result = TimerRepresentation.parseTimer(object, tags);
        } else if (countFound) {
            result = CounterRepresentation.parseCounter(object, tags);
        } else if (valueFound) {
            result = GaugeRepresentation.parseGauge(object, tags);
        }
        return result;
    }

}
