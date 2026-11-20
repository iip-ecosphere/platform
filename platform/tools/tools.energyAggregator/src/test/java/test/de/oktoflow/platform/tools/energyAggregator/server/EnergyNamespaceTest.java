package test.de.oktoflow.platform.tools.energyAggregator.server;

import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.junit.Assert;
import org.junit.Test;

import de.oktoflow.platform.tools.energyAggregator.model.OpcUaQuality;
import de.oktoflow.platform.tools.energyAggregator.server.EnergyNamespace;

/**
 * Tests the quality mapping of {@link EnergyNamespace}.
 * 
 * @author Kodjo Gilles Adjroloh
 */
public class EnergyNamespaceTest {

    /**
     * Tests the mapping of qualities to OPC UA status codes.
     */
    @Test
    public void testQualityMapping() {
        Assert.assertEquals(0x00000000L, EnergyNamespace.toStatusCode(OpcUaQuality.GOOD).getValue());
        Assert.assertEquals(0x40000000L, EnergyNamespace.toStatusCode(OpcUaQuality.UNCERTAIN).getValue());
        Assert.assertEquals(StatusCodes.Bad_NotConnected, 
            EnergyNamespace.toStatusCode(OpcUaQuality.BAD_NOT_CONNECTED).getValue());
        Assert.assertEquals(StatusCodes.Bad_InvalidArgument, 
            EnergyNamespace.toStatusCode(OpcUaQuality.BAD_INVALID_DATA).getValue());
        Assert.assertEquals(StatusCodes.Bad_NoData, EnergyNamespace.toStatusCode(null).getValue());
    }

}
