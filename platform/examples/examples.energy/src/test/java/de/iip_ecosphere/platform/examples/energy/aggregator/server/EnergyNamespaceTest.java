package de.iip_ecosphere.platform.examples.energy.aggregator.server;

import de.iip_ecosphere.platform.examples.energy.aggregator.model.OpcUaQuality;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EnergyNamespaceTest {

    /** OPC UA "Good" status code value. */
    private static final long STATUS_GOOD      = 0x00000000L;
    /** OPC UA "Uncertain" status code value. */
    private static final long STATUS_UNCERTAIN = 0x40000000L;
    /** OPC UA "Bad_NotConnected" status code value. */
    private static final long STATUS_BAD_NOT_CONNECTED = 0x80320000L;
    /** OPC UA "Bad_InvalidArgument" status code value. */
    private static final long STATUS_BAD_INVALID_ARGUMENT = 0x80340000L;
    /** OPC UA "Bad_NoData" status code value. */
    private static final long STATUS_BAD_NO_DATA = 0x809B0000L;

    @Test
    void qualityMapping() {
        assertEquals(STATUS_GOOD,
            EnergyNamespace.toStatusCode(OpcUaQuality.GOOD).getValue());
        assertEquals(STATUS_UNCERTAIN,
            EnergyNamespace.toStatusCode(OpcUaQuality.UNCERTAIN).getValue());
        assertEquals(STATUS_BAD_NOT_CONNECTED,
            EnergyNamespace.toStatusCode(OpcUaQuality.BAD_NOT_CONNECTED).getValue());
        assertEquals(STATUS_BAD_INVALID_ARGUMENT,
            EnergyNamespace.toStatusCode(OpcUaQuality.BAD_INVALID_DATA).getValue());
        assertEquals(STATUS_BAD_NO_DATA,
            EnergyNamespace.toStatusCode(null).getValue());
    }
}
