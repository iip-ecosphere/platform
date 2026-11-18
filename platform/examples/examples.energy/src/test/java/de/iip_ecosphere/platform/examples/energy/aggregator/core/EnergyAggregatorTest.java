package test.de.iip_ecosphere.platform.examples.energy.aggregator.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.Optional;

import org.junit.Test;

import de.iip_ecosphere.platform.examples.energy.aggregator.core.EnergyAggregator;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.MachineEnergyMeasurement;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.MeasurementQuality;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.MeasurementType;

public class EnergyAggregatorTest {

    @Test
    public void testFirstMeasurementIsStored() {
        EnergyAggregator aggregator = new EnergyAggregator();

        MachineEnergyMeasurement measurement = new MachineEnergyMeasurement(
            "shelly-1",
            "lathe",
            MeasurementType.CURRENT_POWER,
            500.0,
            "W",
            Instant.parse("2026-10-08T10:00:00Z"),
            MeasurementQuality.VALID
        );

        aggregator.updateMeasurement(measurement);

        Optional<MachineEnergyMeasurement> result =
            aggregator.getLatestMeasurement(
                "lathe",
                MeasurementType.CURRENT_POWER
            );

        assertTrue(result.isPresent());
        assertEquals(500.0, result.get().value(), 0.0);
    }

    @Test
    public void testNewerMeasurementReplacesExistingMeasurement() {
        EnergyAggregator aggregator = new EnergyAggregator();

        MachineEnergyMeasurement olderMeasurement =
            new MachineEnergyMeasurement(
                "shelly-1",
                "lathe",
                MeasurementType.CURRENT_POWER,
                500.0,
                "W",
                Instant.parse("2026-10-08T10:00:00Z"),
                MeasurementQuality.VALID
            );

        MachineEnergyMeasurement newerMeasurement =
            new MachineEnergyMeasurement(
                "shelly-1",
                "lathe",
                MeasurementType.CURRENT_POWER,
                650.0,
                "W",
                Instant.parse("2026-10-08T10:05:00Z"),
                MeasurementQuality.VALID
            );

        aggregator.updateMeasurement(olderMeasurement);
        aggregator.updateMeasurement(newerMeasurement);

        Optional<MachineEnergyMeasurement> result =
            aggregator.getLatestMeasurement(
                "lathe",
                MeasurementType.CURRENT_POWER
            );

        assertTrue(result.isPresent());
        assertEquals(650.0, result.get().value(), 0.0);
    }
    
    // The answer is true when kept, false when an older value arrives late
    @Test
    public void testUpdateReportsWhetherMeasurementWasAccepted() {
        EnergyAggregator aggregator = new EnergyAggregator();

        assertTrue(aggregator.updateMeasurement(
            measurement("lathe", MeasurementType.CURRENT_POWER, 500.0, "2026-10-08T10:00:00Z")));
        assertTrue(aggregator.updateMeasurement(
            measurement("lathe", MeasurementType.CURRENT_POWER, 650.0, "2026-10-08T10:05:00Z")));
        assertFalse(aggregator.updateMeasurement(
            measurement("lathe", MeasurementType.CURRENT_POWER, 400.0, "2026-10-08T10:01:00Z")));
    }

    // Same timestamp: the first value stays, the second is ignored
    @Test
    public void testEqualTimestampKeepsExistingMeasurement() {
        EnergyAggregator aggregator = new EnergyAggregator();

        aggregator.updateMeasurement(
            measurement("lathe", MeasurementType.CURRENT_POWER, 500.0, "2026-10-08T10:00:00Z"));
        boolean accepted = aggregator.updateMeasurement(
            measurement("lathe", MeasurementType.CURRENT_POWER, 999.0, "2026-10-08T10:00:00Z"));

        assertFalse(accepted);
        assertEquals(500.0, aggregator.getLatestMeasurement(
            "lathe", MeasurementType.CURRENT_POWER).get().value(), 0.0);
    }

    // Lathe and laser each keep their own power value
    @Test
    public void testDifferentDevicesDoNotOverwriteEachOther() {
        EnergyAggregator aggregator = new EnergyAggregator();

        aggregator.updateMeasurement(
            measurement("lathe", MeasurementType.CURRENT_POWER, 500.0, "2026-10-08T10:00:00Z"));
        aggregator.updateMeasurement(
            measurement("laser", MeasurementType.CURRENT_POWER, 1250.0, "2026-10-08T10:05:00Z"));

        assertEquals(500.0, aggregator.getLatestMeasurement(
            "lathe", MeasurementType.CURRENT_POWER).get().value(), 0.0);
        assertEquals(1250.0, aggregator.getLatestMeasurement(
            "laser", MeasurementType.CURRENT_POWER).get().value(), 0.0);
    }

    // Power and energy of the same machine are stored separately
    @Test
    public void testDifferentMeasurementTypesDoNotOverwriteEachOther() {
        EnergyAggregator aggregator = new EnergyAggregator();

        aggregator.updateMeasurement(
            measurement("lathe", MeasurementType.CURRENT_POWER, 500.0, "2026-10-08T10:00:00Z"));
        aggregator.updateMeasurement(
            measurement("lathe", MeasurementType.CUMULATIVE_ENERGY, 12500.0, "2026-10-08T10:05:00Z"));

        assertEquals(500.0, aggregator.getLatestMeasurement(
            "lathe", MeasurementType.CURRENT_POWER).get().value(), 0.0);
        assertEquals(12500.0, aggregator.getLatestMeasurement(
            "lathe", MeasurementType.CUMULATIVE_ENERGY).get().value(), 0.0);
    }

    // Asking for a machine with no data returns "empty", not an error
    @Test
    public void testUnknownDeviceReturnsEmpty() {
        EnergyAggregator aggregator = new EnergyAggregator();

        assertFalse(aggregator.getLatestMeasurement(
            "lathe", MeasurementType.CURRENT_POWER).isPresent());
    }

    // A null measurement is rejected immediately
    @Test(expected = NullPointerException.class)
    public void testNullMeasurementIsRejected() {
        new EnergyAggregator().updateMeasurement(null);
    }
    
    private static MachineEnergyMeasurement measurement(
            String deviceId, MeasurementType type, double value, String timestamp
        ) {
            return new MachineEnergyMeasurement(
                "source-1", deviceId, type, value,
                type == MeasurementType.CURRENT_POWER ? "W" : "Wh",
                Instant.parse(timestamp), MeasurementQuality.VALID);
        }

    @Test
    public void testOlderMeasurementDoesNotReplaceNewerMeasurement() {
        EnergyAggregator aggregator = new EnergyAggregator();

        MachineEnergyMeasurement newerMeasurement =
            new MachineEnergyMeasurement(
                "shelly-1",
                "lathe",
                MeasurementType.CURRENT_POWER,
                650.0,
                "W",
                Instant.parse("2026-10-08T10:05:00Z"),
                MeasurementQuality.VALID
            );

        MachineEnergyMeasurement olderMeasurement =
            new MachineEnergyMeasurement(
                "shelly-1",
                "lathe",
                MeasurementType.CURRENT_POWER,
                500.0,
                "W",
                Instant.parse("2026-10-08T10:00:00Z"),
                MeasurementQuality.VALID
            );

        aggregator.updateMeasurement(newerMeasurement);
        aggregator.updateMeasurement(olderMeasurement);

        Optional<MachineEnergyMeasurement> result =
            aggregator.getLatestMeasurement(
                "lathe",
                MeasurementType.CURRENT_POWER
            );

        assertTrue(result.isPresent());
        assertEquals(650.0, result.get().value(), 0.0);
    }
}