package test.de.oktoflow.platform.tools.energyAggregator.model;

import java.time.Instant;

import org.junit.Assert;
import org.junit.Test;

import de.oktoflow.platform.tools.energyAggregator.model.MachineEnergyMeasurement;
import de.oktoflow.platform.tools.energyAggregator.model.MeasurementQuality;
import de.oktoflow.platform.tools.energyAggregator.model.MeasurementType;

/**
 * Tests {@link MachineEnergyMeasurement}.
 * 
 * @author Kodjo Gilles Adjroloh
 */
public class MachineEnergyMeasurementTest {

    /**
     * Tests that the record returns the values it was created with.
     */
    @Test
    public void testMeasurement() {
        Instant timestamp = Instant.parse("2026-10-07T10:00:00Z");
        MachineEnergyMeasurement measurement = new MachineEnergyMeasurement("shelly-1", "lathe", 
            MeasurementType.CURRENT_POWER, 500.0, "W", timestamp, MeasurementQuality.VALID);
        Assert.assertEquals("shelly-1", measurement.sourceId());
        Assert.assertEquals("lathe", measurement.deviceId());
        Assert.assertEquals(MeasurementType.CURRENT_POWER, measurement.measurementType());
        Assert.assertEquals(500.0, measurement.value(), 0.0);
        Assert.assertEquals("W", measurement.unit());
        Assert.assertEquals(timestamp, measurement.timestamp());
        Assert.assertEquals(MeasurementQuality.VALID, measurement.quality());
    }

}
