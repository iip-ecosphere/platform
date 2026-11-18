package de.iip_ecosphere.platform.examples.energy.aggregator.source;

import de.iip_ecosphere.platform.examples.energy.aggregator.model.MachineEnergyMeasurement;
import de.iip_ecosphere.platform.examples.energy.aggregator.model.MeasurementType;
import de.iip_ecosphere.platform.examples.energy.aggregator.source.ShellyMqttIngestor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class ShellyMqttIngestorTest {

    private ShellyMqttIngestor newIngestor() {
        return new ShellyMqttIngestor(
            "ShellyPro3EM", "LatheNEF400",
            "tcp://localhost:1883", "test",
            "shellies/shellypro3em/+/emeter/0", 1,
            m -> { }, t -> { });
    }

    @Test
    void gen2BothFields() throws Exception {
        List<MachineEnergyMeasurement> r =
            newIngestor().parse("{\"total_act_power\":1310.2,\"total_act\":4590.0}");
        assertEquals(2, r.size());
        assertEquals(MeasurementType.CURRENT_POWER, r.get(0).measurementType());
        assertEquals(MeasurementType.CUMULATIVE_ENERGY, r.get(1).measurementType());
    }

    @Test
    void gen1Fallback() throws Exception {
        List<MachineEnergyMeasurement> r =
            newIngestor().parse("{\"power\":1500.5,\"total\":123456.7}");
        assertEquals(2, r.size());
    }

    @Test
    void onlyPower() throws Exception {
        List<MachineEnergyMeasurement> r =
            newIngestor().parse("{\"total_act_power\":1310.2}");
        assertEquals(1, r.size());
        assertEquals(MeasurementType.CURRENT_POWER, r.get(0).measurementType());
    }

    @Test
    void onlyEnergy() throws Exception {
        List<MachineEnergyMeasurement> r =
            newIngestor().parse("{\"total_act\":4590.0}");
        assertEquals(1, r.size());
        assertEquals(MeasurementType.CUMULATIVE_ENERGY, r.get(0).measurementType());
    }

    @Test
    void invalidJsonThrows() {
        assertThrows(Exception.class, () -> newIngestor().parse("not-json"));
    }
}
