package test.de.oktoflow.platform.tools.energyAggregator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import test.de.oktoflow.platform.tools.energyAggregator.core.EnergyAggregatorTest;
import test.de.oktoflow.platform.tools.energyAggregator.mapping.OpcEnergyModelMapperTest;
import test.de.oktoflow.platform.tools.energyAggregator.model.MachineEnergyMeasurementTest;
import test.de.oktoflow.platform.tools.energyAggregator.server.EnergyAggregatorServerTest;
import test.de.oktoflow.platform.tools.energyAggregator.server.EnergyNamespaceTest;
import test.de.oktoflow.platform.tools.energyAggregator.source.SentronModbusIngestorTest;
import test.de.oktoflow.platform.tools.energyAggregator.source.ShellyMqttIngestorTest;

/**
 * Defines the tests to be executed.
 * 
 * @author Rhoda Ikhuohon-Eboreime
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    MachineEnergyMeasurementTest.class,
    EnergyAggregatorTest.class,
    OpcEnergyModelMapperTest.class,
    EnergyNamespaceTest.class,
    EnergyAggregatorServerTest.class,
    SentronModbusIngestorTest.class,
    ShellyMqttIngestorTest.class
    })
public class AllTests {
}
