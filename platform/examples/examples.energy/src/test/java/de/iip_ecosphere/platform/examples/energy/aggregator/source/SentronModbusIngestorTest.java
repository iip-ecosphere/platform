package de.iip_ecosphere.platform.examples.energy.aggregator.source;

import de.iip_ecosphere.platform.examples.energy.aggregator.model.MachineEnergyMeasurement;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.MeasurementQuality;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.MeasurementType;
import de.iip_ecosphere.platform.examples.energy.aggregator.source.SentronModbusIngestor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SentronModbusIngestorTest {

    @Test
    void decodeFloat32RoundTrip() {
        float v = 842.5f;
        int bits = Float.floatToIntBits(v);
        int r0 = (bits >>> 16) & 0xFFFF;
        int r1 = bits & 0xFFFF;
        assertEquals(v, SentronModbusIngestor.decodeFloat32(r0, r1), 0.0001f);
    }

    @Test
    void decodeFloat64RoundTrip() {
        double v = 123456.7;
        long bits = Double.doubleToLongBits(v);
        int r0 = (int) ((bits >>> 48) & 0xFFFF);
        int r1 = (int) ((bits >>> 32) & 0xFFFF);
        int r2 = (int) ((bits >>> 16) & 0xFFFF);
        int r3 = (int) (bits & 0xFFFF);
        assertEquals(v, SentronModbusIngestor.decodeFloat64(r0, r1, r2, r3), 1e-9);
    }

    @Test
    void errorPathEmitsUnavailableForBothTypes() {
        List<MachineEnergyMeasurement> collected = new ArrayList<>();
        SentronModbusIngestor ing = new SentronModbusIngestor(
            "SentronPAC", "MarkingLaser",
            "127.0.0.1", 1, 1, 1000,
            collected::add, t -> { /* ignore */ });

        // Access the private emitBadQuality via reflection, or add a package-private
        // wrapper emitBadQualityForTest() in the ingestor. Recommended: add the wrapper.
        ing.emitBadQualityForTest();

        assertEquals(2, collected.size());
        assertTrue(collected.stream().anyMatch(m ->
            m.measurementType() == MeasurementType.CURRENT_POWER
            && m.quality() == MeasurementQuality.UNAVAILABLE));
        assertTrue(collected.stream().anyMatch(m ->
            m.measurementType() == MeasurementType.CUMULATIVE_ENERGY
            && m.quality() == MeasurementQuality.UNAVAILABLE));
    }
}
