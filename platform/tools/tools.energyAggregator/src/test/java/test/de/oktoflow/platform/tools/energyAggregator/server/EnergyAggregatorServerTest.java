package test.de.oktoflow.platform.tools.energyAggregator.server;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.oktoflow.platform.tools.energyAggregator.mapping.OpcEnergyModelMapper;
import de.oktoflow.platform.tools.energyAggregator.model.MappedModelUpdate;
import de.oktoflow.platform.tools.energyAggregator.model.OpcUaQuality;
import de.oktoflow.platform.tools.energyAggregator.server.EnergyAggregatorServer;
import de.oktoflow.platform.tools.energyAggregator.server.UnknownTargetException;

/**
 * Tests {@link EnergyAggregatorServer} by starting it on a local port.
 * 
 * @author Rhoda Ikhuohon-Eboreime
 */
public class EnergyAggregatorServerTest {

    /**
     * The port used in the test, differs from the default to avoid conflicts with a running server.
     */
    private static final int PORT = 4855;

    /**
     * Tests start, the address space size, valid and unknown updates and stop.
     * 
     * @throws IOException if the server cannot be started
     * @throws InterruptedException if starting is interrupted
     * @throws UnknownTargetException shall not occur for the valid update
     */
    @Test
    public void testServerLifecycle() throws IOException, InterruptedException, UnknownTargetException {
        EnergyAggregatorServer server = new EnergyAggregatorServer("127.0.0.1", PORT, List.of("Lathe", "Laser"));
        Assert.assertFalse(server.isRunning());
        server.start();
        try {
            Assert.assertTrue(server.isRunning());
            Assert.assertEquals("opc.tcp://127.0.0.1:" + PORT + "/", server.getEndpointUrl());
            Assert.assertEquals(4, server.getNamespace().size()); // 2 machines x (power, energy)
            server.applyUpdate(new MappedModelUpdate("Lathe", OpcEnergyModelMapper.POWER_TARGET, 500.0, "W", 
                Instant.now(), OpcUaQuality.GOOD));
            try {
                server.applyUpdate(new MappedModelUpdate("Drill", OpcEnergyModelMapper.POWER_TARGET, 1.0, "W", 
                    Instant.now(), OpcUaQuality.GOOD));
                Assert.fail("Unknown device shall be rejected");
            } catch (UnknownTargetException e) {
                Assert.assertEquals("Drill." + OpcEnergyModelMapper.POWER_TARGET, e.getTarget());
            }
        } finally {
            server.stop();
        }
        Assert.assertFalse(server.isRunning());
    }

}
