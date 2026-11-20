package de.oktoflow.platform.tools.energyAggregator.model;

/**
 * Protocol-neutral quality of a measurement as determined by the sources/the aggregator.
 * 
 * @author Rhoda Ikhuohon-Eboreime
 */
public enum MeasurementQuality {

    /**
     * The value was received correctly.
     */
    VALID,

    /**
     * The value is outdated, e.g., no update within the expected period.
     */
    STALE,

    /**
     * The source is not reachable; the value is not available.
     */
    UNAVAILABLE,

    /**
     * The received value is broken or malformed.
     */
    INVALID
}
