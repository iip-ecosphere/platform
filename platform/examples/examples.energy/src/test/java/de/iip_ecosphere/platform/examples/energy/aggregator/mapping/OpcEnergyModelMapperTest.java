package test.de.iip_ecosphere.platform.examples.energy.aggregator.mapping;

import static org.junit.Assert.assertEquals;

import java.time.Instant;

import org.junit.Test;

import de.iip_ecosphere.platform.examples.energy.aggregator.mapping.OpcEnergyModelMapper;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.MachineEnergyMeasurement;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.MappedModelUpdate;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.MeasurementQuality;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.MeasurementType;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.OpcUaQuality;

public class OpcEnergyModelMapperTest {

    @Test
    public void testCurrentPowerInWatts() {
        OpcEnergyModelMapper mapper = new OpcEnergyModelMapper();

        MachineEnergyMeasurement measurement =
            new MachineEnergyMeasurement(
                "shelly-1",
                "lathe",
                MeasurementType.CURRENT_POWER,
                500.0,
                "W",
                Instant.parse("2026-10-11T14:00:00Z"),
                MeasurementQuality.VALID
            );

        MappedModelUpdate result = mapper.map(measurement);

        assertEquals("AcActivePowerTotal", result.target());
        assertEquals(500.0, result.value(), 0.0);
        assertEquals("W", result.unit());
        assertEquals(measurement.timestamp(), result.timestamp());
        assertEquals(OpcUaQuality.GOOD, result.quality());
    }

    @Test
    public void testCurrentPowerInKilowattsIsConvertedToWatts() {
        OpcEnergyModelMapper mapper = new OpcEnergyModelMapper();

        MachineEnergyMeasurement measurement =
            new MachineEnergyMeasurement(
                "shelly-1",
                "lathe",
                MeasurementType.CURRENT_POWER,
                1.25,
                "kW",
                Instant.parse("2026-10-11T14:05:00Z"),
                MeasurementQuality.VALID
            );

        MappedModelUpdate result = mapper.map(measurement);

        assertEquals("AcActivePowerTotal", result.target());
        assertEquals(1250.0, result.value(), 0.0);
        assertEquals("W", result.unit());
    }
    
    @Test
    public void testCumulativeEnergyInWattHours() {
        OpcEnergyModelMapper mapper = new OpcEnergyModelMapper();

        MachineEnergyMeasurement measurement =
            new MachineEnergyMeasurement(
                "shelly-1",
                "lathe",
                MeasurementType.CUMULATIVE_ENERGY,
                12500.0,
                "Wh",
                Instant.parse("2026-10-13T17:00:00Z"),
                MeasurementQuality.VALID
            );

        MappedModelUpdate result = mapper.map(measurement);

        assertEquals("AcActiveEnergyTotalImportLp", result.target());
        assertEquals(12500.0, result.value(), 0.0);
        assertEquals("Wh", result.unit());
    }

    @Test
    public void testCumulativeEnergyInKilowattHoursIsConvertedToWattHours() {
        OpcEnergyModelMapper mapper = new OpcEnergyModelMapper();

        MachineEnergyMeasurement measurement =
            new MachineEnergyMeasurement(
                "shelly-1",
                "lathe",
                MeasurementType.CUMULATIVE_ENERGY,
                12.5,
                "kWh",
                Instant.parse("2026-10-13T17:05:00Z"),
                MeasurementQuality.VALID
            );

        MappedModelUpdate result = mapper.map(measurement);

        assertEquals("AcActiveEnergyTotalImportLp", result.target());
        assertEquals(12500.0, result.value(), 0.0);
        assertEquals("Wh", result.unit());
    }
    
    @Test
    public void testValidQualityMapsToGood() {
        OpcEnergyModelMapper mapper = new OpcEnergyModelMapper();

        MachineEnergyMeasurement measurement =
            new MachineEnergyMeasurement(
                "shelly-1",
                "lathe",
                MeasurementType.CURRENT_POWER,
                500.0,
                "W",
                Instant.parse("2026-10-13T17:10:00Z"),
                MeasurementQuality.VALID
            );

        MappedModelUpdate result = mapper.map(measurement);

        assertEquals(OpcUaQuality.GOOD, result.quality());
    }

    @Test
    public void testStaleQualityMapsToUncertain() {
        OpcEnergyModelMapper mapper = new OpcEnergyModelMapper();

        MachineEnergyMeasurement measurement =
            new MachineEnergyMeasurement(
                "shelly-1",
                "lathe",
                MeasurementType.CURRENT_POWER,
                500.0,
                "W",
                Instant.parse("2026-10-13T17:10:00Z"),
                MeasurementQuality.STALE
            );

        MappedModelUpdate result = mapper.map(measurement);

        assertEquals(OpcUaQuality.UNCERTAIN, result.quality());
    }

    @Test
    public void testUnavailableQualityMapsToBadNotConnected() {
        OpcEnergyModelMapper mapper = new OpcEnergyModelMapper();

        MachineEnergyMeasurement measurement =
            new MachineEnergyMeasurement(
                "shelly-1",
                "lathe",
                MeasurementType.CURRENT_POWER,
                500.0,
                "W",
                Instant.parse("2026-10-13T17:10:00Z"),
                MeasurementQuality.UNAVAILABLE
            );

        MappedModelUpdate result = mapper.map(measurement);

        assertEquals(OpcUaQuality.BAD_NOT_CONNECTED, result.quality());
    }

    @Test
    public void testInvalidQualityMapsToBadInvalidData() {
        OpcEnergyModelMapper mapper = new OpcEnergyModelMapper();

        MachineEnergyMeasurement measurement =
            new MachineEnergyMeasurement(
                "shelly-1",
                "lathe",
                MeasurementType.CURRENT_POWER,
                500.0,
                "W",
                Instant.parse("2026-10-13T17:10:00Z"),
                MeasurementQuality.INVALID
            );

        MappedModelUpdate result = mapper.map(measurement);

        assertEquals(OpcUaQuality.BAD_INVALID_DATA, result.quality());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUnsupportedPowerUnitIsRejected() {
        OpcEnergyModelMapper mapper = new OpcEnergyModelMapper();

        MachineEnergyMeasurement measurement =
            new MachineEnergyMeasurement(
                "shelly-1",
                "lathe",
                MeasurementType.CURRENT_POWER,
                1.0,
                "MW",
                Instant.parse("2026-10-13T17:20:00Z"),
                MeasurementQuality.VALID
            );

        mapper.map(measurement);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUnsupportedEnergyUnitIsRejected() {
        OpcEnergyModelMapper mapper = new OpcEnergyModelMapper();

        MachineEnergyMeasurement measurement =
            new MachineEnergyMeasurement(
                "shelly-1",
                "lathe",
                MeasurementType.CUMULATIVE_ENERGY,
                1.0,
                "MWh",
                Instant.parse("2026-10-13T17:20:00Z"),
                MeasurementQuality.VALID
            );

        mapper.map(measurement);
    }
    
    // The mapper keeps the machine the value belongs to
    @Test
    public void testDeviceIdIsPreserved() {
        OpcEnergyModelMapper mapper = new OpcEnergyModelMapper();

        MachineEnergyMeasurement power = new MachineEnergyMeasurement(
            "SentronPAC", "MarkingLaser", MeasurementType.CURRENT_POWER, 1.25, "kW",
            Instant.parse("2026-10-26T10:00:00Z"), MeasurementQuality.VALID);
        MachineEnergyMeasurement energy = new MachineEnergyMeasurement(
            "ShellyPro3EM", "LatheNEF400", MeasurementType.CUMULATIVE_ENERGY, 12.5, "kWh",
            Instant.parse("2026-10-26T10:00:00Z"), MeasurementQuality.VALID);

        assertEquals("MarkingLaser", mapper.map(power).deviceId());
        assertEquals("LatheNEF400", mapper.map(energy).deviceId());
    }

    // A measurement without a type is rejected (covers the "unsupported type" branch)
    @Test(expected = IllegalArgumentException.class)
    public void testMissingMeasurementTypeIsRejected() {
        OpcEnergyModelMapper mapper = new OpcEnergyModelMapper();

        mapper.map(new MachineEnergyMeasurement(
            "shelly-1", "lathe", null, 500.0, "W",
            Instant.parse("2026-10-26T10:00:00Z"), MeasurementQuality.VALID));
    }
}