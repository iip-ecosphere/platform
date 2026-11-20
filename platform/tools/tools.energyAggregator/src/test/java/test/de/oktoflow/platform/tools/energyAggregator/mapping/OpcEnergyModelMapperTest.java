package test.de.oktoflow.platform.tools.energyAggregator.mapping;

import java.time.Instant;

import org.junit.Assert;
import org.junit.Test;

import de.oktoflow.platform.tools.energyAggregator.mapping.OpcEnergyModelMapper;
import de.oktoflow.platform.tools.energyAggregator.model.MachineEnergyMeasurement;
import de.oktoflow.platform.tools.energyAggregator.model.MeasurementQuality;
import de.oktoflow.platform.tools.energyAggregator.model.MeasurementType;
import de.oktoflow.platform.tools.energyAggregator.model.MappedModelUpdate;
import de.oktoflow.platform.tools.energyAggregator.model.OpcUaQuality;

/**
 * Tests {@link OpcEnergyModelMapper}.
 * 
 * @author Kodjo Gilles Adjroloh
 */
public class OpcEnergyModelMapperTest {

    /**
     * The timestamp used in the tests.
     */
    private static final Instant TIMESTAMP = Instant.parse("2026-10-11T14:00:00Z");

    /**
     * The mapper under test.
     */
    private final OpcEnergyModelMapper mapper = new OpcEnergyModelMapper();

    /**
     * Maps a measurement of the lathe.
     * 
     * @param type the measurement type
     * @param value the value
     * @param unit the unit
     * @param quality the quality
     * @return the mapped update
     */
    private MappedModelUpdate map(MeasurementType type, double value, String unit, MeasurementQuality quality) {
        return mapper.map(new MachineEnergyMeasurement("shelly-1", "lathe", type, value, unit, TIMESTAMP, quality));
    }

    /**
     * Tests power in W.
     */
    @Test
    public void testCurrentPowerInWatts() {
        MappedModelUpdate result = map(MeasurementType.CURRENT_POWER, 500.0, "W", MeasurementQuality.VALID);
        Assert.assertEquals(OpcEnergyModelMapper.POWER_TARGET, result.target());
        Assert.assertEquals(500.0, result.value(), 0.0);
        Assert.assertEquals("W", result.unit());
        Assert.assertEquals(TIMESTAMP, result.timestamp());
        Assert.assertEquals(OpcUaQuality.GOOD, result.quality());
    }

    /**
     * Tests that power in kW is converted to W.
     */
    @Test
    public void testCurrentPowerInKilowattsIsConvertedToWatts() {
        MappedModelUpdate result = map(MeasurementType.CURRENT_POWER, 1.25, "kW", MeasurementQuality.VALID);
        Assert.assertEquals(OpcEnergyModelMapper.POWER_TARGET, result.target());
        Assert.assertEquals(1250.0, result.value(), 0.0);
        Assert.assertEquals("W", result.unit());
    }

    /**
     * Tests energy in Wh.
     */
    @Test
    public void testCumulativeEnergyInWattHours() {
        MappedModelUpdate result = map(MeasurementType.CUMULATIVE_ENERGY, 12500.0, "Wh", MeasurementQuality.VALID);
        Assert.assertEquals(OpcEnergyModelMapper.ENERGY_TARGET, result.target());
        Assert.assertEquals(12500.0, result.value(), 0.0);
        Assert.assertEquals("Wh", result.unit());
    }

    /**
     * Tests that energy in kWh is converted to Wh.
     */
    @Test
    public void testCumulativeEnergyInKilowattHoursIsConvertedToWattHours() {
        MappedModelUpdate result = map(MeasurementType.CUMULATIVE_ENERGY, 12.5, "kWh", MeasurementQuality.VALID);
        Assert.assertEquals(OpcEnergyModelMapper.ENERGY_TARGET, result.target());
        Assert.assertEquals(12500.0, result.value(), 0.0);
        Assert.assertEquals("Wh", result.unit());
    }

    /**
     * Tests the mapping of all measurement qualities to OPC UA qualities.
     */
    @Test
    public void testQualityMapping() {
        Assert.assertEquals(OpcUaQuality.GOOD, 
            map(MeasurementType.CURRENT_POWER, 1, "W", MeasurementQuality.VALID).quality());
        Assert.assertEquals(OpcUaQuality.UNCERTAIN, 
            map(MeasurementType.CURRENT_POWER, 1, "W", MeasurementQuality.STALE).quality());
        Assert.assertEquals(OpcUaQuality.BAD_NOT_CONNECTED, 
            map(MeasurementType.CURRENT_POWER, 1, "W", MeasurementQuality.UNAVAILABLE).quality());
        Assert.assertEquals(OpcUaQuality.BAD_INVALID_DATA, 
            map(MeasurementType.CURRENT_POWER, 1, "W", MeasurementQuality.INVALID).quality());
    }

    /**
     * Tests that an unsupported power unit is rejected.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testUnsupportedPowerUnitIsRejected() {
        map(MeasurementType.CURRENT_POWER, 1.0, "MW", MeasurementQuality.VALID);
    }

    /**
     * Tests that an unsupported energy unit is rejected.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testUnsupportedEnergyUnitIsRejected() {
        map(MeasurementType.CUMULATIVE_ENERGY, 1.0, "MWh", MeasurementQuality.VALID);
    }

    /**
     * Tests that a measurement without type is rejected.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMissingMeasurementTypeIsRejected() {
        map(null, 500.0, "W", MeasurementQuality.VALID);
    }

    /**
     * Tests that the mapper keeps the device the value belongs to.
     */
    @Test
    public void testDeviceIdIsPreserved() {
        MachineEnergyMeasurement power = new MachineEnergyMeasurement("SentronPAC", "MarkingLaser", 
            MeasurementType.CURRENT_POWER, 1.25, "kW", TIMESTAMP, MeasurementQuality.VALID);
        MachineEnergyMeasurement energy = new MachineEnergyMeasurement("ShellyPro3EM", "LatheNEF400", 
            MeasurementType.CUMULATIVE_ENERGY, 12.5, "kWh", TIMESTAMP, MeasurementQuality.VALID);
        Assert.assertEquals("MarkingLaser", mapper.map(power).deviceId());
        Assert.assertEquals("LatheNEF400", mapper.map(energy).deviceId());
    }

}
