package de.oktoflow.platform.tools.energyAggregator.model;

import java.time.Instant;

/**
 * Protocol-neutral energy measurement as produced by a source. One instance represents one value of one
 * measurement type, as sources deliver power and energy at different times and rates.
 * 
 * @param sourceId the source (meter) that provided the value, e.g., "ShellyPro3EM"
 * @param deviceId the machine the value belongs to, e.g., "LatheNEF400"
 * @param measurementType the kind of measurement
 * @param value the measured value in {@code unit}
 * @param unit the unit of {@code value}, e.g., "W", "kW", "Wh", "kWh"
 * @param timestamp the time of measurement, i.e., the source timestamp if provided, else the ingestion time
 * @param quality the quality of the measurement
 * @author Rhoda Ikhuohon-Eboreime
 */
public record MachineEnergyMeasurement(
    String sourceId,
    String deviceId,
    MeasurementType measurementType,
    double value,
    String unit,
    Instant timestamp,
    MeasurementQuality quality) {
}
