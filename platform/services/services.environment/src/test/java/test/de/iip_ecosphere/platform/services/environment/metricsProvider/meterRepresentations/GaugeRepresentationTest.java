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

import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.metricsProvider.meterRepresentation.GaugeRepresentation;
import de.iip_ecosphere.platform.support.json.JsonObject;
import de.iip_ecosphere.platform.support.metrics.Gauge;
import de.iip_ecosphere.platform.support.metrics.Measurement;
import de.iip_ecosphere.platform.support.metrics.Meter;
import de.iip_ecosphere.platform.support.metrics.Meter.Id;
import de.iip_ecosphere.platform.support.metrics.MetricsFactory;
import de.iip_ecosphere.platform.support.metrics.Statistic;
import de.iip_ecosphere.platform.support.metrics.Tag;
import test.de.iip_ecosphere.platform.services.environment.metricsProvider.utils.TestUtils;
import static test.de.iip_ecosphere.platform.services.environment.metricsProvider.utils.TestUtils.assertThrows;

/**
 * Tests {@link GaugeRepresentation}.
 * 
 * @author Miguel Gomez
 */
public class GaugeRepresentationTest {

    // Constant values to load the JSON objects
    private static final String FOLDER = "gaugerepresentation";

    private static final String JSON_VALID = "validGauge.json";
    private static final String JSON_UPDATER = "jsonUpdater.json";
    private static final String JSON_INVALID_TWO_MEASUREMENTS = "invalidGaugeTwoMeasurements.json";
    private static final String JSON_INVALID_WRONG_STATISTIC = "invalidGaugeWrongStatistic.json";

    /**
     * Tests {@link GaugeRepresentation#parseGauge(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInitOkNoTags() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_VALID);
        double original = obj.getJsonArray("measurements").getJsonObject(0).getJsonNumber("value").doubleValue();
        List<Tag> tagList = new ArrayList<Tag>();
        Id id = MetricsFactory.buildId(obj.getString("name"), tagList, obj.getString("baseUnit"), 
            obj.getString("description"), Meter.Type.GAUGE);

        Gauge gauge = GaugeRepresentation.parseGauge(obj);
        assertNotNull(gauge);
        assertEquals(id, gauge.getId());
        assertEquals(original, gauge.value(), 0.0);
    }

    /**
     * Tests {@link GaugeRepresentation#parseGauge(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInitOkWithTags() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_VALID);
        double original = obj.getJsonArray("measurements").getJsonObject(0).getJsonNumber("value").doubleValue();
        List<Tag> tagList = new ArrayList<Tag>();
        tagList.add(MetricsFactory.buildImmutableTag("key1", "value1"));
        tagList.add(MetricsFactory.buildImmutableTag("key2", "value2"));
        Id id = MetricsFactory.buildId(obj.getString("name"), tagList, obj.getString("baseUnit"), 
            obj.getString("description"), Meter.Type.GAUGE);

        Gauge gauge = GaugeRepresentation.parseGauge(obj, "key1:value1", "key2:value2");
        assertNotNull(gauge);
        assertEquals(id, gauge.getId());
        assertEquals(original, gauge.value(), 0.0);
    }

    /**
     * Tests {@link GaugeRepresentation#parseGauge(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInitBadTwoMeasurements() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_INVALID_TWO_MEASUREMENTS);

        assertThrows(IllegalArgumentException.class, () -> GaugeRepresentation.parseGauge(obj));
    }

    /**
     * Tests {@link GaugeRepresentation#parseGauge(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInitBadWrongStatistic() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_INVALID_WRONG_STATISTIC);

        assertThrows(IllegalArgumentException.class, () -> GaugeRepresentation.parseGauge(obj));
    }

    /**
     * Tests {@link GaugeRepresentation#parseGauge(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testGetUpdaterGauge() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_VALID);
        JsonObject expected = TestUtils.readJsonFromResources(FOLDER, JSON_UPDATER);

        Gauge gauge = GaugeRepresentation.parseGauge(obj);
        ((GaugeRepresentation) gauge).setValue(3.2);
        JsonObject updater = ((GaugeRepresentation) gauge).getUpdater();

        assertEquals(expected, updater);
    }

    /**
     * Tests {@link GaugeRepresentation#measure()}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testMeasure() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_VALID);
        Gauge gauge = GaugeRepresentation.parseGauge(obj);

        List<Measurement> measurements = (List<Measurement>) gauge.measure();

        assertEquals(1, measurements.size());
        assertEquals(Statistic.VALUE, measurements.get(0).getStatistic());
        assertEquals(gauge.value(), measurements.get(0).getValue(), 0.0);
    }

    /**
     * Tests {@link GaugeRepresentation#createNewGauge(String)}.
     */
    @Test
    public void testNameInitOk() {
        String name = "name";
        List<Tag> tagList = new ArrayList<Tag>();
        Id id = MetricsFactory.buildId(name, tagList, null, null, Meter.Type.GAUGE);

        Gauge gauge = GaugeRepresentation.createNewGauge(name);
        assertNotNull(gauge);
        assertEquals(id, gauge.getId());
        assertEquals(0.0, gauge.value(), 0.0);

        List<Measurement> measurements = (List<Measurement>) gauge.measure();
        assertEquals(1, measurements.size());
        assertEquals(Statistic.VALUE, measurements.get(0).getStatistic());
        assertEquals(gauge.value(), measurements.get(0).getValue(), 0.0);
    }

}
