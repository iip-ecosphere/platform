package test.de.oktoflow.platform.tools.energyAggregator.core;

import java.time.Instant;

import org.junit.Assert;
import org.junit.Test;

import de.oktoflow.platform.tools.energyAggregator.core.EnergyAggregator;
import de.oktoflow.platform.tools.energyAggregator.model.MachineEnergyMeasurement;
import de.oktoflow.platform.tools.energyAggregator.model.MeasurementQuality;
import de.oktoflow.platform.tools.energyAggregator.model.MeasurementType;

/**
 * Tests {@link EnergyAggregator}.
 * 
 * @author Kodjo Gilles Adjroloh
 */
public class EnergyAggregatorTest {

    /**
     * The lathe device id.
     */
    private static final String LATHE = "lathe";

    /**
     * Creates a valid measurement.
     * 
     * @param deviceId the device id
     * @param type the measurement type
     * @param value the value
     * @param timestamp the timestamp in ISO-8601 format
     * @return the measurement
     */
    private static MachineEnergyMeasurement measurement(String deviceId, MeasurementType type, double value, 
        String timestamp) {
        return new MachineEnergyMeasurement("source-1", deviceId, type, value, 
            type == MeasurementType.CURRENT_POWER ? "W" : "Wh", Instant.parse(timestamp), MeasurementQuality.VALID);
    }

    /**
     * Returns the latest power value of the lathe.
     * 
     * @param aggregator the aggregator
     * @param type the measurement type
     * @param deviceId the device id
     * @return the value
     */
    private static double latest(EnergyAggregator aggregator, String deviceId, MeasurementType type) {
        return aggregator.getLatestMeasurement(deviceId, type).orElseThrow().value();
    }

    /**
     * Tests that the first measurement is stored.
     */
    @Test
    public void testFirstMeasurementIsStored() {
        EnergyAggregator aggregator = new EnergyAggregator();
        aggregator.updateMeasurement(measurement(LATHE, MeasurementType.CURRENT_POWER, 500.0, "2026-10-08T10:00:00Z"));
        Assert.assertEquals(500.0, latest(aggregator, LATHE, MeasurementType.CURRENT_POWER), 0.0);
    }

    /**
     * Tests that a newer measurement replaces the stored one.
     */
    @Test
    public void testNewerMeasurementReplacesExistingMeasurement() {
        EnergyAggregator aggregator = new EnergyAggregator();
        aggregator.updateMeasurement(measurement(LATHE, MeasurementType.CURRENT_POWER, 500.0, "2026-10-08T10:00:00Z"));
        aggregator.updateMeasurement(measurement(LATHE, MeasurementType.CURRENT_POWER, 650.0, "2026-10-08T10:05:00Z"));
        Assert.assertEquals(650.0, latest(aggregator, LATHE, MeasurementType.CURRENT_POWER), 0.0);
    }

    /**
     * Tests that an older measurement arriving late does not replace a newer one.
     */
    @Test
    public void testOlderMeasurementDoesNotReplaceNewerMeasurement() {
        EnergyAggregator aggregator = new EnergyAggregator();
        aggregator.updateMeasurement(measurement(LATHE, MeasurementType.CURRENT_POWER, 650.0, "2026-10-08T10:05:00Z"));
        aggregator.updateMeasurement(measurement(LATHE, MeasurementType.CURRENT_POWER, 500.0, "2026-10-08T10:00:00Z"));
        Assert.assertEquals(650.0, latest(aggregator, LATHE, MeasurementType.CURRENT_POWER), 0.0);
    }

    /**
     * Tests the return value: {@code true} if kept, {@code false} if an older value arrives late.
     */
    @Test
    public void testUpdateReportsWhetherMeasurementWasAccepted() {
        EnergyAggregator aggregator = new EnergyAggregator();
        Assert.assertTrue(aggregator.updateMeasurement(
            measurement(LATHE, MeasurementType.CURRENT_POWER, 500.0, "2026-10-08T10:00:00Z")));
        Assert.assertTrue(aggregator.updateMeasurement(
            measurement(LATHE, MeasurementType.CURRENT_POWER, 650.0, "2026-10-08T10:05:00Z")));
        Assert.assertFalse(aggregator.updateMeasurement(
            measurement(LATHE, MeasurementType.CURRENT_POWER, 400.0, "2026-10-08T10:01:00Z")));
    }

    /**
     * Tests that for equal timestamps the first value stays.
     */
    @Test
    public void testEqualTimestampKeepsExistingMeasurement() {
        EnergyAggregator aggregator = new EnergyAggregator();
        aggregator.updateMeasurement(measurement(LATHE, MeasurementType.CURRENT_POWER, 500.0, "2026-10-08T10:00:00Z"));
        Assert.assertFalse(aggregator.updateMeasurement(
            measurement(LATHE, MeasurementType.CURRENT_POWER, 999.0, "2026-10-08T10:00:00Z")));
        Assert.assertEquals(500.0, latest(aggregator, LATHE, MeasurementType.CURRENT_POWER), 0.0);
    }

    /**
     * Tests that different devices keep their own values.
     */
    @Test
    public void testDifferentDevicesDoNotOverwriteEachOther() {
        EnergyAggregator aggregator = new EnergyAggregator();
        aggregator.updateMeasurement(measurement(LATHE, MeasurementType.CURRENT_POWER, 500.0, "2026-10-08T10:00:00Z"));
        aggregator.updateMeasurement(measurement("laser", MeasurementType.CURRENT_POWER, 1250.0, 
            "2026-10-08T10:05:00Z"));
        Assert.assertEquals(500.0, latest(aggregator, LATHE, MeasurementType.CURRENT_POWER), 0.0);
        Assert.assertEquals(1250.0, latest(aggregator, "laser", MeasurementType.CURRENT_POWER), 0.0);
    }

    /**
     * Tests that power and energy of the same device are stored separately.
     */
    @Test
    public void testDifferentMeasurementTypesDoNotOverwriteEachOther() {
        EnergyAggregator aggregator = new EnergyAggregator();
        aggregator.updateMeasurement(measurement(LATHE, MeasurementType.CURRENT_POWER, 500.0, "2026-10-08T10:00:00Z"));
        aggregator.updateMeasurement(measurement(LATHE, MeasurementType.CUMULATIVE_ENERGY, 12500.0, 
            "2026-10-08T10:05:00Z"));
        Assert.assertEquals(500.0, latest(aggregator, LATHE, MeasurementType.CURRENT_POWER), 0.0);
        Assert.assertEquals(12500.0, latest(aggregator, LATHE, MeasurementType.CUMULATIVE_ENERGY), 0.0);
    }

    /**
     * Tests that asking for a device without data returns empty, not an error.
     */
    @Test
    public void testUnknownDeviceReturnsEmpty() {
        Assert.assertFalse(new EnergyAggregator().getLatestMeasurement(LATHE, MeasurementType.CURRENT_POWER)
            .isPresent());
    }

    /**
     * Tests that a <b>null</b> measurement is rejected.
     */
    @Test(expected = NullPointerException.class)
    public void testNullMeasurementIsRejected() {
        new EnergyAggregator().updateMeasurement(null);
    }

}
