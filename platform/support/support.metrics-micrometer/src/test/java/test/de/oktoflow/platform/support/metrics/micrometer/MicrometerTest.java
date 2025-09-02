package test.de.oktoflow.platform.support.metrics.micrometer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.metrics.Clock;
import de.iip_ecosphere.platform.support.metrics.Counter;
import de.iip_ecosphere.platform.support.metrics.Gauge;
import de.iip_ecosphere.platform.support.metrics.Measurement;
import de.iip_ecosphere.platform.support.metrics.Meter;
import de.iip_ecosphere.platform.support.metrics.Meter.Id;
import de.iip_ecosphere.platform.support.metrics.Meter.Type;
import de.iip_ecosphere.platform.support.metrics.MeterFilter;
import de.iip_ecosphere.platform.support.metrics.MeterRegistry;
import de.iip_ecosphere.platform.support.metrics.MetricsFactory;
import de.iip_ecosphere.platform.support.metrics.Statistic;
import de.iip_ecosphere.platform.support.metrics.Tag;
import de.iip_ecosphere.platform.support.metrics.Timer;
import de.oktoflow.platform.support.metrics.micrometer.MicrometerMetricsFactory;
import de.oktoflow.platform.support.metrics.micrometer.MicrometerUtils;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Tests {@link MicrometerMetricsFactory}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MicrometerTest {

    /**
     * Tests basic REST functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testMetrics() throws IOException {
        MetricsFactory fac = MetricsFactory.getInstance();
        Assert.assertTrue(fac instanceof MicrometerMetricsFactory);
        
        MeterRegistry reg = fac.createRegistry();
        Assert.assertNotNull(reg);
        Counter counter = MetricsFactory.buildCounter("myCounter")
            .baseUnit("bit/s")
            .description("myDesc")
            .tags("t1", "vt1")
            .register(reg);
        counter.increment(2);
        Assert.assertTrue(counter.count() > 1);

        AtomicInteger gVal = new AtomicInteger(5);
        Gauge gauge = MetricsFactory.buildGauge("myGauge", gVal, v -> v.doubleValue())
            .description("myDescG")
            .baseUnit("parts/s")
            .tags("t1", "vg1")
            .register(reg);
        Assert.assertEquals(gVal.get(), (int) gauge.value());

        gVal.incrementAndGet();
        Gauge gauge2 = MetricsFactory.buildGauge("myGauge2", () -> gVal.doubleValue())
            .description("myDescG")
            .baseUnit("parts/s")
            .tags("t1", "vg2")
            .register(reg);
        Assert.assertEquals(gVal.get(), (int) gauge2.value());
        assertId(gauge2);

        Timer timer = MetricsFactory.buildTimer("myTimer")
            .description("myDescT")
            .tags("t1", "vt1")
            .register(reg);
        assertId(timer);
        
        testTimerRecord(timer);
        Assert.assertTrue(timer.count() >= 5);
        Assert.assertTrue(timer.max(TimeUnit.MILLISECONDS) > 0);
        Assert.assertTrue(timer.mean(TimeUnit.MILLISECONDS) > 0);
        Assert.assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) > 600);
        Assert.assertNotNull(timer.baseTimeUnit());
        
        reg.config().meterFilter(MetricsFactory.denyNameStartsWith("java."));
        Assert.assertNotNull(reg.config().clock());
        Assert.assertEquals(gauge2, reg.getMeter(gauge2.getName()));
        Assert.assertEquals(gauge2, reg.getMeter(gauge2.getName(), List.of(fac.createTag("t1", "vg2"))));
        
        Assert.assertEquals(gauge, reg.getGauge(gauge.getName()));
        Assert.assertEquals(timer, reg.getTimer(timer.getName()));
        Assert.assertEquals(counter, reg.getCounter(counter.getName()));
        Assert.assertNull(reg.getTimer(counter.getName()));
        Assert.assertNull(reg.getTimer("abc"));
        Assert.assertNull(reg.getMeter("abcd"));
        Assert.assertNull(reg.remove("abcd"));
        
        Assert.assertEquals(gauge2, reg.remove(gauge2));
        Assert.assertNull(reg.getMeter(gauge2.getName()));
        Assert.assertNull(reg.remove(gauge2));
        
        Clock clock = reg.config().clock();
        Assert.assertNotNull(clock);
        Assert.assertTrue(clock.monotonicTime() > 0);
        Assert.assertTrue(clock.wallTime() > 0);
        Assert.assertTrue(clock.equals(clock));
        Assert.assertFalse(clock.equals(reg));
        clock.hashCode();
        
        Assert.assertNotNull(reg.getMeters());
        int size = reg.getMeters().size();
        Assert.assertTrue(size >= 3); // one gauge removed
        reg.config().meterFilter(MetricsFactory.denyNameStartsWith("xxx"));
        MetricsFactory.buildTimer("xxx").register(reg);        
        Assert.assertTrue(reg.getMeters().size() == size);
    }
    
    /**
     * Further metrics tests.
     */
    @Test
    public void testMetrics2() {
        MetricsFactory fac = MetricsFactory.getInstance();
        MeterRegistry reg = fac.createRegistry();
        Counter c1 = reg.counter("c1", "instance", "c1");
        Counter c2 = reg.counter("c1", "instance", "c2");
        reg.remove(c2.getId());
        Tag ic1 = fac.createImmutableTag("instance", "c1");
        Assert.assertEquals("instance", ic1.getKey());
        Assert.assertEquals("c1", ic1.getValue());
        ic1.hashCode();
        Assert.assertTrue(ic1.equals(ic1));
        Tag ic11 = fac.createImmutableTag("instance", "c1");
        Assert.assertTrue(ic1.equals(ic11));
        Assert.assertFalse(ic1.equals(reg));
        Meter m = reg.getMeter("c1", List.of(ic1, fac.createImmutableTag("instance", "c1")));
        Assert.assertNotNull(m);
        Assert.assertEquals(c1, m);
        Assert.assertNull(reg.getMeter("c1", List.of(fac.createImmutableTag("a", "b"))));
        Assert.assertNull(reg.remove(c2.getId()));
        
        Timer t1 = reg.timer("t1", "instance", "t1");
        Assert.assertEquals("t1", t1.getName());

        Assert.assertEquals(12.3, reg.gauge("g1", 12.3), 0.1);
        
        MeterFilter f = fac.createFilterDeny();
        Assert.assertNotNull(f.accept(fac.createId("id", null, null, null, null)));
        Assert.assertNotNull(f.accept(fac.createId("id", List.of(ic1), "m/s", "desc", Type.COUNTER)));
        fac.createFilterAcceptNameStartsWith("i");
        Measurement me = fac.createMeasurement(() -> 2.0, Statistic.VALUE);
        Assert.assertEquals(2.0, me.getValue(), 0.01);
        Assert.assertEquals(Statistic.VALUE, me.getStatistic());
        Assert.assertEquals(Statistic.VALUE.name(), me.getStatisticAsString());
        
        fac.createTimerStart().stop(t1);
    }
    
    /**
     * Tests registry creation.
     */
    @Test
    public void testCreateRegistry() {
        MetricsFactory fac = MetricsFactory.getInstance();
        MeterRegistry reg = fac.createRegistry(new SimpleMeterRegistry());
        Assert.assertNotNull(reg);
        // default, warn
        reg = fac.createRegistry(new Object());
        Assert.assertNotNull(reg);
    }
    
    // checkstyle: stop exception type check
    
    /**
     * Tests the various timer record functions.
     * 
     * @param timer the timer to test with
     */
    private void testTimerRecord(Timer timer) {
        timer.record(() -> {
            TimeUtils.sleep(500);
        });
        Assert.assertEquals(5, (int) timer.record(() -> {
            return 5;
        }));
        timer.record(100, TimeUnit.MILLISECONDS);
        try {
            Assert.assertEquals(1, (int) timer.recordCallable(() -> {
                TimeUtils.sleep(100);
                return 1;
            }));
        } catch (Exception e) {
            Assert.fail("No exception expected");
        }
        try {
            Assert.assertEquals(1, (int) timer.recordCallable(() -> {
                throw new Exception();
            }));
            Assert.fail("Exception expected");
        } catch (Exception e) {
        }
        List<Measurement> measures = CollectionUtils.toList(timer.measure());
        Assert.assertTrue(measures.size() > 0);
        for (Measurement m : measures) {
            Assert.assertNotNull(m.getStatistic());
            Assert.assertNotNull(m.getStatisticAsString());
            Assert.assertNotNull(m.getValue() >= 0);
            m.hashCode();
            Assert.assertTrue(m.equals(m));
            Assert.assertFalse(m.equals(measures));
        }
    }
    
    // checkstyle: resume exception type check
    
    /**
     * Generically asserts the id of {@code meter}.
     * 
     * @param meter the meter to assert
     */
    private void assertId(Meter meter) {
        Id id = meter.getId();
        Assert.assertNotNull(id);
        Assert.assertEquals(meter.getName(), id.getName());
        id.getBaseUnit(); // may not be set
        id.getDescription(); // may not be set
        Assert.assertNull(id.getTag("yxz")); // not define
        id.getTags(); // my not be set
        id.getTagsAsIterable(); // may not be set
        Assert.assertNotNull(id.getType());
        Assert.assertTrue(id.equals(id));
        Assert.assertFalse(id.equals(meter));
        id.hashCode();
        Assert.assertTrue(meter.equals(meter));
        Assert.assertFalse(meter.equals(id));
        meter.hashCode();
        Assert.assertNotNull(id.toString());
    }

    /**
     * Tests the public utility class.
     */
    @Test
    public void testUtils() {
        MetricsFactory fac = MetricsFactory.getInstance();
        MeterRegistry reg = fac.createRegistry();

        List<Tag> tags = List.of(fac.createImmutableTag("i", "c1"), fac.createImmutableTag("i", "c2"));
        List<Tag> tags2 = CollectionUtils.toList(
            MicrometerUtils.mmWrapTagIterable(
                MicrometerUtils.wrapTagIterable(tags)));
        Assert.assertEquals(tags, tags2);

        List<Measurement> meas = List.of(fac.createMeasurement(() -> 1.0, Statistic.VALUE), 
            fac.createMeasurement(() -> 2.0, Statistic.VALUE));
        List<Measurement> meas2 = CollectionUtils.toList(
            MicrometerUtils.mmWrapMeasurementIterable(
                MicrometerUtils.wrapMeasurementIterable(meas)));
        Assert.assertEquals(meas, meas2);

        MeterFilter filter = MetricsFactory.deny();
        MeterFilter filter2 = MicrometerUtils.mmFilterValue(MicrometerUtils.filterValue(filter));
        Assert.assertEquals(filter, filter2);
        filter.hashCode();
        
        MeterFilter[] fi = {filter, MetricsFactory.acceptNameStartsWith("x")};
        MeterFilter[] fi2 = MicrometerUtils.mmFilterValue(MicrometerUtils.filterValue(fi));
        Assert.assertArrayEquals(fi, fi2);
        
        Counter counter = MetricsFactory.buildCounter("counter").register(reg);
        counter.increment();
        Assert.assertNull(MicrometerUtils.createMeter(counter, (id, type, mea) -> { 
            Assert.assertEquals("counter", id.getName());
            Assert.assertEquals(io.micrometer.core.instrument.Meter.Type.COUNTER, type);
            Assert.assertTrue(CollectionUtils.toList(mea.iterator()).size() == 1);
            return null;
        }));
        Assert.assertNull(MicrometerUtils.createMeter(null, (i, t, m) -> null));
        
        Assert.assertEquals(Statistic.VALUE, MicrometerUtils.value(Statistic.class, 
            io.micrometer.core.instrument.Statistic.VALUE , Statistic.UNKNOWN));
        Assert.assertEquals(Statistic.VALUE, MicrometerUtils.value(Statistic.class, (String) null, Statistic.VALUE));
        Assert.assertEquals(Statistic.VALUE, MicrometerUtils.value(Statistic.class, "1xyz", Statistic.VALUE));
        
        Assert.assertNull(MicrometerUtils.filterValue((MeterFilter) null));
        Assert.assertNull(MicrometerUtils.idValue((Id) null));
        Assert.assertNull(MicrometerUtils.tagValue((Tag) null));
        Assert.assertEquals(io.micrometer.core.instrument.Statistic.UNKNOWN, 
            MicrometerUtils.statisticValue((Statistic) null));
    }

    /**
     * Tests the public utility class.
     */
    @Test
    public void testUtilsMM() {
        io.micrometer.core.instrument.config.MeterFilter[] filter = {
            io.micrometer.core.instrument.config.MeterFilter.acceptNameStartsWith("y")};
        io.micrometer.core.instrument.config.MeterFilter[] filter2 = MicrometerUtils.append(filter, 
            io.micrometer.core.instrument.config.MeterFilter.accept(), 
            io.micrometer.core.instrument.config.MeterFilter.deny());
        Assert.assertTrue(filter2.length == 3);
        
        io.micrometer.core.instrument.Meter.Id id 
            = new io.micrometer.core.instrument.Meter.Id("y", null, null, null, null);
        Assert.assertTrue(MicrometerUtils.include(id, filter));
        Assert.assertFalse(MicrometerUtils.include(id, io.micrometer.core.instrument.config.MeterFilter.deny()));
        Assert.assertTrue(MicrometerUtils.include("y", filter));
        Assert.assertFalse(MicrometerUtils.include("y", 
            io.micrometer.core.instrument.config.MeterFilter.deny()));
        Assert.assertTrue(MicrometerUtils.include("y", 
            io.micrometer.core.instrument.config.MeterFilter.accept()));
        Assert.assertTrue(MicrometerUtils.include("y", 
            io.micrometer.core.instrument.config.MeterFilter.acceptNameStartsWith("y")));
        Assert.assertTrue(MicrometerUtils.include("y", 
            io.micrometer.core.instrument.config.MeterFilter.acceptNameStartsWith("y"), 
            io.micrometer.core.instrument.config.MeterFilter.accept()));

        SimpleMeterRegistry reg = new SimpleMeterRegistry();
        MicrometerUtils.apply(reg, filter2);

        Assert.assertEquals(Statistic.UNKNOWN, 
                MicrometerUtils.mmStatisticValue((io.micrometer.core.instrument.Statistic) null));
        Assert.assertEquals(Statistic.ACTIVE_TASKS, 
                MicrometerUtils.mmStatisticValue(io.micrometer.core.instrument.Statistic.ACTIVE_TASKS));
    }
    
}
