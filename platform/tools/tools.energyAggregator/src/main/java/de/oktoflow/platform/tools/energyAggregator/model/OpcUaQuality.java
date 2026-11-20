package de.oktoflow.platform.tools.energyAggregator.model;

/**
 * Quality of a mapped value in OPC UA terms. Kept independent of the OPC UA server library; the server
 * translates it into concrete status codes.
 * 
 * @author Rhoda Ikhuohon-Eboreime
 */
public enum OpcUaQuality {

    /**
     * Trustworthy value (OPC UA Good).
     */
    GOOD,

    /**
     * Value of uncertain quality, e.g., possibly outdated (OPC UA Uncertain).
     */
    UNCERTAIN,

    /**
     * Source not connected (OPC UA Bad_NotConnected).
     */
    BAD_NOT_CONNECTED,

    /**
     * Invalid data received (OPC UA Bad).
     */
    BAD_INVALID_DATA
}
