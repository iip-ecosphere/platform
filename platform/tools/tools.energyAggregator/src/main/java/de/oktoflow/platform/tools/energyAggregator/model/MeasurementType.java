package de.oktoflow.platform.tools.energyAggregator.model;

/**
 * The kinds of energy measurements handled by the aggregator.
 * 
 * @author Rhoda Ikhuohon-Eboreime
 */
public enum MeasurementType {

    /**
     * Current (instantaneous) active power.
     */
    CURRENT_POWER,

    /**
     * Cumulative energy counter, i.e., energy consumed so far.
     */
    CUMULATIVE_ENERGY
}
