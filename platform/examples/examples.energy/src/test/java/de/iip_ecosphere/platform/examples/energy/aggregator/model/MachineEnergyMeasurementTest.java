package test.de.iip_ecosphere.platform.examples.energy.aggregator.model;

import static org.junit.Assert.*;

import java.time.Instant;

import org.junit.Test;

import de.iip_ecosphere.platform.examples.energy.aggregator.model.MachineEnergyMeasurement;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.MeasurementQuality;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.MeasurementType;

public class MachineEnergyMeasurementTest {

    @Test
    public void testMeasurement() {
        Instant timestamp = Instant.parse("2026-10-07T10:00:00Z");

        MachineEnergyMeasurement measurement = new MachineEnergyMeasurement(
            "shelly-1",
            "lathe",
            MeasurementType.CURRENT_POWER,
            500.0,
            "W",
            timestamp,
            MeasurementQuality.VALID
        );

        assertEquals("shelly-1", measurement.sourceId());
        assertEquals("lathe", measurement.deviceId());
        assertEquals(MeasurementType.CURRENT_POWER, measurement.measurementType());
        assertEquals(500.0, measurement.value(), 0.0);
        assertEquals("W", measurement.unit());
        assertEquals(timestamp, measurement.timestamp());
        assertEquals(MeasurementQuality.VALID, measurement.quality());
    }

}