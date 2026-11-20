package test.de.oktoflow.platform.tools.energyAggregator.source;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.oktoflow.platform.tools.energyAggregator.model.MachineEnergyMeasurement;
import de.oktoflow.platform.tools.energyAggregator.model.MeasurementQuality;
import de.oktoflow.platform.tools.energyAggregator.model.MeasurementType;
import de.oktoflow.platform.tools.energyAggregator.source.SentronModbusIngestor;

/**
 * Tests {@link SentronModbusIngestor} without device.
 * 
 * @author Kodjo Gilles Adjroloh
 */
public class SentronModbusIngestorTest {

    /**
     * Tests decoding a FLOAT32 split into two registers.
     */
    @Test
    public void testDecodeFloat32RoundTrip() {
        float value = 842.5f;
        int bits = Float.floatToIntBits(value);
        Assert.assertEquals(value, SentronModbusIngestor.decodeFloat32((bits >>> 16) & 0xFFFF, bits & 0xFFFF), 
            0.0001f);
    }

    /**
     * Tests decoding a FLOAT64 split into four registers.
     */
    @Test
    public void testDecodeFloat64RoundTrip() {
        double value = 123456.7;
        long bits = Double.doubleToLongBits(value);
        Assert.assertEquals(value, SentronModbusIngestor.decodeFloat64((int) ((bits >>> 48) & 0xFFFF), 
            (int) ((bits >>> 32) & 0xFFFF), (int) ((bits >>> 16) & 0xFFFF), (int) (bits & 0xFFFF)), 1e-9);
    }

    /**
     * Tests that a failing poll (nothing listens on port 1) emits both types as unavailable and reports the error.
     */
    @Test
    public void testFailingPollEmitsUnavailableForBothTypes() {
        List<MachineEnergyMeasurement> collected = new ArrayList<>();
        List<Throwable> errors = new ArrayList<>();
        SentronModbusIngestor ingestor = new SentronModbusIngestor("SentronPAC", "MarkingLaser", "127.0.0.1", 1, 1, 
            1000, collected::add, errors::add);
        ingestor.poll();
        Assert.assertFalse(ingestor.isConnected());
        Assert.assertEquals(1, errors.size());
        Assert.assertEquals(2, collected.size());
        Assert.assertTrue(collected.stream().anyMatch(m -> m.measurementType() == MeasurementType.CURRENT_POWER 
            && m.quality() == MeasurementQuality.UNAVAILABLE));
        Assert.assertTrue(collected.stream().anyMatch(m -> m.measurementType() == MeasurementType.CUMULATIVE_ENERGY 
            && m.quality() == MeasurementQuality.UNAVAILABLE));
    }

}
