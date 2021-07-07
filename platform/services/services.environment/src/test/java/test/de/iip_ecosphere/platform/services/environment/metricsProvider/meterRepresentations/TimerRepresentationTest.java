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
import java.util.concurrent.TimeUnit;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.metricsProvider.meterRepresentation.TimerRepresentation;
import test.de.iip_ecosphere.platform.services.environment.metricsProvider.utils.TestUtils;
import static test.de.iip_ecosphere.platform.services.environment.metricsProvider.utils.TestUtils.assertThrows;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Meter.Type;

/**
 * Tests {@link TimerRepresentation}.
 * 
 * @author Miguel Gomez
 */
public class TimerRepresentationTest {

    private static final String FOLDER = "timerrepresentation";

    private static final String JSON_VALID = "validTimer.json";
    private static final String JSON_CLEAN = "cleanTimer.json";
    private static final String JSON_SINGLE = "singleTimer.json";
    private static final String JSON_INVALID_BASE_UNIT = "invalidTimerBadBaseUnit.json";
    private static final String JSON_INVALID_TWO_MEASUREMENTS = "invalidTimerTwoMeasurements.json";
    private static final String JSON_INVALID_FOUR_MEASUREMENTS = "invalidTimerFourMeasurements.json";
    private static final String JSON_INVALID_WRONG_STATISTIC = "invalidTimerWrongStatistic.json";
    private static final String JSON_INVALID_TWO_COUNT = "invalidTimerTwoCount.json";
    private static final String JSON_INVALID_TWO_TOTAL = "invalidTimerTwoTotal.json";
    private static final String JSON_INVALID_TWO_MAX = "invalidTimerTwoMax.json";

    // For the conversions test
    private static final double NANOS = 1000000000.0;
    private static final double MICROS = 1000000.0;
    private static final double MILLIS = 1000.0;
    private static final double SECONDS = 1.0;
    private static final double MINUTES = 0.016667;
    private static final double HOURS = 0.00027778;
    private static final double DAYS = 0.0000115741;

    /**
     * Tests {@link TimerRepresentation#parseTimer(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInitOkNoTags() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_VALID);
        List<Tag> tagList = new ArrayList<Tag>();
        Id id = new Id(obj.getString("name"), Tags.of(tagList), obj.getString("baseUnit"), obj.getString("description"),
                Type.TIMER);

        Timer timer = TimerRepresentation.parseTimer(obj);
        assertNotNull(timer);
        assertEquals(id, timer.getId());
        assertEquals(3, timer.count());
        assertEquals(6.0, timer.totalTime(TimeUnit.SECONDS), 0.0);
        assertEquals(3.0, timer.max(TimeUnit.SECONDS), 0.0);
        assertEquals(TimeUnit.SECONDS, timer.baseTimeUnit());
    }

    /**
     * Tests {@link TimerRepresentation#parseTimer(JsonObject, String...)}.
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
                Type.TIMER);

        Timer timer = TimerRepresentation.parseTimer(obj, "key1:value1", "key2:value2");
        assertNotNull(timer);
        assertEquals(id, timer.getId());
        assertEquals(3, timer.count());
        assertEquals(6.0, timer.totalTime(TimeUnit.SECONDS), 0.0);
        assertEquals(3.0, timer.max(TimeUnit.SECONDS), 0.0);
        assertEquals(TimeUnit.SECONDS, timer.baseTimeUnit());
    }

    /**
     * Tests {@link TimerRepresentation#parseTimer(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInitBadInvalidBaseUnit() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_INVALID_BASE_UNIT);

        assertThrows(IllegalArgumentException.class, () -> TimerRepresentation.parseTimer(obj));
    }

    /**
     * Tests {@link TimerRepresentation#parseTimer(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInitBadTwoMeasurements() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_INVALID_TWO_MEASUREMENTS);

        assertThrows(IllegalArgumentException.class, () -> TimerRepresentation.parseTimer(obj));
    }

    /**
     * Tests {@link TimerRepresentation#parseTimer(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInitBadFourMeasurements() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_INVALID_FOUR_MEASUREMENTS);

        assertThrows(IllegalArgumentException.class, () -> TimerRepresentation.parseTimer(obj));
    }

    /**
     * Tests {@link TimerRepresentation#parseTimer(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInitBadWrongStatistic() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_INVALID_WRONG_STATISTIC);

        assertThrows(IllegalArgumentException.class, () -> TimerRepresentation.parseTimer(obj));
    }

    /**
     * Tests {@link TimerRepresentation#parseTimer(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInitBadTwoCount() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_INVALID_TWO_COUNT);

        assertThrows(IllegalArgumentException.class, () -> TimerRepresentation.parseTimer(obj));
    }

    /**
     * Tests {@link TimerRepresentation#parseTimer(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInitBadTwoTotal() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_INVALID_TWO_TOTAL);

        assertThrows(IllegalArgumentException.class, () -> TimerRepresentation.parseTimer(obj));
    }

    /**
     * Tests {@link TimerRepresentation#parseTimer(JsonObject, String...)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testInitBadTwoMax() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_INVALID_TWO_MAX);

        assertThrows(IllegalArgumentException.class, () -> TimerRepresentation.parseTimer(obj));
    }

    /**
     * Tests {@link TimerRepresentation#takeSnapshot()}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testTakeSnapshot() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_VALID);
        Timer timer = TimerRepresentation.parseTimer(obj);

        assertNotNull(timer.takeSnapshot());
    }

    /**
     * Tests {@link TimerRepresentation#count()}, {@link TimerRepresentation#totalTime(TimeUnit)} and 
     * {@link TimerRepresentation#max(TimeUnit)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testRecordLong() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_CLEAN);
        Timer timer = TimerRepresentation.parseTimer(obj);

        assertEquals(0, timer.count());
        assertEquals(0.0, timer.totalTime(TimeUnit.SECONDS), 0.0);
        assertEquals(0.0, timer.max(TimeUnit.SECONDS), 0.0);

        timer.record(0, TimeUnit.SECONDS);
        assertEquals(1, timer.count());
        assertEquals(0.0, timer.totalTime(TimeUnit.SECONDS), 0.0);
        assertEquals(0.0, timer.max(TimeUnit.SECONDS), 0.0);

        timer.record(1, TimeUnit.SECONDS);
        assertEquals(2, timer.count());
        assertEquals(1.0, timer.totalTime(TimeUnit.SECONDS), 0.0);
        assertEquals(1.0, timer.max(TimeUnit.SECONDS), 0.0);

        timer.record(-1, TimeUnit.SECONDS);
        assertEquals(3, timer.count());
        assertEquals(0.0, timer.totalTime(TimeUnit.SECONDS), 0.0);
        assertEquals(1.0, timer.max(TimeUnit.SECONDS), 0.0);
    }

    /**
     * Tests {@link TimerRepresentation#record(java.util.function.Supplier)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testRecordSupplier() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_CLEAN);
        Timer timer = TimerRepresentation.parseTimer(obj);

        assertEquals(0, timer.count());
        assertEquals(0.0, timer.totalTime(TimeUnit.SECONDS), 0.0);
        assertEquals(0.0, timer.max(TimeUnit.SECONDS), 0.0);

        assertEquals(TestUtils.DATA, timer.record(() -> TestUtils.oneSecondSupplier()));
        assertEquals(1, timer.count());
        assertEquals(1.0, timer.totalTime(TimeUnit.SECONDS), 0.5);
        assertEquals(1.0, timer.max(TimeUnit.SECONDS), 0.5);

        assertEquals(TestUtils.DATA, timer.record(() -> TestUtils.threeSecondSupplier()));
        assertEquals(2, timer.count());
        assertEquals(4.0, timer.totalTime(TimeUnit.SECONDS), 0.5);
        assertEquals(3.0, timer.max(TimeUnit.SECONDS), 0.5);

        assertEquals(TestUtils.DATA, timer.record(() -> TestUtils.twoSecondSupplier()));
        assertEquals(3, timer.count());
        assertEquals(6.0, timer.totalTime(TimeUnit.SECONDS), 0.5);
        assertEquals(3.0, timer.max(TimeUnit.SECONDS), 0.5);
    }

    // checkstyle: stop exception type check

    /**
     * Tests {@link TimerRepresentation#recordCallable(java.util.concurrent.Callable)}.
     * 
     * @throws Exception shall not occur when test is passed
     */
    @Test
    public void testRecordCallable() throws Exception {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_CLEAN);
        Timer timer = TimerRepresentation.parseTimer(obj);

        assertEquals(0, timer.count());
        assertEquals(0.0, timer.totalTime(TimeUnit.SECONDS), 0.0);
        assertEquals(0.0, timer.max(TimeUnit.SECONDS), 0.0);

        assertEquals(TestUtils.DATA, timer.recordCallable(() -> TestUtils.oneSecondSupplier()));
        assertEquals(1, timer.count());
        assertEquals(1.0, timer.totalTime(TimeUnit.SECONDS), 0.5);
        assertEquals(1.0, timer.max(TimeUnit.SECONDS), 0.5);

        assertEquals(TestUtils.DATA, timer.recordCallable(() -> TestUtils.threeSecondSupplier()));
        assertEquals(2, timer.count());
        assertEquals(4.0, timer.totalTime(TimeUnit.SECONDS), 0.5);
        assertEquals(3.0, timer.max(TimeUnit.SECONDS), 0.5);

        assertEquals(TestUtils.DATA, timer.recordCallable(() -> TestUtils.twoSecondSupplier()));
        assertEquals(3, timer.count());
        assertEquals(6.0, timer.totalTime(TimeUnit.SECONDS), 0.5);
        assertEquals(3.0, timer.max(TimeUnit.SECONDS), 0.5);
    }

    // checkstyle: resume exception type check

    /**
     * Tests {@link TimerRepresentation#record(Runnable)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testRecordRunnable() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_CLEAN);
        Timer timer = TimerRepresentation.parseTimer(obj);

        assertEquals(0, timer.count());
        assertEquals(0.0, timer.totalTime(TimeUnit.SECONDS), 0.0);
        assertEquals(0.0, timer.max(TimeUnit.SECONDS), 0.0);

        timer.record(() -> TestUtils.oneSecondRunnable());
        assertEquals(1, timer.count());
        assertEquals(1.0, timer.totalTime(TimeUnit.SECONDS), 0.5);
        assertEquals(1.0, timer.max(TimeUnit.SECONDS), 0.5);

        timer.record(() -> TestUtils.threeSecondRunnable());
        assertEquals(2, timer.count());
        assertEquals(4.0, timer.totalTime(TimeUnit.SECONDS), 0.5);
        assertEquals(3.0, timer.max(TimeUnit.SECONDS), 0.5);

        timer.record(() -> TestUtils.twoSecondRunnable());
        assertEquals(3, timer.count());
        assertEquals(6.0, timer.totalTime(TimeUnit.SECONDS), 0.5);
        assertEquals(3.0, timer.max(TimeUnit.SECONDS), 0.5);
    }

    /**
     * Tests {@link TimerRepresentation#totalTime(TimeUnit)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testTotalTimeConversion() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_SINGLE);
        Timer timer = TimerRepresentation.parseTimer(obj);

        assertEquals(NANOS, timer.totalTime(TimeUnit.NANOSECONDS), 0.0);
        assertEquals(MICROS, timer.totalTime(TimeUnit.MICROSECONDS), 0.0);
        assertEquals(MILLIS, timer.totalTime(TimeUnit.MILLISECONDS), 0.0);
        assertEquals(SECONDS, timer.totalTime(TimeUnit.SECONDS), 0.0);
        assertEquals(SECONDS, timer.totalTime(TimeUnit.SECONDS), 0.0);
        assertEquals(MINUTES, timer.totalTime(TimeUnit.MINUTES), 0.0000005);
        assertEquals(HOURS, timer.totalTime(TimeUnit.HOURS), 0.000000005);
        assertEquals(DAYS, timer.totalTime(TimeUnit.DAYS), 0.00000000005);
    }

    /**
     * Tests {@link TimerRepresentation#max(TimeUnit)}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testMaxTimeConversion() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_SINGLE);
        Timer timer = TimerRepresentation.parseTimer(obj);

        assertEquals(NANOS, timer.max(TimeUnit.NANOSECONDS), 0.0);
        assertEquals(MICROS, timer.max(TimeUnit.MICROSECONDS), 0.0);
        assertEquals(MILLIS, timer.max(TimeUnit.MILLISECONDS), 0.0);
        assertEquals(SECONDS, timer.max(TimeUnit.SECONDS), 0.0);
        assertEquals(SECONDS, timer.max(TimeUnit.SECONDS), 0.0);
        assertEquals(MINUTES, timer.max(TimeUnit.MINUTES), 0.0000005);
        assertEquals(HOURS, timer.max(TimeUnit.HOURS), 0.000000005);
        assertEquals(DAYS, timer.max(TimeUnit.DAYS), 0.00000000005);
    }
    
    // checkstyle: stop exception type check

    /**
     * Tests {@link TimerRepresentation#getUpdater()}.
     * 
     * @throws Exception shall not occur when test is passed
     */
    @Test
    public void testGetUpdater() throws Exception {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_SINGLE);
        Timer timer = TimerRepresentation.parseTimer(obj);
        double second = TimeUnit.SECONDS.toNanos(1);

        timer.record(1, TimeUnit.SECONDS);
        timer.record(() -> TestUtils.oneSecondSupplier());
        timer.recordCallable(() -> TestUtils.oneSecondSupplier());
        timer.record(() -> TestUtils.oneSecondRunnable());

        JsonObject updater = ((TimerRepresentation) timer).getUpdater();
        JsonArray recordings = updater.getJsonArray("recordings");

        assertEquals(obj.getString("name"), updater.getString("name"));
        assertEquals(4, recordings.size());

        for (int i = 0; i < recordings.size(); i++) {
            assertEquals(second, Double.valueOf(recordings.getJsonNumber(i).doubleValue()), second / 5);
        }
    }

    // checkstyle: resume exception type check

    /**
     * Tests {@link TimerRepresentation#measure()}.
     * 
     * @throws IOException shall not occur when test is passed
     */
    @Test
    public void testMeasure() throws IOException {
        JsonObject obj = TestUtils.readJsonFromResources(FOLDER, JSON_VALID);
        Timer timer = TimerRepresentation.parseTimer(obj);

        List<Measurement> measurements = (List<Measurement>) timer.measure();
        assertEquals(3, measurements.size());
        double expected;
        for (Measurement m : measurements) {
            switch (m.getStatistic()) {
            case COUNT:
                expected = timer.count();
                break;
            case MAX:
                expected = timer.max(TimeUnit.SECONDS);
                break;
            case TOTAL_TIME:
                expected = timer.totalTime(TimeUnit.SECONDS);
                break;
            default:
                expected = 0.0;
                fail("Not a valid statistic!");
                break;
            }
            assertEquals(expected, m.getValue(), 0.0);
        }
    }

    /**
     * Tests {@link TimerRepresentation#createNewTimer(String)}.
     */
    @Test
    public void testNameInitOk() {
        String name = "name";
        List<Tag> tagList = new ArrayList<Tag>();
        Id id = new Id(name, Tags.of(tagList), null, null, Type.TIMER);

        Timer timer = TimerRepresentation.createNewTimer(name);
        assertNotNull(timer);
        assertEquals(id, timer.getId());
        assertEquals(0, timer.count());
        assertEquals(0.0, timer.totalTime(TimeUnit.SECONDS), 0.0);
        assertEquals(0.0, timer.max(TimeUnit.SECONDS), 0.0);
        assertEquals(TimeUnit.SECONDS, timer.baseTimeUnit());

        List<Measurement> measurements = (List<Measurement>) timer.measure();
        assertEquals(3, measurements.size());
        double expected;
        for (Measurement m : measurements) {
            switch (m.getStatistic()) {
            case COUNT:
                expected = timer.count();
                break;
            case MAX:
                expected = timer.max(TimeUnit.SECONDS);
                break;
            case TOTAL_TIME:
                expected = timer.totalTime(TimeUnit.SECONDS);
                break;
            default:
                expected = 0.0;
                fail("Not a valid statistic!");
                break;
            }
            assertEquals(expected, m.getValue(), 0.0);
        }
    }

}
