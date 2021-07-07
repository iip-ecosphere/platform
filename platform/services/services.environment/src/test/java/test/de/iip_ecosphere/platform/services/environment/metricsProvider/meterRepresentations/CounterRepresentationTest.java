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

package test.de.iip_ecosphere.platform.services.environment.metricsProvider.meterRepresentations;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.json.JsonObject;

import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.metricsProvider.meterRepresentation.CounterRepresentation;
import test.de.iip_ecosphere.platform.services.environment.metricsProvider.utils.TestUtils;
import static test.de.iip_ecosphere.platform.services.environment.metricsProvider.utils.TestUtils.assertThrows;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Meter.Type;
import io.micrometer.core.instrument.Statistic;

/**
 * Tests {@link CounterRepresentation}.
 * 
 * @author Miguel Gomez
 */
public class CounterRepresentationTest {

    // Constant values to load the JSON objects
    private static final String FOLDER = "counterrepresentation";

    private static final String JSON_VALID = "validCounter.json";
    private static final String JSON_UPDATER = "jsonUpdater.json";
    private static final String JSON_INVALID_TWO_MEASUREMENTS = "invalidCounterTwoMeasurements.json";
    private static final String JSON_INVALID_WRONG_STATISTIC = "invalidCounterWrongStatistic.json";

    /**
     * Tests {@link CounterRepresentation#parseCounter(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInitOkNoTags() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_VALID);
        List<Tag> tagList = new ArrayList<Tag>();
        Id id = new Id(obj.getString("name"), Tags.of(tagList), obj.getString("baseUnit"), obj.getString("description"),
                Type.COUNTER);

        Counter counter = CounterRepresentation.parseCounter(obj);
        assertNotNull(counter);
        assertEquals(id, counter.getId());
        assertEquals(obj.getJsonArray("measurements").get(0).asJsonObject().getJsonNumber("value").doubleValue(),
                counter.count(), 0.0);
    }

    /**
     * Tests {@link CounterRepresentation#parseCounter(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInitOkWithTags() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_VALID);
        List<Tag> tagList = new ArrayList<Tag>();
        tagList.add(new ImmutableTag("key1", "value1"));
        tagList.add(new ImmutableTag("key2", "value2"));
        Id id = new Id(obj.getString("name"), Tags.of(tagList), obj.getString("baseUnit"), obj.getString("description"),
                Type.COUNTER);

        Counter counter = CounterRepresentation.parseCounter(obj, "key1:value1", "key2:value2");
        assertNotNull(counter);
        assertEquals(id, counter.getId());
        assertEquals(obj.getJsonArray("measurements").get(0).asJsonObject().getJsonNumber("value").doubleValue(),
                counter.count(), 0.0);
    }

    /**
     * Tests {@link CounterRepresentation#parseCounter(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInitBadTwoMeasurements() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_INVALID_TWO_MEASUREMENTS);

        assertThrows(IllegalArgumentException.class, () -> CounterRepresentation.parseCounter(obj));
    }

    /**
     * Tests {@link CounterRepresentation#parseCounter(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInitBadWrongStatistic() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_INVALID_WRONG_STATISTIC);

        assertThrows(IllegalArgumentException.class, () -> CounterRepresentation.parseCounter(obj));
    }

    /**
     * Tests {@link CounterRepresentation#parseCounter(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testIncrementByOne() throws IOException {
        double increment = 1.0;
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_VALID);
        double original = obj.getJsonArray("measurements").get(0).asJsonObject().getJsonNumber("value").doubleValue();

        Counter counter = CounterRepresentation.parseCounter(obj);
        assertEquals(original, counter.count(), 0.0);
        counter.increment(increment);
        assertEquals(original + increment, counter.count(), 0.0);
    }

    /**
     * Tests {@link CounterRepresentation#parseCounter(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testIncrementByZero() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_VALID);
        double original = obj.getJsonArray("measurements").get(0).asJsonObject().getJsonNumber("value").doubleValue();

        Counter counter = CounterRepresentation.parseCounter(obj);
        assertEquals(original, counter.count(), 0.0);
        counter.increment(0.0);
        assertEquals(original, counter.count(), 0.0);
    }

    /**
     * Tests {@link CounterRepresentation#parseCounter(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testIncrementByNegativeOne() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_VALID);

        Counter counter = CounterRepresentation.parseCounter(obj);
        assertThrows(IllegalArgumentException.class, () -> counter.increment(-1.0));
    }

    /**
     * Tests {@link CounterRepresentation#getUpdater()}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testGetUpdater() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_VALID);
        JsonObject expected = TestUtils.readJsonFromResources(FOLDER, JSON_UPDATER);

        Counter counter = CounterRepresentation.parseCounter(obj);
        counter.increment(5.1);

        JsonObject updater = ((CounterRepresentation) counter).getUpdater();

        assertEquals(expected, updater);
    }

    /**
     * Tests {@link CounterRepresentation#measure()}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testMeasure() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_VALID);
        Counter counter = CounterRepresentation.parseCounter(obj);

        List<Measurement> measurements = (List<Measurement>) counter.measure();

        assertEquals(1, measurements.size());
        assertEquals(Statistic.COUNT, measurements.get(0).getStatistic());
        assertEquals(counter.count(), measurements.get(0).getValue(), 0.0);
    }

    /**
     * Tests {@link CounterRepresentation#createNewCounter(String)}.
     */
    @Test
    public void testNameInitOk() {
        String name = "name";
        List<Tag> tagList = new ArrayList<Tag>();
        Id id = new Id(name, Tags.of(tagList), null, null, Type.COUNTER);

        Counter counter = CounterRepresentation.createNewCounter(name);
        assertNotNull(counter);
        assertEquals(id, counter.getId());
        assertEquals(0.0, counter.count(), 0.0);

        List<Measurement> measurements = (List<Measurement>) counter.measure();

        assertEquals(1, measurements.size());
        assertEquals(Statistic.COUNT, measurements.get(0).getStatistic());
        assertEquals(counter.count(), measurements.get(0).getValue(), 0.0);
    }

}
