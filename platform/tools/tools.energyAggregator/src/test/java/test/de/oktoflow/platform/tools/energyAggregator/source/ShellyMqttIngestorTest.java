package test.de.oktoflow.platform.tools.energyAggregator.source;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.oktoflow.platform.tools.energyAggregator.model.MachineEnergyMeasurement;
import de.oktoflow.platform.tools.energyAggregator.model.MeasurementQuality;
import de.oktoflow.platform.tools.energyAggregator.model.MeasurementType;
import de.oktoflow.platform.tools.energyAggregator.source.ShellyMqttIngestor;

/**
 * Tests the payload parsing of {@link ShellyMqttIngestor} without broker.
 * 
 * @author Kodjo Gilles Adjroloh
 */
public class ShellyMqttIngestorTest {

    /**
     * Creates an ingestor that is not connected.
     * 
     * @return the ingestor
     */
    private static ShellyMqttIngestor newIngestor() {
        return new ShellyMqttIngestor("ShellyPro3EM", "LatheNEF400", "tcp://localhost:1883", "test", 
            "shellies/shellypro3em/+/emeter/0", 1, m -> { }, t -> { });
    }

    /**
     * Tests a Gen2 payload with power and energy.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testGen2BothFields() throws IOException {
        List<MachineEnergyMeasurement> result = newIngestor()
            .parse("{\"total_act_power\":1310.2,\"total_act\":4590.0}");
        Assert.assertEquals(2, result.size());
        Assert.assertEquals(MeasurementType.CURRENT_POWER, result.get(0).measurementType());
        Assert.assertEquals(1310.2, result.get(0).value(), 1e-9);
        Assert.assertEquals("W", result.get(0).unit());
        Assert.assertEquals(MeasurementType.CUMULATIVE_ENERGY, result.get(1).measurementType());
        Assert.assertEquals(4590.0, result.get(1).value(), 1e-9);
        Assert.assertEquals("Wh", result.get(1).unit());
        Assert.assertEquals("LatheNEF400", result.get(0).deviceId());
        Assert.assertEquals(MeasurementQuality.VALID, result.get(0).quality());
    }

    /**
     * Tests the fallback to Gen1 field names.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testGen1Fallback() throws IOException {
        List<MachineEnergyMeasurement> result = newIngestor().parse("{\"power\":1500.5,\"total\":123456.7}");
        Assert.assertEquals(2, result.size());
        Assert.assertEquals(1500.5, result.get(0).value(), 1e-9);
        Assert.assertEquals(123456.7, result.get(1).value(), 1e-9);
    }

    /**
     * Tests a payload with power only.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testOnlyPower() throws IOException {
        List<MachineEnergyMeasurement> result = newIngestor().parse("{\"total_act_power\":1310.2}");
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(MeasurementType.CURRENT_POWER, result.get(0).measurementType());
    }

    /**
     * Tests a payload with energy only.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testOnlyEnergy() throws IOException {
        List<MachineEnergyMeasurement> result = newIngestor().parse("{\"total_act\":4590.0}");
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(MeasurementType.CUMULATIVE_ENERGY, result.get(0).measurementType());
    }

    /**
     * Tests that invalid JSON is rejected.
     * 
     * @throws IOException expected
     */
    @Test(expected = IOException.class)
    public void testInvalidJsonIsRejected() throws IOException {
        newIngestor().parse("not-json");
    }

}
