package test.de.iip_ecosphere.platform.services.environment.metricsProvider;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.metricsProvider.CapacityBaseUnit;
import de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProvider;
import de.iip_ecosphere.platform.services.environment.metricsProvider.meterRepresentation.MeterRepresentation;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.TimeUtils;
import test.de.iip_ecosphere.platform.services.environment.metricsProvider.utils.TestUtils;
import static test.de.iip_ecosphere.platform.services.environment.metricsProvider.utils.TestUtils.assertThrows;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.search.RequiredSearch;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Tests {@link MetricsProvider}.
 * 
 * @author Miguel Gomez
 */
public class MetricsProviderTest {

    // Constant value for tests
    private static final String ID_OK = "id.ok";
    private static final String ID_GOOD = "id.good";
    private static final String ID_PASSABLE = "id.passable";
    private static final String ID_BAD = "id.bad";

    // Global Metrics provider
    private static MetricsProvider provider;

    /**
     * Creates the metrics provider to test.
     * 
     * @param registry the meter registry
     * @return the provider
     */
    protected MetricsProvider createProvider(MeterRegistry registry) {
        return new MetricsProvider(registry);
    }
    
    /**
     * Sets up the metrics provider via a simple meter registry.
     */
    @Before
    public void setUpMetricsProvider() {
        provider = createProvider(new SimpleMeterRegistry());
    }

    /**
     * Tests {@link MetricsProvider#MetricsProvider(io.micrometer.core.instrument.MeterRegistry)}.
     */
    @Test
    public void testInitOk() {
        MetricsProvider mProvider = createProvider(new SimpleMeterRegistry());
        assertNotNull(mProvider);
    }

    /**
     * Tests {@link MetricsProvider#MetricsProvider(io.micrometer.core.instrument.MeterRegistry)}.
     */
    @Test
    public void testInitNull() {
        assertThrows(IllegalArgumentException.class, () -> createProvider(null));
    }

    /**
     * Tests {@link MetricsProvider} Gauge CRUD operations.
     */
    @Test
    public void testGaugeCrudOperations() {
        double value = 1.0;
        double value2 = 1.2;
        double negVal = -1.3;

        assertEquals(0, provider.getNumberOfCustomGauges());
        assertThrows(IllegalArgumentException.class, () -> provider.removeGauge(ID_BAD));
        assertThrows(IllegalArgumentException.class, () -> provider.removeGauge(null));
        assertThrows(IllegalArgumentException.class, () -> provider.addGaugeValue(null, 1.0));
        assertEquals(0, provider.getNumberOfCustomGauges());

        provider.addGaugeValue(ID_GOOD, value);
        assertEquals(1, provider.getNumberOfCustomGauges());
        assertEquals(value, provider.getGaugeValue(ID_GOOD), 0.0);

        provider.addGaugeValue(ID_GOOD, value2);
        assertEquals(1, provider.getNumberOfCustomGauges());
        assertEquals(value2, provider.getGaugeValue(ID_GOOD), 0.0);

        provider.calculateMetrics();
        assertTrue(provider.getRegisteredGaugeValue(MetricsProvider.SYS_MEM_TOTAL) > 0);
        assertEquals(value2, provider.getRegisteredGaugeValue(ID_GOOD), 0.0);
        
        provider.addGaugeValue(ID_GOOD, negVal);
        assertEquals(1, provider.getNumberOfCustomGauges());
        assertEquals(negVal, provider.getGaugeValue(ID_GOOD), 0.0);
        assertEquals(0, provider.getGaugeValue(ID_BAD), 0.0);
        assertEquals(0, provider.getGaugeValue(null), 0.0);

        provider.removeGauge(ID_GOOD);
        assertEquals(0, provider.getNumberOfCustomGauges());
        assertEquals(0.0, provider.getGaugeValue(ID_GOOD), 0.0);
        assertThrows(IllegalArgumentException.class, () -> provider.removeGauge(ID_GOOD));
    }

    /**
     * Tests {@link MetricsProvider} Counter CRUD operations.
     */
    @Test
    public void testCounterCrudOperations() {
        double value = 1.1;
        double valueX2 = value + value;
        double negValue = -value;

        assertEquals(0, provider.getNumberOfCustomCounters());
        assertThrows(IllegalArgumentException.class, () -> provider.removeCounter(ID_BAD));
        assertThrows(IllegalArgumentException.class, () -> provider.removeCounter(null));
        assertThrows(IllegalArgumentException.class, () -> provider.increaseCounter(null));
        assertThrows(IllegalArgumentException.class, () -> provider.increaseCounterBy((String) null, value));
        assertEquals(0, provider.getNumberOfCustomCounters());

        provider.increaseCounter(ID_GOOD);
        assertEquals(1, provider.getNumberOfCustomCounters());
        assertEquals(1.0, provider.getCounterValue(ID_GOOD), 0.0);

        provider.increaseCounter(ID_GOOD);
        assertEquals(1, provider.getNumberOfCustomCounters());
        assertEquals(2.0, provider.getCounterValue(ID_GOOD), 0.0);
        assertEquals(2.0, provider.getRegisteredCounterValue(ID_GOOD), 0.0); // incomplete, there might not be a pre-reg

        provider.increaseCounterBy(ID_OK, value);
        assertEquals(2, provider.getNumberOfCustomCounters());
        assertEquals(value, provider.getCounterValue(ID_OK), 0.0);

        provider.increaseCounterBy(ID_OK, value);
        assertEquals(2, provider.getNumberOfCustomCounters());
        assertEquals(valueX2, provider.getCounterValue(ID_OK), 0.0);

        assertThrows(IllegalArgumentException.class, () -> provider.increaseCounterBy(ID_OK, negValue));
        assertEquals(2, provider.getNumberOfCustomCounters());
        assertEquals(valueX2, provider.getCounterValue(ID_OK), 0.0);

        assertEquals(0.0, provider.getCounterValue(ID_BAD), 0.0);
        assertEquals(0.0, provider.getCounterValue(null), 0.0);

        provider.removeCounter(ID_GOOD);
        assertEquals(1, provider.getNumberOfCustomCounters());
        assertThrows(IllegalArgumentException.class, () -> provider.removeCounter(ID_GOOD));
    }

    /**
     * Prepares the timer test in {@link #testTimerCrudOperations()}.
     */
    private void prepareTimerCrudTest() {
        assertEquals(0, provider.getNumberOfCustomTimers());
        assertThrows(IllegalArgumentException.class, () -> provider.removeTimer(ID_BAD));
        assertThrows(IllegalArgumentException.class, () -> provider.removeTimer(null));
        assertThrows(IllegalArgumentException.class,
                () -> provider.recordWithTimer(null, () -> TestUtils.oneSecondRunnable()));
        assertThrows(IllegalArgumentException.class, () -> provider.recordWithTimer(ID_GOOD, (Runnable) null));
        assertThrows(IllegalArgumentException.class,
                () -> provider.recordWithTimer(null, () -> TestUtils.oneSecondSupplier()));
        assertThrows(IllegalArgumentException.class, () -> provider.recordWithTimer(ID_GOOD, (Supplier<String>) null));
        assertEquals(0, provider.getNumberOfCustomTimers());
    }

    /**
     * Tests {@link MetricsProvider} Timer CRUD operations.
     */
    @Test
    public void testTimerCrudOperations() {
        prepareTimerCrudTest();
        
        provider.recordWithTimer(ID_GOOD, () -> TestUtils.oneSecondRunnable());
        assertEquals(1, provider.getNumberOfCustomTimers());
        assertEquals(1.0, provider.getTotalTimeFromTimer(ID_GOOD), 0.5);
        assertEquals(1.0, provider.getMaxTimeFromTimer(ID_GOOD), 0.5);
        assertEquals(1, provider.getTimerCount(ID_GOOD));

        provider.recordWithTimer(ID_GOOD, () -> TestUtils.threeSecondRunnable());
        assertEquals(1, provider.getNumberOfCustomTimers());
        assertEquals(4.0, provider.getTotalTimeFromTimer(ID_GOOD), 0.5);
        assertEquals(3.0, provider.getMaxTimeFromTimer(ID_GOOD), 0.5);
        assertEquals(2, provider.getTimerCount(ID_GOOD));
        assertEquals(2, provider.getRegisteredTimerCount(ID_GOOD)); // incomplete, there might not be a pre-reg one

        provider.recordWithTimer(ID_GOOD, () -> TestUtils.twoSecondRunnable());
        assertEquals(1, provider.getNumberOfCustomTimers());
        assertEquals(6.0, provider.getTotalTimeFromTimer(ID_GOOD), 0.5);
        assertEquals(3.0, provider.getMaxTimeFromTimer(ID_GOOD), 0.5);
        assertEquals(3, provider.getTimerCount(ID_GOOD));

        assertEquals(TestUtils.DATA, provider.recordWithTimer(ID_OK, () -> TestUtils.oneSecondSupplier()));
        assertEquals(2, provider.getNumberOfCustomTimers());
        assertEquals(1.0, provider.getTotalTimeFromTimer(ID_OK), 0.5);
        assertEquals(1.0, provider.getMaxTimeFromTimer(ID_OK), 0.5);
        assertEquals(1, provider.getTimerCount(ID_OK));

        assertEquals(TestUtils.DATA, provider.recordWithTimer(ID_OK, () -> TestUtils.threeSecondSupplier()));
        assertEquals(2, provider.getNumberOfCustomTimers());
        assertEquals(4.0, provider.getTotalTimeFromTimer(ID_OK), 0.5);
        assertEquals(3.0, provider.getMaxTimeFromTimer(ID_OK), 0.5);
        assertEquals(2, provider.getTimerCount(ID_OK));

        assertEquals(TestUtils.DATA, provider.recordWithTimer(ID_OK, () -> TestUtils.twoSecondSupplier()));
        assertEquals(2, provider.getNumberOfCustomTimers());
        assertEquals(6.0, provider.getTotalTimeFromTimer(ID_OK), 0.5);
        assertEquals(3.0, provider.getMaxTimeFromTimer(ID_OK), 0.5);
        assertEquals(3, provider.getTimerCount(ID_OK));

        provider.recordWithTimer(ID_PASSABLE, 1000, TimeUnit.MILLISECONDS);
        assertEquals(3, provider.getNumberOfCustomTimers());
        assertEquals(1.0, provider.getTotalTimeFromTimer(ID_PASSABLE), 0.0);
        assertEquals(1.0, provider.getMaxTimeFromTimer(ID_PASSABLE), 0.0);
        assertEquals(1, provider.getTimerCount(ID_PASSABLE));

        provider.recordWithTimer(ID_PASSABLE, 3000, TimeUnit.MILLISECONDS);
        assertEquals(3, provider.getNumberOfCustomTimers());
        assertEquals(4.0, provider.getTotalTimeFromTimer(ID_PASSABLE), 0.0);
        assertEquals(3.0, provider.getMaxTimeFromTimer(ID_PASSABLE), 0.0);
        assertEquals(2, provider.getTimerCount(ID_PASSABLE));

        provider.recordWithTimer(ID_PASSABLE, 2000, TimeUnit.MILLISECONDS);
        assertEquals(3, provider.getNumberOfCustomTimers());
        assertEquals(6.0, provider.getTotalTimeFromTimer(ID_PASSABLE), 0.0);
        assertEquals(3.0, provider.getMaxTimeFromTimer(ID_PASSABLE), 0.0);
        assertEquals(3, provider.getTimerCount(ID_PASSABLE));
        assertThrows(IllegalArgumentException.class,
                () -> provider.recordWithTimer(ID_PASSABLE, -1000, TimeUnit.MILLISECONDS));
        assertThrows(IllegalArgumentException.class, () -> provider.recordWithTimer(ID_PASSABLE, 1000, null));

        assertEquals(0.0, provider.getTotalTimeFromTimer(ID_BAD), 0.0);
        assertEquals(0.0, provider.getTotalTimeFromTimer(null), 0.0);
        assertEquals(0.0, provider.getMaxTimeFromTimer(ID_BAD), 0.0);
        assertEquals(0.0, provider.getMaxTimeFromTimer(null), 0.0);
        assertEquals(0, provider.getTimerCount(ID_BAD));
        assertEquals(0, provider.getTimerCount(null));

        provider.removeTimer(ID_GOOD);
        assertEquals(2, provider.getNumberOfCustomTimers());
        assertThrows(IllegalArgumentException.class, () -> provider.removeTimer(ID_GOOD));
    }

    /**
     * Tests {@link MetricsProvider#setMemoryBaseUnit(CapacityBaseUnit)}.
     */
    @Test
    public void setMemoryBaseUnitOk() {
        provider.setMemoryBaseUnit(CapacityBaseUnit.KILOBYTES);
        assertEquals(CapacityBaseUnit.KILOBYTES, provider.getMemoryBaseUnit());
    }

    /**
     * Tests {@link MetricsProvider#setMemoryBaseUnit(CapacityBaseUnit)}.
     */
    @Test
    public void setMemoryBaseUnitNull() {
        assertThrows(IllegalArgumentException.class, () -> provider.setMemoryBaseUnit(null));
    }

    /**
     * Tests {@link MetricsProvider#setDiskBaseUnit(CapacityBaseUnit)}.
     */
    @Test
    public void setDiskBaseUnitOk() {
        provider.setDiskBaseUnit(CapacityBaseUnit.MEGABYTES);
        assertEquals(CapacityBaseUnit.MEGABYTES, provider.getDiskBaseUnit());
    }

    /**
     * Tests {@link MetricsProvider#setDiskBaseUnit(CapacityBaseUnit)}.
     */
    @Test
    public void setDiskBaseUnitNull() {
        assertThrows(IllegalArgumentException.class, () -> provider.setDiskBaseUnit(null));
    }
    
    /**
     * Tests the lists returned by the provider.
     */
    @Test
    public void testLists() {
        assertList(provider.getCustomCounterList(), provider.getNumberOfCustomCounters() == 0);
        assertList(provider.getCustomGaugeList(), provider.getNumberOfCustomGauges() == 0);
        assertList(provider.getCustomTimerList(), provider.getNumberOfCustomTimers() == 0);
        assertList(provider.getTaggedMeterList(), false);
        assertList(provider.getSimpleMeterList(), false);
    }
    
    /**
     * Asserts a List in JsonArray format.
     * 
     * @param list the list in textual JsonArray format
     * @param expectedEmpty whether the list shall be empty or filled with entries
     */
    private void assertList(String list, boolean expectedEmpty) {
        if (expectedEmpty) {
            assertEquals("[]", list);
        } else {
            assertTrue(list.length() > 2);
            assertTrue(list.matches("\\[(\"\\S+\"(,\\s*\"\\S+\")*)?\\]"));
        }
    }
    
    /**
     * Tests serializing/deserializing.
     */
    @Test
    public void testJson() {
        double gValue = 10;
        Counter counter = Counter
            .builder("service.sent").baseUnit("tuple/s")
            .description("Tuples sent out by a service")
            .tags("service", "SimpleReceiver", "application", "SimpleMeshApp", "device", "dev0")
            .register(provider.getRegistry());
        Counter.builder("service.received")
            .baseUnit("tuple/s")
            .description("Tuples received by a service")
            .tags("service", "SimpleReceiver", "application", "SimpleMeshApp", "device", "dev0")
            .register(provider.getRegistry());
        Counter.builder("system.online")
            .baseUnit("ms")
            .description("Time the system is online")
            .tags("service", "SimpleReceiver", "application", "SimpleMeshApp", "device", "dev2")
            .register(provider.getRegistry());
        Timer timer = Timer.builder("system.time")
            .description("Time the system is opeating")
            .tags("service", "SimpleReceiver", "application", "SimpleMeshApp", "device", "dev2")
            .register(provider.getRegistry());
        Gauge gauge = Gauge.builder("system.value", () -> gValue)
            .description("Some value")
            .tags("service", "SimpleReceiver", "application", "SimpleMeshApp", "device", "dev2")
            .register(provider.getRegistry());

        MetricsProvider.increaseCounterBy(counter, 1);
        timer.record(() -> { TimeUtils.sleep(400); return "ABBA"; });
        MetricsProvider.recordMsTime(timer, () -> 400, () -> { }); // pretend there were another 400 ms
        MetricsProvider.recordNsTime(timer, () -> 400, () -> { }); // and some 400 ns
        
        String json = provider.toJson("id0", false,
            MeterFilter.acceptNameStartsWith("service.sent"),
            MeterFilter.denyNameStartsWith("services.received"));
        assertMeters(json, provider.getRegistry());
        JsonObject obj = Json.createReader(new StringReader(json)).readObject();
        String id = obj.getString("id");
        assertEquals("id0", id);
        if (null != id) {
            for (Map.Entry<String, JsonValue> e : obj.getJsonObject("meters").entrySet()) {
                Meter meter = MeterRepresentation.parseMeter(e.getValue().toString(), "device:dev1");
                assertNotNull(meter);
                if (meter.getId().getName().equals(counter.getId().getName())) {
                    meter.getId().getBaseUnit().equals("tuple/s");
                    meter.getId().getDescription().equals("Tuples sent out by a service");
                    assertEquals("SimpleReceiver", meter.getId().getTag("service"));
                    assertEquals("SimpleMeshApp", meter.getId().getTag("application"));
                    assertEquals("dev0", meter.getId().getTag("device")); // shall not override
                    Iterator<Measurement> iter = meter.measure().iterator();
                    assertTrue(iter.hasNext());
                    assertEquals(1, iter.next().getValue(), 0.001);
                } else if (meter.getId().getName().equals(timer.getId().getName())) {
                    assertEquals("dev2", meter.getId().getTag("device")); // shall not override
                    Iterator<Measurement> iter = meter.measure().iterator();
                    boolean foundCount = false;
                    boolean foundTotalTime = false;
                    while (iter.hasNext()) {
                        Measurement m = iter.next();
                        if (Statistic.COUNT.name().equals(m.getStatistic().name())) {
                            foundCount = true;
                            assertTrue(m.getValue() > 0);
                        } else if (Statistic.TOTAL_TIME.name().equals(m.getStatistic().name())) {
                            foundTotalTime = true;
                            assertTrue(m.getValue() > 0);
                        }
                    }
                    assertTrue(foundCount && foundTotalTime);
                } else if (meter.getId().getName().equals(gauge.getId().getName())) {
                    assertEquals("dev2", meter.getId().getTag("device")); // shall not override
                    Iterator<Measurement> iter = meter.measure().iterator();
                    while (iter.hasNext()) {
                        Measurement m = iter.next();
                        if (Statistic.VALUE.name().equals(m.getStatistic().name())) {
                            assertEquals(gValue, m.getValue(), 0.01);
                        }
                    }
                }
            }
        }
    }

    /**
     * Asserts that the meters in {@code json} are in the originating {@code registry} and of the same meter type.
     * 
     * @param json the JSON representation
     * @param registry the registry to check against
     */
    private void assertMeters(String json, MeterRegistry registry) {
        JsonObject obj = Json.createReader(new StringReader(json)).readObject();
        String id = obj.getString("id");
        Assert.assertNotNull(id);
        Assert.assertTrue(id.length() > 0);
        for (Map.Entry<String, JsonValue> e : obj.getJsonObject("meters").entrySet()) {
            Meter meter = MeterRepresentation.parseMeter(e.getValue().toString(), "device:abcd");
            RequiredSearch s = registry.get(meter.getId().getName());
            Assert.assertNotNull(s.meter());
            Assert.assertTrue(meter.getId().getType() == s.meter().getId().getType());
            List<Measurement> mMeasure = CollectionUtils.toList(meter.measure().iterator());
            List<Measurement> sMeasure = CollectionUtils.toList(s.meter().measure().iterator());
            Assert.assertEquals(mMeasure.size(), sMeasure.size());
            for (int i = 0; i < mMeasure.size(); i++) {
                Measurement mM = mMeasure.get(i);
                Measurement sM = sMeasure.get(i);
                Assert.assertEquals(mM.getValue(), sM.getValue(), 0.01);
                Assert.assertEquals(mM.getStatistic(), sM.getStatistic());
            }
        }
    }
    
    /**
     * Tests {@link MetricsProvider#include(String, MeterFilter...)}.
     */
    @Test
    public void testInclude() {
        assertTrue(MetricsProvider.include("jvm_memory_pool_collection_max_bytes", 
            MetricsProvider.DEFAULT_METER_FILTERS)); // filters use dot
        assertFalse(MetricsProvider.include("jvm_memory_pool_collection_max_bytes".replaceAll("_", "."), 
            MetricsProvider.DEFAULT_METER_FILTERS)); // filters use dot
    }

    /**
     * Tests {@link MetricsProvider#append(MeterFilter[], MeterFilter...)}.
     */
    @Test
    public void testAppend() {
        MeterFilter add = MeterFilter.deny();
        MeterFilter[] t = MetricsProvider.append(MetricsProvider.DEFAULT_METER_FILTERS, add);
        assertTrue(t.length == MetricsProvider.DEFAULT_METER_FILTERS.length + 1);
        for (int i = 0; i < t.length; i++) {
            assertNotNull(t[i]);
        }
        for (int i = 0; i < MetricsProvider.DEFAULT_METER_FILTERS.length; i++) {
            assertTrue(t[i] == MetricsProvider.DEFAULT_METER_FILTERS[i]);
        }
        assertTrue(t[t.length - 1] == add);
    }

}
