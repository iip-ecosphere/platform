package de.iip_ecosphere.platform.examples.energy.aggregator.model;

import java.time.Instant;

public record MachineEnergyMeasurement(
    String sourceId,
    String deviceId,
    MeasurementType measurementType,
    double value,
    String unit,
    Instant timestamp,
    MeasurementQuality quality
) {
}