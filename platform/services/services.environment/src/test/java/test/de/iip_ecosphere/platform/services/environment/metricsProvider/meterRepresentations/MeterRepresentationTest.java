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
import de.iip_ecosphere.platform.support.metrics.Meter;
import de.iip_ecosphere.platform.support.metrics.Meter.Id;
import de.iip_ecosphere.platform.support.metrics.MetricsFactory;
import de.iip_ecosphere.platform.support.metrics.Tag;
import test.de.iip_ecosphere.platform.services.environment.metricsProvider.utils.TestUtils;
import static test.de.iip_ecosphere.platform.services.environment.metricsProvider.utils.TestUtils.assertThrows;

/**
 * Tests meter representation via {@link GaugeRepresentation}.
 * 
 * @author Miguel Gomez
 */
public class MeterRepresentationTest {

    // Constant values to load the JSON objects
    private static final String FOLDER = "meterrepresentation";

    private static final String JSON_VALID = "valid.json";
    private static final String JSON_VALID_NO_BASE_UNIT = "validNoBaseUnit.json";
    private static final String JSON_VALID_NO_DESCRIPTION = "validNoDescription.json";
    private static final String JSON_INVALID_NO_NAME = "invalidNoName.json";
    private static final String JSON_INVALID_NO_NAME_VALUE = "invalidNoNameValue.json";
    private static final String JSON_INVALID_NO_BASE_UNIT = "invalidNoBaseUnit.json";
    private static final String JSON_INVALID_NO_DESCRIPTION = "invalidNoDescription.json";

    /* Tests for parseMeter(JsonObject, String[]) */

    /**
     * Tests {@link GaugeRepresentation#parseGauge(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInitOkNoTags() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_VALID);
        List<Tag> tagList = new ArrayList<Tag>();
        Id id = MetricsFactory.buildId(obj.getString("name"), tagList, obj.getString("baseUnit"), 
            obj.getString("description"), Meter.Type.GAUGE);

        Meter meter = GaugeRepresentation.parseGauge(obj);
        assertNotNull(meter);
        assertEquals(id, meter.getId());
    }

    /**
     * Tests {@link GaugeRepresentation#parseGauge(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInitOkWithTags() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_VALID);
        List<Tag> tagList = new ArrayList<Tag>();
        tagList.add(MetricsFactory.buildImmutableTag("key1", "value1"));
        tagList.add(MetricsFactory.buildImmutableTag("key2", "value2"));
        Id id = MetricsFactory.buildId(obj.getString("name"), tagList, obj.getString("baseUnit"), 
            obj.getString("description"), Meter.Type.GAUGE);

        Meter meter = GaugeRepresentation.parseGauge(obj, "key1:value1", "key2:value2");
        assertNotNull(meter);
        assertEquals(id, meter.getId());
    }

    /**
     * Tests {@link GaugeRepresentation#parseGauge(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInitOkNoTagsNoBaseUnitValue() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_VALID_NO_BASE_UNIT);
        List<Tag> tagList = new ArrayList<Tag>();
        Id id = MetricsFactory.buildId(obj.getString("name"), tagList, null, obj.getString("description"), 
            Meter.Type.OTHER);

        Meter meter = GaugeRepresentation.parseGauge(obj);
        assertNotNull(meter);
        assertEquals(id, meter.getId());
    }

    /**
     * Tests {@link GaugeRepresentation#parseGauge(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInitOkNoTagsNoDescriptionValue() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_VALID_NO_DESCRIPTION);
        List<Tag> tagList = new ArrayList<Tag>();
        Id id = MetricsFactory.buildId(obj.getString("name"), tagList, obj.getString("baseUnit"), 
            null, Meter.Type.OTHER);

        Meter meter = GaugeRepresentation.parseGauge(obj);
        assertNotNull(meter);
        assertEquals(id, meter.getId());
    }

    /**
     * Tests {@link GaugeRepresentation#parseGauge(JsonObject, String...)}.
     */
    @Test
    public void testInitNullJson() {
        assertThrows(IllegalArgumentException.class, () -> GaugeRepresentation.parseGauge(null));
    }

    /**
     * Tests {@link GaugeRepresentation#parseGauge(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testTagNoKey() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_VALID_NO_DESCRIPTION);
        GaugeRepresentation.parseGauge(obj, ":value");
    }

    /**
     * Tests {@link GaugeRepresentation#parseGauge(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testTagNoValue() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_VALID_NO_DESCRIPTION);
        String[] tags = {"key:"};
        GaugeRepresentation.parseGauge(obj, tags);
    }

    /**
     * Tests {@link GaugeRepresentation#parseGauge(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testTagRandomString() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_VALID_NO_DESCRIPTION);
        String[] tags = {"potato"};

        GaugeRepresentation.parseGauge(obj, tags);
    }

    /**
     * Tests {@link GaugeRepresentation#parseGauge(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInvalidJsonNoName() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_INVALID_NO_NAME);

        assertThrows(IllegalArgumentException.class, () -> GaugeRepresentation.parseGauge(obj));
    }

    /**
     * Tests {@link GaugeRepresentation#parseGauge(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInvalidJsonNoNameValue() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_INVALID_NO_NAME_VALUE);

        assertThrows(IllegalArgumentException.class, () -> GaugeRepresentation.parseGauge(obj));
    }

    /**
     * Tests {@link GaugeRepresentation#parseGauge(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInvalidJsonNoBaseUnit() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_INVALID_NO_BASE_UNIT);

        assertThrows(IllegalArgumentException.class, () -> GaugeRepresentation.parseGauge(obj));
    }

    /**
     * Tests {@link GaugeRepresentation#parseGauge(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInvalidJsonNoDescription() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_INVALID_NO_DESCRIPTION);

        assertThrows(IllegalArgumentException.class, () -> GaugeRepresentation.parseGauge(obj));
    }

    /**
     * Tests {@link GaugeRepresentation#createNewGauge(String)}.
     */
    @Test
    public void testNameInitOk() {
        String name = "name";
        List<Tag> tagList = new ArrayList<Tag>();
        Id id = MetricsFactory.buildId(name, tagList, null, null, Meter.Type.GAUGE);

        Meter meter = GaugeRepresentation.createNewGauge(name);
        assertNotNull(meter);
        assertEquals(id, meter.getId());

    }

    /**
     * Tests {@link GaugeRepresentation#createNewGauge(String)}.
     */
    @Test
    public void testNameInitNullName() {
        assertThrows(IllegalArgumentException.class, () -> GaugeRepresentation.createNewGauge(null));
    }

    /**
     * Tests {@link GaugeRepresentation#createNewGauge(String)}.
     */
    @Test
    public void testNameInitEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> GaugeRepresentation.createNewGauge(""));
    }

}
